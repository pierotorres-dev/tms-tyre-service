package com.dliriotech.tms.tyreservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisenoReencaucheResponse {
    private Integer id;

    private String nombreDiseno;

    private String proveedorReencauche;
}