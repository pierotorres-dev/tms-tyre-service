package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("tipos_observacion")
public class TipoObservacion {
    @Id
    private Integer id;

    private String nombre;

    private String ambito;

    private String descripcion;

    private Boolean activo;
}