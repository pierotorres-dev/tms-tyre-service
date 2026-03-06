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
@Table(name = "catalogo_neumaticos")
public class CatalogoNeumatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_marca", insertable = false, updatable = false)
    private Integer marcaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_marca")
    private MarcaNeumatico marca;

    @Column(name = "id_medida", insertable = false, updatable = false)
    private Integer medidaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_medida")
    private MedidaNeumatico medida;

    @Column(name = "modelo_diseno")
    private String modeloDiseno;

    @Column(name = "tipo_uso")
    private String tipoUso;

    @Column(name = "rtd_original")
    private BigDecimal rtdOriginal;

    @Column(name = "presion_maxima_psi")
    private Integer presionMaximaPsi;

    private Integer treadwear;

    private String traccion;

    private String temperatura;
}