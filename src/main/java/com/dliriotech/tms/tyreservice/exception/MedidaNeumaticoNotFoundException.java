package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando una medida de neumático no es encontrada.
 */
public class MedidaNeumaticoNotFoundException extends ResourceNotFoundException {
    
    public MedidaNeumaticoNotFoundException(String identifier) {
        super("Medida de neumático", identifier);
    }

    public MedidaNeumaticoNotFoundException(String identifier, Throwable cause) {
        super("Medida de neumático", identifier, cause);
    }
}
