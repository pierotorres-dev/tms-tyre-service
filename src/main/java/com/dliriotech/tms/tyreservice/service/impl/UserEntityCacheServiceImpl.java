package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.UserInfoResponse;
import com.dliriotech.tms.tyreservice.service.UserEntityCacheService;
import com.dliriotech.tms.tyreservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Implementación del servicio de cache para entidades de usuarios.
 * Utiliza Redis para cachear la información de usuarios con TTL configurable.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserEntityCacheServiceImpl implements UserEntityCacheService {

    private final UserService userService;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    @Value("${app.cache.user-info.ttl-hours:48}")
    private long cacheUserInfoTtlHours;

    @Value("${app.cache.prefixes.user-info:cache:userInfo}")
    private String userInfoCachePrefix;

    @Override
    public Mono<UserInfoResponse> getUserInfo(Integer userId) {
        if (userId == null || userId <= 0) {
            log.info("ID de usuario inválido: {}", userId);
            return Mono.empty();
        }

        String cacheKey = buildUserInfoCacheKey(userId);
        
        return redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(UserInfoResponse.class)
                .doOnNext(cached -> log.info("Usuario {} encontrado en cache", userId))
                .onErrorResume(error -> {
                    log.warn("Error al acceder al cache para usuario {}: {}", userId, error.getMessage());
                    return Mono.empty();
                })
                .switchIfEmpty(
                    fetchAndCacheUserInfo(userId, cacheKey)
                        .doOnNext(user -> log.info("Usuario {} obtenido de base de datos y cacheado", userId))
                )
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> invalidateUserInfo(Integer userId) {
        if (userId == null || userId <= 0) {
            return Mono.empty();
        }

        String cacheKey = buildUserInfoCacheKey(userId);
        
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(deleted -> {
                    if (deleted > 0) {
                        log.info("Cache invalidado para usuario: {}", userId);
                    }
                })
                .doOnError(error -> log.warn("Error al invalidar cache para usuario {}: {}", userId, error.getMessage()))
                .onErrorResume(error -> Mono.empty())
                .then();
    }

    @Override
    public Mono<Void> invalidateAllUserInfo() {
        String pattern = userInfoCachePrefix + ":*";
        
        return redisTemplate.keys(pattern)
                .flatMap(redisTemplate::delete)
                .reduce(0L, Long::sum)
                .doOnSuccess(totalDeleted -> log.info("Invalidadas {} entradas de cache de usuarios", totalDeleted))
                .doOnError(error -> log.warn("Error al invalidar todo el cache de usuarios: {}", error.getMessage()))
                .onErrorResume(error -> Mono.just(0L))
                .then();
    }

    /**
     * Obtiene la información del usuario desde la base de datos y la almacena en cache.
     */
    private Mono<UserInfoResponse> fetchAndCacheUserInfo(Integer userId, String cacheKey) {
        return userService.getUserInfoById(userId)
                .flatMap(userInfo -> 
                    cacheUserInfo(cacheKey, userInfo)
                        .thenReturn(userInfo)
                )
                .doOnNext(userInfo -> log.info("Usuario {} cacheado exitosamente", userId))
                .onErrorResume(error -> {
                    log.error("Error al obtener información del usuario {}: {}", userId, error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Almacena la información del usuario en cache con TTL.
     */
    private Mono<Boolean> cacheUserInfo(String cacheKey, UserInfoResponse userInfo) {
        Duration ttl = Duration.ofHours(cacheUserInfoTtlHours);
        
        return redisTemplate.opsForValue()
                .set(cacheKey, userInfo, ttl)
                .doOnError(error -> log.warn("Error al cachear información de usuario: {}", error.getMessage()))
                .onErrorReturn(false);
    }

    /**
     * Construye la clave de cache para la información del usuario.
     */
    private String buildUserInfoCacheKey(Integer userId) {
        return userInfoCachePrefix + ":" + userId;
    }
}
