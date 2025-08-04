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
@Builder(toBuilder = true)
@Table("observaciones_neumatico")
public class ObservacionNeumatico {
    @Id
    private Integer id;

    @Column("id_neumatico")
    private Integer neumaticoId;

    @Column("id_equipo")
    private Integer equipoId;

    private Integer posicion;

    @Column("id_tipo_observacion")
    private Integer tipoObservacionId;

    private String descripcion;

    @Column("id_estado_observacion")
    private Integer estadoObservacionId;

    @Column("fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column("id_usuario_creacion")
    private Integer usuarioCreacionId;

    @Column("fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column("id_usuario_resolucion")
    private Integer usuarioResolucionId;

    @Column("comentario_resolucion")
    private String comentarioResolucion;
}