package com.dliriotech.tms.tyreservice.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NeumaticoInspeccionadoRequest {

    @NotNull(message = "neumaticoId es obligatorio")
    @Positive(message = "neumaticoId debe ser un número positivo")
    private Integer neumaticoId;

    @NotNull(message = "RTD1 es obligatorio")
    @DecimalMin(value = "0.0", message = "RTD1 debe ser positivo")
    @Digits(integer = 3, fraction = 2, message = "RTD1 debe tener máximo 3 enteros y 2 decimales")
    private BigDecimal rtd1;

    @NotNull(message = "RTD2 es obligatorio")
    @DecimalMin(value = "0.0", message = "RTD2 debe ser positivo")
    @Digits(integer = 3, fraction = 2, message = "RTD2 debe tener máximo 3 enteros y 2 decimales")
    private BigDecimal rtd2;

    @NotNull(message = "RTD3 es obligatorio")
    @DecimalMin(value = "0.0", message = "RTD3 debe ser positivo")
    @Digits(integer = 3, fraction = 2, message = "RTD3 debe tener máximo 3 enteros y 2 decimales")
    private BigDecimal rtd3;
}