package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando un neumático ya está asignado a un equipo/posición.
 */
public class NeumaticoAlreadyAssignedException extends TyreServiceException {
    
    public NeumaticoAlreadyAssignedException(String neumaticoId, String equipoId, String posicion) {
        super(
            String.format("El neumático '%s' ya está asignado al equipo '%s' en la posición '%s'", 
                neumaticoId, equipoId, posicion),
            HttpStatus.CONFLICT,
            "TYR-NEU-ASSIGN-001"
        );
    }

    public NeumaticoAlreadyAssignedException(String neumaticoId) {
        super(
            String.format("El neumático '%s' ya está asignado a otro equipo", neumaticoId),
            HttpStatus.CONFLICT,
            "TYR-NEU-ASSIGN-002"
        );
    }
}
