package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NeumaticoRequest {
    
    @NotNull(message = "El ID de empresa es obligatorio")
    @Positive(message = "El ID de empresa debe ser positivo")
    private Integer empresaId;

    @NotNull(message = "El ID del catálogo de neumático es obligatorio")
    @Positive(message = "El ID del catálogo debe ser positivo")
    private Integer catalogoNeumaticoId;

    @NotNull(message = "El ID del equipo es obligatorio")
    @Positive(message = "El ID del equipo debe ser positivo")
    private Integer equipoId;

    @NotNull(message = "La posición es obligatoria")
    @Min(value = 1, message = "La posición debe ser mayor a 0")
    @Max(value = 50, message = "La posición no puede ser mayor a 50")
    private Integer posicion;

    @NotBlank(message = "El código de serie es obligatorio")
    @Size(max = 50, message = "El código de serie no puede exceder 50 caracteres")
    private String serieCodigo;

    @NotNull(message = "El costo inicial es obligatorio")
    @DecimalMin(value = "0.0", inclusive = false, message = "El costo inicial debe ser mayor a 0")
    @Digits(integer = 10, fraction = 2, message = "El costo inicial debe tener máximo 10 enteros y 2 decimales")
    private BigDecimal costoInicial;

    private Integer proveedorCompraId;

    @NotNull(message = "Los kilómetros de instalación son obligatorios")
    @PositiveOrZero(message = "Los kilómetros de instalación deben ser positivos o cero")
    private Integer kmInstalacion;

    @NotNull(message = "La fecha de instalación es obligatoria")
    @PastOrPresent(message = "La fecha de instalación no puede ser futura")
    private LocalDate fechaInstalacion;

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

    @NotNull(message = "RTD actual es obligatorio")
    @DecimalMin(value = "0.0", message = "RTD actual debe ser positivo")
    @Digits(integer = 3, fraction = 2, message = "RTD actual debe tener máximo 3 enteros y 2 decimales")
    private BigDecimal rtdActual;

    @NotNull(message = "Los kilómetros acumulados son obligatorios")
    @PositiveOrZero(message = "Los kilómetros acumulados deben ser positivos o cero")
    private Integer kmAcumulados;

    @NotNull(message = "El número de reencauches es obligatorio")
    @Min(value = 0, message = "El número de reencauches debe ser mayor o igual a 0")
    @Max(value = 10, message = "El número de reencauches no puede ser mayor a 10")
    private Integer numeroReencauches;

    private Integer disenoReencaucheActualId;

    private Integer clasificacionId;
}