package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Equipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface EquipoRepository extends JpaRepository<Equipo, Integer> {

    /**
     * Actualiza la fecha de inspección de un equipo.
     */
    @Modifying
    @Query("UPDATE Equipo e SET e.fechaInspeccion = :fechaInspeccion WHERE e.id = :equipoId")
    int updateFechaInspeccion(@Param("equipoId") Integer equipoId, @Param("fechaInspeccion") LocalDate fechaInspeccion);
}