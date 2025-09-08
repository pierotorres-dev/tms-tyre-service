package com.dliriotech.tms.tyreservice.controller;

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
            @Valid @RequestBody FinalizarInspeccionRequest request) {
        
        log.info("Solicitud de finalización de inspección recibida para equipo: {}", equipoId);
        
        // Solo validación de coherencia HTTP - el equipoId del path prevalece sobre el del body
        return validarCoherenciaHttpRequest(equipoId, request)
            .then(inspeccionNeumaticoService.finalizarInspeccion(equipoId, request))
            .doOnSuccess(result -> log.info("Inspección finalizada exitosamente para equipo: {}", equipoId))
            .doOnError(error -> log.error("Error al procesar inspección para equipo {}: {}", 
                equipoId, error.getMessage()));
    }

    /**
     * Validación de coherencia HTTP únicamente.
     */
    private Mono<Void> validarCoherenciaHttpRequest(Integer equipoId, FinalizarInspeccionRequest request) {
        // Establecemos el equipoId del path en el request para garantizar coherencia
        // Esta es una responsabilidad del controller: garantizar coherencia entre URL y body
        request.setEquipoId(equipoId);
        
        return Mono.empty();
    }
}