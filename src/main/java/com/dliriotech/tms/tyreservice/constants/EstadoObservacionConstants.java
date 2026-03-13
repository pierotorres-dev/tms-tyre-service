package com.dliriotech.tms.tyreservice.constants;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public final class EstadoObservacionConstants {
    public static final String PENDIENTE = "Pendiente";
    public static final String RESUELTO = "Resuelta";
    public static final String CANCELADO = "Cancelada";

    // IDs hardcodeados — valores estables de la tabla estados_observaciones
    public static final int ID_PENDIENTE = 1;
    public static final int ID_RESUELTA = 2;
    public static final int ID_CANCELADA = 3;

    // Mensajes descriptivos para transiciones
    public static final String MSG_ESTADO_FINAL = "Las observaciones en estado final no pueden ser modificadas";
    public static final String MSG_TRANSICION_INVALIDA = "Transición de estado no permitida";
}