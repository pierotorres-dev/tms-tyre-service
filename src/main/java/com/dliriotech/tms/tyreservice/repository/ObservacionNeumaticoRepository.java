package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ObservacionNeumaticoRepository extends ReactiveCrudRepository<ObservacionNeumatico, Integer> {

    @Query("SELECT obs.id, obs.id_neumatico, obs.id_equipo, obs.posicion, obs.id_tipo_observacion, " +
            "obs.descripcion, obs.id_estado_observacion, obs.fecha_creacion, obs.id_usuario_creacion, " +
            "obs.fecha_resolucion, obs.id_usuario_resolucion, obs.comentario_resolucion " +
            "FROM observaciones_neumatico AS obs " +
            "JOIN neumaticos AS neu ON obs.id_neumatico = neu.id " +
            "WHERE obs.id_neumatico = :neumaticoId AND neu.id_empresa = :empresaId " +
            "AND obs.id_tipo_observacion IN (:tipoObservacionIds) AND obs.id_estado_observacion = :estadoObservacionId")
    Flux<ObservacionNeumatico> findByNeumaticoIdAndEmpresaIdAndTipoObservacionIdsAndEstadoObservacionId(
            Integer neumaticoId,
            Integer empresaId,
            Iterable<Integer> tipoObservacionIds,
            Integer estadoObservacionId
    );

    @Query("SELECT obs.id, obs.id_neumatico, obs.id_equipo, obs.posicion, obs.id_tipo_observacion, " +
            "obs.descripcion, obs.id_estado_observacion, obs.fecha_creacion, obs.id_usuario_creacion, " +
            "obs.fecha_resolucion, obs.id_usuario_resolucion, obs.comentario_resolucion " +
            "FROM observaciones_neumatico AS obs " +
            "JOIN neumaticos AS neu ON obs.id_neumatico = neu.id " +
            "WHERE obs.id_neumatico = :neumaticoId AND neu.id_empresa = :empresaId " +
            "ORDER BY obs.fecha_creacion DESC")
    Flux<ObservacionNeumatico> findByNeumaticoIdAndEmpresaIdOrderByFechaCreacionDesc(Integer neumaticoId, Integer empresaId);

    @Query("SELECT obs.id, obs.id_neumatico, obs.id_equipo, obs.posicion, obs.id_tipo_observacion, " +
            "obs.descripcion, obs.id_estado_observacion, obs.fecha_creacion, obs.id_usuario_creacion, " +
            "obs.fecha_resolucion, obs.id_usuario_resolucion, obs.comentario_resolucion " +
            "FROM observaciones_neumatico AS obs " +
            "JOIN neumaticos AS neu ON obs.id_neumatico = neu.id " +
            "WHERE obs.id_neumatico = :neumaticoId AND neu.id_empresa = :empresaId " +
            "AND obs.id_estado_observacion = :estadoObservacionId ORDER BY obs.fecha_creacion DESC")
    Flux<ObservacionNeumatico> findByNeumaticoIdAndEmpresaIdAndEstadoObservacionIdOrderByFechaCreacionDesc(
            Integer neumaticoId,
            Integer empresaId,
            Integer estadoObservacionId
    );

    @Query("SELECT obs.id, obs.id_neumatico, obs.id_equipo, obs.posicion, obs.id_tipo_observacion, " +
            "obs.descripcion, obs.id_estado_observacion, obs.fecha_creacion, obs.id_usuario_creacion, " +
            "obs.fecha_resolucion, obs.id_usuario_resolucion, obs.comentario_resolucion " +
            "FROM observaciones_neumatico AS obs " +
            "JOIN neumaticos AS neu ON obs.id_neumatico = neu.id " +
            "WHERE neu.id_equipo = :equipoId AND neu.id_empresa = :empresaId AND obs.id_estado_observacion = :estadoId")
    Flux<ObservacionNeumatico> findByEquipoIdAndEmpresaIdAndEstadoObservacionId(Integer equipoId, Integer empresaId, Integer estadoId);
}