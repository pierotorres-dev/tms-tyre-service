package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un neumático no es encontrado.
 */
public class NeumaticoNotFoundException extends TyreServiceException {

    public NeumaticoNotFoundException(String identifier) {
        super(ErrorCode.NEUMATICO_NOT_FOUND,
            String.format("Neumático con identificador '%s' no fue encontrado", identifier));
    }

    public NeumaticoNotFoundException(String identifier, Throwable cause) {
        super(ErrorCode.NEUMATICO_NOT_FOUND,
            String.format("Neumático con identificador '%s' no fue encontrado", identifier), cause);
    }
}