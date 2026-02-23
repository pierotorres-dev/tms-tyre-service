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

    /**
     * Constructor que acepta un {@link ErrorCode} como fuente única de verdad.
     * Usa el mensaje por defecto del ErrorCode.
     */
    protected BaseException(ErrorCode errorCode) {
        super(errorCode.getDefaultMessage());
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
    }

    /**
     * Constructor que acepta un {@link ErrorCode} y sobreescribe el mensaje por defecto.
     */
    protected BaseException(ErrorCode errorCode, String message) {
        super(message);
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
    }

    /**
     * Constructor que acepta un {@link ErrorCode}, mensaje personalizado y causa.
     */
    protected BaseException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.status = errorCode.getStatus();
        this.code = errorCode.getCode();
    }
}