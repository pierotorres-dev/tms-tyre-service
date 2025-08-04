package com.dliriotech.tms.tyreservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    private Integer neumaticoId;

    private Integer equipoId;

    private Integer posicion;

    private TipoObservacionResponse tipoObservacionResponse;

    private String descripcion;

    private EstadoObservacionResponse estadoObservacionResponse;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;

    private Integer usuarioCreacionId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaResolucion;

    private Integer usuarioResolucionId;

    private String comentarioResolucion;
}