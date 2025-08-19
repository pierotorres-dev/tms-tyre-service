package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay problemas con los datos maestros de observaciones (cache, etc.).
 */
public class ObservacionMasterDataException extends BaseException {
    
    public ObservacionMasterDataException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "TYR-OBS-MD-001");
    }
    
    public ObservacionMasterDataException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "TYR-OBS-MD-001", cause);
    }
    
    public static ObservacionMasterDataException estadoNotFound(String estadoNombre) {
        return new ObservacionMasterDataException(
            String.format("No se pudo encontrar el estado de observación: %s", estadoNombre));
    }
    
    public static ObservacionMasterDataException tipoNotFound(Integer tipoId) {
        return new ObservacionMasterDataException(
            String.format("No se pudo encontrar el tipo de observación con ID: %d", tipoId));
    }
    
    public static ObservacionMasterDataException cacheError(String operation, Throwable cause) {
        return new ObservacionMasterDataException(
            String.format("Error en operación de caché: %s", operation), cause);
    }
}
