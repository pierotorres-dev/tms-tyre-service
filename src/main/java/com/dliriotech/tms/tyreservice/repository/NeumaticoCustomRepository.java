package com.dliriotech.tms.tyreservice.repository;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Repositorio custom para operaciones batch sobre neumáticos
 * que no se pueden expresar eficientemente con Spring Data JPA.
 */
public interface NeumaticoCustomRepository {

    /**
     * Actualiza las mediciones RTD de múltiples neumáticos en una sola operación JDBC batch,
     * restringido a una empresa (tenant isolation).
     * <p>
     * Usa {@code JdbcTemplate.batchUpdate} con {@code rewriteBatchedStatements=true}
     * para enviar todos los UPDATEs en un solo round-trip a MySQL.
     *
     * @param rtdUpdates mapa de neumaticoId → valores RTD calculados (rtd1, rtd2, rtd3, rtdActual)
     * @param empresaId  ID de la empresa (tenant isolation)
     * @return número total de filas actualizadas
     * @throws com.dliriotech.tms.tyreservice.exception.InspeccionProcessingException
     *         si algún neumático no se pudo actualizar (0 rows affected)
     */
    int batchUpdateRtdMeasurements(Map<Integer, BigDecimal[]> rtdUpdates, Integer empresaId);
}


