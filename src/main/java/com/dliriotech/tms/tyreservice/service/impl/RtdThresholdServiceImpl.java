package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.RtdThresholdsResponse;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.service.MasterDataCacheService;
import com.dliriotech.tms.tyreservice.service.RtdThresholdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class RtdThresholdServiceImpl implements RtdThresholdService {

    private final MasterDataCacheService masterDataCacheService;

    @Override
    public Mono<RtdThresholdsResponse> calculateRtdThresholds(Neumatico neumatico, BigDecimal rtdOriginal) {
        log.debug("Calculando umbrales RTD para neumático ID: {}", neumatico.getId());
        
        Mono<BigDecimal> rtdMinimoMono = calculateRtdMinimo(neumatico);
        Mono<BigDecimal> rtdMaximoMono = calculateRtdMaximo(neumatico, rtdOriginal);
        
        return Mono.zip(rtdMinimoMono, rtdMaximoMono)
                .map(tuple -> RtdThresholdsResponse.builder()
                        .rtdMinimo(tuple.getT1())
                        .rtdMaximo(tuple.getT2())
                        .build())
                .doOnSuccess(result -> log.debug("Umbrales RTD calculados para neumático {}: mínimo={}, máximo={}", 
                        neumatico.getId(), result.getRtdMinimo(), result.getRtdMaximo()))
                .doOnError(error -> log.error("Error calculando umbrales RTD para neumático {}: {}", 
                        neumatico.getId(), error.getMessage()));
    }

    /**
     * Calcula el RTD mínimo usando la lógica de COALESCE optimizada con cache:
     * 1. Busca configuración específica de empresa-tipo_equipo
     * 2. Si no existe, usa la configuración por defecto del tipo de equipo
     */
    private Mono<BigDecimal> calculateRtdMinimo(Neumatico neumatico) {
        log.debug("Calculando RTD mínimo con cache para neumático ID: {}, equipoId: {}", 
                neumatico.getId(), neumatico.getEquipoId());
        
        // Usar el método optimizado del cache que combina todas las consultas
        return masterDataCacheService.getRtdMinimo(neumatico.getEquipoId())
                .doOnSuccess(result -> log.debug("RTD mínimo calculado para neumático {}: {}", 
                        neumatico.getId(), result))
                .doOnError(error -> log.error("Error calculando RTD mínimo para neumático {}: {}", 
                        neumatico.getId(), error.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    /**
     * Calcula el RTD máximo:
     * 1. Si el neumático tiene reencauches (numeroReencauches > 0):
     *    - Busca el último movimiento de reencauche (tipo 4) y usa rtdPostReencauche
     * 2. Si no tiene reencauches:
     *    - Usa el rtdOriginal del catálogo
     */
    private Mono<BigDecimal> calculateRtdMaximo(Neumatico neumatico, BigDecimal rtdOriginal) {
        log.debug("Calculando RTD máximo para neumático ID: {}, numeroReencauches: {}, rtdOriginal: {}", 
                neumatico.getId(), neumatico.getNumeroReencauches(), rtdOriginal);
        
        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            log.debug("Neumático {} tiene {} reencauches, buscando último movimiento con cache", 
                    neumatico.getId(), neumatico.getNumeroReencauches());
            
            return masterDataCacheService.getLatestReencaucheMovement(neumatico.getId())
                    .doOnNext(movimiento -> log.debug("Encontrado movimiento de reencauche para neumático {}: " +
                            "movimientoId={}, rtdPostReencauche={}, fechaMovimiento={}", 
                            neumatico.getId(), movimiento.getId(), movimiento.getRtdPostReencauche(), 
                            movimiento.getFechaMovimiento()))
                    .map(MovimientoNeumatico::getRtdPostReencauche)
                    .defaultIfEmpty(rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO)
                    .doOnSuccess(result -> log.debug("RTD máximo calculado para neumático {}: {}", 
                            neumatico.getId(), result));
        } else {
            log.debug("Neumático {} no tiene reencauches, usando RTD original: {}", 
                    neumatico.getId(), rtdOriginal);
            return Mono.just(rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO)
                    .doOnSuccess(result -> log.debug("RTD máximo (original) para neumático {}: {}", 
                            neumatico.getId(), result));
        }
    }
}