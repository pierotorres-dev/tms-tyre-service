package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.RtdThresholdsResponse;
import com.dliriotech.tms.tyreservice.entity.Neumatico;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Servicio responsable de calcular los umbrales RTD (mínimo y máximo) para neumáticos.
 * Implementa la lógica de negocio para determinar los valores de RTD según las
 * configuraciones específicas de empresa y tipo de equipo.
 */
public interface RtdThresholdService {

    RtdThresholdsResponse calculateRtdThresholds(Neumatico neumatico, BigDecimal rtdOriginal);

    /**
     * Calcula los umbrales RTD para un lote de neumáticos de forma optimizada.
     * Pre-carga datos compartidos (equipo, configuración empresa-equipo) una sola vez,
     * y ejecuta un solo query batch para los movimientos de reencauche.
     *
     * @param neumaticos lista de neumáticos (típicamente del mismo equipo)
     * @param rtdOriginalByNeumaticoId mapa de neumáticoId -> rtdOriginal del catálogo
     * @return mapa de neumáticoId -> RtdThresholdsResponse
     */
    Map<Integer, RtdThresholdsResponse> calculateRtdThresholdsBatch(
            List<Neumatico> neumaticos,
            Map<Integer, BigDecimal> rtdOriginalByNeumaticoId);
}