package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ObservacionNeumaticoRepository extends ReactiveCrudRepository<ObservacionNeumatico, Integer> {

    @Query("SELECT * FROM observaciones_neumatico WHERE id_neumatico = :neumaticoId AND id_tipo_observacion IN (:tipoObservacionIds) AND id_estado_observacion = :estadoObservacionId")
    Flux<ObservacionNeumatico> findByNeumaticoIdAndTipoObservacionIdsAndEstadoObservacionId(
            Integer neumaticoId,
            Iterable<Integer> tipoObservacionIds,
            Integer estadoObservacionId
    );

    @Query("SELECT * FROM observaciones_neumatico WHERE id_neumatico = :neumaticoId ORDER BY fecha_creacion DESC")
    Flux<ObservacionNeumatico> findByNeumaticoIdOrderByFechaCreacionDesc(Integer neumaticoId);

    @Query("SELECT * FROM observaciones_neumatico WHERE id_neumatico = :neumaticoId AND id_estado_observacion = :estadoObservacionId ORDER BY fecha_creacion DESC")
    Flux<ObservacionNeumatico> findByNeumaticoIdAndEstadoObservacionIdOrderByFechaCreacionDesc(
            Integer neumaticoId, 
            Integer estadoObservacionId
    );
}