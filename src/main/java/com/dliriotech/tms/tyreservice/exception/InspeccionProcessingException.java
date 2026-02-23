package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando ocurre un error durante el procesamiento de una inspección.
 */
public class InspeccionProcessingException extends TyreServiceException {

    private final String equipoId;
    private final String operacionFallida;

    public InspeccionProcessingException(String equipoId, String operacionFallida, String message) {
        super(ErrorCode.INSPECCION_PROCESSING_ERROR,
            String.format("Error procesando inspección para equipo %s en operación '%s': %s",
                equipoId, operacionFallida, message));
        this.equipoId = equipoId;
        this.operacionFallida = operacionFallida;
    }

    public InspeccionProcessingException(String equipoId, String operacionFallida, String message, Throwable cause) {
        super(ErrorCode.INSPECCION_PROCESSING_ERROR,
            String.format("Error procesando inspección para equipo %s en operación '%s': %s",
                equipoId, operacionFallida, message),
            cause);
        this.equipoId = equipoId;
        this.operacionFallida = operacionFallida;
    }

    public String getEquipoId() { return equipoId; }
    public String getOperacionFallida() { return operacionFallida; }
}