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
    private Integer neumaticoId;

    private Integer equipoId;

    private Integer posicion;

    private Integer tipoObservacionId;

    private String descripcion;

    private Integer usuarioCreacionId;
}