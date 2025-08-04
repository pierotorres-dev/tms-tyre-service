package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoNuevoRequest;
import com.dliriotech.tms.tyreservice.dto.ObservacionNeumaticoResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ObservacionNeumaticoService {
    Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoIdAndTipoMovimientoId(Integer neumaticoId, Integer tipoMovimientoId);
    Flux<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoId(Integer neumaticoId);
    Flux<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByNeumaticoId(Integer neumaticoId);
    Mono<ObservacionNeumaticoResponse> saveObservacion(ObservacionNeumaticoNuevoRequest observacionNeumaticoNuevoRequest);
}