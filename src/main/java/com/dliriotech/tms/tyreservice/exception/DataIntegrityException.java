package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay conflictos de integridad de datos.
 */
public class DataIntegrityException extends TyreServiceException {
    
    public DataIntegrityException(String message) {
        super(message, HttpStatus.CONFLICT, "TYR-DATA-001");
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(message, HttpStatus.CONFLICT, "TYR-DATA-002", cause);
    }

    public DataIntegrityException(String resource, String constraint) {
        super(
            String.format("Violación de integridad en %s: %s", resource, constraint),
            HttpStatus.CONFLICT,
            "TYR-DATA-003"
        );
    }
}
