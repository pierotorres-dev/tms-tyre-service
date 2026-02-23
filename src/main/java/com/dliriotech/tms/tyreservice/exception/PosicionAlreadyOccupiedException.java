package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando se intenta asignar un neumático a una posición
 * que ya está ocupada por otro neumático en el mismo equipo.
 */
public class PosicionAlreadyOccupiedException extends TyreServiceException {

    public PosicionAlreadyOccupiedException(Integer equipoId, Integer posicion) {
        super(ErrorCode.NEUMATICO_POSICION_OCCUPIED,
            String.format("La posición '%d' del equipo '%d' ya está ocupada por otro neumático",
                posicion, equipoId));
    }

    public PosicionAlreadyOccupiedException(Integer equipoId, Integer posicion, String neumaticoAsignado) {
        super(ErrorCode.NEUMATICO_POSICION_OCCUPIED,
            String.format("La posición '%d' del equipo '%d' ya está ocupada por el neumático '%s'",
                posicion, equipoId, neumaticoAsignado));
    }

    public PosicionAlreadyOccupiedException(String message) {
        super(ErrorCode.NEUMATICO_POSICION_OCCUPIED, message);
    }
}