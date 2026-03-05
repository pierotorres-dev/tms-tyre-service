package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.TipoMovimientoNeumatico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipoMovimientoNeumaticoRepository extends JpaRepository<TipoMovimientoNeumatico, Integer> {
    Optional<TipoMovimientoNeumatico> findByNombre(String nombre);
}