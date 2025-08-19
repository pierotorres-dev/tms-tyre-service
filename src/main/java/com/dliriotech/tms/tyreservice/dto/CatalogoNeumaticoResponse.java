package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoNeumaticoResponse {
    private Integer id;

    private MarcaNeumaticoResponse marcaNeumaticoResponse;

    private MedidaNeumaticoResponse medidaNeumaticoResponse;

    private String modeloDiseno;

    private String tipoUso;

    private BigDecimal rtdOriginal;

    private Integer presionMaximaPsi;

    private Integer treadwear;

    private String traccion;

    private String temperatura;
}