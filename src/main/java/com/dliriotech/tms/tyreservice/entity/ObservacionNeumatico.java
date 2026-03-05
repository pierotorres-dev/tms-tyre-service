package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
@Entity
@Table(name = "observaciones_neumatico")
public class ObservacionNeumatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_neumatico")
    private Integer neumaticoId;

    @Column(name = "id_equipo")
    private Integer equipoId;

    private Integer posicion;

    @Column(name = "id_tipo_observacion")
    private Integer tipoObservacionId;

    private String descripcion;

    @Column(name = "id_estado_observacion")
    private Integer estadoObservacionId;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "id_usuario_creacion")
    private Integer usuarioCreacionId;

    @Column(name = "fecha_resolucion")
    private LocalDateTime fechaResolucion;

    @Column(name = "id_usuario_resolucion")
    private Integer usuarioResolucionId;

    @Column(name = "comentario_resolucion")
    private String comentarioResolucion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ObservacionNeumatico that = (ObservacionNeumatico) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}