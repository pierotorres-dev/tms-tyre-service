package com.dliriotech.tms.tyreservice.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NeumaticoResponse {
    private Integer id;

    private Integer empresaId;

    private CatalogoNeumaticoResponse catalogoNeumaticoResponse;

    private Integer equipoId;

    private Integer posicion;

    private String serieCodigo;

    private BigDecimal costoInicial;

    private ProveedorResponse proveedorResponse;

    private Integer kmInstalacion;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInstalacion;

    private BigDecimal rtd1;
    private BigDecimal rtd2;
    private BigDecimal rtd3;

    private BigDecimal rtdActual;

    private Integer kmAcumulados;

    private Integer kmCicloActual;

    private Integer numeroReencauches;

    private DisenoReencaucheResponse disenoReencaucheResponse;

    private ClasificacionNeumaticoResponse clasificacionNeumaticoResponse;

    private RtdThresholdsResponse rtdThresholds;
}