package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta realizar una operación no permitida 
 * en el estado actual del neumático.
 */
public class InvalidNeumaticoStateException extends TyreServiceException {
    
    public InvalidNeumaticoStateException(String neumaticoId, String currentState, String operation) {
        super(
            String.format("No se puede realizar la operación '%s' en el neumático '%s' con estado '%s'", 
                operation, neumaticoId, currentState),
            HttpStatus.BAD_REQUEST,
            "TYR-NEU-STATE-001"
        );
    }

    public InvalidNeumaticoStateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "TYR-NEU-STATE-002");
    }
}
