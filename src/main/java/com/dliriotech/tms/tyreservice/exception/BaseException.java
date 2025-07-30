package com.dliriotech.tms.tyreservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Excepción base para todas las excepciones del servicio de neumáticos.
 * Proporciona una estructura común para el manejo de errores.
 */
@Getter
public abstract class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    protected BaseException(String message, HttpStatus status, String code) {
        super(message);
        this.status = status;
        this.code = code;
    }

    protected BaseException(String message, HttpStatus status, String code, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }
}
