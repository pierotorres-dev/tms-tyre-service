package com.dliriotech.tms.tyreservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("catalogo_neumaticos")
public class CatalogoNeumatico {
    @Id
    private Integer id;

    @Column("id_marca")
    private Integer marcaId;

    @Column("id_medida")
    private Integer medidaId;

    @Column("modelo_diseno")
    private String modeloDiseno;

    @Column("tipo_uso")
    private TipoUso tipoUso;

    @Column("rtd_original")
    private BigDecimal rtdOriginal;

    @Column("presion_maxima_psi")
    private Integer presionMaximaPsi;

    private Integer treadwear;

    private String traccion;

    private String temperatura;

    @Getter
    public enum TipoUso {
        TODA_POSICION("Toda Posición"),
        DIRECCIONAL("Direccional"),
        TRACCION("Tracción"),
        REMOLQUE("Remolque");

        private final String valor;

        TipoUso(String valor) {
            this.valor = valor;
        }
    }
}