package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando una medida de neumático no es encontrada.
 */
public class MedidaNeumaticoNotFoundException extends ResourceNotFoundException {

    public MedidaNeumaticoNotFoundException(String identifier) {
        super(ErrorCode.MEDIDA_NEUMATICO_NOT_FOUND, "Medida de neumático", identifier);
    }

    public MedidaNeumaticoNotFoundException(String identifier, Throwable cause) {
        super(ErrorCode.MEDIDA_NEUMATICO_NOT_FOUND, "Medida de neumático", identifier, cause);
    }
}