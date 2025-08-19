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
    
    Mono<RtdThresholdsResponse> calculateRtdThresholds(Neumatico neumatico, java.math.BigDecimal rtdOriginal);
}