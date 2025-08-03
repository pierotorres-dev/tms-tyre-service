package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.entity.ConfiguracionEmpresaEquipo;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.TipoEquipo;
import com.dliriotech.tms.tyreservice.repository.*;
import com.dliriotech.tms.tyreservice.service.MasterDataCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasterDataCacheServiceImpl implements MasterDataCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    private final EquipoRepository equipoRepository;
    private final ConfiguracionEmpresaEquipoRepository configuracionEmpresaEquipoRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;

    // TTL para diferentes tipos de datos
    private static final Duration EQUIPO_TTL = Duration.ofHours(4);
    private static final Duration MASTER_DATA_TTL = Duration.ofHours(24);
    private static final Duration MOVEMENT_TTL = Duration.ofHours(1);

    // Prefijos de cache
    private static final String EQUIPO_PREFIX = "cache:equipo:";
    private static final String CONFIG_PREFIX = "cache:config:";
    private static final String TIPO_EQUIPO_PREFIX = "cache:tipoEquipo:";
    private static final String MOVEMENT_PREFIX = "cache:movement:";
    private static final String RTD_MINIMO_PREFIX = "cache:rtdMinimo:";

    @Override
    public Mono<Equipo> getEquipo(Integer equipoId) {
        String cacheKey = EQUIPO_PREFIX + equipoId;
        
        return getCachedEntity(cacheKey, Equipo.class)
                .switchIfEmpty(
                    equipoRepository.findById(equipoId)
                            .flatMap(equipo -> cacheEntity(cacheKey, equipo, EQUIPO_TTL)
                                    .thenReturn(equipo))
                            .doOnSuccess(equipo -> log.debug("Equipo {} cargado desde BD y cacheado", equipoId))
                )
                .doOnNext(equipo -> log.debug("Equipo {} obtenido desde cache", equipoId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ConfiguracionEmpresaEquipo> getConfiguracionEmpresaEquipo(Integer empresaId, Integer tipoEquipoId) {
        String cacheKey = CONFIG_PREFIX + empresaId + ":" + tipoEquipoId;
        
        return getCachedEntity(cacheKey, ConfiguracionEmpresaEquipo.class)
                .switchIfEmpty(
                    configuracionEmpresaEquipoRepository.findByIdEmpresaAndIdTipoEquipo(empresaId, tipoEquipoId)
                            .flatMap(config -> cacheEntity(cacheKey, config, MASTER_DATA_TTL)
                                    .thenReturn(config))
                            .doOnSuccess(config -> log.debug("Configuración empresa {} - tipo {} cargada desde BD", 
                                    empresaId, tipoEquipoId))
                )
                .doOnNext(config -> log.debug("Configuración empresa {} - tipo {} obtenida desde cache", 
                        empresaId, tipoEquipoId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<TipoEquipo> getTipoEquipo(Integer tipoEquipoId) {
        String cacheKey = TIPO_EQUIPO_PREFIX + tipoEquipoId;
        
        return getCachedEntity(cacheKey, TipoEquipo.class)
                .switchIfEmpty(
                    tipoEquipoRepository.findById(tipoEquipoId)
                            .flatMap(tipo -> cacheEntity(cacheKey, tipo, MASTER_DATA_TTL)
                                    .thenReturn(tipo))
                            .doOnSuccess(tipo -> log.debug("Tipo equipo {} cargado desde BD y cacheado", tipoEquipoId))
                )
                .doOnNext(tipo -> log.debug("Tipo equipo {} obtenido desde cache", tipoEquipoId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<MovimientoNeumatico> getLatestReencaucheMovement(Integer neumaticoId) {
        String cacheKey = MOVEMENT_PREFIX + neumaticoId + ":latest_reencauche";
        
        return getCachedEntity(cacheKey, MovimientoNeumatico.class)
                .switchIfEmpty(
                    movimientoNeumaticoRepository.findTopByIdNeumaticoAndIdTipoMovimientoOrderByIdDesc(neumaticoId, 4)
                            .flatMap(movement -> cacheEntity(cacheKey, movement, MOVEMENT_TTL)
                                    .thenReturn(movement))
                            .doOnSuccess(movement -> log.debug("Último movimiento reencauche para neumático {} cargado desde BD", 
                                    neumaticoId))
                )
                .doOnNext(movement -> log.debug("Último movimiento reencauche para neumático {} obtenido desde cache", 
                        neumaticoId))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<BigDecimal> getRtdMinimo(Integer equipoId) {
        String cacheKey = RTD_MINIMO_PREFIX + equipoId;
        
        return getCachedEntity(cacheKey, BigDecimal.class)
                .switchIfEmpty(
                    calculateAndCacheRtdMinimo(equipoId, cacheKey)
                )
                .doOnNext(rtdMinimo -> log.debug("RTD mínimo para equipo {} obtenido: {}", equipoId, rtdMinimo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<BigDecimal> calculateAndCacheRtdMinimo(Integer equipoId, String cacheKey) {
        return getEquipo(equipoId)
                .flatMap(equipo -> 
                    getConfiguracionEmpresaEquipo(equipo.getEmpresaId(), equipo.getTipoEquipoId())
                            .map(ConfiguracionEmpresaEquipo::getRtdMinimoReencauche)
                            .switchIfEmpty(
                                getTipoEquipo(equipo.getTipoEquipoId())
                                        .map(TipoEquipo::getRtadMinimoReencauche)
                            )
                )
                .defaultIfEmpty(BigDecimal.ZERO)
                .flatMap(rtdMinimo -> 
                    cacheEntity(cacheKey, rtdMinimo, EQUIPO_TTL)
                            .thenReturn(rtdMinimo)
                )
                .doOnSuccess(rtdMinimo -> log.debug("RTD mínimo para equipo {} calculado y cacheado: {}", 
                        equipoId, rtdMinimo));
    }

    @Override
    public Mono<Void> invalidateEquipoCache(Integer equipoId) {
        return Mono.when(
                redisTemplate.delete(EQUIPO_PREFIX + equipoId),
                redisTemplate.delete(RTD_MINIMO_PREFIX + equipoId)
        ).doOnSuccess(v -> log.debug("Cache de equipo {} invalidado", equipoId));
    }

    @Override
    public Mono<Void> invalidateConfiguracionCache(Integer empresaId, Integer tipoEquipoId) {
        String cacheKey = CONFIG_PREFIX + empresaId + ":" + tipoEquipoId;
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(v -> log.debug("Cache de configuración empresa {} - tipo {} invalidado", 
                        empresaId, tipoEquipoId))
                .then();
    }

    @Override
    public Mono<Void> invalidateMovimientoCache(Integer neumaticoId) {
        String cacheKey = MOVEMENT_PREFIX + neumaticoId + ":latest_reencauche";
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(v -> log.debug("Cache de movimiento para neumático {} invalidado", neumaticoId))
                .then();
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