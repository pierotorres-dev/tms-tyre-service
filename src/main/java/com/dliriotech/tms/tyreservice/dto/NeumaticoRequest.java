package com.dliriotech.tms.tyreservice.dto;

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
public class NeumaticoRequest {
    private Integer empresaId;

    private Integer catalogoNeumaticoId;

    private Integer equipoId;

    private Integer posicion;

    private String serieCodigo;

    private BigDecimal costoInicial;

    private Integer proveedorCompraId;

    private Integer kmInstalacion;

    private LocalDate fechaInstalacion;

    private BigDecimal rtd1;
    private BigDecimal rtd2;
    private BigDecimal rtd3;

    private BigDecimal rtdActual;

    private Integer kmAcumulados;

    private Integer numeroReencauches;

    private Integer disenoReencaucheActualId;

    private Integer clasificacionId;
}