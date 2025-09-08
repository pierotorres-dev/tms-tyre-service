package com.dliriotech.tms.tyreservice.exception;

public class EquipoNotFoundException extends ResourceNotFoundException {
    
    public EquipoNotFoundException(String equipoId) {
        super("Equipo", equipoId);
    }
    
    public EquipoNotFoundException(String equipoId, Throwable cause) {
        super("Equipo", equipoId, cause);
    }
}
