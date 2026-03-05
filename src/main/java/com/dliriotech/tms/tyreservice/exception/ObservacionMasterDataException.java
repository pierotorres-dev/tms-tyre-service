package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay problemas con los datos maestros de observaciones (caché, etc.).
 */
public class ObservacionMasterDataException extends TyreServiceException {

    public ObservacionMasterDataException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ObservacionMasterDataException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static ObservacionMasterDataException estadoNotFound(String estadoNombre) {
        return new ObservacionMasterDataException(ErrorCode.OBSERVACION_ESTADO_NOT_FOUND,
            String.format("No se pudo encontrar el estado de observación: %s", estadoNombre));
    }

    public static ObservacionMasterDataException tipoNotFound(Integer tipoId) {
        return new ObservacionMasterDataException(ErrorCode.OBSERVACION_TIPO_NOT_FOUND,
            String.format("No se pudo encontrar el tipo de observación con ID: %d", tipoId));
    }
}