package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción específica para operaciones relacionadas con neumáticos.
 */
public class NeumaticoException extends TyreServiceException {

    public NeumaticoException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NeumaticoException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public NeumaticoException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}