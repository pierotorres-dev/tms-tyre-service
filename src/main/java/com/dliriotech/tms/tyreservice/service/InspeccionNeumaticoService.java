package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.FinalizarInspeccionRequest;

/**
 * Servicio para la gestión de inspecciones de neumáticos.
 */
public interface InspeccionNeumaticoService {

    /**
     * Finaliza una inspección de neumáticos, actualizando las entidades correspondientes:
     * - Equipo: fecha de inspección
     * - Neumatico: mediciones RTD
     * - MovimientoNeumatico: registro del movimiento de inspección
     * - ObservacionNeumatico: observaciones detectadas durante la inspección
     *
     * @param equipoId  ID del equipo inspeccionado
     * @param empresaId ID de la empresa (tenant isolation)
     * @param request   datos de la inspección
     */
    void finalizarInspeccion(Integer equipoId, Integer empresaId, FinalizarInspeccionRequest request);
}
