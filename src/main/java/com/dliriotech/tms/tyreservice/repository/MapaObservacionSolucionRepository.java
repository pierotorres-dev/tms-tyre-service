package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MapaObservacionSolucion;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface MapaObservacionSolucionRepository extends ReactiveCrudRepository<MapaObservacionSolucion, Integer> {
    
    @Query("SELECT id_tipo_observacion FROM mapa_observacion_solucion WHERE id_tipo_movimiento = :tipoMovimientoId")
    Flux<Integer> findTipoObservacionIdsByTipoMovimientoId(Integer tipoMovimientoId);
}