package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response del procesamiento de un lote de inspección.
 * Devuelve los IDs de las observaciones creadas y resueltas
 * para que el fleet-service (orquestador) pueda amarrar
 * evidencias fotográficas u otras operaciones.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InspeccionLoteResponse {

    /** Observaciones nuevas registradas durante la inspección. */
    private List<ObservacionCreadaResponse> observacionesCreadas;

    /** IDs de las observaciones que fueron resueltas durante la inspección. */
    private List<Integer> observacionesResueltasIds;
}

