package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface MovimientoNeumaticoRepository extends ReactiveCrudRepository<MovimientoNeumatico, Integer> {
    
    /**
     * Busca el último movimiento de un neumático específico por tipo de movimiento.
     * Utilizado para obtener el RTD post-reencauche más reciente.
     */
    Mono<MovimientoNeumatico> findTopByNeumaticoIdAndTipoMovimientoIdOrderByIdDesc(Integer neumaticoId, Integer tipoMovimientoId);
}
