package com.dliriotech.tms.tyreservice.repository.custom;

import com.dliriotech.tms.tyreservice.dto.NeumaticoResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repository custom para consultas complejas de Neumatico
 * Permite usar RowMapper custom para mapeo directo a Response
 */
public interface NeumaticoRepositoryCustom {
    
    Mono<NeumaticoResponse> findByIdWithRelationsAsResponse(Integer id);
    
    Flux<NeumaticoResponse> findByEquipoIdWithRelationsAsResponse(Integer equipoId);
}