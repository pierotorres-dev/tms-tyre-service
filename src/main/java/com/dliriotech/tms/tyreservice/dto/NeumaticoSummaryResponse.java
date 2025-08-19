package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que contiene información resumida de un neumático.
 * Utilizado en respuestas donde solo se necesitan datos básicos del neumático.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NeumaticoSummaryResponse {
    
    /**
     * Identificador único del neumático
     */
    private Integer id;
    
    /**
     * Código de serie del neumático
     */
    private String serieCodigo;
}
