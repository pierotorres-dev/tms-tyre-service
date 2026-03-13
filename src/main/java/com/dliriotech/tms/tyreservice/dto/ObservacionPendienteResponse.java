package com.dliriotech.tms.tyreservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO ligero para representar observaciones pendientes asociadas a un neumático.
 * Diseñado para incluirse dentro de NeumaticoResponse sin duplicar toda la estructura
 * de ObservacionNeumaticoResponse.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionPendienteResponse {

    private Integer id;

    private Integer neumaticoId;

    private Integer posicion;

    private Integer tipoObservacionId;

    private String descripcion;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime fechaCreacion;
}

