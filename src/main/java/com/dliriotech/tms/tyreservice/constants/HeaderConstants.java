package com.dliriotech.tms.tyreservice.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Constantes para los headers HTTP inyectados por el API Gateway.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeaderConstants {
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_EMPRESA_ID = "X-Empresa-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
}