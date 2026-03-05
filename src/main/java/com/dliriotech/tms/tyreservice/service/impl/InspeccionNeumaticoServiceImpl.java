package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.TipoMovimientoConstants;
import com.dliriotech.tms.tyreservice.dto.FinalizarInspeccionRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoInspeccionadoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.entity.CatalogoServiciosEmpresa;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.entity.TipoMovimientoNeumatico;
import com.dliriotech.tms.tyreservice.exception.EquipoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.InspeccionProcessingException;
import com.dliriotech.tms.tyreservice.exception.NeumaticoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.RtdInvalidIncrementException;
import com.dliriotech.tms.tyreservice.repository.CatalogoServiciosEmpresaRepository;
import com.dliriotech.tms.tyreservice.repository.EquipoRepository;
import com.dliriotech.tms.tyreservice.repository.MovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.TipoMovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.InspeccionNeumaticoService;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InspeccionNeumaticoServiceImpl implements InspeccionNeumaticoService {

    private final EquipoRepository equipoRepository;
    private final NeumaticoRepository neumaticoRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;
    private final TipoMovimientoNeumaticoRepository tipoMovimientoNeumaticoRepository;
    private final CatalogoServiciosEmpresaRepository catalogoServiciosEmpresaRepository;
    private final ObservacionNeumaticoService observacionNeumaticoService;

    private static final Integer DLIRIO_TYRE_PROVEEDOR_ID = 3;
    private static final Integer CLASIFICACION_RODANDO_ID = 1;

    @Override
    @Transactional
    public void finalizarInspeccion(Integer equipoId, Integer empresaId, FinalizarInspeccionRequest request) {
        log.info("Iniciando finalización de inspección para equipo: {}", equipoId);
        request.setEmpresaId(empresaId);

        // 1. Validar datos de entrada
        validarDatosEntrada(equipoId, request);

        // 2. Validar que el equipo existe
        equipoRepository.findById(equipoId)
                .orElseThrow(() -> new EquipoNotFoundException(equipoId.toString()));

        // 3. Cargar neumáticos del equipo para validación RTD (una sola query)
        Map<Integer, Neumatico> neumaticosExistentes = neumaticoRepository
                .findAllByEquipoIdAndEmpresaIdOrderByPosicionDesc(equipoId, empresaId)
                .stream()
                .collect(Collectors.toMap(Neumatico::getId, Function.identity()));

        // 4. Validar que RTD no incrementa
        validarRtdNoIncremento(request.getNeumaticosInspeccionados(), neumaticosExistentes);

        // 5. Actualizar fecha de inspección del equipo
        int rowsUpdated = equipoRepository.updateFechaInspeccion(equipoId, LocalDate.now(ZoneId.of("America/Lima")));
        if (rowsUpdated == 0) {
            throw new InspeccionProcessingException(equipoId.toString(),
                    "actualizacion_fecha_inspeccion",
                    "No se pudo actualizar la fecha de inspección del equipo");
        }

        // 6. Obtener datos maestros necesarios (una sola vez para todos los neumáticos)
        Integer tipoMovimientoId = obtenerTipoMovimientoInspeccion();
        Integer kilometraje = obtenerKilometraje(request);
        BigDecimal costoMovimiento = obtenerCostoMovimiento(request, tipoMovimientoId);

        // 7. Procesar cada neumático
        for (NeumaticoInspeccionadoRequest neumaticoReq : request.getNeumaticosInspeccionados()) {
            procesarNeumaticoIndividual(neumaticoReq, request, empresaId,
                    neumaticosExistentes, tipoMovimientoId, kilometraje, costoMovimiento);
        }

        log.info("Inspección finalizada exitosamente para equipo: {}", equipoId);
    }

    private void validarDatosEntrada(Integer equipoId, FinalizarInspeccionRequest request) {
        if (equipoId == null || equipoId <= 0) {
            throw new InspeccionProcessingException(
                    String.valueOf(equipoId), "validacion_entrada",
                    "El ID del equipo debe ser un número positivo");
        }
        if (request.getNeumaticosInspeccionados() == null || request.getNeumaticosInspeccionados().isEmpty()) {
            throw new InspeccionProcessingException(
                    equipoId.toString(), "validacion_entrada",
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

    private void procesarNeumaticoIndividual(NeumaticoInspeccionadoRequest neumaticoReq,
                                             FinalizarInspeccionRequest request,
                                             Integer empresaId,
                                             Map<Integer, Neumatico> neumaticosExistentes,
                                             Integer tipoMovimientoId,
                                             Integer kilometraje,
                                             BigDecimal costoMovimiento) {
        Integer neumaticoId = neumaticoReq.getNeumaticoId();
        Neumatico neumatico = neumaticosExistentes.get(neumaticoId);

        // Actualizar mediciones RTD
        BigDecimal rtdActual = calcularRtdActual(neumaticoReq.getRtd1(), neumaticoReq.getRtd2(), neumaticoReq.getRtd3());
        int updated = neumaticoRepository.updateRtdMeasurements(
                neumaticoId, empresaId,
                neumaticoReq.getRtd1(), neumaticoReq.getRtd2(), neumaticoReq.getRtd3(), rtdActual);
        if (updated == 0) {
            throw new InspeccionProcessingException(neumaticoId.toString(),
                    "actualizacion_mediciones_rtd",
                    String.format("No se pudieron actualizar las mediciones RTD del neumático %d", neumaticoId));
        }

        // Crear movimiento de inspección
        MovimientoNeumatico movimiento = MovimientoNeumatico.builder()
                .neumaticoId(neumaticoId)
                .fechaMovimiento(LocalDateTime.now())
                .equipoOrigenId(request.getEquipoId())
                .equipoDestinoId(request.getEquipoId())
                .posicionOrigen(neumatico.getPosicion())
                .posicionDestino(neumatico.getPosicion())
                .clasificacionOrigenId(CLASIFICACION_RODANDO_ID)
                .clasificacionDestinoId(CLASIFICACION_RODANDO_ID)
                .tipoMovimientoId(tipoMovimientoId)
                .kilometraje(kilometraje)
                .rtd1(neumaticoReq.getRtd1())
                .rtd2(neumaticoReq.getRtd2())
                .rtd3(neumaticoReq.getRtd3())
                .rtdActual(rtdActual)
                .rtdPostReencauche(null)
                .usuarioId(request.getUsuarioId())
                .costoMovimiento(costoMovimiento)
                .proveedorServicioId(DLIRIO_TYRE_PROVEEDOR_ID)
                .comentario(null)
                .build();
        movimientoNeumaticoRepository.save(movimiento);

        // Procesar observaciones
        procesarObservaciones(neumaticoReq, request);
    }

    private BigDecimal calcularRtdActual(BigDecimal rtd1, BigDecimal rtd2, BigDecimal rtd3) {
        return rtd1.min(rtd2).min(rtd3);
    }

    private Integer obtenerTipoMovimientoInspeccion() {
        return tipoMovimientoNeumaticoRepository.findByNombre(TipoMovimientoConstants.INSPECCION_PERIODICA)
                .map(TipoMovimientoNeumatico::getId)
                .orElseThrow(() -> new InspeccionProcessingException("N/A",
                        "tipo_movimiento", "No se encontró el tipo de movimiento 'Inspección Periódica'"));
    }

    private Integer obtenerKilometraje(FinalizarInspeccionRequest request) {
        if (request.getKilometraje() != null) {
            return request.getKilometraje();
        }
        return equipoRepository.findById(request.getEquipoId())
                .map(Equipo::getKilometraje)
                .orElse(0);
    }

    private BigDecimal obtenerCostoMovimiento(FinalizarInspeccionRequest request, Integer tipoMovimientoId) {
        return catalogoServiciosEmpresaRepository
                .findByEmpresaIdAndTipoEquipoIdAndTipoMovimientoId(
                        request.getEmpresaId(), request.getTipoEquipoId(), tipoMovimientoId)
                .map(CatalogoServiciosEmpresa::getCostoServicio)
                .orElse(BigDecimal.ZERO);
    }

    private void procesarObservaciones(NeumaticoInspeccionadoRequest neumaticoReq,
                                       FinalizarInspeccionRequest request) {
        if (neumaticoReq.getObservacionNeumaticoList() == null
                || neumaticoReq.getObservacionNeumaticoList().isEmpty()) {
            return;
        }
        for (ObservacionNeumaticoNuevoRequest observacion : neumaticoReq.getObservacionNeumaticoList()) {
            observacion.setUsuarioCreacionId(request.getUsuarioId());
            observacion.setEquipoId(request.getEquipoId());
            observacionNeumaticoService.saveObservacion(observacion);
        }
    }
}