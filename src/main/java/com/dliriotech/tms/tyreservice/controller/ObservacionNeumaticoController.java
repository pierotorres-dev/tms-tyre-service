package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoUpdateRequest;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/observaciones-neumaticos")
@RequiredArgsConstructor
public class ObservacionNeumaticoController {
    private final ObservacionNeumaticoService observacionNeumaticoService;

    // Endpoint para listar todas las observaciones de un neumático
    @GetMapping(value = "/neumatico/{neumaticoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ObservacionNeumaticoResponse> getObservacionesByNeumatico(
            @PathVariable Integer neumaticoId,
            @RequestParam(value = "estado", required = false) String estado) {
        if ("pendiente".equalsIgnoreCase(estado)) {
            return observacionNeumaticoService.getAllObservacionesPendientesAndByNeumaticoId(neumaticoId);
        }
        return observacionNeumaticoService.getAllObservacionesByNeumaticoId(neumaticoId);
    }

    // Endpoint para observaciones solucionables
    @GetMapping(value = "/neumatico/{neumaticoId}/solucionables", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ObservacionNeumaticoResponse> getObservacionesSolucionables(
            @PathVariable Integer neumaticoId,
            @RequestParam Integer tipoMovimientoId) {
        return observacionNeumaticoService.getAllObservacionesByNeumaticoIdAndTipoMovimientoId(neumaticoId, tipoMovimientoId);
    }

    // Endpoint para observaciones por equipo
    @GetMapping(value = "/equipo/{equipoId}/pendientes", produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ObservacionNeumaticoResponse> getObservacionesPendientesByEquipo(
            @PathVariable Integer equipoId) {
        return observacionNeumaticoService.getAllObservacionesPendientesAndByEquipoId(equipoId);
    }

    // Creación de observaciones
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ObservacionNeumaticoResponse> createObservacionNeumatico(
            @Valid @RequestBody ObservacionNeumaticoNuevoRequest request) {
        return observacionNeumaticoService.saveObservacion(request);
    }

    // Actualización de observaciones
    @PatchMapping(value = "/{observacionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ObservacionNeumaticoResponse> updateObservacionNeumatico(
            @PathVariable Integer observacionId,
            @Valid @RequestBody ObservacionNeumaticoUpdateRequest request) {
        return observacionNeumaticoService.updateObservacion(observacionId, request);
    }
}