package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MarcaNeumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MarcaNeumaticoRepository extends ReactiveCrudRepository<MarcaNeumatico, Integer> {
}