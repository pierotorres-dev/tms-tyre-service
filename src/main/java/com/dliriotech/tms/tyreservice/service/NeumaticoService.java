package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.NeumaticoRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface NeumaticoService {
    Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(Integer equipoId);
    
    Mono<NeumaticoResponse> saveNeumatico(NeumaticoRequest request);
    
    Mono<NeumaticoResponse> updateNeumatico(Integer id, NeumaticoRequest request);
}