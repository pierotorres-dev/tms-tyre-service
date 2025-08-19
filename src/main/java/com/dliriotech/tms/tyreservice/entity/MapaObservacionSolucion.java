package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("mapa_observacion_solucion")
public class MapaObservacionSolucion {
    @Id
    private Integer id;

    @Column("id_tipo_observacion")
    private Integer tipoObservacionId;

    @Column("id_tipo_movimiento")
    private Integer tipoMovimientoId;
}