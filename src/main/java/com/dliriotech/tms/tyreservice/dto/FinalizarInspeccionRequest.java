package com.dliriotech.tms.tyreservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinalizarInspeccionRequest {

    /**
     * Ignorado en la deserialización del body.
     * Es inyectado exclusivamente desde el path variable /{equipoId}.
     */
    @JsonIgnore
    private Integer equipoId;

    /**
     * Ignorado en la deserialización del body.
     * Es inyectado exclusivamente desde el header X-Empresa-Id por el API Gateway.
     */
    @JsonIgnore
    private Integer empresaId;

    /**
     * Ignorado en la deserialización del body.
     * Es inyectado exclusivamente desde el header X-User-Id por el API Gateway.
     */
    @JsonIgnore
    private Integer usuarioId;
    private Integer tipoEquipoId;
    private Integer kilometraje;
    private List<NeumaticoInspeccionadoRequest> neumaticosInspeccionados;
}