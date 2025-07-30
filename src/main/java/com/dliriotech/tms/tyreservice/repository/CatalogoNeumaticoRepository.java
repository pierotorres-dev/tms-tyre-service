package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.CatalogoNeumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface CatalogoNeumaticoRepository extends ReactiveCrudRepository<CatalogoNeumatico, Integer> {
}