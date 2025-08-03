package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.EstadoObservacion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EstadoObservacionRepository extends ReactiveCrudRepository<EstadoObservacion, Integer> {
}