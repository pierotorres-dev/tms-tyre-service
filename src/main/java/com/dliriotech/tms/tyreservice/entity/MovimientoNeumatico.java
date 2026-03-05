package com.dliriotech.tms.tyreservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "movimientos_neumaticos")
public class MovimientoNeumatico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_neumatico")
    private Integer neumaticoId;

    @Column(name = "fecha_movimiento")
    private LocalDateTime fechaMovimiento;

    @Column(name = "id_equipo_origen")
    private Integer equipoOrigenId;

    @Column(name = "posicion_origen")
    private Integer posicionOrigen;

    @Column(name = "id_equipo_destino")
    private Integer equipoDestinoId;

    @Column(name = "posicion_destino")
    private Integer posicionDestino;

    @Column(name = "id_clasificacion_origen")
    private Integer clasificacionOrigenId;

    @Column(name = "id_clasificacion_destino")
    private Integer clasificacionDestinoId;

    @Column(name = "id_tipo_movimiento")
    private Integer tipoMovimientoId;

    private Integer kilometraje;

    private BigDecimal rtd1;

    private BigDecimal rtd2;

    private BigDecimal rtd3;

    @Column(name = "rtd_actual")
    private BigDecimal rtdActual;

    @Column(name = "rtd_post_reencauche")
    private BigDecimal rtdPostReencauche;

    @Column(name = "id_usuario")
    private Integer usuarioId;

    @Column(name = "costo_movimiento")
    private BigDecimal costoMovimiento;

    @Column(name = "id_proveedor_servicio")
    private Integer proveedorServicioId;

    private String comentario;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovimientoNeumatico that = (MovimientoNeumatico) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}