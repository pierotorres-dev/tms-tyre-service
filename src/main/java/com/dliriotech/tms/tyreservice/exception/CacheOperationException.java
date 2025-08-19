package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay errores en operaciones de cache.
 */
public class CacheOperationException extends TyreServiceException {
    
    public CacheOperationException(String operation, String message) {
        super(
            String.format("Error en operación de cache '%s': %s", operation, message),
            HttpStatus.INTERNAL_SERVER_ERROR,
            "TYR-CACHE-001"
        );
    }

    public CacheOperationException(String operation, String message, Throwable cause) {
        super(
            String.format("Error en operación de cache '%s': %s", operation, message),
            HttpStatus.INTERNAL_SERVER_ERROR,
            "TYR-CACHE-002",
            cause
        );
    }

    public CacheOperationException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "TYR-CACHE-003");
    }
}
