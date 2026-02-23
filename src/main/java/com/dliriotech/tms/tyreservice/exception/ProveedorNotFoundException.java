package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un proveedor no es encontrado.
 */
public class ProveedorNotFoundException extends ResourceNotFoundException {

    public ProveedorNotFoundException(String identifier) {
        super(ErrorCode.PROVEEDOR_NOT_FOUND, "Proveedor", identifier);
    }

    public ProveedorNotFoundException(String identifier, Throwable cause) {
        super(ErrorCode.PROVEEDOR_NOT_FOUND, "Proveedor", identifier, cause);
    }
}