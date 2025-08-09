package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.EstadoObservacionConstants;
import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import com.dliriotech.tms.tyreservice.exception.ObservacionCreationException;
import com.dliriotech.tms.tyreservice.exception.ObservacionMasterDataException;
import com.dliriotech.tms.tyreservice.exception.ObservacionNotFoundException;
import com.dliriotech.tms.tyreservice.exception.ObservacionProcessingException;
import com.dliriotech.tms.tyreservice.exception.ObservacionUpdateException;
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
                                //.switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId, tipoMovimientoId)))
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
                //.switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId)))
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
                        //.switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId, EstadoObservacionConstants.PENDIENTE)))
                )
                .flatMap(this::enrichObservacionWithRelations)
                .doOnError(error -> log.error("Error al obtener observaciones pendientes para neumático {}: {}", 
                    neumaticoId, error.getMessage()));
    }

    @Override
    public Flux<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByEquipoId(Integer equipoId) {
        log.info("Obteniendo observaciones pendientes de neumáticos del equipo {}", equipoId);

        // Validación de parámetros
        if (equipoId == null || equipoId <= 0) {
            return Flux.error(ObservacionProcessingException.invalidEquipoId(equipoId));
        }

        return observacionMasterDataCacheService.getEstadoObservacionIdByNombre(EstadoObservacionConstants.PENDIENTE)
                .onErrorMap(error -> ObservacionMasterDataException.estadoNotFound(EstadoObservacionConstants.PENDIENTE))
                .flatMapMany(estadoPendienteId ->
                                observacionNeumaticoRepository.findByEquipoIdAndEstadoObservacionId(
                                        equipoId, estadoPendienteId)
                        //.switchIfEmpty(Flux.error(new ObservacionNotFoundException(neumaticoId, EstadoObservacionConstants.PENDIENTE)))
                )
                .flatMap(this::enrichObservacionWithRelations)
                .doOnError(error -> log.error("Error al obtener observaciones pendientes para neumáticos por equipo {}: {}",
                        equipoId, error.getMessage()));
    }

    @Override
    public Mono<ObservacionNeumaticoResponse> saveObservacion(ObservacionNeumaticoNuevoRequest observacionNeumaticoNuevoRequest) {
        log.info("Creando nueva observación para neumático {}", observacionNeumaticoNuevoRequest.getNeumaticoId());
        
        // Validaciones de entrada
        return Mono.fromCallable(() -> validateObservacionRequest(observacionNeumaticoNuevoRequest))
                .subscribeOn(Schedulers.boundedElastic())
                .then(observacionMasterDataCacheService.getEstadoObservacionIdByNombre(EstadoObservacionConstants.PENDIENTE)
                    .onErrorMap(error -> ObservacionMasterDataException.estadoNotFound(EstadoObservacionConstants.PENDIENTE)))
                .flatMap(estadoPendienteId -> 
                    observacionMasterDataCacheService.getTipoObservacion(observacionNeumaticoNuevoRequest.getTipoObservacionId())
                        .onErrorMap(error -> ObservacionCreationException.tipoObservacionNotFound(observacionNeumaticoNuevoRequest.getTipoObservacionId()))
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
                    observacionNeumaticoNuevoRequest.getNeumaticoId(), error.getMessage()));
    }

    @Override
    public Mono<ObservacionNeumaticoResponse> updateObservacion(Integer id, ObservacionNeumaticoUpdateRequest request) {
        log.info("Actualizando observación con ID: {}", id);
        
        // Validaciones de entrada
        return Mono.fromCallable(() -> validateUpdateRequest(id, request))
                .subscribeOn(Schedulers.boundedElastic())
                // Buscar la observación existente
                .flatMap(validatedRequest -> 
                    observacionNeumaticoRepository.findById(id)
                        .switchIfEmpty(Mono.error(ObservacionUpdateException.notFound(id)))
                )
                // Validar que el nuevo estado existe (si se está actualizando)
                .flatMap(existingObservacion -> {
                    if (request.getEstadoObservacionId() != null) {
                        return observacionMasterDataCacheService.getEstadoObservacion(request.getEstadoObservacionId())
                            .onErrorMap(error -> ObservacionUpdateException.estadoNotFound(request.getEstadoObservacionId()))
                            .map(estadoResponse -> existingObservacion);
                    }
                    return Mono.just(existingObservacion);
                })
                // Validar reglas de negocio y aplicar cambios
                .flatMap(existingObservacion ->
                    validateBusinessRules(existingObservacion, request)
                        .then(Mono.fromCallable(() -> applyUpdates(existingObservacion, request))
                            .subscribeOn(Schedulers.boundedElastic()))
                )
                // Guardar los cambios
                .flatMap(updatedObservacion ->
                    observacionNeumaticoRepository.save(updatedObservacion)
                        .onErrorMap(error -> ObservacionUpdateException.databaseError("actualizar observación", error))
                )
                // Enriquecer con relaciones y devolver response
                .flatMap(this::enrichObservacionWithRelations)
                .doOnSuccess(response -> log.info("Observación actualizada exitosamente con ID: {}", response.getId()))
                .doOnError(error -> log.error("Error al actualizar observación con ID {}: {}", id, error.getMessage()));
    }
    
    private ObservacionNeumaticoUpdateRequest validateUpdateRequest(Integer id, ObservacionNeumaticoUpdateRequest request) {
        if (id == null || id <= 0) {
            throw ObservacionUpdateException.invalidRequest("id", id);
        }
        if (request == null) {
            throw ObservacionUpdateException.invalidRequest("request", "null");
        }
        // Validar que al menos un campo está presente para actualizar
        if (request.getEstadoObservacionId() == null && 
            request.getUsuarioResolucionId() == null && 
            (request.getComentarioResolucion() == null || request.getComentarioResolucion().trim().isEmpty())) {
            throw ObservacionUpdateException.invalidRequest("request", "no hay campos para actualizar");
        }
        // Validar campos específicos si están presentes
        if (request.getEstadoObservacionId() != null && request.getEstadoObservacionId() <= 0) {
            throw ObservacionUpdateException.invalidRequest("estadoObservacionId", request.getEstadoObservacionId());
        }
        if (request.getUsuarioResolucionId() != null && request.getUsuarioResolucionId() <= 0) {
            throw ObservacionUpdateException.invalidRequest("usuarioResolucionId", request.getUsuarioResolucionId());
        }
        return request;
    }
    
    private ObservacionNeumatico applyUpdates(ObservacionNeumatico existing, ObservacionNeumaticoUpdateRequest request) {
        ObservacionNeumatico.ObservacionNeumaticoBuilder builder = existing.toBuilder();
        
        // Si se está cambiando el estado de observación
        if (request.getEstadoObservacionId() != null) {
            builder.estadoObservacionId(request.getEstadoObservacionId());
            
            // Si se está cambiando a un estado diferente al actual
            if (!request.getEstadoObservacionId().equals(existing.getEstadoObservacionId())) {
                // Marcar fecha de resolución automáticamente
                builder.fechaResolucion(LocalDateTime.now(ZoneId.of("America/Lima")));
                
                // Si se proporciona usuario de resolución, usarlo; sino es requerido para estados finales
                if (request.getUsuarioResolucionId() != null) {
                    builder.usuarioResolucionId(request.getUsuarioResolucionId());
                } else if (existing.getUsuarioResolucionId() == null) {
                    // Si no hay usuario de resolución previo y no se proporciona uno, usar el usuario de creación como fallback
                    builder.usuarioResolucionId(existing.getUsuarioCreacionId());
                }
            }
        } else if (request.getUsuarioResolucionId() != null) {
            // Si solo se está actualizando el usuario de resolución sin cambiar estado
            builder.usuarioResolucionId(request.getUsuarioResolucionId());
        }
        
        // Actualizar comentario de resolución si se proporciona
        if (request.getComentarioResolucion() != null) {
            builder.comentarioResolucion(request.getComentarioResolucion().trim());
        }
        
        return builder.build();
    }
    
    private Mono<Void> validateBusinessRules(ObservacionNeumatico existing, ObservacionNeumaticoUpdateRequest request) {
        return observacionMasterDataCacheService.getEstadoObservacion(existing.getEstadoObservacionId())
                .flatMap(currentState -> {
                    String currentStateName = currentState.getNombre();
                    
                    // Validar que no se puede actualizar una observación ya resuelta o cancelada
                    if (EstadoObservacionConstants.RESUELTO.equalsIgnoreCase(currentStateName)) {
                        return Mono.error(ObservacionUpdateException.alreadyResolved(existing.getId()));
                    }
                    
                    if (EstadoObservacionConstants.CANCELADO.equalsIgnoreCase(currentStateName)) {
                        return Mono.error(ObservacionUpdateException.invalidStateTransition(
                            currentStateName, "cualquier estado"));
                    }
                    
                    // Si se está cambiando el estado, validar transiciones válidas
                    if (request.getEstadoObservacionId() != null && 
                        !request.getEstadoObservacionId().equals(existing.getEstadoObservacionId())) {
                        
                        return observacionMasterDataCacheService.getEstadoObservacion(request.getEstadoObservacionId())
                                .flatMap(newState -> {
                                    String newStateName = newState.getNombre();
                                    
                                    // Validar transiciones específicas según reglas de negocio
                                    if (EstadoObservacionConstants.PENDIENTE.equalsIgnoreCase(currentStateName)) {
                                        // Desde Pendiente se puede ir a Resuelto o Cancelado
                                        if (EstadoObservacionConstants.RESUELTO.equalsIgnoreCase(newStateName) ||
                                            EstadoObservacionConstants.CANCELADO.equalsIgnoreCase(newStateName)) {
                                            return Mono.<Void>empty();
                                        } else {
                                            return Mono.error(ObservacionUpdateException.invalidStateTransition(
                                                currentStateName, newStateName));
                                        }
                                    } else {
                                        // Desde cualquier otro estado no se permite cambiar
                                        return Mono.error(ObservacionUpdateException.invalidStateTransition(
                                            currentStateName, newStateName));
                                    }
                                })
                                .onErrorMap(error -> {
                                    if (error instanceof ObservacionUpdateException) {
                                        return error;
                                    }
                                    return ObservacionUpdateException.estadoNotFound(request.getEstadoObservacionId());
                                });
                    }
                    
                    // Si no se está cambiando el estado, permitir la actualización
                    return Mono.<Void>empty();
                })
                .onErrorMap(error -> {
                    if (error instanceof ObservacionUpdateException) {
                        return error;
                    }
                    return ObservacionMasterDataException.cacheError("validar reglas de negocio", error);
                });
    }

    private ObservacionNeumaticoNuevoRequest validateObservacionRequest(ObservacionNeumaticoNuevoRequest request) {
        if (request == null) {
            throw ObservacionCreationException.invalidRequest("request", "null");
        }
        if (request.getNeumaticoId() == null || request.getNeumaticoId() <= 0) {
            throw ObservacionCreationException.invalidRequest("neumaticoId", request.getNeumaticoId());
        }
        if (request.getEquipoId() == null || request.getEquipoId() <= 0) {
            throw ObservacionCreationException.invalidRequest("equipoId", request.getEquipoId());
        }
        if (request.getPosicion() == null || request.getPosicion() <= 0) {
            throw ObservacionCreationException.invalidRequest("posicion", request.getPosicion());
        }
        if (request.getTipoObservacionId() == null || request.getTipoObservacionId() <= 0) {
            throw ObservacionCreationException.invalidRequest("tipoObservacionId", request.getTipoObservacionId());
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw ObservacionCreationException.invalidRequest("descripcion", request.getDescripcion());
        }
        if (request.getUsuarioCreacionId() == null || request.getUsuarioCreacionId() <= 0) {
            throw ObservacionCreationException.invalidRequest("usuarioCreacionId", request.getUsuarioCreacionId());
        }
        return request;
    }
    
    private ObservacionNeumatico buildObservacionEntity(ObservacionNeumaticoNuevoRequest request, Integer estadoPendienteId) {
        return ObservacionNeumatico.builder()
                .neumaticoId(request.getNeumaticoId())
                .equipoId(request.getEquipoId())
                .posicion(request.getPosicion())
                .tipoObservacionId(request.getTipoObservacionId())
                .descripcion(request.getDescripcion().trim())
                .estadoObservacionId(estadoPendienteId)
                .fechaCreacion(LocalDateTime.now(ZoneId.of("America/Lima")))
                .usuarioCreacionId(request.getUsuarioCreacionId())
                // Los campos de resolución se dejan null inicialmente
                .fechaResolucion(null)
                .usuarioResolucionId(null)
                .comentarioResolucion(null)
                .build();
    }

    private Mono<ObservacionNeumaticoResponse> enrichObservacionWithRelations(ObservacionNeumatico observacion) {
        log.debug("Enriqueciendo observación: {}", observacion.getId());
        
        // Obtener las entidades relacionadas de forma paralela usando cache
        Mono<TipoObservacionResponse> tipoObservacionMono = observacion.getTipoObservacionId() != null ?
                observacionMasterDataCacheService.getTipoObservacion(observacion.getTipoObservacionId())
                    .onErrorMap(error -> ObservacionMasterDataException.tipoNotFound(observacion.getTipoObservacionId()))
                    .subscribeOn(Schedulers.boundedElastic()) :
                Mono.just(TipoObservacionResponse.builder().build());

        Mono<EstadoObservacionResponse> estadoObservacionMono = observacion.getEstadoObservacionId() != null ?
                observacionMasterDataCacheService.getEstadoObservacion(observacion.getEstadoObservacionId())
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
                .neumaticoId(entity.getNeumaticoId())
                .equipoId(entity.getEquipoId())
                .posicion(entity.getPosicion())
                .tipoObservacionResponse(tipoObservacionResponse.getId() != null ? tipoObservacionResponse : null)
                .descripcion(entity.getDescripcion())
                .estadoObservacionResponse(estadoObservacionResponse.getId() != null ? estadoObservacionResponse : null)
                .fechaCreacion(entity.getFechaCreacion())
                .usuarioCreacionId(entity.getUsuarioCreacionId())
                .fechaResolucion(entity.getFechaResolucion())
                .usuarioResolucionId(entity.getUsuarioResolucionId())
                .comentarioResolucion(entity.getComentarioResolucion())
                .build();
    }
}