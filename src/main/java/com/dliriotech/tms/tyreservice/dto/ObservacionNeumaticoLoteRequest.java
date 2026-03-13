package com.dliriotech.tms.tyreservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para crear observaciones de neumático dentro de un lote de inspección.
 * A diferencia de {@link ObservacionNeumaticoNuevoRequest}, este DTO no porta
 * {@code usuarioCreacionId} ya que éste se inyecta desde el header {@code X-User-Id}
 * en el controller del lote.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionNeumaticoLoteRequest {

    @NotNull(message = "neumaticoId es obligatorio")
    @Positive(message = "neumaticoId debe ser un número positivo")
    private Integer neumaticoId;

    @NotNull(message = "equipoId es obligatorio")
    @Positive(message = "equipoId debe ser un número positivo")
    private Integer equipoId;

    @NotNull(message = "posicion es obligatorio")
    @Positive(message = "posicion debe ser un número positivo")
    private Integer posicion;

    @NotNull(message = "tipoObservacionId es obligatorio")
    @Positive(message = "tipoObservacionId debe ser un número positivo")
    private Integer tipoObservacionId;

    @NotBlank(message = "descripcion es obligatorio")
    private String descripcion;

    /** Identificador temporal asignado por el frontend para correlación con evidencias. */
    private String tempId;
}

