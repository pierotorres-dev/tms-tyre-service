package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.exception.NeumaticoException;
import com.dliriotech.tms.tyreservice.repository.*;
import com.dliriotech.tms.tyreservice.service.NeumaticoEntityCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeumaticoEntityCacheServiceImpl implements NeumaticoEntityCacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CatalogoNeumaticoRepository catalogoNeumaticoRepository;
    private final ProveedorRepository proveedorRepository;
    private final DisenoReencaucheRepository disenoReencaucheRepository;
    private final ClasificacionNeumaticoRepository clasificacionNeumaticoRepository;
    private final MarcaNeumaticoRepository marcaNeumaticoRepository;
    private final MedidaNeumaticoRepository medidaNeumaticoRepository;

    @Value("${app.cache.ttl-hours:24}")
    private long cacheTtlHours;

    @Value("${app.cache.prefixes.catalogo-neumatico:cache:catalogoNeumatico}")
    private String catalogoCachePrefix;

    @Value("${app.cache.prefixes.proveedor:cache:proveedor}")
    private String proveedorCachePrefix;

    @Value("${app.cache.prefixes.diseno-reencauche:cache:disenoReencauche}")
    private String disenoCachePrefix;

    @Value("${app.cache.prefixes.clasificacion-neumatico:cache:clasificacionNeumatico}")
    private String clasificacionCachePrefix;

    @Value("${app.cache.prefixes.marca-neumatico:cache:marcaNeumatico}")
    private String marcaCachePrefix;

    @Value("${app.cache.prefixes.medida-neumatico:cache:medidaNeumatico}")
    private String medidaCachePrefix;

    @Override
    public Mono<CatalogoNeumaticoResponse> getCatalogoNeumatico(Integer catalogoId) {
        String cacheKey = buildCacheKey(catalogoCachePrefix, catalogoId);
        
        return getCachedEntity(cacheKey, CatalogoNeumaticoResponse.class)
                .switchIfEmpty(catalogoNeumaticoRepository.findById(catalogoId)
                        .switchIfEmpty(Mono.error(new NeumaticoException(
                                "TYR-CAT-NF-001", "Catálogo de neumático no encontrado: " + catalogoId)))
                        .flatMap(this::enrichCatalogoWithRelations)
                        .flatMap(catalogoResponse -> cacheEntity(cacheKey, catalogoResponse)
                                .thenReturn(catalogoResponse)))
                .doOnSuccess(catalogo -> log.debug("Catálogo de neumático {} obtenido desde cache/DB", catalogoId))
                .doOnError(error -> log.error("Error al obtener catálogo de neumático {}: {}", catalogoId, error.getMessage()));
    }

    @Override
    public Mono<ProveedorResponse> getProveedor(Integer proveedorId) {
        String cacheKey = buildCacheKey(proveedorCachePrefix, proveedorId);
        
        return getCachedEntity(cacheKey, ProveedorResponse.class)
                .switchIfEmpty(proveedorRepository.findById(proveedorId)
                        .switchIfEmpty(Mono.error(new NeumaticoException(
                                "TYR-PRV-NF-001", "Proveedor no encontrado: " + proveedorId)))
                        .map(proveedor -> ProveedorResponse.builder()
                                .id(proveedor.getId())
                                .nombre(proveedor.getNombre())
                                .ruc(proveedor.getRuc())
                                .build())
                        .flatMap(proveedorResponse -> cacheEntity(cacheKey, proveedorResponse)
                                .thenReturn(proveedorResponse)))
                .doOnSuccess(proveedor -> log.debug("Proveedor {} obtenido desde cache/DB", proveedorId))
                .doOnError(error -> log.error("Error al obtener proveedor {}: {}", proveedorId, error.getMessage()));
    }

    @Override
    public Mono<DisenoReencaucheResponse> getDisenoReencauche(Integer disenoId) {
        String cacheKey = buildCacheKey(disenoCachePrefix, disenoId);
        
        return getCachedEntity(cacheKey, DisenoReencaucheResponse.class)
                .switchIfEmpty(disenoReencaucheRepository.findById(disenoId)
                        .switchIfEmpty(Mono.error(new NeumaticoException(
                                "TYR-DIS-NF-001", "Diseño de reencauche no encontrado: " + disenoId)))
                        .map(diseno -> DisenoReencaucheResponse.builder()
                                .id(diseno.getId())
                                .nombreDiseno(diseno.getNombreDiseno())
                                .proveedorReencauche(diseno.getProveedorReencauche())
                                .build())
                        .flatMap(disenoResponse -> cacheEntity(cacheKey, disenoResponse)
                                .thenReturn(disenoResponse)))
                .doOnSuccess(diseno -> log.debug("Diseño de reencauche {} obtenido desde cache/DB", disenoId))
                .doOnError(error -> log.error("Error al obtener diseño de reencauche {}: {}", disenoId, error.getMessage()));
    }

    @Override
    public Mono<ClasificacionNeumaticoResponse> getClasificacionNeumatico(Integer clasificacionId) {
        String cacheKey = buildCacheKey(clasificacionCachePrefix, clasificacionId);
        
        return getCachedEntity(cacheKey, ClasificacionNeumaticoResponse.class)
                .switchIfEmpty(clasificacionNeumaticoRepository.findById(clasificacionId)
                        .switchIfEmpty(Mono.error(new NeumaticoException(
                                "TYR-CLS-NF-001", "Clasificación de neumático no encontrada: " + clasificacionId)))
                        .map(clasificacion -> ClasificacionNeumaticoResponse.builder()
                                .id(clasificacion.getId())
                                .nombre(clasificacion.getNombre())
                                .descripcion(clasificacion.getDescripcion())
                                .build())
                        .flatMap(clasificacionResponse -> cacheEntity(cacheKey, clasificacionResponse)
                                .thenReturn(clasificacionResponse)))
                .doOnSuccess(clasificacion -> log.debug("Clasificación de neumático {} obtenida desde cache/DB", clasificacionId))
                .doOnError(error -> log.error("Error al obtener clasificación de neumático {}: {}", clasificacionId, error.getMessage()));
    }

    @Override
    public Mono<MarcaNeumaticoResponse> getMarcaNeumatico(Integer marcaId) {
        String cacheKey = buildCacheKey(marcaCachePrefix, marcaId);
        
        return getCachedEntity(cacheKey, MarcaNeumaticoResponse.class)
                .switchIfEmpty(marcaNeumaticoRepository.findById(marcaId)
                        .switchIfEmpty(Mono.error(new NeumaticoException(
                                "TYR-MRC-NF-001", "Marca de neumático no encontrada: " + marcaId)))
                        .map(marca -> MarcaNeumaticoResponse.builder()
                                .id(marca.getId())
                                .nombre(marca.getNombre())
                                .build())
                        .flatMap(marcaResponse -> cacheEntity(cacheKey, marcaResponse)
                                .thenReturn(marcaResponse)))
                .doOnSuccess(marca -> log.debug("Marca de neumático {} obtenida desde cache/DB", marcaId))
                .doOnError(error -> log.error("Error al obtener marca de neumático {}: {}", marcaId, error.getMessage()));
    }

    @Override
    public Mono<MedidaNeumaticoResponse> getMedidaNeumatico(Integer medidaId) {
        String cacheKey = buildCacheKey(medidaCachePrefix, medidaId);
        
        return getCachedEntity(cacheKey, MedidaNeumaticoResponse.class)
                .switchIfEmpty(medidaNeumaticoRepository.findById(medidaId)
                        .switchIfEmpty(Mono.error(new NeumaticoException(
                                "TYR-MED-NF-001", "Medida de neumático no encontrada: " + medidaId)))
                        .map(medida -> MedidaNeumaticoResponse.builder()
                                .id(medida.getId())
                                .descripcion(medida.getDescripcion())
                                .tipoConstruccion(medida.getTipoConstruccion())
                                .indiceCarga(medida.getIndiceCarga())
                                .simboloVelocidad(medida.getSimboloVelocidad())
                                .plyRating(medida.getPlyRating())
                                .build())
                        .flatMap(medidaResponse -> cacheEntity(cacheKey, medidaResponse)
                                .thenReturn(medidaResponse)))
                .doOnSuccess(medida -> log.debug("Medida de neumático {} obtenida desde cache/DB", medidaId))
                .doOnError(error -> log.error("Error al obtener medida de neumático {}: {}", medidaId, error.getMessage()));
    }

    @Override
    public Mono<Void> invalidateCatalogoNeumatico(Integer catalogoId) {
        String cacheKey = buildCacheKey(catalogoCachePrefix, catalogoId);
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(result -> log.debug("Cache invalidado para catálogo de neumático {}", catalogoId))
                .then();
    }

    @Override
    public Mono<Void> invalidateProveedor(Integer proveedorId) {
        String cacheKey = buildCacheKey(proveedorCachePrefix, proveedorId);
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(result -> log.debug("Cache invalidado para proveedor {}", proveedorId))
                .then();
    }

    @Override
    public Mono<Void> invalidateDisenoReencauche(Integer disenoId) {
        String cacheKey = buildCacheKey(disenoCachePrefix, disenoId);
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(result -> log.debug("Cache invalidado para diseño de reencauche {}", disenoId))
                .then();
    }

    @Override
    public Mono<Void> invalidateClasificacionNeumatico(Integer clasificacionId) {
        String cacheKey = buildCacheKey(clasificacionCachePrefix, clasificacionId);
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(result -> log.debug("Cache invalidado para clasificación de neumático {}", clasificacionId))
                .then();
    }

    @Override
    public Mono<Void> invalidateMarcaNeumatico(Integer marcaId) {
        String cacheKey = buildCacheKey(marcaCachePrefix, marcaId);
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(result -> log.debug("Cache invalidado para marca de neumático {}", marcaId))
                .then();
    }

    @Override
    public Mono<Void> invalidateMedidaNeumatico(Integer medidaId) {
        String cacheKey = buildCacheKey(medidaCachePrefix, medidaId);
        return redisTemplate.delete(cacheKey)
                .doOnSuccess(result -> log.debug("Cache invalidado para medida de neumático {}", medidaId))
                .then();
    }

    private Mono<CatalogoNeumaticoResponse> enrichCatalogoWithRelations(com.dliriotech.tms.tyreservice.entity.CatalogoNeumatico catalogo) {
        Mono<MarcaNeumaticoResponse> marcaMono = getMarcaNeumatico(catalogo.getMarcaId());
        Mono<MedidaNeumaticoResponse> medidaMono = getMedidaNeumatico(catalogo.getMedidaId());

        return Mono.zip(
                marcaMono.subscribeOn(Schedulers.boundedElastic()),
                medidaMono.subscribeOn(Schedulers.boundedElastic())
        ).flatMap(tuple -> Mono.fromCallable(() -> CatalogoNeumaticoResponse.builder()
                .id(catalogo.getId())
                .marcaNeumaticoResponse(tuple.getT1())
                .medidaNeumaticoResponse(tuple.getT2())
                .modeloDiseno(catalogo.getModeloDiseno())
                .tipoUso(catalogo.getTipoUso())
                .rtdOriginal(catalogo.getRtdOriginal())
                .presionMaximaPsi(catalogo.getPresionMaximaPsi())
                .treadwear(catalogo.getTreadwear())
                .traccion(catalogo.getTraccion())
                .temperatura(catalogo.getTemperatura())
                .build()).subscribeOn(Schedulers.boundedElastic()));
    }

    private String buildCacheKey(String prefix, Integer id) {
        return String.format("%s:%d", prefix, id);
    }

    private <T> Mono<T> getCachedEntity(String cacheKey, Class<T> entityType) {
        return redisTemplate.opsForValue()
                .get(cacheKey)
                .cast(String.class)
                .flatMap(json -> Mono.fromCallable(() -> objectMapper.readValue(json, entityType))
                        .subscribeOn(Schedulers.boundedElastic()))
                .onErrorResume(e -> {
                    log.debug("Error al deserializar desde cache para clave {}: {}", cacheKey, e.getMessage());
                    return Mono.empty();
                });
    }

    private <T> Mono<Void> cacheEntity(String cacheKey, T entity) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(entity))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(json -> redisTemplate.opsForValue()
                        .set(cacheKey, json, Duration.ofHours(cacheTtlHours)))
                .onErrorResume(e -> {
                    log.warn("Error al cachear entidad con clave {}: {}", cacheKey, e.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
