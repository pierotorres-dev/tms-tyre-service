package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionNeumaticoNuevoRequest {
    private Integer idNeumatico;

    private Integer idEquipo;

    private Integer posicion;

    private Integer idTipoObservacion;

    private String descripcion;

    private Integer idUsuarioCreacion;
}