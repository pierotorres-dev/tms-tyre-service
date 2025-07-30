package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un diseño de reencauche no es encontrado.
 */
public class DisenoReencaucheNotFoundException extends ResourceNotFoundException {
    
    public DisenoReencaucheNotFoundException(String identifier) {
        super("Diseño de reencauche", identifier);
    }

    public DisenoReencaucheNotFoundException(String identifier, Throwable cause) {
        super("Diseño de reencauche", identifier, cause);
    }
}
