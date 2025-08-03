package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tipos_equipos")
public class TipoEquipo {
    
    @Id
    private Integer id;
    
    private String nombre;
    
    private String descripcion;
    
    @Column("rtd_minimo_reencauche")
    private BigDecimal rtadMinimoReencauche;
    
    @Column("rtd_minimo_scrap")
    private BigDecimal rtadMinimoScrap;
}