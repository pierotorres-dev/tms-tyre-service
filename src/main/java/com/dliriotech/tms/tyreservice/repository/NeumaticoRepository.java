package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Neumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface NeumaticoRepository extends ReactiveCrudRepository<Neumatico, Integer> {
}