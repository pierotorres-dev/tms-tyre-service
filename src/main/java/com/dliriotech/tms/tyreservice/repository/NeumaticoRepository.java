package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Neumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface NeumaticoRepository extends ReactiveCrudRepository<Neumatico, Integer> {

    Flux<Neumatico> getAllByEquipoIdOrderByPosicionDesc(int equipoId);
}