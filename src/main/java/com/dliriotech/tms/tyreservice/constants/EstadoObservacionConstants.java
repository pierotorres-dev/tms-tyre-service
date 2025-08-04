package com.dliriotech.tms.tyreservice.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class EstadoObservacionConstants {
    public static final String PENDIENTE = "Pendiente";
    public static final String RESUELTO = "Resuelta";
    public static final String CANCELADO = "Cancelada";
    
    // Mensajes descriptivos para transiciones
    public static final String MSG_ESTADO_FINAL = "Las observaciones en estado final no pueden ser modificadas";
    public static final String MSG_TRANSICION_INVALIDA = "Transición de estado no permitida";
}