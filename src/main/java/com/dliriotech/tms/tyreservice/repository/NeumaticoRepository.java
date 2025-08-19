package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Neumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NeumaticoRepository extends ReactiveCrudRepository<Neumatico, Integer> {

    Flux<Neumatico> getAllByEquipoIdOrderByPosicionDesc(int equipoId);
    
    /**
     * Busca un neumático por equipo y posición específica.
     * Utilizado para validar si una posición ya está ocupada.
     */
    Mono<Neumatico> findByEquipoIdAndPosicion(Integer equipoId, Integer posicion);
    
    /**
     * Busca un neumático por equipo y posición específica, excluyendo un ID particular.
     * Útil para actualizaciones donde queremos verificar si otro neumático ya ocupa la posición.
     */
    Mono<Neumatico> findByEquipoIdAndPosicionAndIdNot(Integer equipoId, Integer posicion, Integer excludeId);
}