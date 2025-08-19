package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.UserInfoResponse;
import reactor.core.publisher.Mono;

/**
 * Servicio especializado para el cache de entidades de usuarios.
 * Implementa el patrón Repository para el cache, separando la lógica de cache
 * de la lógica de negocio principal.
 */
public interface UserEntityCacheService {

    /**
     * Obtiene información de usuario desde cache o base de datos.
     * @param userId ID del usuario
     * @return Mono con la información del usuario, o Mono.empty() si no existe
     */
    Mono<UserInfoResponse> getUserInfo(Integer userId);

    /**
     * Invalida el cache para un usuario específico.
     * @param userId ID del usuario a invalidar
     * @return Mono que indica la finalización
     */
    Mono<Void> invalidateUserInfo(Integer userId);

    /**
     * Invalida todo el cache de usuarios.
     * @return Mono que indica la finalización
     */
    Mono<Void> invalidateAllUserInfo();
}
