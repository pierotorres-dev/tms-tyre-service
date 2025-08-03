package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.RtdThresholdsResponse;
import com.dliriotech.tms.tyreservice.entity.ConfiguracionEmpresaEquipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.entity.TipoEquipo;
import com.dliriotech.tms.tyreservice.repository.*;
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

    private final EquipoRepository equipoRepository;
    private final ConfiguracionEmpresaEquipoRepository configuracionEmpresaEquipoRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;

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
     * Calcula el RTD mínimo usando la lógica de COALESCE:
     * 1. Busca configuración específica de empresa-tipo_equipo
     * 2. Si no existe, usa la configuración por defecto del tipo de equipo
     */
    private Mono<BigDecimal> calculateRtdMinimo(Neumatico neumatico) {
        return equipoRepository.findById(neumatico.getEquipoId())
                .flatMap(equipo -> 
                    // Intentar obtener configuración específica de empresa-tipo_equipo
                    configuracionEmpresaEquipoRepository
                            .findByIdEmpresaAndIdTipoEquipo(equipo.getEmpresaId(), equipo.getTipoEquipoId())
                            .map(ConfiguracionEmpresaEquipo::getRtdMinimoReencauche)
                            .switchIfEmpty(
                                // Si no hay configuración específica, usar la del tipo de equipo
                                tipoEquipoRepository.findById(equipo.getTipoEquipoId())
                                        .map(TipoEquipo::getRtadMinimoReencauche)
                            )
                )
                .defaultIfEmpty(BigDecimal.ZERO)
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
        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            return findLatestReencaucheMovement(neumatico.getId())
                    .map(MovimientoNeumatico::getRtdPostReencauche)
                    .defaultIfEmpty(rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO);
        } else {
            return Mono.just(rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO);
        }
    }

    /**
     * Busca el último movimiento de reencauche para un neumático específico.
     * Filtra por tipo de movimiento 4 (reencauche) y selecciona el de mayor ID.
     */
    private Mono<com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico> findLatestReencaucheMovement(Integer neumaticoId) {
        return movimientoNeumaticoRepository
                .findTopByIdNeumaticoAndIdTipoMovimientoOrderByIdDesc(neumaticoId, 4)
                .subscribeOn(Schedulers.boundedElastic());
    }
}
