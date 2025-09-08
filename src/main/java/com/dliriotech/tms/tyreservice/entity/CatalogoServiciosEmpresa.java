package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("catalogo_servicios_empresa")
public class CatalogoServiciosEmpresa {
    
    @Id
    private Integer id;
    
    @Column("id_empresa")
    private Integer empresaId;
    
    @Column("id_tipo_equipo")
    private Integer tipoEquipoId;
    
    @Column("id_tipo_movimiento")
    private Integer tipoMovimientoId;
    
    @Column("costo_servicio")
    private BigDecimal costoServicio;
    
    private String descripcion;
}
