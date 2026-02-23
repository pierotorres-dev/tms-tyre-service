package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay problemas al actualizar una observación existente.
 */
public class ObservacionUpdateException extends TyreServiceException {

    public ObservacionUpdateException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ObservacionUpdateException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static ObservacionUpdateException notFound(Integer observacionId) {
        return new ObservacionUpdateException(ErrorCode.OBSERVACION_NOT_FOUND,
            String.format("No se puede actualizar: observación con ID %d no existe", observacionId));
    }

    public static ObservacionUpdateException invalidRequest(String field, Object value) {
        return new ObservacionUpdateException(ErrorCode.OBSERVACION_UPDATE_INVALID,
            String.format("Campo inválido '%s' con valor: %s", field, value));
    }

    public static ObservacionUpdateException estadoNotFound(Integer estadoObservacionId) {
        return new ObservacionUpdateException(ErrorCode.OBSERVACION_ESTADO_NOT_FOUND,
            String.format("No se puede actualizar: estado de observación con ID %d no existe", estadoObservacionId));
    }

    public static ObservacionUpdateException alreadyResolved(Integer observacionId) {
        return new ObservacionUpdateException(ErrorCode.OBSERVACION_FINAL_STATE,
            String.format("No se puede actualizar: la observación %d ya está en estado final (Resuelto/Cancelado)", observacionId));
    }

    public static ObservacionUpdateException invalidStateTransition(String fromState, String toState) {
        return new ObservacionUpdateException(ErrorCode.OBSERVACION_STATE_TRANSITION,
            String.format("Transición de estado no permitida: de '%s' a '%s'. Solo se permite cambiar desde 'Pendiente' a 'Resuelto' o 'Cancelado'", fromState, toState));
    }

    public static ObservacionUpdateException databaseError(String operation, Throwable cause) {
        return new ObservacionUpdateException(ErrorCode.OBSERVACION_DB_ERROR,
            String.format("Error de base de datos durante: %s", operation), cause);
    }
}