package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando una marca de neumático no es encontrada.
 */
public class MarcaNeumaticoNotFoundException extends ResourceNotFoundException {

    public MarcaNeumaticoNotFoundException(String identifier) {
        super(ErrorCode.MARCA_NEUMATICO_NOT_FOUND, "Marca de neumático", identifier);
    }

    public MarcaNeumaticoNotFoundException(String identifier, Throwable cause) {
        super(ErrorCode.MARCA_NEUMATICO_NOT_FOUND, "Marca de neumático", identifier, cause);
    }
}