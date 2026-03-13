package com.dliriotech.tms.tyreservice.controller;

import com.dliriotech.tms.tyreservice.constants.HeaderConstants;
import com.dliriotech.tms.tyreservice.dto.InspeccionLoteResponse;
import com.dliriotech.tms.tyreservice.dto.LoteNeumaticosInspeccionRequest;
import com.dliriotech.tms.tyreservice.service.MovimientoNeumaticoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * Controller receptor de lotes de inspección delegados por el fleet-service (orquestador).
 * Procesa neumáticos inspeccionados y sus observaciones de forma atómica.
 */
@RestController
@RequestMapping("/api/v1/movimientos-neumaticos")
@RequiredArgsConstructor
@Slf4j
public class MovimientoNeumaticoController {

    private final MovimientoNeumaticoService movimientoNeumaticoService;

    /**
     * Recibe un lote de neumáticos inspeccionados junto con sus observaciones
     * desde el fleet-service (orquestador) y los procesa transaccionalmente.
     *
     * <p>Operaciones atómicas:
     * <ul>
     *   <li>Actualización de mediciones RTD por neumático</li>
     *   <li>Creación de movimientos de inspección (tipo 31)</li>
     *   <li>Creación de observaciones de neumático nuevas</li>
     *   <li>Resolución de observaciones pendientes</li>
     * </ul>
     *
     * @param userId    ID del usuario que realiza la inspección (header X-User-Id)
     * @param empresaId ID de la empresa para aislamiento multi-tenant (header X-Empresa-Id)
     * @param request   payload con neumáticos inspeccionados y observaciones
     * @return response con las observaciones creadas y resueltas
     */
    @PostMapping(value = "/inspeccion-lote",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InspeccionLoteResponse procesarLoteNeumaticos(
            @RequestHeader(HeaderConstants.HEADER_USER_ID) Integer userId,
            @RequestHeader(HeaderConstants.HEADER_EMPRESA_ID) Integer empresaId,
            @Valid @RequestBody LoteNeumaticosInspeccionRequest request) {

        log.info("Recibido lote de inspección — equipoId: {}, inspeccionId: {}, neumáticos: {}, obs. nuevas: {}, obs. resueltas: {}",
                request.getEquipoId(),
                request.getInspeccionId(),
                request.getNeumaticosInspeccionados().size(),
                request.getObservacionesNuevas() != null ? request.getObservacionesNuevas().size() : 0,
                request.getObservacionesResueltas() != null ? request.getObservacionesResueltas().size() : 0);

        InspeccionLoteResponse response = movimientoNeumaticoService.procesarInspeccionLote(request, userId, empresaId);

        log.info("Lote de inspección procesado exitosamente — equipoId: {}, inspeccionId: {}, obs. creadas: {}, obs. resueltas: {}",
                request.getEquipoId(), request.getInspeccionId(),
                response.getObservacionesCreadas() != null ? response.getObservacionesCreadas().size() : 0,
                response.getObservacionesResueltasIds() != null ? response.getObservacionesResueltasIds().size() : 0);

        return response;
    }
}

