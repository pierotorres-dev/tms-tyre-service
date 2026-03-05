package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MapaObservacionSolucion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MapaObservacionSolucionRepository extends JpaRepository<MapaObservacionSolucion, Integer> {

    @Query("SELECT m.tipoObservacionId FROM MapaObservacionSolucion m WHERE m.tipoMovimientoId = :tipoMovimientoId")
    List<Integer> findTipoObservacionIdsByTipoMovimientoId(@Param("tipoMovimientoId") Integer tipoMovimientoId);
}