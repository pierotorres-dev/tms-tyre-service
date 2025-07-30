package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.NeumaticoResponse;
import reactor.core.publisher.Flux;

public interface NeumaticoService {
    Flux<NeumaticoResponse>getAllNeumaticosByEquipoId(Integer equipoId);
}