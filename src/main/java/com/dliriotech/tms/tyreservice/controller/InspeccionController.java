package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.constants.HeaderConstants;
import com.dliriotech.tms.tyreservice.dto.FinalizarInspeccionRequest;
import com.dliriotech.tms.tyreservice.service.InspeccionNeumaticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

/**
 * Controller para gestionar las inspecciones de neumáticos.
 */
@RestController
@RequestMapping("/api/v1/movimientos-neumaticos")
@RequiredArgsConstructor
@Slf4j
public class InspeccionController {

    private final InspeccionNeumaticoService inspeccionNeumaticoService;

    @PostMapping(value = "/{equipoId}/inspecciones",
                consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> finalizarInspeccion(
            @PathVariable Integer equipoId,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId,
            @RequestHeader(HeaderConstants.HEADER_USER_ID) Integer userId,
            @Valid @RequestBody FinalizarInspeccionRequest request) {

        log.info("Solicitud de finalización de inspección recibida para equipo: {}", equipoId);

        // Valores del contexto de seguridad prevalecen sobre el body
        request.setEquipoId(equipoId);
        request.setUsuarioId(userId);

        return inspeccionNeumaticoService.finalizarInspeccion(equipoId, empresaId, request)
            .doOnSuccess(result -> log.info("Inspección finalizada exitosamente para equipo: {}", equipoId))
            .doOnError(error -> log.error("Error al procesar inspección para equipo {}: {}",
                equipoId, error.getMessage()));
    }
}