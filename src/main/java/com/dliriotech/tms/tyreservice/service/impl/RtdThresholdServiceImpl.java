package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.RtdThresholdsResponse;
import com.dliriotech.tms.tyreservice.entity.ConfiguracionEmpresaEquipo;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.entity.TipoEquipo;
import com.dliriotech.tms.tyreservice.repository.ConfiguracionEmpresaEquipoRepository;
import com.dliriotech.tms.tyreservice.repository.EquipoRepository;
import com.dliriotech.tms.tyreservice.repository.MovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.TipoEquipoRepository;
import com.dliriotech.tms.tyreservice.service.RtdThresholdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RtdThresholdServiceImpl implements RtdThresholdService {

    private static final int TIPO_MOVIMIENTO_REENCAUCHE_ID = 4;

    private final EquipoRepository equipoRepository;
    private final ConfiguracionEmpresaEquipoRepository configuracionEmpresaEquipoRepository;
    private final TipoEquipoRepository tipoEquipoRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;

    @Override
    public RtdThresholdsResponse calculateRtdThresholds(Neumatico neumatico, BigDecimal rtdOriginal) {
        log.debug("Calculando umbrales RTD para neumático ID: {}", neumatico.getId());

        BigDecimal rtdMinimo = calculateRtdMinimo(neumatico);
        BigDecimal rtdMaximo = calculateRtdMaximo(neumatico, rtdOriginal);

        return RtdThresholdsResponse.builder()
                .rtdMinimo(rtdMinimo)
                .rtdMaximo(rtdMaximo)
                .build();
    }

    @Override
    public Map<Integer, RtdThresholdsResponse> calculateRtdThresholdsBatch(
            List<Neumatico> neumaticos,
            Map<Integer, BigDecimal> rtdOriginalByNeumaticoId) {

        if (neumaticos.isEmpty()) {
            return Map.of();
        }

        log.debug("Calculando umbrales RTD en batch para {} neumáticos", neumaticos.size());

        // 1. Pre-cargar RTD mínimo: todos comparten el mismo equipoId → 1 query equipo + 1 query config
        BigDecimal rtdMinimoBatch = calculateRtdMinimoBatch(neumaticos.getFirst());

        // 2. Pre-cargar movimientos de reencauche en batch para los que tienen reencauches
        List<Integer> neumaticoIdsConReencauche = neumaticos.stream()
                .filter(n -> n.getNumeroReencauches() != null && n.getNumeroReencauches() > 0)
                .map(Neumatico::getId)
                .toList();

        Map<Integer, BigDecimal> rtdPostReencaucheByNeumaticoId = new HashMap<>();
        if (!neumaticoIdsConReencauche.isEmpty()) {
            List<MovimientoNeumatico> ultimosReencauches = movimientoNeumaticoRepository
                    .findLastMovimientosByNeumaticoIdsAndTipo(neumaticoIdsConReencauche, TIPO_MOVIMIENTO_REENCAUCHE_ID);

            rtdPostReencaucheByNeumaticoId = ultimosReencauches.stream()
                    .collect(Collectors.toMap(
                            MovimientoNeumatico::getNeumaticoId,
                            m -> m.getRtdPostReencauche() != null ? m.getRtdPostReencauche() : BigDecimal.ZERO
                    ));
        }

        // 3. Construir resultado
        Map<Integer, RtdThresholdsResponse> result = new HashMap<>();
        for (Neumatico neumatico : neumaticos) {
            BigDecimal rtdOriginal = rtdOriginalByNeumaticoId.getOrDefault(neumatico.getId(), BigDecimal.ZERO);
            BigDecimal rtdMaximo;

            if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
                rtdMaximo = rtdPostReencaucheByNeumaticoId.getOrDefault(
                        neumatico.getId(),
                        rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO);
            } else {
                rtdMaximo = rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO;
            }

            result.put(neumatico.getId(), RtdThresholdsResponse.builder()
                    .rtdMinimo(rtdMinimoBatch)
                    .rtdMaximo(rtdMaximo)
                    .build());
        }

        return result;
    }

    /**
     * Calcula el RTD mínimo una sola vez para un lote de neumáticos del mismo equipo.
     * Como todos comparten equipoId, el resultado es el mismo para todos.
     */
    private BigDecimal calculateRtdMinimoBatch(Neumatico sampleNeumatico) {
        return calculateRtdMinimo(sampleNeumatico);
    }

    /**
     * Calcula el RTD mínimo usando la lógica de COALESCE:
     * 1. Busca configuración específica de empresa-tipo_equipo
     * 2. Si no existe, usa la configuración por defecto del tipo de equipo
     */
    private BigDecimal calculateRtdMinimo(Neumatico neumatico) {
        if (neumatico.getEquipoId() == null) {
            return BigDecimal.ZERO;
        }

        Equipo equipo = equipoRepository.findById(neumatico.getEquipoId()).orElse(null);
        if (equipo == null) {
            return BigDecimal.ZERO;
        }

        // 1. Buscar configuración específica empresa-tipo_equipo
        BigDecimal rtdMinimo = configuracionEmpresaEquipoRepository
                .findByEmpresaIdAndTipoEquipoId(equipo.getEmpresaId(), equipo.getTipoEquipoId())
                .map(ConfiguracionEmpresaEquipo::getRtdMinimoScrap)
                .orElse(null);

        if (rtdMinimo != null) {
            return rtdMinimo;
        }

        // 2. Fallback: configuración por defecto del tipo de equipo
        return tipoEquipoRepository.findById(equipo.getTipoEquipoId())
                .map(TipoEquipo::getRtadMinimoScrap)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * Calcula el RTD máximo:
     * 1. Si tiene reencauches: usa rtdPostReencauche del último movimiento tipo reencauche
     * 2. Si no tiene reencauches: usa el rtdOriginal del catálogo
     */
    private BigDecimal calculateRtdMaximo(Neumatico neumatico, BigDecimal rtdOriginal) {
        BigDecimal defaultRtd = rtdOriginal != null ? rtdOriginal : BigDecimal.ZERO;

        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            return movimientoNeumaticoRepository
                    .findTopByNeumaticoIdAndTipoMovimientoIdOrderByIdDesc(
                            neumatico.getId(), TIPO_MOVIMIENTO_REENCAUCHE_ID)
                    .map(MovimientoNeumatico::getRtdPostReencauche)
                    .orElse(defaultRtd);
        }

        return defaultRtd;
    }
}