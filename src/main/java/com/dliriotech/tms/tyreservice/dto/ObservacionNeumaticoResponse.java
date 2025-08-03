package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionNeumaticoResponse {
    private Integer id;

    private Integer idNeumatico;

    private Integer idEquipo;

    private Integer posicion;

    private TipoObservacionResponse tipoObservacionResponse;

    private String descripcion;

    private EstadoObservacionResponse estadoObservacionResponse;

    private LocalDateTime fechaCreacion;

    private Integer idUsuarioCreacion;

    private LocalDateTime fechaResolucion;

    private Integer idUsuarioResolucion;

    private String comentarioResolucion;
}