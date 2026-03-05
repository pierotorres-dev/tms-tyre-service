package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MovimientoNeumaticoRepository extends JpaRepository<MovimientoNeumatico, Integer> {

    /**
     * Busca el último movimiento de un neumático específico por tipo de movimiento.
     * Utilizado para obtener el RTD post-reencauche más reciente.
     */
    Optional<MovimientoNeumatico> findTopByNeumaticoIdAndTipoMovimientoIdOrderByIdDesc(
        Integer neumaticoId, Integer tipoMovimientoId
    );
}
