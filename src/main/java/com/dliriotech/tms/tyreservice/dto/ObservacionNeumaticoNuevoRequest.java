package com.dliriotech.tms.tyreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    /**
     * Ignorado en la deserialización del body.
     * Es inyectado exclusivamente desde el header X-User-Id por el API Gateway.
     */
    @JsonIgnore
    private Integer usuarioCreacionId;
}