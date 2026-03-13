package com.dliriotech.tms.tyreservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resolver observaciones de neumático existentes dentro de un lote de inspección.
 * Representa una observación previamente pendiente que fue resuelta durante la inspección.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacionNeumaticoResolucionLoteRequest {

    @NotNull(message = "observacionId es obligatorio")
    @Positive(message = "observacionId debe ser un número positivo")
    private Integer observacionId;

    /** Comentario opcional de resolución. */
    private String comentarioResolucion;
}

