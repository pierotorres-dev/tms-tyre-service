package com.dliriotech.tms.tyreservice.exception;

import org.springframework.http.HttpStatus;

/**
 * Excepción lanzada cuando se intenta asignar un neumático a una posición 
 * que ya está ocupada por otro neumático en el mismo equipo.
 */
public class PosicionAlreadyOccupiedException extends TyreServiceException {
    
    public PosicionAlreadyOccupiedException(Integer equipoId, Integer posicion) {
        super(
            String.format("La posición '%d' del equipo '%d' ya está ocupada por otro neumático", 
                posicion, equipoId),
            HttpStatus.CONFLICT,
            "TYR-NEU-POS-001"
        );
    }

    public PosicionAlreadyOccupiedException(Integer equipoId, Integer posicion, String neumaticoAsignado) {
        super(
            String.format("La posición '%d' del equipo '%d' ya está ocupada por el neumático '%s'", 
                posicion, equipoId, neumaticoAsignado),
            HttpStatus.CONFLICT,
            "TYR-NEU-POS-002"
        );
    }

    public PosicionAlreadyOccupiedException(String message) {
        super(message, HttpStatus.CONFLICT, "TYR-NEU-POS-003");
    }
}
