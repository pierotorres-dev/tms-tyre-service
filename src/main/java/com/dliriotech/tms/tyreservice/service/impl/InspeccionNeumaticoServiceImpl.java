package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.TipoMovimientoConstants;
import com.dliriotech.tms.tyreservice.dto.ClasificacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.FinalizarInspeccionRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoInspeccionadoRequest;
import com.dliriotech.tms.tyreservice.entity.CatalogoServiciosEmpresa;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.exception.EquipoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.NeumaticoNotFoundException;
import com.dliriotech.tms.tyreservice.repository.EquipoRepository;
import com.dliriotech.tms.tyreservice.repository.MovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.InspeccionNeumaticoService;
import com.dliriotech.tms.tyreservice.service.MasterDataCacheService;
import com.dliriotech.tms.tyreservice.service.NeumaticoEntityCacheService;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InspeccionNeumaticoServiceImpl implements InspeccionNeumaticoService {

    private final EquipoRepository equipoRepository;
    private final NeumaticoRepository neumaticoRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;
    private final MasterDataCacheService masterDataCacheService;
    private final NeumaticoEntityCacheService neumaticoEntityCacheService;
    private final ObservacionNeumaticoService observacionNeumaticoService;

    // ID fijo del proveedor de servicio "DLirio Tyre"
    private static final Integer DLIRIO_TYRE_PROVEEDOR_ID = 3;

    @Override
    public Mono<Void> finalizarInspeccion(Integer equipoId, FinalizarInspeccionRequest request) {
        log.info("Iniciando finalización de inspección para equipo: {}", equipoId);
        
        return validarEquipoExiste(equipoId)
            .then(actualizarFechaInspeccionEquipo(equipoId))
            .then(procesarNeumaticosinspeccionados(request))
            .doOnSuccess(result -> log.info("Inspección finalizada exitosamente para equipo: {}", equipoId))
            .doOnError(error -> log.error("Error al finalizar inspección para equipo {}: {}", equipoId, error.getMessage()));
    }

    private Mono<Void> validarEquipoExiste(Integer equipoId) {
        return masterDataCacheService.getEquipo(equipoId)
            .switchIfEmpty(Mono.error(new EquipoNotFoundException(equipoId.toString())))
            .then();
    }

    private Mono<Void> actualizarFechaInspeccionEquipo(Integer equipoId) {
        LocalDate fechaInspeccion = LocalDate.now();
        return equipoRepository.updateFechaInspeccion(equipoId, fechaInspeccion)
            .doOnSuccess(result -> log.debug("Fecha de inspección actualizada para equipo: {}", equipoId))
            .then();
    }

    private Mono<Void> procesarNeumaticosinspeccionados(FinalizarInspeccionRequest request) {
        return Flux.fromIterable(request.getNeumaticosInspeccionados())
            .flatMap(neumatico -> procesarNeumaticoIndividual(neumatico, request))
            .then();
    }

    private Mono<Void> procesarNeumaticoIndividual(NeumaticoInspeccionadoRequest neumaticoRequest, 
                                                   FinalizarInspeccionRequest request) {
        return validarNeumaticoExiste(neumaticoRequest.getNeumaticoId())
            .then(actualizarMedicionesRtdNeumatico(neumaticoRequest))
            .then(crearMovimientoInspeccion(neumaticoRequest, request))
            .then(procesarObservaciones(neumaticoRequest, request))
            .doOnSuccess(result -> log.debug("Neumático {} procesado exitosamente", neumaticoRequest.getNeumaticoId()));
    }

    private Mono<Neumatico> validarNeumaticoExiste(Integer neumaticoId) {
        return neumaticoRepository.findById(neumaticoId)
            .switchIfEmpty(Mono.error(new NeumaticoNotFoundException(neumaticoId.toString())));
    }

    private Mono<Void> actualizarMedicionesRtdNeumatico(NeumaticoInspeccionadoRequest request) {
        BigDecimal rtdActual = calcularRtdActual(request.getRtd1(), request.getRtd2(), request.getRtd3());
        
        return neumaticoRepository.updateRtdMeasurements(
                request.getNeumaticoId(),
                request.getRtd1(),
                request.getRtd2(),
                request.getRtd3(),
                rtdActual
            )
            .doOnSuccess(result -> log.debug("Mediciones RTD actualizadas para neumático: {}", request.getNeumaticoId()))
            .then();
    }

    private BigDecimal calcularRtdActual(BigDecimal rtd1, BigDecimal rtd2, BigDecimal rtd3) {
        return rtd1.min(rtd2).min(rtd3);
    }

    private Mono<Void> crearMovimientoInspeccion(NeumaticoInspeccionadoRequest neumaticoRequest, 
                                                FinalizarInspeccionRequest request) {
        return validarNeumaticoExiste(neumaticoRequest.getNeumaticoId())
            .flatMap(neumatico -> construirMovimientoInspeccion(neumatico, neumaticoRequest, request))
            .flatMap(movimientoNeumaticoRepository::save)
            .doOnSuccess(movimiento -> log.debug("Movimiento de inspección creado con ID: {}", movimiento.getId()))
            .then();
    }

    private Mono<MovimientoNeumatico> construirMovimientoInspeccion(Neumatico neumatico,
                                                                   NeumaticoInspeccionadoRequest neumaticoRequest,
                                                                   FinalizarInspeccionRequest request) {
        return Mono.zip(
                obtenerClasificacionRodando(),
                obtenerTipoMovimientoInspeccion(),
                obtenerKilometraje(request),
                obtenerCostoMovimiento(request)
            )
            .map(tuple -> {
                Integer clasificacionId = tuple.getT1();
                Integer tipoMovimientoId = tuple.getT2();
                Integer kilometraje = tuple.getT3();
                BigDecimal costoMovimiento = tuple.getT4();
                
                return MovimientoNeumatico.builder()
                    .neumaticoId(neumaticoRequest.getNeumaticoId())
                    .fechaMovimiento(LocalDateTime.now())
                    .equipoOrigenId(request.getEquipoId())
                    .equipoDestinoId(request.getEquipoId())
                    .posicionOrigen(neumatico.getPosicion())
                    .posicionDestino(neumatico.getPosicion())
                    .clasificacionOrigenId(clasificacionId)
                    .clasificacionDestinoId(clasificacionId)
                    .tipoMovimientoId(tipoMovimientoId)
                    .kilometraje(kilometraje)
                    .rtd1(neumaticoRequest.getRtd1())
                    .rtd2(neumaticoRequest.getRtd2())
                    .rtd3(neumaticoRequest.getRtd3())
                    .rtdActual(calcularRtdActual(neumaticoRequest.getRtd1(), neumaticoRequest.getRtd2(), neumaticoRequest.getRtd3()))
                    .rtdPostReencauche(null) // NULL para inspecciones
                    .usuarioId(request.getUsuarioId())
                    .costoMovimiento(costoMovimiento)
                    .proveedorServicioId(DLIRIO_TYRE_PROVEEDOR_ID)
                    .comentario(null) // Vacío para inspecciones
                    .build();
            });
    }

    private Mono<Integer> obtenerClasificacionRodando() {
        return neumaticoEntityCacheService.getClasificacionNeumatico(1) // Asumiendo que ID 1 es "Rodando"
            .map(ClasificacionNeumaticoResponse::getId)
            .doOnError(error -> log.error("Error al obtener clasificación 'Rodando': {}", error.getMessage()));
    }

    private Mono<Integer> obtenerTipoMovimientoInspeccion() {
        return masterDataCacheService.getTipoMovimientoIdByNombre(TipoMovimientoConstants.INSPECCION_PERIODICA)
            .doOnError(error -> log.error("Error al obtener tipo de movimiento 'Inspección Periódica': {}", error.getMessage()));
    }

    private Mono<Integer> obtenerKilometraje(FinalizarInspeccionRequest request) {
        if (request.getKilometraje() != null) {
            return Mono.just(request.getKilometraje());
        }
        
        return masterDataCacheService.getEquipo(request.getEquipoId())
            .map(Equipo::getKilometraje)
            .switchIfEmpty(Mono.just(0)); // Valor por defecto si no hay kilometraje
    }

    private Mono<BigDecimal> obtenerCostoMovimiento(FinalizarInspeccionRequest request) {
        return obtenerTipoMovimientoInspeccion()
            .flatMap(tipoMovimientoId -> 
                masterDataCacheService.getCatalogoServiciosEmpresa(
                    request.getEmpresaId(),
                    request.getTipoEquipoId(),
                    tipoMovimientoId
                ))
            .map(CatalogoServiciosEmpresa::getCostoServicio)
            .onErrorReturn(BigDecimal.ZERO) // Costo 0 si no se encuentra configuración
            .doOnNext(costo -> log.debug("Costo de movimiento obtenido: {}", costo));
    }

    private Mono<Void> procesarObservaciones(NeumaticoInspeccionadoRequest neumaticoRequest, 
                                           FinalizarInspeccionRequest request) {
        if (neumaticoRequest.getObservacionNeumaticoList() == null || 
            neumaticoRequest.getObservacionNeumaticoList().isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(neumaticoRequest.getObservacionNeumaticoList())
            .flatMap(observacion -> {
                // Establecer el usuarioCreacionId si no está presente
                if (observacion.getUsuarioCreacionId() == null) {
                    observacion.setUsuarioCreacionId(request.getUsuarioId());
                }
                // Establecer el equipoId si no está presente
                if (observacion.getEquipoId() == null) {
                    observacion.setEquipoId(request.getEquipoId());
                }
                
                return observacionNeumaticoService.saveObservacion(observacion);
            })
            .then()
            .doOnSuccess(result -> log.debug("Observaciones procesadas para neumático: {}", neumaticoRequest.getNeumaticoId()));
    }
}
