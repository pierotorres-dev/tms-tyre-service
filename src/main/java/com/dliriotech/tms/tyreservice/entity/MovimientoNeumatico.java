package com.dliriotech.tms.tyreservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("movimientos_neumaticos")
public class MovimientoNeumatico {
    
    @Id
    private Integer id;
    
    @Column("id_tipo_movimiento")
    private Integer idTipoMovimiento;
    
    @Column("id_neumatico")
    private Integer idNeumatico;
    
    @Column("id_equipo")
    private Integer idEquipo;
    
    @Column("posicion_origen")
    private Integer posicionOrigen;
    
    @Column("posicion_destino")
    private Integer posicionDestino;
    
    @Column("fecha_movimiento")
    private LocalDateTime fechaMovimiento;
    
    @Column("kilometraje_equipo")
    private BigDecimal kilometrajeEquipo;
    
    @Column("observaciones")
    private String observaciones;
    
    @Column("id_usuario")
    private Integer idUsuario;
    
    @Column("activo")
    private Integer activo;
}