package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.TipoObservacion;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TipoObservacionRepository extends ReactiveCrudRepository<TipoObservacion, Integer> {
}