package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un neumático ya está asignado a un equipo/posición.
 */
public class NeumaticoAlreadyAssignedException extends TyreServiceException {

    public NeumaticoAlreadyAssignedException(String neumaticoId, String equipoId, String posicion) {
        super(ErrorCode.NEUMATICO_ALREADY_ASSIGNED,
            String.format("El neumático '%s' ya está asignado al equipo '%s' en la posición '%s'",
                neumaticoId, equipoId, posicion));
    }

    public NeumaticoAlreadyAssignedException(String neumaticoId) {
        super(ErrorCode.NEUMATICO_ALREADY_ASSIGNED,
            String.format("El neumático '%s' ya está asignado a otro equipo", neumaticoId));
    }
}