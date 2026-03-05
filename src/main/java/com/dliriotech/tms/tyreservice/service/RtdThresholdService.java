package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.RtdThresholdsResponse;
import com.dliriotech.tms.tyreservice.entity.Neumatico;

import java.math.BigDecimal;

/**
 * Servicio responsable de calcular los umbrales RTD (mínimo y máximo) para neumáticos.
 * Implementa la lógica de negocio para determinar los valores de RTD según las
 * configuraciones específicas de empresa y tipo de equipo.
 */
public interface RtdThresholdService {

    RtdThresholdsResponse calculateRtdThresholds(Neumatico neumatico, BigDecimal rtdOriginal);
}