package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.EstadoObservacionConstants;
import com.dliriotech.tms.tyreservice.dto.EstadoObservacionResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.TipoObservacionResponse;
import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import com.dliriotech.tms.tyreservice.exception.ObservacionCreationException;
import com.dliriotech.tms.tyreservice.exception.ObservacionMasterDataException;
import com.dliriotech.tms.tyreservice.exception.ObservacionNotFoundException;
import com.dliriotech.tms.tyreservice.exception.ObservacionProcessingException;
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

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservacionNeumaticoServiceImpl implements ObservacionNeumaticoService {

    private final MapaObservacionSolucionRepository mapaObservacionSolucionRepository;
    private final ObservacionNeumaticoRepository observacionNeumaticoRepository;
    private final ObservacionMasterDataCacheService observacionMasterDataCacheService;

    @Override
    public Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoIdAndTipoMovimientoId(Integer neumaticoId, Integer tipoMovimientoId) {
        log.info("Obteniendo observaciones solucionables para neumático {} y tipo movimiento {}", neumaticoId, tipoMovimientoId);

        // Validación de parámetros
        if (neumaticoId == null || neumaticoId <= 0) {
            return Flux.error(ObservacionProcessingException.invalidNeumaticoId(neumaticoId));
        }
        if (tipoMovimientoId == null || tipoMovimientoId <= 0) {
            return Flux.error(ObservacionProcessingException.invalidTipoMovimiento(tipoMovimientoId));
        }

        // Primero obtener el ID del estado "Pendiente" desde el caché
        return observacionMasterDataCacheService.getEstadoObservacionIdByNombre(EstadoObservacionConstants.PENDIENTE)
                .onErrorMap(error -> ObservacionMasterDataException.estadoNotFound(EstadoObservacionConstants.PENDIENTE))
                .flatMapMany(estadoPendienteId ->
                        mapaObservacionSolucionRepository.findTipoObservacionIdsByTipoMovimientoId(tipoMovimientoId)
                                .collectList()
                                .filter(tipoObservacionIds -> !tipoObservacionIds.isEmpty())
                                .flatMapMany(tipoObservacionIds ->
                                        observacionNeumaticoRepository.findByNeumaticoIdAndTipoObservacionIdsAndEstadoObservacionId(
                                                neumaticoId, tipoObservacionIds, estadoPendienteId)
                                )
                                .switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId, tipoMovimientoId)))
                )
                .flatMap(this::enrichObservacionWithRelations)
                .doOnError(error -> log.error("Error al obtener observaciones para neumático {} y tipo movimiento {}: {}",
                        neumaticoId, tipoMovimientoId, error.getMessage()));
    }

    @Override
    public Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoId(Integer neumaticoId) {
        log.info("Obteniendo todas las observaciones para neumático {}", neumaticoId);
        
        // Validación de parámetros
        if (neumaticoId == null || neumaticoId <= 0) {
            return Flux.error(ObservacionProcessingException.invalidNeumaticoId(neumaticoId));
        }
        
        return observacionNeumaticoRepository.findByNeumaticoIdOrderByFechaCreacionDesc(neumaticoId)
                .switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId)))
                .flatMap(this::enrichObservacionWithRelations)
                .doOnError(error -> log.error("Error al obtener observaciones para neumático {}: {}", 
                    neumaticoId, error.getMessage()));
    }

    @Override
    public Flux<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByNeumaticoId(Integer neumaticoId) {
        log.info("Obteniendo observaciones pendientes para neumático {}", neumaticoId);
        
        // Validación de parámetros
        if (neumaticoId == null || neumaticoId <= 0) {
            return Flux.error(ObservacionProcessingException.invalidNeumaticoId(neumaticoId));
        }
        
        // Primero obtener el ID del estado "Pendiente" desde el caché
        return observacionMasterDataCacheService.getEstadoObservacionIdByNombre(EstadoObservacionConstants.PENDIENTE)
                .onErrorMap(error -> ObservacionMasterDataException.estadoNotFound(EstadoObservacionConstants.PENDIENTE))
                .flatMapMany(estadoPendienteId ->
                    observacionNeumaticoRepository.findByNeumaticoIdAndEstadoObservacionIdOrderByFechaCreacionDesc(
                        neumaticoId, estadoPendienteId)
                        .switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId, EstadoObservacionConstants.PENDIENTE)))
                )
                .flatMap(this::enrichObservacionWithRelations)
                .doOnError(error -> log.error("Error al obtener observaciones pendientes para neumático {}: {}", 
                    neumaticoId, error.getMessage()));
    }

    @Override
    public Mono<ObservacionNeumaticoResponse> saveObservacion(ObservacionNeumaticoNuevoRequest observacionNeumaticoNuevoRequest) {
        log.info("Creando nueva observación para neumático {}", observacionNeumaticoNuevoRequest.getIdNeumatico());
        
        // Validaciones de entrada
        return Mono.fromCallable(() -> validateObservacionRequest(observacionNeumaticoNuevoRequest))
                .subscribeOn(Schedulers.boundedElastic())
                .then(observacionMasterDataCacheService.getEstadoObservacionIdByNombre(EstadoObservacionConstants.PENDIENTE)
                    .onErrorMap(error -> ObservacionMasterDataException.estadoNotFound(EstadoObservacionConstants.PENDIENTE)))
                .flatMap(estadoPendienteId -> 
                    observacionMasterDataCacheService.getTipoObservacion(observacionNeumaticoNuevoRequest.getIdTipoObservacion())
                        .onErrorMap(error -> ObservacionCreationException.tipoObservacionNotFound(observacionNeumaticoNuevoRequest.getIdTipoObservacion()))
                        .map(tipoObservacion -> estadoPendienteId)
                )
                .flatMap(estadoPendienteId ->
                    Mono.fromCallable(() -> buildObservacionEntity(observacionNeumaticoNuevoRequest, estadoPendienteId))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .flatMap(observacionEntity ->
                    observacionNeumaticoRepository.save(observacionEntity)
                        .onErrorMap(error -> ObservacionCreationException.databaseError("guardar observación", error))
                )
                .flatMap(this::enrichObservacionWithRelations)
                .doOnSuccess(response -> log.info("Observación creada exitosamente con ID: {}", response.getId()))
                .doOnError(error -> log.error("Error al crear observación para neumático {}: {}", 
                    observacionNeumaticoNuevoRequest.getIdNeumatico(), error.getMessage()));
    }
    
    private ObservacionNeumaticoNuevoRequest validateObservacionRequest(ObservacionNeumaticoNuevoRequest request) {
        if (request == null) {
            throw ObservacionCreationException.invalidRequest("request", "null");
        }
        if (request.getIdNeumatico() == null || request.getIdNeumatico() <= 0) {
            throw ObservacionCreationException.invalidRequest("idNeumatico", request.getIdNeumatico());
        }
        if (request.getIdEquipo() == null || request.getIdEquipo() <= 0) {
            throw ObservacionCreationException.invalidRequest("idEquipo", request.getIdEquipo());
        }
        if (request.getPosicion() == null || request.getPosicion() <= 0) {
            throw ObservacionCreationException.invalidRequest("posicion", request.getPosicion());
        }
        if (request.getIdTipoObservacion() == null || request.getIdTipoObservacion() <= 0) {
            throw ObservacionCreationException.invalidRequest("idTipoObservacion", request.getIdTipoObservacion());
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw ObservacionCreationException.invalidRequest("descripcion", request.getDescripcion());
        }
        if (request.getIdUsuarioCreacion() == null || request.getIdUsuarioCreacion() <= 0) {
            throw ObservacionCreationException.invalidRequest("idUsuarioCreacion", request.getIdUsuarioCreacion());
        }
        return request;
    }
    
    private ObservacionNeumatico buildObservacionEntity(ObservacionNeumaticoNuevoRequest request, Integer estadoPendienteId) {
        return ObservacionNeumatico.builder()
                .idNeumatico(request.getIdNeumatico())
                .idEquipo(request.getIdEquipo())
                .posicion(request.getPosicion())
                .idTipoObservacion(request.getIdTipoObservacion())
                .descripcion(request.getDescripcion().trim())
                .idEstadoObservacion(estadoPendienteId)
                .fechaCreacion(LocalDateTime.now(ZoneId.of("America/Lima")))
                .idUsuarioCreacion(request.getIdUsuarioCreacion())
                // Los campos de resolución se dejan null inicialmente
                .fechaResolucion(null)
                .idUsuarioResolucion(null)
                .comentarioResolucion(null)
                .build();
    }

    private Mono<ObservacionNeumaticoResponse> enrichObservacionWithRelations(ObservacionNeumatico observacion) {
        log.debug("Enriqueciendo observación: {}", observacion.getId());
        
        // Obtener las entidades relacionadas de forma paralela usando cache
        Mono<TipoObservacionResponse> tipoObservacionMono = observacion.getIdTipoObservacion() != null ?
                observacionMasterDataCacheService.getTipoObservacion(observacion.getIdTipoObservacion())
                    .onErrorMap(error -> ObservacionMasterDataException.tipoNotFound(observacion.getIdTipoObservacion()))
                    .subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(TipoObservacionResponse.builder().build());

        Mono<EstadoObservacionResponse> estadoObservacionMono = observacion.getIdEstadoObservacion() != null ?
                observacionMasterDataCacheService.getEstadoObservacion(observacion.getIdEstadoObservacion())
                    .onErrorMap(error -> ObservacionMasterDataException.cacheError("obtener estado observacion", error))
                    .subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(EstadoObservacionResponse.builder().build());

        // Combinar los resultados
        return Mono.zip(tipoObservacionMono, estadoObservacionMono)
                .flatMap(tuple -> 
                    Mono.fromCallable(() -> mapEntityToResponse(observacion, tuple.getT1(), tuple.getT2()))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .onErrorMap(error -> ObservacionProcessingException.enrichmentError(observacion.getId().toString(), error));
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