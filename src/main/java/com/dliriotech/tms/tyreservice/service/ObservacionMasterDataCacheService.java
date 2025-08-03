package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.dto.EstadoObservacionResponse;
import com.dliriotech.tms.tyreservice.dto.TipoObservacionResponse;
import reactor.core.publisher.Mono;

public interface ObservacionMasterDataCacheService {

    Mono<TipoObservacionResponse> getTipoObservacion(Integer tipoObservacionId);

    Mono<EstadoObservacionResponse> getEstadoObservacion(Integer estadoObservacionId);

    Mono<Void> invalidateTipoObservacionCache(Integer tipoObservacionId);

    Mono<Void> invalidateEstadoObservacionCache(Integer estadoObservacionId);
}
