package com.dliriotech.tms.tyreservice.exception;

public class EquipoNotFoundException extends ResourceNotFoundException {

    public EquipoNotFoundException(String equipoId) {
        super(ErrorCode.EQUIPO_NOT_FOUND, "Equipo", equipoId);
    }

    public EquipoNotFoundException(String equipoId, Throwable cause) {
        super(ErrorCode.EQUIPO_NOT_FOUND, "Equipo", equipoId, cause);
    }
}