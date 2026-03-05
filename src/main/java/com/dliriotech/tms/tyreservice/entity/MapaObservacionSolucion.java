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
@Table(name = "mapa_observacion_solucion")
public class MapaObservacionSolucion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_tipo_observacion")
    private Integer tipoObservacionId;

    @Column(name = "id_tipo_movimiento")
    private Integer tipoMovimientoId;
}