package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("observaciones_neumatico")
public class ObservacionNeumatico {
    @Id
    private Integer id;

    @Column("id_neumatico")
    private Integer idNeumatico;

    @Column("id_equipo")
    private Integer idEquipo;

    private Integer posicion;

    @Column("id_tipo_observacion")
    private Integer idTipoObservacion;

    private String descripcion;

    @Column("id_estado_observacion")
    private Integer idEstadoObservacion;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column("id_usuario_creacion")
    private Integer idUsuarioCreacion;

    @Column("fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column("id_usuario_resolucion")
    private Integer idUsuarioResolucion;

    @Column("comentario_resolucion")
    private String comentarioResolucion;
}