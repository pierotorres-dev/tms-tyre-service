package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.*;
import reactor.core.publisher.Mono;

public interface NeumaticoEntityCacheService {
    
    Mono<CatalogoNeumaticoResponse> getCatalogoNeumatico(Integer catalogoId);
    
    Mono<ProveedorResponse> getProveedor(Integer proveedorId);
    
    Mono<DisenoReencaucheResponse> getDisenoReencauche(Integer disenoId);
    
    Mono<ClasificacionNeumaticoResponse> getClasificacionNeumatico(Integer clasificacionId);
    
    Mono<MarcaNeumaticoResponse> getMarcaNeumatico(Integer marcaId);
    
    Mono<MedidaNeumaticoResponse> getMedidaNeumatico(Integer medidaId);
    
    /**
     * Obtiene información resumida de un neumático (id y serie).
     * Utilizado cuando solo se necesitan datos básicos del neumático.
     */
    Mono<NeumaticoSummaryResponse> getNeumaticoSummary(Integer neumaticoId);
    
    // Métodos para invalidar cache
    Mono<Void> invalidateCatalogoNeumatico(Integer catalogoId);
    
    Mono<Void> invalidateProveedor(Integer proveedorId);
    
    Mono<Void> invalidateDisenoReencauche(Integer disenoId);
    
    Mono<Void> invalidateClasificacionNeumatico(Integer clasificacionId);
    
    Mono<Void> invalidateMarcaNeumatico(Integer marcaId);
    
    Mono<Void> invalidateMedidaNeumatico(Integer medidaId);
    
    /**
     * Invalida el cache de información resumida de un neumático.
     */
    Mono<Void> invalidateNeumaticoSummary(Integer neumaticoId);
}