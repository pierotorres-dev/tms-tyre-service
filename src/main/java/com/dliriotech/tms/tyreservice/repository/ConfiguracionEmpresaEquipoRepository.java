package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ConfiguracionEmpresaEquipo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionEmpresaEquipoRepository extends JpaRepository<ConfiguracionEmpresaEquipo, Integer> {

    /**
     * Busca configuración específica por empresa y tipo de equipo.
     * Usado para obtener umbrales RTD específicos de una empresa.
     */
    Optional<ConfiguracionEmpresaEquipo> findByEmpresaIdAndTipoEquipoId(Integer empresaId, Integer tipoEquipoId);
}