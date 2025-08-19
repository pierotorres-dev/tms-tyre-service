package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay problemas al crear una nueva observación.
 */
public class ObservacionCreationException extends BaseException {
    
    public ObservacionCreationException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "TYR-OBS-CREATE-001");
    }
    
    public ObservacionCreationException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "TYR-OBS-CREATE-002", cause);
    }
    
    public static ObservacionCreationException invalidRequest(String field, Object value) {
        return new ObservacionCreationException(
            String.format("Campo inválido '%s' con valor: %s", field, value));
    }
    
    public static ObservacionCreationException neumaticoNotFound(Integer neumaticoId) {
        return new ObservacionCreationException(
            String.format("No se puede crear observación: neumático con ID %d no existe", neumaticoId));
    }
    
    public static ObservacionCreationException tipoObservacionNotFound(Integer tipoObservacionId) {
        return new ObservacionCreationException(
            String.format("No se puede crear observación: tipo de observación con ID %d no existe", tipoObservacionId));
    }
    
    public static ObservacionCreationException databaseError(String operation, Throwable cause) {
        return new ObservacionCreationException(
            String.format("Error de base de datos durante: %s", operation), cause);
    }
}
