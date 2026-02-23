package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un catálogo de neumático no es encontrado.
 */
public class CatalogoNeumaticoNotFoundException extends ResourceNotFoundException {

    public CatalogoNeumaticoNotFoundException(String identifier) {
        super(ErrorCode.CATALOGO_NEUMATICO_NOT_FOUND, "Catálogo de neumático", identifier);
    }

    public CatalogoNeumaticoNotFoundException(String identifier, Throwable cause) {
        super(ErrorCode.CATALOGO_NEUMATICO_NOT_FOUND, "Catálogo de neumático", identifier, cause);
    }
}