package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando se intenta realizar una operación no permitida
 * en el estado actual del neumático.
 */
public class InvalidNeumaticoStateException extends TyreServiceException {

    public InvalidNeumaticoStateException(String neumaticoId, String currentState, String operation) {
        super(ErrorCode.NEUMATICO_INVALID_STATE,
            String.format("No se puede realizar la operación '%s' en el neumático '%s' con estado '%s'",
                operation, neumaticoId, currentState));
    }

    public InvalidNeumaticoStateException(String message) {
        super(ErrorCode.NEUMATICO_INVALID_STATE, message);
    }
}