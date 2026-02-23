package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.CatalogoServiciosEmpresa;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CatalogoServiciosEmpresaRepository extends ReactiveCrudRepository<CatalogoServiciosEmpresa, Integer> {
    
    /**
     * Busca el costo de servicio por empresa, tipo de equipo y tipo de movimiento
     */
    Mono<CatalogoServiciosEmpresa> findByEmpresaIdAndTipoEquipoIdAndTipoMovimientoId(
        Integer empresaId, 
        Integer tipoEquipoId, 
        Integer tipoMovimientoId
    );
}
