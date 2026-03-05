package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.EstadoObservacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EstadoObservacionRepository extends JpaRepository<EstadoObservacion, Integer> {
    Optional<EstadoObservacion> findByNombre(String nombre);
}