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
    
    // Métodos para invalidar cache
    Mono<Void> invalidateCatalogoNeumatico(Integer catalogoId);
    
    Mono<Void> invalidateProveedor(Integer proveedorId);
    
    Mono<Void> invalidateDisenoReencauche(Integer disenoId);
    
    Mono<Void> invalidateClasificacionNeumatico(Integer clasificacionId);
    
    Mono<Void> invalidateMarcaNeumatico(Integer marcaId);
    
    Mono<Void> invalidateMedidaNeumatico(Integer medidaId);
}