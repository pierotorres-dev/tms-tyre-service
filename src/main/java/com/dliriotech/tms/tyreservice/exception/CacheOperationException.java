package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay errores en operaciones de caché.
 */
public class CacheOperationException extends TyreServiceException {

    public CacheOperationException(String operation, String message) {
        super(ErrorCode.CACHE_OPERATION_ERROR,
            String.format("Error en operación de caché '%s': %s", operation, message));
    }

    public CacheOperationException(String operation, String message, Throwable cause) {
        super(ErrorCode.CACHE_OPERATION_ERROR,
            String.format("Error en operación de caché '%s': %s", operation, message), cause);
    }
}