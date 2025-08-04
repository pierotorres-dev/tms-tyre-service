package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionNeumaticoUpdateRequest {
    private Integer idEstadoObservacion;

    private Integer idUsuarioResolucion;

    private String comentarioResolucion;
}
