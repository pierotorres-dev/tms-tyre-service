package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando no se encuentran observaciones específicas.
 */
public class ObservacionNotFoundException extends TyreServiceException {

    public ObservacionNotFoundException(String message) {
        super(ErrorCode.OBSERVACION_NOT_FOUND, message);
    }

    public ObservacionNotFoundException(Integer neumaticoId) {
        super(ErrorCode.OBSERVACION_NOT_FOUND,
            String.format("No se encontraron observaciones para el neumático con ID: %d", neumaticoId));
    }

    public ObservacionNotFoundException(Integer neumaticoId, Integer tipoMovimientoId) {
        super(ErrorCode.OBSERVACION_NOT_FOUND,
            String.format("No se encontraron observaciones solucionables para el neumático %d y tipo de movimiento %d",
                neumaticoId, tipoMovimientoId));
    }

    public ObservacionNotFoundException(Integer neumaticoId, String estadoObservacion) {
        super(ErrorCode.OBSERVACION_NOT_FOUND,
            String.format("No se encontraron observaciones en estado '%s' para el neumático con ID: %d",
                estadoObservacion, neumaticoId));
    }
}