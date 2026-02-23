package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando hay problemas específicos en el procesamiento de observaciones.
 */
public class ObservacionProcessingException extends TyreServiceException {

    public ObservacionProcessingException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ObservacionProcessingException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public static ObservacionProcessingException invalidNeumaticoId(Integer neumaticoId) {
        return new ObservacionProcessingException(ErrorCode.OBSERVACION_INVALID_NEUMATICO,
            String.format("ID de neumático inválido: %d", neumaticoId));
    }

    public static ObservacionProcessingException invalidEquipoId(Integer equipoId) {
        return new ObservacionProcessingException(ErrorCode.OBSERVACION_INVALID_EQUIPO,
            String.format("ID de equipo inválido: %d", equipoId));
    }

    public static ObservacionProcessingException invalidTipoMovimiento(Integer tipoMovimientoId) {
        return new ObservacionProcessingException(ErrorCode.OBSERVACION_INVALID_TIPO_MOVIMIENTO,
            String.format("Tipo de movimiento inválido: %d", tipoMovimientoId));
    }

    public static ObservacionProcessingException enrichmentError(String observacionId, Throwable cause) {
        return new ObservacionProcessingException(ErrorCode.OBSERVACION_ENRICH_ERROR,
            String.format("Error al enriquecer observación %s con datos relacionados", observacionId), cause);
    }
}