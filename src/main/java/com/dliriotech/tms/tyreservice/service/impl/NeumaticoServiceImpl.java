package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.NeumaticoRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoResponse;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeumaticoServiceImpl implements NeumaticoService {

    private final NeumaticoRepository neumaticoRepository;

    @Override
    public Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(Integer equipoId) {
        return null;
    }

    @Override
    public Mono<NeumaticoResponse> saveNeumatico(NeumaticoRequest request) {
        return null;
    }
}