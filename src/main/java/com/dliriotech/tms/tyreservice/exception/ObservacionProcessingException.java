package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando hay problemas específicos en el procesamiento de observaciones.
 */
public class ObservacionProcessingException extends BaseException {
    
    public ObservacionProcessingException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "TYR-OBS-PROC-001");
    }
    
    public ObservacionProcessingException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "TYR-OBS-PROC-002", cause);
    }
    
    public static ObservacionProcessingException invalidNeumaticoId(Integer neumaticoId) {
        return new ObservacionProcessingException(
            String.format("ID de neumático inválido: %d", neumaticoId));
    }

    public static ObservacionProcessingException invalidEquipoId(Integer equipoId) {
        return new ObservacionProcessingException(
                String.format("ID de neumático equipo: %d", equipoId));
    }
    
    public static ObservacionProcessingException invalidTipoMovimiento(Integer tipoMovimientoId) {
        return new ObservacionProcessingException(
            String.format("Tipo de movimiento inválido: %d", tipoMovimientoId));
    }
    
    public static ObservacionProcessingException enrichmentError(String observacionId, Throwable cause) {
        return new ObservacionProcessingException(
            String.format("Error al enriquecer observación %s con datos relacionados", observacionId), cause);
    }
}
