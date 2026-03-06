package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "neumaticos")
public class Neumatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_empresa")
    private Integer empresaId;

    @Column(name = "id_catalogo_neumatico", insertable = false, updatable = false)
    private Integer catalogoNeumaticoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_catalogo_neumatico")
    private CatalogoNeumatico catalogoNeumatico;

    @Column(name = "id_equipo")
    private Integer equipoId;

    private Integer posicion;

    @Column(name = "serie_codigo")
    private String serieCodigo;

    @Column(name = "costo_inicial")
    private BigDecimal costoInicial;

    @Column(name = "id_proveedor_compra", insertable = false, updatable = false)
    private Integer proveedorCompraId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor_compra")
    private Proveedor proveedorCompra;

    @Column(name = "km_instalacion")
    private Integer kmInstalacion;

    @Column(name = "fecha_instalacion")
    private LocalDate fechaInstalacion;

    private BigDecimal rtd1;

    private BigDecimal rtd2;

    private BigDecimal rtd3;

    @Column(name = "rtd_actual")
    private BigDecimal rtdActual;

    @Column(name = "km_acumulados")
    private Integer kmAcumulados;

    @Column(name = "km_ciclo_actual")
    private Integer kmCicloActual;

    @Column(name = "numero_reencauches")
    private Integer numeroReencauches;

    @Column(name = "id_diseno_reencauche_actual", insertable = false, updatable = false)
    private Integer disenoReencaucheActualId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_diseno_reencauche_actual")
    private DisenoReencauche disenoReencaucheActual;

    @Column(name = "id_clasificacion", insertable = false, updatable = false)
    private Integer clasificacionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_clasificacion")
    private ClasificacionNeumatico clasificacion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Neumatico that = (Neumatico) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}