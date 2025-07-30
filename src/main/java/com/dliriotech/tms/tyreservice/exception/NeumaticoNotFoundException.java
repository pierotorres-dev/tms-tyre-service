package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un neumático no es encontrado.
 */
public class NeumaticoNotFoundException extends ResourceNotFoundException {
    
    public NeumaticoNotFoundException(String identifier) {
        super("Neumático", identifier);
    }

    public NeumaticoNotFoundException(String identifier, Throwable cause) {
        super("Neumático", identifier, cause);
    }
}
