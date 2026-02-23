package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay conflictos de integridad de datos.
 */
public class DataIntegrityException extends TyreServiceException {

    public DataIntegrityException(String message) {
        super(ErrorCode.DATA_INTEGRITY_CONFLICT, message);
    }

    public DataIntegrityException(String message, Throwable cause) {
        super(ErrorCode.DATA_INTEGRITY_CONFLICT, message, cause);
    }

    public DataIntegrityException(String resource, String constraint) {
        super(ErrorCode.DATA_INTEGRITY_CONFLICT,
            String.format("Violación de integridad en %s: %s", resource, constraint));
    }
}