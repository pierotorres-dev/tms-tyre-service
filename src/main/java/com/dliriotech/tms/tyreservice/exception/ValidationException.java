package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay errores de validación en los datos de entrada.
 */
public class ValidationException extends TyreServiceException {

    public ValidationException(String field, String message) {
        super(ErrorCode.VALIDATION_FIELD_ERROR,
            String.format("Error de validación en el campo '%s': %s", field, message));
    }

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(ErrorCode.VALIDATION_ERROR, message, cause);
    }
}