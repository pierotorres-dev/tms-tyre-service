package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "equipos")
public class Equipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String placa;

    private String negocio;

    @Column(name = "id_tipo_equipo")
    private Integer tipoEquipoId;

    @Column(name = "id_esquema_equipo")
    private Integer esquemaEquipoId;

    @Column(name = "fecha_inspeccion")
    private LocalDate fechaInspeccion;

    private Integer kilometraje;

    @Column(name = "fecha_actualizacion_kilometraje")
    private LocalDate fechaActualizacionKilometraje;

    @Column(name = "id_estado_equipo")
    private Integer estadoId;

    @Column(name = "id_empresa")
    private Integer empresaId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipo that = (Equipo) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}