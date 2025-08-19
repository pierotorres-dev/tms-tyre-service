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

    @Column("id_neumatico")
    private Integer neumaticoId;

    @Column("fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Column("id_equipo_origen")
    private Integer equipoOrigenId;

    @Column("posicion_origen")
    private Integer posicionOrigen;

    @Column("id_equipo_destino")
    private Integer equipoDestinoId;

    @Column("posicion_destino")
    private Integer posicionDestino;

    @Column("id_clasificacion_origen")
    private Integer clasificacionOrigenId;

    @Column("id_clasificacion_destino")
    private Integer clasificacionDestinoId;

    @Column("id_tipo_movimiento")
    private Integer tipoMovimientoId;

    @Column("kilometraje")
    private Integer kilometraje;

    @Column("rtd1")
    private BigDecimal rtd1;

    @Column("rtd2")
    private BigDecimal rtd2;

    @Column("rtd3")
    private BigDecimal rtd3;

    @Column("rtd_actual")
    private BigDecimal rtdActual;

    @Column("rtd_post_reencauche")
    private BigDecimal rtdPostReencauche;

    @Column("id_usuario")
    private Integer usuarioId;

    @Column("costo_movimiento")
    private BigDecimal costoMovimiento;

    @Column("proveedor_servicio")
    private String proveedorServicio;

    @Column("comentario")
    private String comentario;
}