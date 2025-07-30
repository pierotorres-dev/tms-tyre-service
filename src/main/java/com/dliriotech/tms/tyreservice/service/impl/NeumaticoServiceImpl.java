package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.exception.NeumaticoException;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.NeumaticoEntityCacheService;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeumaticoServiceImpl implements NeumaticoService {

    private final NeumaticoRepository neumaticoRepository;
    private final NeumaticoEntityCacheService neumaticoEntityCacheService;

    @Override
    public Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(Integer equipoId) {
        return neumaticoRepository.getAllByEquipoIdOrderByPosicionDesc(equipoId)
                .flatMap(this::enrichNeumaticoWithRelations)
                .doOnSubscribe(s -> log.debug("Iniciando consulta de neumáticos para equipo {}", equipoId))
                .doOnComplete(() -> log.debug("Consulta de neumáticos para equipo {} completada", equipoId))
                .doOnError(error -> log.error("Error al obtener neumáticos para equipo {}: {}",
                        equipoId, error.getMessage()))
                .onErrorResume(e -> Flux.error(new NeumaticoException(
                        "TYR-NEU-OPE-001", "Error al obtener neumáticos del equipo " + equipoId)));
    }

    @Override
    public Mono<NeumaticoResponse> saveNeumatico(NeumaticoRequest request) {
        return null;
    }

    private Mono<NeumaticoResponse> enrichNeumaticoWithRelations(Neumatico neumatico) {
        // Obtener las entidades relacionadas usando el servicio de cache
        Mono<CatalogoNeumaticoResponse> catalogoMono = neumaticoEntityCacheService
                .getCatalogoNeumatico(neumatico.getCatalogoNeumaticoId());

        Mono<ProveedorResponse> proveedorMono = neumatico.getProveedorCompraId() != null ?
                neumaticoEntityCacheService.getProveedor(neumatico.getProveedorCompraId()) :
                Mono.just(ProveedorResponse.builder().build());

        Mono<DisenoReencaucheResponse> disenoMono = neumatico.getDisenoReencaucheActualId() != null ?
                neumaticoEntityCacheService.getDisenoReencauche(neumatico.getDisenoReencaucheActualId()) :
                Mono.just(DisenoReencaucheResponse.builder().build());

        Mono<ClasificacionNeumaticoResponse> clasificacionMono = neumatico.getClasificacionId() != null ?
                neumaticoEntityCacheService.getClasificacionNeumatico(neumatico.getClasificacionId()) :
                Mono.just(ClasificacionNeumaticoResponse.builder().build());

        return Mono.zip(
                catalogoMono.subscribeOn(Schedulers.boundedElastic()),
                proveedorMono.subscribeOn(Schedulers.boundedElastic()),
                disenoMono.subscribeOn(Schedulers.boundedElastic()),
                clasificacionMono.subscribeOn(Schedulers.boundedElastic())
        ).flatMap(tuple -> Mono.fromCallable(() -> mapEntityToResponse(
                neumatico, 
                tuple.getT1(), 
                tuple.getT2(), 
                tuple.getT3(), 
                tuple.getT4()))
                .subscribeOn(Schedulers.boundedElastic()));
    }

    private NeumaticoResponse mapEntityToResponse(
            Neumatico entity,
            CatalogoNeumaticoResponse catalogoResponse,
            ProveedorResponse proveedorResponse,
            DisenoReencaucheResponse disenoResponse,
            ClasificacionNeumaticoResponse clasificacionResponse) {
        
        return NeumaticoResponse.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresaId())
                .catalogoNeumaticoResponse(catalogoResponse)
                .equipoId(entity.getEquipoId())
                .posicion(entity.getPosicion())
                .serieCodigo(entity.getSerieCodigo())
                .costoInicial(entity.getCostoInicial())
                .proveedorResponse(proveedorResponse.getId() != null ? proveedorResponse : null)
                .kmInstalacion(entity.getKmInstalacion())
                .fechaInstalacion(entity.getFechaInstalacion())
                .rtd1(entity.getRtd1())
                .rtd2(entity.getRtd2())
                .rtd3(entity.getRtd3())
                .rtdActual(entity.getRtdActual())
                .kmAcumulados(entity.getKmAcumulados())
                .numeroReencauches(entity.getNumeroReencauches())
                .disenoReencaucheResponse(disenoResponse.getId() != null ? disenoResponse : null)
                .clasificacionNeumaticoResponse(clasificacionResponse.getId() != null ? clasificacionResponse : null)
                .build();
    }
}