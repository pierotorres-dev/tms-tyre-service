package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "medidas_neumatico")
public class MedidaNeumatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String descripcion;

    @Column(name = "tipo_construccion")
    private String tipoConstruccion;

    @Column(name = "indice_carga")
    private String indiceCarga;

    @Column(name = "simbolo_velocidad")
    private String simboloVelocidad;

    @Column(name = "ply_rating")
    private Integer plyRating;
}