package com.dliriotech.tms.tyreservice.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("neumaticos")
public class Neumatico {
    @Id
    private Integer id;

    @Column("id_empresa")
    private Integer empresaId;

    @Column("id_catalogo_neumatico")
    private Integer catalogoNeumaticoId;

    @Column("id_equipo")
    private Integer equipoId;

    private Integer posicion;

    @Column("serie_codigo")
    private String serieCodigo;

    @Column("costo_inicial")
    private BigDecimal costoInicial;

    @Column("id_proveedor_compra")
    private Integer proveedorCompraId;

    @Column("km_instalacion")
    private Integer kmInstalacion;

    @Column("fecha_instalacion")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaInstalacion;

    private BigDecimal rtd1;
    private BigDecimal rtd2;
    private BigDecimal rtd3;

    @Column("rtd_actual")
    private BigDecimal rtdActual;

    @Column("km_acumulados")
    private Integer kmAcumulados;

    @Column("numero_reencauches")
    private Integer numeroReencauches;

    @Column("id_diseno_reencauche_actual")
    private Integer disenoReencaucheActualId;

    @Column("id_clasificacion")
    private Integer clasificacionId;
}