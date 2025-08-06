package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
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
    private final ObservacionNeumaticoService observacionNeumaticoService;

    @GetMapping(value = "/equipo/{equipoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(@PathVariable Integer equipoId) {
        return neumaticoService.getAllNeumaticosByEquipoId(equipoId);
    }

    //TODO: Improve this method
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<NeumaticoResponse> createNeumatico(@Valid @RequestBody NeumaticoRequest request) {
        return neumaticoService.saveNeumatico(request);
    }

    //TODO: Improve this method
    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<NeumaticoResponse> updateNeumatico(@PathVariable Integer id, 
                                                   @Valid @RequestBody NeumaticoRequest request) {
        return neumaticoService.updateNeumatico(id, request);
    }

    @GetMapping(value = "/{neumaticoId}/observaciones/solucionables", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ObservacionNeumaticoResponse> getObservacionesSolucionables(
            @PathVariable Integer neumaticoId,
            @RequestParam Integer tipoMovimientoId) {
        return observacionNeumaticoService.getAllObservacionesByNeumaticoIdAndTipoMovimientoId(neumaticoId, tipoMovimientoId);
    }

    @GetMapping(value = "/{neumaticoId}/observaciones", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ObservacionNeumaticoResponse> getObservacionesByNeumatico(
            @PathVariable Integer neumaticoId,
            @RequestParam(value = "estado", required = false) String estado) {
        if ("pendiente".equalsIgnoreCase(estado)) {
            return observacionNeumaticoService.getAllObservacionesPendientesAndByNeumaticoId(neumaticoId);
        }
        return observacionNeumaticoService.getAllObservacionesByNeumaticoId(neumaticoId);
    }

    @PostMapping(value = "/observaciones", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ObservacionNeumaticoResponse> createObservacionNeumatico(
            @Valid @RequestBody ObservacionNeumaticoNuevoRequest request) {
        return observacionNeumaticoService.saveObservacion(request);
    }

    @PatchMapping(value = "/observaciones/{observacionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ObservacionNeumaticoResponse> updateObservacionNeumatico(
            @PathVariable Integer observacionId,
            @Valid @RequestBody ObservacionNeumaticoUpdateRequest request) {
        return observacionNeumaticoService.updateObservacion(observacionId, request);
    }
}