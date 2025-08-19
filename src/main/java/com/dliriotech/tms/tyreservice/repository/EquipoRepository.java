package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Equipo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface EquipoRepository extends ReactiveCrudRepository<Equipo, Integer> {
}