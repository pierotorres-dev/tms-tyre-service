package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.EstadoObservacion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface EstadoObservacionRepository extends ReactiveCrudRepository<EstadoObservacion, Integer> {
    Mono<EstadoObservacion> findByNombre(String nombre);
}