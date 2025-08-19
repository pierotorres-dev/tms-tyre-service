package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un catálogo de neumático no es encontrado.
 */
public class CatalogoNeumaticoNotFoundException extends ResourceNotFoundException {
    
    public CatalogoNeumaticoNotFoundException(String identifier) {
        super("Catálogo de neumático", identifier);
    }

    public CatalogoNeumaticoNotFoundException(String identifier, Throwable cause) {
        super("Catálogo de neumático", identifier, cause);
    }
}
