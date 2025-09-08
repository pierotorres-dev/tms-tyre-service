package com.dliriotech.tms.tyreservice.dto;

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
    private Integer equipoId;
    private Integer usuarioId;
    private Integer empresaId;
    private Integer tipoEquipoId;
    private Integer kilometraje;
    private List<NeumaticoInspeccionadoRequest> neumaticosInspeccionados;
}