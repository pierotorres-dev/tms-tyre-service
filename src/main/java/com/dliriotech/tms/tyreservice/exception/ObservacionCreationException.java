package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay problemas al crear una nueva observación.
 */
public class ObservacionCreationException extends TyreServiceException {

    public ObservacionCreationException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ObservacionCreationException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static ObservacionCreationException invalidRequest(String field, Object value) {
        return new ObservacionCreationException(ErrorCode.OBSERVACION_CREATION_INVALID,
            String.format("Campo inválido '%s' con valor: %s", field, value));
    }

    public static ObservacionCreationException neumaticoNotFound(Integer neumaticoId) {
        return new ObservacionCreationException(ErrorCode.NEUMATICO_NOT_FOUND,
            String.format("No se puede crear observación: neumático con ID %d no existe", neumaticoId));
    }

    public static ObservacionCreationException tipoObservacionNotFound(Integer tipoObservacionId) {
        return new ObservacionCreationException(ErrorCode.OBSERVACION_TIPO_NOT_FOUND,
            String.format("No se puede crear observación: tipo de observación con ID %d no existe", tipoObservacionId));
    }

    public static ObservacionCreationException databaseError(String operation, Throwable cause) {
        return new ObservacionCreationException(ErrorCode.OBSERVACION_DB_ERROR,
            String.format("Error de base de datos durante: %s", operation), cause);
    }
}