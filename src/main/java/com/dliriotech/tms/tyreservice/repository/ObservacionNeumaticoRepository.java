package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ObservacionNeumaticoRepository extends ReactiveCrudRepository<ObservacionNeumatico, Integer> {
}