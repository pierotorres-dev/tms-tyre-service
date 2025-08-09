package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.exception.NeumaticoException;
import com.dliriotech.tms.tyreservice.exception.ValidationException;
import com.dliriotech.tms.tyreservice.exception.DataIntegrityException;
import com.dliriotech.tms.tyreservice.exception.PosicionAlreadyOccupiedException;
import com.dliriotech.tms.tyreservice.exception.NeumaticoNotFoundException;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.NeumaticoEntityCacheService;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import com.dliriotech.tms.tyreservice.service.RtdThresholdService;
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
    private final RtdThresholdService rtdThresholdService;

    @Override
    public Flux<NeumaticoResponse> getAllNeumaticosByEquipoId(Integer equipoId) {
        if (equipoId == null || equipoId <= 0) {
            return Flux.error(new ValidationException("equipoId", "debe ser un número positivo válido"));
        }

        return neumaticoRepository.getAllByEquipoIdOrderByPosicionDesc(equipoId)
                .flatMap(this::enrichNeumaticoWithRelations)
                .doOnSubscribe(s -> log.info("Iniciando consulta de neumáticos para equipo {}", equipoId))
                .doOnComplete(() -> log.info("Consulta de neumáticos para equipo {} completada", equipoId))
                .doOnError(error -> log.error("Error al obtener neumáticos para equipo {}: {}",
                        equipoId, error.getMessage()))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ValidationException) {
                        return Flux.error(throwable);
                    }
                    return Flux.error(new NeumaticoException(
                            "TYR-NEU-OPE-001", "Error al obtener neumáticos del equipo " + equipoId, throwable));
                });
    }

    @Override
    public Mono<NeumaticoResponse> saveNeumatico(NeumaticoRequest request) {
        return validateNeumaticoRequest(request)
                .then(validatePosicionAvailability(request.getEquipoId(), request.getPosicion()))
                .then(Mono.fromCallable(() -> mapRequestToEntity(request)))
                .flatMap(entity -> neumaticoRepository.save(entity)
                        .flatMap(this::enrichNeumaticoWithRelations))
                .doOnSubscribe(s -> log.info("Iniciando guardado de nuevo neumático"))
                .doOnSuccess(result -> log.info("Neumático guardado exitosamente: {}", result.getId()))
                .doOnError(error -> log.error("Error al guardar neumático: {}", error.getMessage()))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ValidationException || 
                        throwable instanceof PosicionAlreadyOccupiedException) {
                        return Mono.error(throwable);
                    }
                    
                    // Manejar violaciones específicas de constraints de base de datos
                    String errorMessage = throwable.getMessage();
                    if (errorMessage != null) {
                        // Constraint de serie única
                        if (errorMessage.contains("Duplicate entry") && errorMessage.contains("serie_codigo")) {
                            return Mono.error(new DataIntegrityException(
                                "Ya existe un neumático con el código de serie '" + request.getSerieCodigo() + "'", 
                                throwable));
                        }
                        
                        // Constraint de equipo-posición única
                        if (errorMessage.contains("uk_equipo_posicion") || 
                           (errorMessage.contains("Duplicate entry") && errorMessage.contains("id_equipo") && errorMessage.contains("posicion"))) {
                            return Mono.error(new PosicionAlreadyOccupiedException(
                                request.getEquipoId(), request.getPosicion()));
                        }
                        
                        // Otras violaciones de integridad
                        if (errorMessage.contains("Duplicate entry")) {
                            return Mono.error(new DataIntegrityException(
                                "Ya existe un registro con los datos proporcionados", throwable));
                        }
                        
                        // Violaciones de foreign key
                        if (errorMessage.contains("foreign key constraint") || errorMessage.contains("Cannot add or update")) {
                            if (errorMessage.contains("catalogo_neumatico")) {
                                return Mono.error(new ValidationException("catalogoNeumaticoId", 
                                    "no existe en el catálogo de neumáticos"));
                            }
                            if (errorMessage.contains("proveedor")) {
                                return Mono.error(new ValidationException("proveedorCompraId", 
                                    "no existe en el registro de proveedores"));
                            }
                            if (errorMessage.contains("clasificacion")) {
                                return Mono.error(new ValidationException("clasificacionId", 
                                    "no existe en las clasificaciones de neumáticos"));
                            }
                            if (errorMessage.contains("diseno_reencauche")) {
                                return Mono.error(new ValidationException("disenoReencaucheActualId", 
                                    "no existe en los diseños de reencauche"));
                            }
                            return Mono.error(new DataIntegrityException(
                                "Referencia inválida a un registro relacionado", throwable));
                        }
                    }
                    
                    return Mono.error(new NeumaticoException(
                            "TYR-NEU-OPE-002", "Error al guardar neumático", throwable));
                });
    }

    @Override
    public Mono<NeumaticoResponse> updateNeumatico(Integer id, NeumaticoRequest request) {
        if (id == null || id <= 0) {
            return Mono.error(new ValidationException("id", "debe ser un número positivo válido"));
        }

        return validateNeumaticoRequest(request)
                .then(neumaticoRepository.findById(id)
                        .switchIfEmpty(Mono.error(new NeumaticoNotFoundException(id.toString())))
                        .flatMap(existingNeumatico -> {
                            // Solo validar posición si está cambiando
                            if (!existingNeumatico.getEquipoId().equals(request.getEquipoId()) ||
                                !existingNeumatico.getPosicion().equals(request.getPosicion())) {
                                return validatePosicionAvailabilityForUpdate(request.getEquipoId(), request.getPosicion(), id)
                                        .thenReturn(existingNeumatico);
                            }
                            return Mono.just(existingNeumatico);
                        })
                        .map(existingNeumatico -> {
                            // Actualizar los campos del neumático existente
                            existingNeumatico.setEmpresaId(request.getEmpresaId());
                            existingNeumatico.setCatalogoNeumaticoId(request.getCatalogoNeumaticoId());
                            existingNeumatico.setEquipoId(request.getEquipoId());
                            existingNeumatico.setPosicion(request.getPosicion());
                            existingNeumatico.setSerieCodigo(request.getSerieCodigo());
                            existingNeumatico.setCostoInicial(request.getCostoInicial());
                            existingNeumatico.setProveedorCompraId(request.getProveedorCompraId());
                            existingNeumatico.setKmInstalacion(request.getKmInstalacion());
                            existingNeumatico.setFechaInstalacion(request.getFechaInstalacion());
                            existingNeumatico.setRtd1(request.getRtd1());
                            existingNeumatico.setRtd2(request.getRtd2());
                            existingNeumatico.setRtd3(request.getRtd3());
                            existingNeumatico.setRtdActual(request.getRtdActual());
                            existingNeumatico.setKmAcumulados(request.getKmAcumulados());
                            existingNeumatico.setNumeroReencauches(request.getNumeroReencauches());
                            existingNeumatico.setDisenoReencaucheActualId(request.getDisenoReencaucheActualId());
                            existingNeumatico.setClasificacionId(request.getClasificacionId());
                            return existingNeumatico;
                        })
                        .flatMap(neumaticoRepository::save)
                        .flatMap(this::enrichNeumaticoWithRelations))
                .doOnSubscribe(s -> log.info("Iniciando actualización de neumático {}", id))
                .doOnSuccess(result -> log.info("Neumático {} actualizado exitosamente", id))
                .doOnError(error -> log.error("Error al actualizar neumático {}: {}", id, error.getMessage()))
                .onErrorResume(throwable -> {
                    if (throwable instanceof ValidationException || 
                        throwable instanceof NeumaticoNotFoundException ||
                        throwable instanceof PosicionAlreadyOccupiedException) {
                        return Mono.error(throwable);
                    }
                    
                    // Manejar violaciones específicas de constraints de base de datos (similar al saveNeumatico)
                    String errorMessage = throwable.getMessage();
                    if (errorMessage != null) {
                        if (errorMessage.contains("Duplicate entry") && errorMessage.contains("serie_codigo")) {
                            return Mono.error(new DataIntegrityException(
                                "Ya existe otro neumático con el código de serie '" + request.getSerieCodigo() + "'", 
                                throwable));
                        }
                        
                        if (errorMessage.contains("uk_equipo_posicion") || 
                           (errorMessage.contains("Duplicate entry") && errorMessage.contains("id_equipo") && errorMessage.contains("posicion"))) {
                            return Mono.error(new PosicionAlreadyOccupiedException(
                                request.getEquipoId(), request.getPosicion()));
                        }
                    }
                    
                    return Mono.error(new NeumaticoException(
                            "TYR-NEU-OPE-003", "Error al actualizar neumático", throwable));
                });
    }

    private Mono<Void> validateNeumaticoRequest(NeumaticoRequest request) {
        if (request == null) {
            return Mono.error(new ValidationException("La solicitud no puede ser nula"));
        }
        if (request.getCatalogoNeumaticoId() == null || request.getCatalogoNeumaticoId() <= 0) {
            return Mono.error(new ValidationException("catalogoNeumaticoId", "debe ser un número positivo válido"));
        }
        if (request.getEquipoId() == null || request.getEquipoId() <= 0) {
            return Mono.error(new ValidationException("equipoId", "debe ser un número positivo válido"));
        }
        if (request.getSerieCodigo() == null || request.getSerieCodigo().trim().isEmpty()) {
            return Mono.error(new ValidationException("serieCodigo", "no puede estar vacío"));
        }
        if (request.getPosicion() == null || request.getPosicion() <= 0) {
            return Mono.error(new ValidationException("posicion", "debe ser un número positivo válido"));
        }
        return Mono.empty();
    }

    private Mono<Void> validatePosicionAvailability(Integer equipoId, Integer posicion) {
        log.info("Validando disponibilidad de posición {} en equipo {}", posicion, equipoId);
        return neumaticoRepository.findByEquipoIdAndPosicion(equipoId, posicion)
                .doOnNext(found -> log.info("Encontrado neumático existente en posición: {}", found.getSerieCodigo()))
                .flatMap(existingNeumatico -> {
                    log.warn("Posición {} del equipo {} ya está ocupada por neumático {}", 
                        posicion, equipoId, existingNeumatico.getSerieCodigo());
                    return Mono.<Void>error(
                        new PosicionAlreadyOccupiedException(equipoId, posicion, existingNeumatico.getSerieCodigo()));
                })
                .doOnSuccess(v -> log.info("Posición {} en equipo {} está disponible", posicion, equipoId))
                .then();
    }

    private Mono<Void> validatePosicionAvailabilityForUpdate(Integer equipoId, Integer posicion, Integer excludeId) {
        return neumaticoRepository.findByEquipoIdAndPosicionAndIdNot(equipoId, posicion, excludeId)
                .flatMap(existingNeumatico -> Mono.<Void>error(
                    new PosicionAlreadyOccupiedException(equipoId, posicion, existingNeumatico.getSerieCodigo())))
                .then();
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
        ).flatMap(tuple -> {
            // Calcular RTD thresholds usando el rtdOriginal del catálogo
            Mono<RtdThresholdsResponse> rtdThresholdsMono = rtdThresholdService
                    .calculateRtdThresholds(neumatico, tuple.getT1().getRtdOriginal())
                    .subscribeOn(Schedulers.boundedElastic());
            
            return rtdThresholdsMono.flatMap(rtdThresholds -> 
                Mono.fromCallable(() -> mapEntityToResponse(
                        neumatico, 
                        tuple.getT1(), 
                        tuple.getT2(), 
                        tuple.getT3(), 
                        tuple.getT4(),
                        rtdThresholds))
                        .subscribeOn(Schedulers.boundedElastic())
            );
        });
    }

    private Neumatico mapRequestToEntity(NeumaticoRequest request) {
        return Neumatico.builder()
                .empresaId(request.getEmpresaId())
                .catalogoNeumaticoId(request.getCatalogoNeumaticoId())
                .equipoId(request.getEquipoId())
                .posicion(request.getPosicion())
                .serieCodigo(request.getSerieCodigo())
                .costoInicial(request.getCostoInicial())
                .proveedorCompraId(request.getProveedorCompraId())
                .kmInstalacion(request.getKmInstalacion())
                .fechaInstalacion(request.getFechaInstalacion())
                .rtd1(request.getRtd1())
                .rtd2(request.getRtd2())
                .rtd3(request.getRtd3())
                .rtdActual(request.getRtdActual())
                .kmAcumulados(request.getKmAcumulados())
                .kmCicloActual(request.getKmCicloActual())
                .numeroReencauches(request.getNumeroReencauches())
                .disenoReencaucheActualId(request.getDisenoReencaucheActualId())
                .clasificacionId(request.getClasificacionId())
                .build();
    }

    private NeumaticoResponse mapEntityToResponse(
            Neumatico entity,
            CatalogoNeumaticoResponse catalogoResponse,
            ProveedorResponse proveedorResponse,
            DisenoReencaucheResponse disenoResponse,
            ClasificacionNeumaticoResponse clasificacionResponse,
            RtdThresholdsResponse rtdThresholds) {

        String disenoVigente = null;
        if (entity.getNumeroReencauches() != null && entity.getNumeroReencauches() > 0) {
            disenoVigente = disenoResponse != null && disenoResponse.getId() != null ?
                    disenoResponse.getNombreDiseno() : null;
        } else {
            disenoVigente = catalogoResponse != null ? catalogoResponse.getModeloDiseno() : null;
        }

        String estadoReencauche = "Nuevo";
        if (entity.getNumeroReencauches() != null && entity.getNumeroReencauches() > 0) {
            estadoReencauche = "R" + entity.getNumeroReencauches();
        }

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
                .kmCicloActual(entity.getKmCicloActual())
                .numeroReencauches(entity.getNumeroReencauches())
                .disenoReencaucheResponse(disenoResponse.getId() != null ? disenoResponse : null)
                .clasificacionNeumaticoResponse(clasificacionResponse.getId() != null ? clasificacionResponse : null)
                .rtdThresholds(rtdThresholds)
                .disenoVigente(disenoVigente)
                .estadoReencauche(estadoReencauche)
                .build();
    }
}