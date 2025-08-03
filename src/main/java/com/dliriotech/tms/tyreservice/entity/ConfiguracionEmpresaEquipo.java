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
@Table("configuracion_empresa_equipo")
public class ConfiguracionEmpresaEquipo {
    
    @Id
    private Integer id;
    
    @Column("id_empresa")
    private Integer idEmpresa;
    
    @Column("id_tipo_equipo")
    private Integer idTipoEquipo;
    
    @Column("rtd_minimo_reencauche")
    private BigDecimal rtdMinimoReencauche;
    
    @Column("rtd_minimo_scrap")
    private BigDecimal rtdMinimoScrap;
}