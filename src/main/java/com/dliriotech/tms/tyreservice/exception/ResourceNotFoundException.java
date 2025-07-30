package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción base para recursos no encontrados.
 */
public abstract class ResourceNotFoundException extends TyreServiceException {
    
    protected ResourceNotFoundException(String resourceType, String identifier) {
        super(
            String.format("%s con identificador '%s' no fue encontrado", resourceType, identifier),
            HttpStatus.NOT_FOUND,
            generateCode(resourceType)
        );
    }

    protected ResourceNotFoundException(String resourceType, String identifier, Throwable cause) {
        super(
            String.format("%s con identificador '%s' no fue encontrado", resourceType, identifier),
            HttpStatus.NOT_FOUND,
            generateCode(resourceType),
            cause
        );
    }

    private static String generateCode(String resourceType) {
        return String.format("TYR-%s-NF-001", 
            resourceType.toUpperCase().replace(" ", "-"));
    }
}
