package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.FinalizarInspeccionRequest;
import reactor.core.publisher.Mono;

/**
 * Servicio para la gestión de inspecciones de neumáticos.
 */
public interface InspeccionNeumaticoService {
    
    /**
     * Finaliza una inspección de neumáticos, actualizando las entidades correspondientes:
     * - Equipo: fecha de inspección
     * - Neumatico: mediciones RTD
     * - MovimientoNeumatico: registro del movimiento de inspección
     * - ObservacionNeumatico: observaciones encontradas
     * 
     * @param equipoId ID del equipo inspeccionado
     * @param request Datos de la inspección
     * @return Mono<Void> que indica la finalización exitosa del proceso
     */
    Mono<Void> finalizarInspeccion(Integer equipoId, FinalizarInspeccionRequest request);
}
