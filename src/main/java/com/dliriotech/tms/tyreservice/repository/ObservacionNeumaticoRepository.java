package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    @Query("SELECT obs.id, obs.id_neumatico, obs.id_equipo, obs.posicion, obs.id_tipo_observacion, " +
            "obs.descripcion, obs.id_estado_observacion, obs.fecha_creacion, obs.id_usuario_creacion, " +
            "obs.fecha_resolucion, obs.id_usuario_resolucion, obs.comentario_resolucion " +
            "FROM observaciones_neumatico AS obs " +
            "JOIN neumaticos AS neu ON obs.id_neumatico = neu.id " +
            "WHERE neu.id_equipo = :equipoId AND obs.id_estado_observacion = :estadoId")
    Flux<ObservacionNeumatico> findByEquipoIdAndEstadoObservacionId(Integer equipoId, Integer estadoId);
}