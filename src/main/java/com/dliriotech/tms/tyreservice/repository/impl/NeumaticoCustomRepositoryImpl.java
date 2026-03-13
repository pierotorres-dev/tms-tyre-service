package com.dliriotech.tms.tyreservice.repository.impl;

import com.dliriotech.tms.tyreservice.exception.InspeccionProcessingException;
import com.dliriotech.tms.tyreservice.repository.NeumaticoCustomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Implementación del repositorio custom de neumáticos que usa JdbcTemplate
 * para operaciones batch de alto rendimiento.
 * <p>
 * Con {@code rewriteBatchedStatements=true} en la URL de MySQL,
 * el driver reescribe N sentencias UPDATE individuales en un solo paquete de red,
 * reduciendo la latencia de N round-trips a 1.
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class NeumaticoCustomRepositoryImpl implements NeumaticoCustomRepository {

    private final JdbcTemplate jdbcTemplate;

    private static final String UPDATE_RTD_SQL = """
            UPDATE neumaticos
            SET rtd1 = ?, rtd2 = ?, rtd3 = ?, rtd_actual = ?
            WHERE id = ? AND id_empresa = ?
            """;

    @Override
    public int batchUpdateRtdMeasurements(Map<Integer, BigDecimal[]> rtdUpdates, Integer empresaId) {
        int[][] results = jdbcTemplate.batchUpdate(
                UPDATE_RTD_SQL,
                rtdUpdates.entrySet().stream().toList(),
                rtdUpdates.size(),
                (ps, entry) -> {
                    BigDecimal[] rtdValues = entry.getValue();
                    ps.setBigDecimal(1, rtdValues[0]); // rtd1
                    ps.setBigDecimal(2, rtdValues[1]); // rtd2
                    ps.setBigDecimal(3, rtdValues[2]); // rtd3
                    ps.setBigDecimal(4, rtdValues[3]); // rtdActual
                    ps.setInt(5, entry.getKey());       // neumaticoId
                    ps.setInt(6, empresaId);            // empresaId
                }
        );

        // Validar que cada neumático fue actualizado
        int totalUpdated = 0;
        int index = 0;
        var entries = rtdUpdates.entrySet().stream().toList();
        for (int[] batchResult : results) {
            for (int rowsAffected : batchResult) {
                if (rowsAffected == 0) {
                    Integer neumaticoId = entries.get(index).getKey();
                    throw new InspeccionProcessingException(
                            neumaticoId.toString(),
                            "actualizacion_mediciones_rtd",
                            String.format("No se pudieron actualizar las mediciones RTD del neumático %d", neumaticoId));
                }
                totalUpdated += rowsAffected;
                index++;
            }
        }

        log.debug("Batch RTD update completado — {} neumáticos actualizados", totalUpdated);
        return totalUpdated;
    }
}

