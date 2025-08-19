package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay errores de validación en los datos de entrada.
 */
public class ValidationException extends TyreServiceException {
    
    public ValidationException(String field, String message) {
        super(
            String.format("Error de validación en el campo '%s': %s", field, message),
            HttpStatus.BAD_REQUEST,
            "TYR-VAL-001"
        );
    }

    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "TYR-VAL-002");
    }

    public ValidationException(String message, Throwable cause) {
        super(message, HttpStatus.BAD_REQUEST, "TYR-VAL-003", cause);
    }
}
