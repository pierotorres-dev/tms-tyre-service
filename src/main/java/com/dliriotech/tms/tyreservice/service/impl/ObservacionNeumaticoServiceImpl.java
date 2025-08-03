package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.EstadoObservacionResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.TipoObservacionResponse;
import com.dliriotech.tms.tyreservice.entity.EstadoObservacion;
import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import com.dliriotech.tms.tyreservice.entity.TipoObservacion;
import com.dliriotech.tms.tyreservice.repository.EstadoObservacionRepository;
import com.dliriotech.tms.tyreservice.repository.MapaObservacionSolucionRepository;
import com.dliriotech.tms.tyreservice.repository.ObservacionNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.TipoObservacionRepository;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservacionNeumaticoServiceImpl implements ObservacionNeumaticoService {

    private final MapaObservacionSolucionRepository mapaObservacionSolucionRepository;
    private final ObservacionNeumaticoRepository observacionNeumaticoRepository;
    private final TipoObservacionRepository tipoObservacionRepository;
    private final EstadoObservacionRepository estadoObservacionRepository;

    @Override
    public Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoIdAndTipoMovimientoId(Integer neumaticoId, Integer tipoMovimientoId) {
        log.debug("Obteniendo observaciones solucionables para neumático {} y tipo movimiento {}", neumaticoId, tipoMovimientoId);
        
        return mapaObservacionSolucionRepository.findTipoObservacionIdsByTipoMovimientoId(tipoMovimientoId)
                .collectList()
                .filter(tipoObservacionIds -> !tipoObservacionIds.isEmpty())
                .flatMapMany(tipoObservacionIds -> 
                    observacionNeumaticoRepository.findByNeumaticoIdAndTipoObservacionIdsAndEstadoObservacionId(
                        neumaticoId, tipoObservacionIds)
                )
                .flatMap(this::enrichObservacionWithRelations)
                .doOnError(error -> log.error("Error al obtener observaciones para neumático {} y tipo movimiento {}: {}", 
                    neumaticoId, tipoMovimientoId, error.getMessage()))
                .onErrorResume(error -> {
                    log.warn("Retornando flujo vacío debido a error: {}", error.getMessage());
                    return Flux.empty();
                });
    }

    private Mono<ObservacionNeumaticoResponse> enrichObservacionWithRelations(ObservacionNeumatico observacion) {
        log.debug("Enriqueciendo observación: {}", observacion.getId());
        
        // Obtener las entidades relacionadas de forma paralela
        Mono<TipoObservacionResponse> tipoObservacionMono = observacion.getIdTipoObservacion() != null ?
                getTipoObservacionResponse(observacion.getIdTipoObservacion()).subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(TipoObservacionResponse.builder().build());

        Mono<EstadoObservacionResponse> estadoObservacionMono = observacion.getIdEstadoObservacion() != null ?
                getEstadoObservacionResponse(observacion.getIdEstadoObservacion()).subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(EstadoObservacionResponse.builder().build());

        // Combinar los resultados
        return Mono.zip(tipoObservacionMono, estadoObservacionMono)
                .flatMap(tuple -> 
                    Mono.fromCallable(() -> mapEntityToResponse(observacion, tuple.getT1(), tuple.getT2()))
                        .subscribeOn(Schedulers.boundedElastic())
                );
    }

    private Mono<TipoObservacionResponse> getTipoObservacionResponse(Integer tipoObservacionId) {
        log.debug("Obteniendo tipo de observación para ID: {}", tipoObservacionId);
        
        return tipoObservacionRepository.findById(tipoObservacionId)
                .map(this::mapTipoObservacionToResponse)
                .defaultIfEmpty(TipoObservacionResponse.builder().build())
                .onErrorResume(throwable -> {
                    log.error("Error al obtener tipo de observación con ID {}: {}", tipoObservacionId, throwable.getMessage());
                    return Mono.just(TipoObservacionResponse.builder().build());
                });
    }

    private Mono<EstadoObservacionResponse> getEstadoObservacionResponse(Integer estadoObservacionId) {
        log.debug("Obteniendo estado de observación para ID: {}", estadoObservacionId);
        
        return estadoObservacionRepository.findById(estadoObservacionId)
                .map(this::mapEstadoObservacionToResponse)
                .defaultIfEmpty(EstadoObservacionResponse.builder().build())
                .onErrorResume(throwable -> {
                    log.error("Error al obtener estado de observación con ID {}: {}", estadoObservacionId, throwable.getMessage());
                    return Mono.just(EstadoObservacionResponse.builder().build());
                });
    }

    private ObservacionNeumaticoResponse mapEntityToResponse(
            ObservacionNeumatico entity,
            TipoObservacionResponse tipoObservacionResponse,
            EstadoObservacionResponse estadoObservacionResponse) {
        
        return ObservacionNeumaticoResponse.builder()
                .id(entity.getId())
                .idNeumatico(entity.getIdNeumatico())
                .idEquipo(entity.getIdEquipo())
                .posicion(entity.getPosicion())
                .tipoObservacionResponse(tipoObservacionResponse.getId() != null ? tipoObservacionResponse : null)
                .descripcion(entity.getDescripcion())
                .estadoObservacionResponse(estadoObservacionResponse.getId() != null ? estadoObservacionResponse : null)
                .fechaCreacion(entity.getFechaCreacion())
                .idUsuarioCreacion(entity.getIdUsuarioCreacion())
                .fechaResolucion(entity.getFechaResolucion())
                .idUsuarioResolucion(entity.getIdUsuarioResolucion())
                .comentarioResolucion(entity.getComentarioResolucion())
                .build();
    }

    private TipoObservacionResponse mapTipoObservacionToResponse(TipoObservacion entity) {
        return TipoObservacionResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .ambito(entity.getAmbito())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo() != null && entity.getActivo() ? 1 : 0)
                .build();
    }

    private EstadoObservacionResponse mapEstadoObservacionToResponse(EstadoObservacion entity) {
        return EstadoObservacionResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .build();
    }
}
