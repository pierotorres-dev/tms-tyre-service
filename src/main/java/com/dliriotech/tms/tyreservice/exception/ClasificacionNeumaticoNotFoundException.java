package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando una clasificación de neumático no es encontrada.
 */
public class ClasificacionNeumaticoNotFoundException extends ResourceNotFoundException {
    
    public ClasificacionNeumaticoNotFoundException(String identifier) {
        super("Clasificación de neumático", identifier);
    }

    public ClasificacionNeumaticoNotFoundException(String identifier, Throwable cause) {
        super("Clasificación de neumático", identifier, cause);
    }
}
