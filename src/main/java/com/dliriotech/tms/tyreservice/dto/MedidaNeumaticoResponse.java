package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedidaNeumaticoResponse {
    private Integer id;

    private String descripcion;

    private String tipoConstruccion;

    private String indiceCarga;

    private String simboloVelocidad;

    private Integer plyRating;
}