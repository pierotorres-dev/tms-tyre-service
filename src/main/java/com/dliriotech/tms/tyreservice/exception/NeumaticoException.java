package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción específica para operaciones relacionadas con neumáticos.
 */
public class NeumaticoException extends TyreServiceException {
    
    public NeumaticoException(String code, String message) {
        super(message, code);
    }

    public NeumaticoException(String code, String message, HttpStatus status) {
        super(message, status, code);
    }

    public NeumaticoException(String code, String message, Throwable cause) {
        super(message, code, cause);
    }

    public NeumaticoException(String code, String message, HttpStatus status, Throwable cause) {
        super(message, status, code, cause);
    }
}
