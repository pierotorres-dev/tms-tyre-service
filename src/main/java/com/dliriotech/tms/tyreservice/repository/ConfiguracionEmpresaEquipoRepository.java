package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.ConfiguracionEmpresaEquipo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface ConfiguracionEmpresaEquipoRepository extends ReactiveCrudRepository<ConfiguracionEmpresaEquipo, Integer> {
    
    /**
     * Busca configuración específica por empresa y tipo de equipo.
     * Usado para obtener umbrales RTD específicos de una empresa.
     */
    Mono<ConfiguracionEmpresaEquipo> findByEmpresaIdAndTipoEquipoId(Integer empresaId, Integer tipoEquipoId);
}