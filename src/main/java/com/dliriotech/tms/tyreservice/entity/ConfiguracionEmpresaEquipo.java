package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "configuracion_empresa_equipo")
public class ConfiguracionEmpresaEquipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_empresa")
    private Integer empresaId;

    @Column(name = "id_tipo_equipo")
    private Integer tipoEquipoId;

    @Column(name = "rtd_minimo_reencauche")
    private BigDecimal rtdMinimoReencauche;

    @Column(name = "rtd_minimo_scrap")
    private BigDecimal rtdMinimoScrap;
}