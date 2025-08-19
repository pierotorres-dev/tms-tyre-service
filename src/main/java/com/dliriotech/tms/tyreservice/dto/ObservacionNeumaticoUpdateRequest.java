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
    private Integer estadoObservacionId;

    private Integer usuarioResolucionId;

    private String comentarioResolucion;
}