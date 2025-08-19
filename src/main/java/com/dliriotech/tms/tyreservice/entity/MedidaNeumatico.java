package com.dliriotech.tms.tyreservice.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("medidas_neumatico")
public class MedidaNeumatico {
    @Id
    private Integer id;

    private String descripcion;

    @Column("tipo_construccion")
    private String tipoConstruccion;

    @Column("indice_carga")
    private String indiceCarga;

    @Column("simbolo_velocidad")
    private String simboloVelocidad;

    @Column("ply_rating")
    private Integer plyRating;
}