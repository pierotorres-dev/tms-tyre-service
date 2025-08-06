package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoObservacionResponse {
    private Integer id;

    private String nombre;

    private String ambito;

    private String descripcion;

    private Boolean activo;
}