package com.dliriotech.tms.tyreservice.exception;

import lombok.Getter;

@Getter
public class NeumaticoException extends RuntimeException {
    private final String errorCode;
    private final String message;

    public NeumaticoException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.message = message;
    }

    public NeumaticoException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.message = message;
    }
}
