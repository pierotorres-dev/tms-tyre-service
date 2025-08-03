package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("equipos")
public class Equipo {
    @Id
    private Integer id;

    private String placa;
    private String negocio;

    @Column("id_tipo_equipo")
    private Integer tipoEquipoId;

    @Column("id_esquema_equipo")
    private Integer esquemaEquipoId;

    @Column("fecha_inspeccion")
    private LocalDate fechaInspeccion;

    private Integer kilometraje;

    @Column("fecha_actualizacion_kilometraje")
    private LocalDate fechaActualizacionKilometraje;

    @Column("id_estado_equipo")
    private Integer estadoId;

    @Column("id_empresa")
    private Integer empresaId;
}