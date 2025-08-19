package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.MedidaNeumatico;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface MedidaNeumaticoRepository extends ReactiveCrudRepository<MedidaNeumatico, Integer> {
}