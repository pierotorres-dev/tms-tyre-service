package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ObservacionNeumaticoRepository extends JpaRepository<ObservacionNeumatico, Integer> {

    /**
     * Observaciones de un neumático filtradas por empresa, tipos de observación y estado.
     * Usado para obtener observaciones solucionables por tipo de movimiento.
     */
    @Query("""
        SELECT obs FROM ObservacionNeumatico obs
        JOIN Neumatico neu ON obs.neumaticoId = neu.id
        WHERE obs.neumaticoId = :neumaticoId AND neu.empresaId = :empresaId
        AND obs.tipoObservacionId IN :tipoObservacionIds AND obs.estadoObservacionId = :estadoObservacionId
        """)
    List<ObservacionNeumatico> findByNeumaticoIdAndEmpresaIdAndTipoObservacionIdsAndEstadoObservacionId(
        @Param("neumaticoId") Integer neumaticoId,
        @Param("empresaId") Integer empresaId,
        @Param("tipoObservacionIds") Collection<Integer> tipoObservacionIds,
        @Param("estadoObservacionId") Integer estadoObservacionId
    );

    /**
     * Todas las observaciones de un neumático filtradas por empresa, ordenadas por fecha desc.
     */
    @Query("""
        SELECT obs FROM ObservacionNeumatico obs
        JOIN Neumatico neu ON obs.neumaticoId = neu.id
        WHERE obs.neumaticoId = :neumaticoId AND neu.empresaId = :empresaId
        ORDER BY obs.fechaCreacion DESC
        """)
    List<ObservacionNeumatico> findByNeumaticoIdAndEmpresaIdOrderByFechaCreacionDesc(
        @Param("neumaticoId") Integer neumaticoId,
        @Param("empresaId") Integer empresaId
    );

    /**
     * Observaciones pendientes de un neumático filtradas por empresa.
     */
    @Query("""
        SELECT obs FROM ObservacionNeumatico obs
        JOIN Neumatico neu ON obs.neumaticoId = neu.id
        WHERE obs.neumaticoId = :neumaticoId AND neu.empresaId = :empresaId
        AND obs.estadoObservacionId = :estadoObservacionId
        ORDER BY obs.fechaCreacion DESC
        """)
    List<ObservacionNeumatico> findByNeumaticoIdAndEmpresaIdAndEstadoObservacionIdOrderByFechaCreacionDesc(
        @Param("neumaticoId") Integer neumaticoId,
        @Param("empresaId") Integer empresaId,
        @Param("estadoObservacionId") Integer estadoObservacionId
    );

    /**
     * Observaciones de neumáticos de un equipo filtradas por empresa y estado.
     */
    @Query("""
        SELECT obs FROM ObservacionNeumatico obs
        JOIN Neumatico neu ON obs.neumaticoId = neu.id
        WHERE neu.equipoId = :equipoId AND neu.empresaId = :empresaId
        AND obs.estadoObservacionId = :estadoId
        """)
    List<ObservacionNeumatico> findByEquipoIdAndEmpresaIdAndEstadoObservacionId(
        @Param("equipoId") Integer equipoId,
        @Param("empresaId") Integer empresaId,
        @Param("estadoId") Integer estadoId
    );
}