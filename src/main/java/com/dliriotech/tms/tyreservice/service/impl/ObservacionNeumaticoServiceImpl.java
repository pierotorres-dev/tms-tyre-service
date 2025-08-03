package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.EstadoObservacionResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.TipoObservacionResponse;
import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import com.dliriotech.tms.tyreservice.repository.MapaObservacionSolucionRepository;
import com.dliriotech.tms.tyreservice.repository.ObservacionNeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.ObservacionMasterDataCacheService;
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
    private final ObservacionMasterDataCacheService observacionMasterDataCacheService;

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
        
        // Obtener las entidades relacionadas de forma paralela usando cache
        Mono<TipoObservacionResponse> tipoObservacionMono = observacion.getIdTipoObservacion() != null ?
                observacionMasterDataCacheService.getTipoObservacion(observacion.getIdTipoObservacion()).subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(TipoObservacionResponse.builder().build());

        Mono<EstadoObservacionResponse> estadoObservacionMono = observacion.getIdEstadoObservacion() != null ?
                observacionMasterDataCacheService.getEstadoObservacion(observacion.getIdEstadoObservacion()).subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(EstadoObservacionResponse.builder().build());

        // Combinar los resultados
        return Mono.zip(tipoObservacionMono, estadoObservacionMono)
                .flatMap(tuple -> 
                    Mono.fromCallable(() -> mapEntityToResponse(observacion, tuple.getT1(), tuple.getT2()))
                        .subscribeOn(Schedulers.boundedElastic())
                );
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
}