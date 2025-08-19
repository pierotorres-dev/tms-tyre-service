package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando no se encuentran observaciones específicas.
 */
public class ObservacionNotFoundException extends BaseException {
    
    public ObservacionNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "TYR-OBS-001");
    }
    
    public ObservacionNotFoundException(Integer neumaticoId) {
        super(String.format("No se encontraron observaciones para el neumático con ID: %d", neumaticoId), 
              HttpStatus.NOT_FOUND, "TYR-OBS-001");
    }
    
    public ObservacionNotFoundException(Integer neumaticoId, Integer tipoMovimientoId) {
        super(String.format("No se encontraron observaciones solucionables para el neumático %d y tipo de movimiento %d", 
              neumaticoId, tipoMovimientoId), 
              HttpStatus.NOT_FOUND, "TYR-OBS-002");
    }
    
    public ObservacionNotFoundException(Integer neumaticoId, String estadoObservacion) {
        super(String.format("No se encontraron observaciones en estado '%s' para el neumático con ID: %d", 
              estadoObservacion, neumaticoId), 
              HttpStatus.NOT_FOUND, "TYR-OBS-003");
    }
}
