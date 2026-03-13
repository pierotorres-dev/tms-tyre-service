package com.dliriotech.tms.tyreservice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Payload que el fleet-service (orquestador) envía al tyre-service
 * para procesar un lote de neumáticos inspeccionados y sus observaciones
 * de forma atómica.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteNeumaticosInspeccionRequest {

    @NotNull(message = "inspeccionId es obligatorio")
    @Positive(message = "inspeccionId debe ser un número positivo")
    private Integer inspeccionId;

    @NotNull(message = "equipoId es obligatorio")
    @Positive(message = "equipoId debe ser un número positivo")
    private Integer equipoId;

    @NotNull(message = "tipoEquipoId es obligatorio")
    @Positive(message = "tipoEquipoId debe ser un número positivo")
    private Integer tipoEquipoId;

    /** Kilometraje actual del equipo. Si es null se obtiene de BD. */
    private Integer kilometraje;

    @NotEmpty(message = "Debe incluir al menos un neumático inspeccionado")
    @Valid
    private List<NeumaticoInspeccionadoRequest> neumaticosInspeccionados;

    /** Observaciones nuevas de neumáticos detectadas durante la inspección. Puede ser vacía o null. */
    @Valid
    private List<ObservacionNeumaticoLoteRequest> observacionesNuevas;

    /** Observaciones pendientes que fueron resueltas durante la inspección. Puede ser vacía o null. */
    @Valid
    private List<ObservacionNeumaticoResolucionLoteRequest> observacionesResueltas;
}

