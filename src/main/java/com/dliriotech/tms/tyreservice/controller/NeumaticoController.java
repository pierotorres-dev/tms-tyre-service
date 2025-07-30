package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.dto.NeumaticoRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoResponse;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import com.dliriotech.tms.tyreservice.service.impl.NeumaticoServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/neumaticos")
@RequiredArgsConstructor
@Slf4j
public class NeumaticoController {

    private final NeumaticoService neumaticoService;

    @GetMapping(value = "/equipo/{equipoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(@PathVariable Integer equipoId) {
        return neumaticoService.getAllNeumaticosByEquipoId(equipoId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<NeumaticoResponse> createNeumatico(@Valid @RequestBody NeumaticoRequest request) {
        return neumaticoService.saveNeumatico(request);
    }
}
