package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.InspeccionLoteResponse;
import com.dliriotech.tms.tyreservice.dto.LoteNeumaticosInspeccionRequest;

/**
 * Servicio para procesar lotes de neumáticos inspeccionados delegados
 * por el fleet-service (orquestador).
 */
public interface MovimientoNeumaticoService {

    /**
     * Procesa un lote de neumáticos inspeccionados de forma atómica:
     * <ol>
     *   <li>Valida datos de entrada y existencia de entidades</li>
     *   <li>Actualiza mediciones RTD de cada neumático</li>
     *   <li>Crea movimientos de inspección periódica</li>
     *   <li>Crea observaciones de neumático detectadas</li>
     *   <li>Resuelve observaciones pendientes indicadas</li>
     * </ol>
     *
     * @param request   datos del lote (neumáticos + observaciones nuevas + resoluciones)
     * @param userId    ID del usuario que realiza la inspección
     * @param empresaId ID de la empresa (tenant isolation)
     * @return response con las observaciones creadas y resueltas
     */
    InspeccionLoteResponse procesarInspeccionLote(LoteNeumaticosInspeccionRequest request, Integer userId, Integer empresaId);
}

