package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO wrapper que agrupa los neumáticos de un equipo junto con sus observaciones pendientes.
 * Permite al frontend obtener toda la información necesaria en una sola llamada.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquipoNeumaticoResponse {

    private Integer equipoId;

    private List<NeumaticoResponse> neumaticos;

    /**
     * Observaciones en estado "Pendiente" asociadas a los neumáticos del equipo.
     * Permite al usuario ver qué acciones correctivas están por resolver.
     */
    private List<ObservacionPendienteResponse> observacionesPendientes;
}

