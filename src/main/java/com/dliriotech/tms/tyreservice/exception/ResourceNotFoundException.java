package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción base para recursos no encontrados.
 * Las subclases deben proporcionar el {@link ErrorCode} correspondiente.
 */
public abstract class ResourceNotFoundException extends TyreServiceException {

    protected ResourceNotFoundException(ErrorCode errorCode, String resourceType, String identifier) {
        super(errorCode,
            String.format("%s con identificador '%s' no fue encontrado", resourceType, identifier));
    }

    protected ResourceNotFoundException(ErrorCode errorCode, String resourceType, String identifier, Throwable cause) {
        super(errorCode,
            String.format("%s con identificador '%s' no fue encontrado", resourceType, identifier), cause);
    }
}