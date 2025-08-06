package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.EstadoObservacionResponse;
import com.dliriotech.tms.tyreservice.dto.TipoObservacionResponse;
import com.dliriotech.tms.tyreservice.entity.EstadoObservacion;
import com.dliriotech.tms.tyreservice.entity.TipoObservacion;
import com.dliriotech.tms.tyreservice.exception.NeumaticoException;
import com.dliriotech.tms.tyreservice.repository.EstadoObservacionRepository;
import com.dliriotech.tms.tyreservice.repository.TipoObservacionRepository;
import com.dliriotech.tms.tyreservice.service.ObservacionMasterDataCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservacionMasterDataCacheServiceImpl implements ObservacionMasterDataCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final TipoObservacionRepository tipoObservacionRepository;
    private final EstadoObservacionRepository estadoObservacionRepository;

    // TTL para datos maestros
    private static final Duration MASTER_DATA_TTL = Duration.ofHours(24);

    // Prefijos de cache
    private static final String TIPO_OBSERVACION_PREFIX = "cache:tipoObservacion:";
    private static final String ESTADO_OBSERVACION_PREFIX = "cache:estadoObservacion:";
    private static final String ESTADO_OBSERVACION_BYNAME_PREFIX = "cache:estadoObservacion:byName:";

    @Override
    public Mono<TipoObservacionResponse> getTipoObservacion(Integer tipoObservacionId) {
        String cacheKey = TIPO_OBSERVACION_PREFIX + tipoObservacionId;

        return getCachedEntity(cacheKey, TipoObservacionResponse.class)
                .switchIfEmpty(
                        tipoObservacionRepository.findById(tipoObservacionId)
                                .map(this::mapTipoObservacionToResponse)
                                .flatMap(tipoResponse -> cacheEntity(cacheKey, tipoResponse, MASTER_DATA_TTL)
                                        .thenReturn(tipoResponse))
                                .doOnSuccess(tipo -> log.debug("Tipo observación {} cargado desde BD y cacheado", tipoObservacionId))
                )
                .defaultIfEmpty(TipoObservacionResponse.builder().build())
                .doOnNext(tipo -> log.debug("Tipo observación {} obtenido desde cache", tipoObservacionId))
                .onErrorResume(throwable -> {
                    log.error("Error al obtener tipo de observación {}: {}", tipoObservacionId, throwable.getMessage());
                    return Mono.just(TipoObservacionResponse.builder().build());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<EstadoObservacionResponse> getEstadoObservacion(Integer estadoObservacionId) {
        String cacheKey = ESTADO_OBSERVACION_PREFIX + estadoObservacionId;

        return getCachedEntity(cacheKey, EstadoObservacionResponse.class)
                .switchIfEmpty(
                        estadoObservacionRepository.findById(estadoObservacionId)
                                .map(this::mapEstadoObservacionToResponse)
                                .flatMap(estadoResponse -> cacheEntity(cacheKey, estadoResponse, MASTER_DATA_TTL)
                                        .thenReturn(estadoResponse))
                                .doOnSuccess(estado -> log.debug("Estado observación {} cargado desde BD y cacheado", estadoObservacionId))
                )
                .defaultIfEmpty(EstadoObservacionResponse.builder().build())
                .doOnNext(estado -> log.debug("Estado observación {} obtenido desde cache", estadoObservacionId))
                .onErrorResume(throwable -> {
                    log.error("Error al obtener estado de observación {}: {}", estadoObservacionId, throwable.getMessage());
                    return Mono.just(EstadoObservacionResponse.builder().build());
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Integer> getEstadoObservacionIdByNombre(String nombre) {
        String cacheKey = ESTADO_OBSERVACION_BYNAME_PREFIX + nombre;

        return redisTemplate.opsForValue().get(cacheKey)
                .cast(Integer.class)
                .switchIfEmpty(
                        estadoObservacionRepository.findByNombre(nombre)
                                .map(EstadoObservacion::getId)
                                .flatMap(id -> {
                                    // Guardar en cache por 24 horas
                                    return redisTemplate.opsForValue()
                                            .set(cacheKey, id, Duration.ofHours(24))
                                            .thenReturn(id);
                                })
                                .doOnError(error -> log.error("Error al obtener ID de estado de observación para nombre {}: {}",
                                        nombre, error.getMessage()))
                                .switchIfEmpty(Mono.error(new NeumaticoException("404",
                                        "No se encontró estado de observación con nombre: " + nombre)))
                );
    }

    @Override
    public Mono<Void> invalidateTipoObservacionCache(Integer tipoObservacionId) {
        String cacheKey = TIPO_OBSERVACION_PREFIX + tipoObservacionId;
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(v -> log.debug("Cache de tipo observación {} invalidado", tipoObservacionId))
                .then();
    }

    @Override
    public Mono<Void> invalidateEstadoObservacionCache(Integer estadoObservacionId) {
        String cacheKey = ESTADO_OBSERVACION_PREFIX + estadoObservacionId;
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(v -> log.debug("Cache de estado observación {} invalidado", estadoObservacionId))
                .then();
    }

    @Override
    public Mono<Void> invalidateEstadoObservacionIdByNombreCache(String nombre) {
        String cacheKey = ESTADO_OBSERVACION_BYNAME_PREFIX + nombre;
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(v -> log.debug("Cache de estado observación by name {} invalidado", nombre))
                .then();
    }

    /**
     * Mapea entidad TipoObservacion a TipoObservacionResponse.
     */
    private TipoObservacionResponse mapTipoObservacionToResponse(TipoObservacion entity) {
        return TipoObservacionResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .ambito(entity.getAmbito())
                .descripcion(entity.getDescripcion())
                .activo(entity.getActivo())
                .build();
    }

    /**
     * Mapea entidad EstadoObservacion a EstadoObservacionResponse.
     */
    private EstadoObservacionResponse mapEstadoObservacionToResponse(EstadoObservacion entity) {
        return EstadoObservacionResponse.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .build();
    }

    private <T> Mono<T> getCachedEntity(String cacheKey, Class<T> entityType) {
        return redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(String.class)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, entityType)))
                .onErrorResume(throwable -> {
                    log.warn("Error deserializando desde cache key {}: {}", cacheKey, throwable.getMessage());
                    return Mono.empty();
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

    private <T> Mono<Void> cacheEntity(String cacheKey, T entity, Duration ttl) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(entity))
                .flatMap(json -> redisTemplate.opsForValue().set(cacheKey, json, ttl))
                .onErrorResume(throwable -> {
                    log.warn("Error cacheando entity con key {}: {}", cacheKey, throwable.getMessage());
                    return Mono.empty();
                })
                .then()
                .subscribeOn(Schedulers.boundedElastic());
    }
}
