package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO liviano que representa una observación de neumático recién creada.
 * Diseñado para comunicación inter-servicio: solo transporta los IDs
 * y datos clave que el fleet-service necesita para amarrar evidencias
 * u otras operaciones post-inspección.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionCreadaResponse {

    /** ID auto-generado de la observación en BD. */
    private Integer id;

    /** ID del neumático al que pertenece la observación. */
    private Integer neumaticoId;

    /** ID del equipo al que pertenece el neumático. */
    private Integer equipoId;

    /** Posición del neumático en el equipo. */
    private Integer posicion;

    /** ID del tipo de observación registrada. */
    private Integer tipoObservacionId;

    /** Descripción de la observación. */
    private String descripcion;

    /** Identificador temporal del frontend para correlación con evidencias. */
    private String tempId;
}

