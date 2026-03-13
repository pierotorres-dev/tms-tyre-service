package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MovimientoNeumaticoRepository extends JpaRepository<MovimientoNeumatico, Integer> {

    /**
     * Busca el último movimiento de un neumático específico por tipo de movimiento.
     * Utilizado para obtener el RTD post-reencauche más reciente.
     */
    Optional<MovimientoNeumatico> findTopByNeumaticoIdAndTipoMovimientoIdOrderByIdDesc(
        Integer neumaticoId, Integer tipoMovimientoId
    );

    /**
     * Busca el último movimiento de reencauche para cada neumático en un lote.
     * Usa una subconsulta para obtener el MAX(id) por neumático, evitando N queries individuales.
     */
    @Query("""
        SELECT m FROM MovimientoNeumatico m
        WHERE m.id IN (
            SELECT MAX(m2.id) FROM MovimientoNeumatico m2
            WHERE m2.neumaticoId IN :neumaticoIds
              AND m2.tipoMovimientoId = :tipoMovimientoId
            GROUP BY m2.neumaticoId
        )
        """)
    List<MovimientoNeumatico> findLastMovimientosByNeumaticoIdsAndTipo(
            @Param("neumaticoIds") List<Integer> neumaticoIds,
            @Param("tipoMovimientoId") Integer tipoMovimientoId);
}
