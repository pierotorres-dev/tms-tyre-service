package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoUpdateRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ObservacionNeumaticoService {
    Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoIdAndTipoMovimientoId(Integer neumaticoId, Integer tipoMovimientoId, Integer empresaId);
    Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoId(Integer neumaticoId, Integer empresaId);
    Flux<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByNeumaticoId(Integer neumaticoId, Integer empresaId);
    Flux<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByEquipoId(Integer equipoId, Integer empresaId);
    Mono<ObservacionNeumaticoResponse> saveObservacion(ObservacionNeumaticoNuevoRequest request);
    Mono<ObservacionNeumaticoResponse> updateObservacion(Integer id, ObservacionNeumaticoUpdateRequest request, Integer empresaId);
}