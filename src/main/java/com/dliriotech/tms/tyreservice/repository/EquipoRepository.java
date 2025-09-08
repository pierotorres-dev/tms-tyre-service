package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Equipo;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface EquipoRepository extends ReactiveCrudRepository<Equipo, Integer> {
    
    /**
     * Actualiza la fecha de inspección de un equipo
     */
    @Query("UPDATE equipos SET fecha_inspeccion = :fechaInspeccion WHERE id = :equipoId")
    Mono<Integer> updateFechaInspeccion(Integer equipoId, LocalDate fechaInspeccion);
}