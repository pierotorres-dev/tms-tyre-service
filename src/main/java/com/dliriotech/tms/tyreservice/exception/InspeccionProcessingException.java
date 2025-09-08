package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando ocurre un error durante el procesamiento de una inspección de neumáticos.
 * Esta excepción puede encapsular múltiples errores ocurridos durante las operaciones transaccionales.
 */
public class InspeccionProcessingException extends TyreServiceException {
    
    private final String equipoId;
    private final String operacionFallida;

    public InspeccionProcessingException(String equipoId, String operacionFallida, String message) {
        super(
            String.format("Error procesando inspección para equipo %s en operación '%s': %s", 
                equipoId, operacionFallida, message),
            HttpStatus.UNPROCESSABLE_ENTITY,
            "TYR-INSP-PROC-001"
        );
        this.equipoId = equipoId;
        this.operacionFallida = operacionFallida;
    }

    public InspeccionProcessingException(String equipoId, String operacionFallida, String message, Throwable cause) {
        super(
            String.format("Error procesando inspección para equipo %s en operación '%s': %s", 
                equipoId, operacionFallida, message),
            HttpStatus.UNPROCESSABLE_ENTITY,
            "TYR-INSP-PROC-001",
            cause
        );
        this.equipoId = equipoId;
        this.operacionFallida = operacionFallida;
    }

    public String getEquipoId() {
        return equipoId;
    }

    public String getOperacionFallida() {
        return operacionFallida;
    }
}
