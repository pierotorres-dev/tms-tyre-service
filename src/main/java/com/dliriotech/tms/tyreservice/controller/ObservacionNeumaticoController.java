package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.constants.HeaderConstants;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoUpdateRequest;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/observaciones-neumaticos")
@RequiredArgsConstructor
public class ObservacionNeumaticoController {
    private final ObservacionNeumaticoService observacionNeumaticoService;

    @GetMapping(value = "/neumatico/{neumaticoId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ObservacionNeumaticoResponse> getObservacionesByNeumatico(
            @PathVariable Integer neumaticoId,
            @RequestParam(value = "estado", required = false) String estado,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId) {
        if ("pendiente".equalsIgnoreCase(estado)) {
            return observacionNeumaticoService.getAllObservacionesPendientesAndByNeumaticoId(neumaticoId, empresaId);
        }
        return observacionNeumaticoService.getAllObservacionesByNeumaticoId(neumaticoId, empresaId);
    }

    @GetMapping(value = "/neumatico/{neumaticoId}/solucionables", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ObservacionNeumaticoResponse> getObservacionesSolucionables(
            @PathVariable Integer neumaticoId,
            @RequestParam Integer tipoMovimientoId,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId) {
        return observacionNeumaticoService.getAllObservacionesByNeumaticoIdAndTipoMovimientoId(neumaticoId, tipoMovimientoId, empresaId);
    }

    @GetMapping(value = "/equipo/{equipoId}/pendientes", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ObservacionNeumaticoResponse> getObservacionesPendientesByEquipo(
            @PathVariable Integer equipoId,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId) {
        return observacionNeumaticoService.getAllObservacionesPendientesAndByEquipoId(equipoId, empresaId);
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ObservacionNeumaticoResponse createObservacionNeumatico(
            @RequestHeader(HeaderConstants.HEADER_USER_ID) Integer userId,
            @Valid @RequestBody ObservacionNeumaticoNuevoRequest request) {
        request.setUsuarioCreacionId(userId);
        return observacionNeumaticoService.saveObservacion(request);
    }

    @PatchMapping(value = "/{observacionId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ObservacionNeumaticoResponse updateObservacionNeumatico(
            @PathVariable Integer observacionId,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId,
            @Valid @RequestBody ObservacionNeumaticoUpdateRequest request) {
        return observacionNeumaticoService.updateObservacion(observacionId, request, empresaId);
    }
}