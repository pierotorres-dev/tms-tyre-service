package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "catalogo_servicios_empresa")
public class CatalogoServiciosEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_empresa")
    private Integer empresaId;

    @Column(name = "id_tipo_equipo")
    private Integer tipoEquipoId;

    @Column(name = "id_tipo_movimiento")
    private Integer tipoMovimientoId;

    @Column(name = "costo_servicio")
    private BigDecimal costoServicio;

    private Boolean activo;
}