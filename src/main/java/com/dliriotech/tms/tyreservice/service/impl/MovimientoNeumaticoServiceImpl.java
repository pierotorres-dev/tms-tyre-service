package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.EstadoObservacionConstants;
import com.dliriotech.tms.tyreservice.constants.TipoMovimientoConstants;
import com.dliriotech.tms.tyreservice.dto.InspeccionLoteResponse;
import com.dliriotech.tms.tyreservice.dto.LoteNeumaticosInspeccionRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoInspeccionadoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionCreadaResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoLoteRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResolucionLoteRequest;
import com.dliriotech.tms.tyreservice.entity.CatalogoServiciosEmpresa;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import com.dliriotech.tms.tyreservice.entity.TipoMovimientoNeumatico;
import com.dliriotech.tms.tyreservice.exception.EquipoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.InspeccionProcessingException;
import com.dliriotech.tms.tyreservice.exception.NeumaticoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.ObservacionUpdateException;
import com.dliriotech.tms.tyreservice.exception.RtdInvalidIncrementException;
import com.dliriotech.tms.tyreservice.repository.CatalogoServiciosEmpresaRepository;
import com.dliriotech.tms.tyreservice.repository.EquipoRepository;
import com.dliriotech.tms.tyreservice.repository.MovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.NeumaticoCustomRepository;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.ObservacionNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.TipoMovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.TipoObservacionRepository;
import com.dliriotech.tms.tyreservice.service.MovimientoNeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de procesamiento de lotes de inspección.
 * <p>
 * Recibe el payload delegado por el fleet-service (orquestador) y ejecuta
 * de forma atómica:
 * <ol>
 *   <li>Actualización de fecha de inspección del equipo</li>
 *   <li>Validación y actualización de mediciones RTD por neumático</li>
 *   <li>Creación de movimientos de inspección periódica</li>
 *   <li>Creación de observaciones de neumático</li>
 * </ol>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MovimientoNeumaticoServiceImpl implements MovimientoNeumaticoService {

    private final EquipoRepository equipoRepository;
    private final NeumaticoRepository neumaticoRepository;
    private final NeumaticoCustomRepository neumaticoCustomRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;
    private final TipoMovimientoNeumaticoRepository tipoMovimientoNeumaticoRepository;
    private final CatalogoServiciosEmpresaRepository catalogoServiciosEmpresaRepository;
    private final ObservacionNeumaticoRepository observacionNeumaticoRepository;
    private final TipoObservacionRepository tipoObservacionRepository;

    private static final Integer DLIRIO_TYRE_PROVEEDOR_ID = 3;
    private static final Integer CLASIFICACION_RODANDO_ID = 1;

    /**
     * Agrupa los datos maestros resueltos una sola vez para todo el lote,
     * evitando múltiples parámetros en métodos internos.
     */
    private record DatosMaestrosInspeccion(
            Integer tipoMovimientoId,
            Integer kilometraje,
            BigDecimal costoMovimiento
    ) {}

    @Override
    @Transactional
    public InspeccionLoteResponse procesarInspeccionLote(LoteNeumaticosInspeccionRequest request,
                                       Integer userId,
                                       Integer empresaId) {
        Integer equipoId = request.getEquipoId();
        log.info("Iniciando procesamiento de lote de inspección — equipoId: {}, inspeccionId: {}",
                equipoId, request.getInspeccionId());

        // 1. Validar datos de entrada
        validarDatosEntrada(request);

        // 2. Validar que el equipo existe
        Equipo equipo = equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoNotFoundException(equipoId.toString()));

        // 3. Cargar neumáticos del equipo para validación RTD (una sola query)
        Map<Integer, Neumatico> neumaticosExistentes = neumaticoRepository
                .findAllByEquipoIdAndEmpresaIdOrderByPosicionDesc(equipoId, empresaId)
                .stream()
                .collect(Collectors.toMap(Neumatico::getId, Function.identity()));

        // 4. Validar que RTD no incrementa
        validarRtdNoIncremento(request.getNeumaticosInspeccionados(), neumaticosExistentes);

        // 5. Actualizar fecha de inspección del equipo
        actualizarFechaInspeccionEquipo(equipoId);

        // 6. Obtener datos maestros necesarios (una sola vez para todos los neumáticos)
        Integer tipoMovimientoId = obtenerTipoMovimientoInspeccion();
        DatosMaestrosInspeccion datosMaestros = new DatosMaestrosInspeccion(
                tipoMovimientoId,
                resolverKilometraje(request, equipo),
                obtenerCostoMovimiento(empresaId, request.getTipoEquipoId(), tipoMovimientoId)
        );

        // 7. Procesar neumáticos inspeccionados en batch (RTD updates + movimientos)
        procesarNeumaticosEnBatch(request, userId, empresaId, neumaticosExistentes, datosMaestros);

        // 8. Procesar observaciones nuevas de neumáticos (batch)
        List<ObservacionCreadaResponse> observacionesCreadas =
                procesarObservacionesNuevas(request.getObservacionesNuevas(), request.getInspeccionId(), userId);

        // 9. Resolver observaciones pendientes (batch)
        List<Integer> observacionesResueltasIds =
                resolverObservacionesLote(request.getObservacionesResueltas(), userId, empresaId);

        log.info("Lote de inspección procesado exitosamente — equipoId: {}, inspeccionId: {}",
                equipoId, request.getInspeccionId());

        return InspeccionLoteResponse.builder()
                .observacionesCreadas(observacionesCreadas)
                .observacionesResueltasIds(observacionesResueltasIds)
                .build();
    }

    // ── Validaciones ──────────────────────────────────────────────────

    private void validarDatosEntrada(LoteNeumaticosInspeccionRequest request) {
        if (request.getEquipoId() == null || request.getEquipoId() <= 0) {
            throw new InspeccionProcessingException(
                    String.valueOf(request.getEquipoId()), "validacion_entrada",
                    "El ID del equipo debe ser un número positivo");
        }
        if (request.getInspeccionId() == null || request.getInspeccionId() <= 0) {
            throw new InspeccionProcessingException(
                    request.getEquipoId().toString(), "validacion_entrada",
                    "El ID de la inspección debe ser un número positivo");
        }
        if (request.getNeumaticosInspeccionados() == null || request.getNeumaticosInspeccionados().isEmpty()) {
            throw new InspeccionProcessingException(
                    request.getEquipoId().toString(), "validacion_entrada",
                    "Debe incluir al menos un neumático para inspeccionar");
        }
    }

    private void validarRtdNoIncremento(List<NeumaticoInspeccionadoRequest> inspeccionados,
                                        Map<Integer, Neumatico> neumaticosExistentes) {
        for (NeumaticoInspeccionadoRequest insp : inspeccionados) {
            Neumatico existente = neumaticosExistentes.get(insp.getNeumaticoId());
            if (existente == null) {
                throw new NeumaticoNotFoundException(insp.getNeumaticoId().toString());
            }
            BigDecimal rtdCalculado = calcularRtdActual(insp.getRtd1(), insp.getRtd2(), insp.getRtd3());
            if (existente.getRtdActual() != null && rtdCalculado.compareTo(existente.getRtdActual()) > 0) {
                throw new RtdInvalidIncrementException(insp.getNeumaticoId(), existente.getRtdActual(), rtdCalculado);
            }
        }
    }

    // ── Procesamiento de neumáticos ──────────────────────────────────

    private void actualizarFechaInspeccionEquipo(Integer equipoId) {
        int rowsUpdated = equipoRepository.updateFechaInspeccion(equipoId, LocalDate.now(ZoneId.of("America/Lima")));
        if (rowsUpdated == 0) {
            throw new InspeccionProcessingException(equipoId.toString(),
                    "actualizacion_fecha_inspeccion",
                    "No se pudo actualizar la fecha de inspección del equipo");
        }
    }

    private void procesarNeumaticosEnBatch(LoteNeumaticosInspeccionRequest request,
                                             Integer userId,
                                             Integer empresaId,
                                             Map<Integer, Neumatico> neumaticosExistentes,
                                             DatosMaestrosInspeccion datosMaestros) {
        List<NeumaticoInspeccionadoRequest> inspeccionados = request.getNeumaticosInspeccionados();

        // 1. Preparar datos RTD para batch update (LinkedHashMap mantiene orden de inserción)
        Map<Integer, BigDecimal[]> rtdUpdates = new LinkedHashMap<>(inspeccionados.size());
        Map<Integer, BigDecimal> rtdActualMap = new LinkedHashMap<>(inspeccionados.size());

        for (NeumaticoInspeccionadoRequest req : inspeccionados) {
            BigDecimal rtdActual = calcularRtdActual(req.getRtd1(), req.getRtd2(), req.getRtd3());
            rtdUpdates.put(req.getNeumaticoId(),
                    new BigDecimal[]{req.getRtd1(), req.getRtd2(), req.getRtd3(), rtdActual});
            rtdActualMap.put(req.getNeumaticoId(), rtdActual);
        }

        // 2. Ejecutar batch UPDATE de RTD (1 round-trip JDBC en vez de N)
        neumaticoCustomRepository.batchUpdateRtdMeasurements(rtdUpdates, empresaId);

        // 3. Construir todos los movimientos y hacer batch insert
        List<MovimientoNeumatico> movimientos = new ArrayList<>(inspeccionados.size());
        for (NeumaticoInspeccionadoRequest neumaticoReq : inspeccionados) {
            Integer neumaticoId = neumaticoReq.getNeumaticoId();
            Neumatico neumatico = neumaticosExistentes.get(neumaticoId);

            movimientos.add(MovimientoNeumatico.builder()
                    .neumaticoId(neumaticoId)
                    .fechaMovimiento(LocalDateTime.now())
                    .equipoOrigenId(request.getEquipoId())
                    .equipoDestinoId(request.getEquipoId())
                    .posicionOrigen(neumatico.getPosicion())
                    .posicionDestino(neumatico.getPosicion())
                    .clasificacionOrigenId(CLASIFICACION_RODANDO_ID)
                    .clasificacionDestinoId(CLASIFICACION_RODANDO_ID)
                    .tipoMovimientoId(datosMaestros.tipoMovimientoId())
                    .kilometraje(datosMaestros.kilometraje())
                    .rtd1(neumaticoReq.getRtd1())
                    .rtd2(neumaticoReq.getRtd2())
                    .rtd3(neumaticoReq.getRtd3())
                    .rtdActual(rtdActualMap.get(neumaticoId))
                    .rtdPostReencauche(null)
                    .usuarioId(userId)
                    .costoMovimiento(datosMaestros.costoMovimiento())
                    .proveedorServicioId(DLIRIO_TYRE_PROVEEDOR_ID)
                    .comentario(null)
                    .idInspeccion(request.getInspeccionId())
                    .build());
        }

        // 4. Batch insert de todos los movimientos
        movimientoNeumaticoRepository.saveAll(movimientos);

        log.debug("Batch procesado — {} neumáticos actualizados y {} movimientos creados para inspeccionId: {}",
                inspeccionados.size(), movimientos.size(), request.getInspeccionId());
    }

    // ── Procesamiento de observaciones nuevas (batch) ──────────────

    private List<ObservacionCreadaResponse> procesarObservacionesNuevas(List<ObservacionNeumaticoLoteRequest> observaciones,
                                              Integer inspeccionId,
                                              Integer userId) {
        if (observaciones == null || observaciones.isEmpty()) {
            log.debug("Sin observaciones nuevas de neumático para la inspección {}", inspeccionId);
            return List.of();
        }

        // Validar que todos los tipos de observación existen (1 query en vez de N)
        List<Integer> tipoObsIds = observaciones.stream()
                .map(ObservacionNeumaticoLoteRequest::getTipoObservacionId)
                .distinct()
                .toList();
        List<Integer> tiposExistentes = tipoObservacionRepository.findAllById(tipoObsIds)
                .stream()
                .map(tipo -> tipo.getId())
                .toList();
        tipoObsIds.stream()
                .filter(id -> !tiposExistentes.contains(id))
                .findFirst()
                .ifPresent(idFaltante -> {
                    // Buscar el neumaticoId asociado para el mensaje de error
                    Integer neumaticoId = observaciones.stream()
                            .filter(obs -> obs.getTipoObservacionId().equals(idFaltante))
                            .findFirst()
                            .map(ObservacionNeumaticoLoteRequest::getNeumaticoId)
                            .orElse(0);
                    throw new InspeccionProcessingException(
                            neumaticoId.toString(), "tipo_observacion",
                            String.format("No se encontró el tipo de observación con ID %d", idFaltante));
                });

        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));

        List<ObservacionNeumatico> entities = new ArrayList<>(observaciones.size());
        for (ObservacionNeumaticoLoteRequest obs : observaciones) {
            entities.add(ObservacionNeumatico.builder()
                    .neumaticoId(obs.getNeumaticoId())
                    .equipoId(obs.getEquipoId())
                    .posicion(obs.getPosicion())
                    .tipoObservacionId(obs.getTipoObservacionId())
                    .descripcion(obs.getDescripcion().trim())
                    .estadoObservacionId(EstadoObservacionConstants.ID_PENDIENTE)
                    .fechaCreacion(ahora)
                    .usuarioCreacionId(userId)
                    .fechaResolucion(null)
                    .usuarioResolucionId(null)
                    .comentarioResolucion(null)
                    .idInspeccion(inspeccionId)
                    .build());
        }

        List<ObservacionNeumatico> saved = observacionNeumaticoRepository.saveAll(entities);
        log.info("Se crearon {} observaciones nuevas para la inspección {}",
                saved.size(), inspeccionId);

        // Mapear response preservando tempId del request (correlación por índice)
        List<ObservacionCreadaResponse> responses = new ArrayList<>(saved.size());
        for (int i = 0; i < saved.size(); i++) {
            ObservacionNeumatico entity = saved.get(i);
            String tempId = observaciones.get(i).getTempId();
            responses.add(ObservacionCreadaResponse.builder()
                    .id(entity.getId())
                    .neumaticoId(entity.getNeumaticoId())
                    .equipoId(entity.getEquipoId())
                    .posicion(entity.getPosicion())
                    .tipoObservacionId(entity.getTipoObservacionId())
                    .descripcion(entity.getDescripcion())
                    .tempId(tempId)
                    .build());
        }

        return responses;
    }

    // ── Resolución de observaciones pendientes (batch) ──────────────

    private List<Integer> resolverObservacionesLote(List<ObservacionNeumaticoResolucionLoteRequest> resoluciones,
                                           Integer userId,
                                           Integer empresaId) {
        if (resoluciones == null || resoluciones.isEmpty()) {
            log.debug("Sin observaciones para resolver en este lote");
            return List.of();
        }

        // 1. Batch-load todas las observaciones en 1 query
        List<Integer> obsIds = resoluciones.stream()
                .map(ObservacionNeumaticoResolucionLoteRequest::getObservacionId)
                .toList();
        Map<Integer, ObservacionNeumatico> obsMap = observacionNeumaticoRepository.findAllById(obsIds)
                .stream()
                .collect(Collectors.toMap(ObservacionNeumatico::getId, Function.identity()));

        // Validar que todas existen
        obsIds.stream()
                .filter(id -> !obsMap.containsKey(id))
                .findFirst()
                .ifPresent(idFaltante -> { throw ObservacionUpdateException.notFound(idFaltante); });

        // 2. Batch-load todos los neumáticos necesarios para tenant validation (1 query)
        List<Integer> neumaticoIds = obsMap.values().stream()
                .map(ObservacionNeumatico::getNeumaticoId)
                .distinct()
                .toList();
        Map<Integer, Neumatico> neumaticosMap = neumaticoRepository.findAllById(neumaticoIds)
                .stream()
                .collect(Collectors.toMap(Neumatico::getId, Function.identity()));

        // 3. Validar tenant isolation y estado en memoria
        for (ObservacionNeumatico existente : obsMap.values()) {
            Neumatico neumatico = neumaticosMap.get(existente.getNeumaticoId());
            if (neumatico == null || !empresaId.equals(neumatico.getEmpresaId())) {
                throw ObservacionUpdateException.notFound(existente.getId());
            }
            if (existente.getEstadoObservacionId() == EstadoObservacionConstants.ID_RESUELTA) {
                throw ObservacionUpdateException.alreadyResolved(existente.getId());
            }
            if (existente.getEstadoObservacionId() == EstadoObservacionConstants.ID_CANCELADA) {
                throw ObservacionUpdateException.invalidStateTransition(
                        EstadoObservacionConstants.CANCELADO, EstadoObservacionConstants.RESUELTO);
            }
        }

        // 4. Construir mapa de comentarios de resolución por observacionId
        Map<Integer, String> comentariosMap = resoluciones.stream()
                .collect(Collectors.toMap(
                        ObservacionNeumaticoResolucionLoteRequest::getObservacionId,
                        r -> r.getComentarioResolucion() != null ? r.getComentarioResolucion().trim() : "",
                        (a, b) -> a));

        // 5. Aplicar resoluciones y acumular para batch save
        LocalDateTime ahora = LocalDateTime.now(ZoneId.of("America/Lima"));
        List<ObservacionNeumatico> actualizadas = new ArrayList<>(resoluciones.size());
        List<Integer> resueltasIds = new ArrayList<>(resoluciones.size());

        for (Integer obsId : obsIds) {
            ObservacionNeumatico existente = obsMap.get(obsId);
            String comentario = comentariosMap.getOrDefault(obsId, "");

            ObservacionNeumatico actualizada = existente.toBuilder()
                    .estadoObservacionId(EstadoObservacionConstants.ID_RESUELTA)
                    .fechaResolucion(ahora)
                    .usuarioResolucionId(userId)
                    .comentarioResolucion(comentario.isEmpty() ? null : comentario)
                    .build();

            actualizadas.add(actualizada);
            resueltasIds.add(obsId);
        }

        // 6. Batch save de todas las observaciones resueltas
        observacionNeumaticoRepository.saveAll(actualizadas);
        log.info("Se resolvieron {} observaciones en este lote", resueltasIds.size());
        return resueltasIds;
    }

    // ── Helpers de datos maestros ────────────────────────────────────

    private BigDecimal calcularRtdActual(BigDecimal rtd1, BigDecimal rtd2, BigDecimal rtd3) {
        return rtd1.min(rtd2).min(rtd3);
    }

    private Integer obtenerTipoMovimientoInspeccion() {
        return tipoMovimientoNeumaticoRepository.findByNombre(TipoMovimientoConstants.INSPECCION_PERIODICA)
                .map(TipoMovimientoNeumatico::getId)
                .orElseThrow(() -> new InspeccionProcessingException("N/A",
                        "tipo_movimiento", "No se encontró el tipo de movimiento 'Inspección Periódica'"));
    }

    /**
     * Resuelve el kilometraje: si el request trae un valor lo usa,
     * de lo contrario lo obtiene del equipo en BD.
     */
    private Integer resolverKilometraje(LoteNeumaticosInspeccionRequest request, Equipo equipo) {
        if (request.getKilometraje() != null) {
            return request.getKilometraje();
        }
        return equipo.getKilometraje() != null ? equipo.getKilometraje() : 0;
    }

    private BigDecimal obtenerCostoMovimiento(Integer empresaId, Integer tipoEquipoId, Integer tipoMovimientoId) {
        return catalogoServiciosEmpresaRepository
                .findByEmpresaIdAndTipoEquipoIdAndTipoMovimientoId(empresaId, tipoEquipoId, tipoMovimientoId)
                .map(CatalogoServiciosEmpresa::getCostoServicio)
                .orElse(BigDecimal.ZERO);
    }
}





