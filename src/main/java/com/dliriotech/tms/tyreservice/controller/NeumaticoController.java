package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.constants.HeaderConstants;
import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/neumaticos")
@RequiredArgsConstructor
public class NeumaticoController {

    private final NeumaticoService neumaticoService;

    @GetMapping(value = "/equipo/{equipoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(
            @PathVariable Integer equipoId,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId) {
        return neumaticoService.getAllNeumaticosByEquipoId(equipoId, empresaId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<NeumaticoResponse> createNeumatico(
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId,
            @Valid @RequestBody NeumaticoRequest request) {
        return neumaticoService.saveNeumatico(request, empresaId);
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NeumaticoResponse> updateNeumatico(
            @PathVariable Integer id,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId,
            @Valid @RequestBody NeumaticoRequest request) {
        return neumaticoService.updateNeumatico(id, request, empresaId);
    }
}