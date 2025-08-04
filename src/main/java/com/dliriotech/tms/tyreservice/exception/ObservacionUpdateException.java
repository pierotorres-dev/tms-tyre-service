package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay problemas al actualizar una observación existente.
 */
public class ObservacionUpdateException extends BaseException {
    
    public ObservacionUpdateException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "TYR-OBS-UPDATE-001");
    }
    
    public ObservacionUpdateException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "TYR-OBS-UPDATE-002", cause);
    }
    
    public static ObservacionUpdateException notFound(Integer observacionId) {
        return new ObservacionUpdateException(
            String.format("No se puede actualizar: observación con ID %d no existe", observacionId));
    }
    
    public static ObservacionUpdateException invalidRequest(String field, Object value) {
        return new ObservacionUpdateException(
            String.format("Campo inválido '%s' con valor: %s", field, value));
    }
    
    public static ObservacionUpdateException estadoNotFound(Integer estadoObservacionId) {
        return new ObservacionUpdateException(
            String.format("No se puede actualizar: estado de observación con ID %d no existe", estadoObservacionId));
    }
    
    public static ObservacionUpdateException alreadyResolved(Integer observacionId) {
        return new ObservacionUpdateException(
            String.format("No se puede actualizar: observación con ID %d ya está resuelta", observacionId));
    }
    
    public static ObservacionUpdateException invalidStateTransition(String fromState, String toState) {
        return new ObservacionUpdateException(
            String.format("Transición de estado inválida: de '%s' a '%s'", fromState, toState));
    }
    
    public static ObservacionUpdateException databaseError(String operation, Throwable cause) {
        return new ObservacionUpdateException(
            String.format("Error de base de datos durante: %s", operation), cause);
    }
}
