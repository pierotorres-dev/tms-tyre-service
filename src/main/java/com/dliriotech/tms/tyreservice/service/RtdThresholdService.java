package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.RtdThresholdsResponse;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import reactor.core.publisher.Mono;

/**
 * Servicio responsable de calcular los umbrales RTD (mínimo y máximo) para neumáticos.
 * Implementa la lógica de negocio para determinar los valores de RTD según las 
 * configuraciones específicas de empresa y tipo de equipo.
 */
public interface RtdThresholdService {
    
    /**
     * Calcula los umbrales RTD para un neumático específico.
     * 
     * @param neumatico El neumático para el cual calcular los umbrales
     * @param rtdOriginal El RTD original del catálogo del neumático
     * @return Mono que emite los umbrales RTD calculados
     */
    Mono<RtdThresholdsResponse> calculateRtdThresholds(Neumatico neumatico, java.math.BigDecimal rtdOriginal);
}
