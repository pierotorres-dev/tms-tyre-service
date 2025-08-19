package com.dliriotech.tms.tyreservice.exception;

/**
 * Excepción lanzada cuando un proveedor no es encontrado.
 */
public class ProveedorNotFoundException extends ResourceNotFoundException {
    
    public ProveedorNotFoundException(String identifier) {
        super("Proveedor", identifier);
    }

    public ProveedorNotFoundException(String identifier, Throwable cause) {
        super("Proveedor", identifier, cause);
    }
}
