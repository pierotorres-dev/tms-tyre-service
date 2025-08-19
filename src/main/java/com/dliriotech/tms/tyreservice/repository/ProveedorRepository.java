package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Proveedor;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ProveedorRepository extends ReactiveCrudRepository<Proveedor, Integer> {
}