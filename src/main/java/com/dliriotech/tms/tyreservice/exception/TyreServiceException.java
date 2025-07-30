package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción principal del servicio de neumáticos.
 * Todas las excepciones específicas del dominio deben heredar de esta clase.
 */
public class TyreServiceException extends BaseException {
    
    protected TyreServiceException(String message, String code) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, code);
    }

    protected TyreServiceException(String message, HttpStatus status, String code) {
        super(message, status, code);
    }

    protected TyreServiceException(String message, String code, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, code, cause);
    }

    protected TyreServiceException(String message, HttpStatus status, String code, Throwable cause) {
        super(message, status, code, cause);
    }
}
