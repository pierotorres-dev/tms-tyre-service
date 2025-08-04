package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.TipoMovimientoNeumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface TipoMovimientoNeumaticoRepository extends ReactiveCrudRepository<TipoMovimientoNeumatico, Integer> {
    Mono<TipoMovimientoNeumatico> findByNombre(String nombre);
}