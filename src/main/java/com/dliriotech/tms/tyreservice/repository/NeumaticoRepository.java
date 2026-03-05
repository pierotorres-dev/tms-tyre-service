package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.Neumatico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface NeumaticoRepository extends JpaRepository<Neumatico, Integer> {

    /**
     * Obtiene todos los neumáticos de un equipo filtrados por empresa (tenant isolation).
     */
    List<Neumatico> findAllByEquipoIdAndEmpresaIdOrderByPosicionDesc(Integer equipoId, Integer empresaId);

    /**
     * Busca un neumático por ID y empresa (tenant isolation).
     */
    Optional<Neumatico> findByIdAndEmpresaId(Integer id, Integer empresaId);

    /**
     * Busca un neumático por equipo y posición específica.
     * Utilizado para validar si una posición ya está ocupada.
     */
    Optional<Neumatico> findByEquipoIdAndPosicion(Integer equipoId, Integer posicion);

    /**
     * Busca un neumático por equipo y posición específica, excluyendo un ID particular.
     * Útil para actualizaciones donde queremos verificar si otro neumático ya ocupa la posición.
     */
    Optional<Neumatico> findByEquipoIdAndPosicionAndIdNot(Integer equipoId, Integer posicion, Integer excludeId);

    /**
     * Actualiza las mediciones RTD de un neumático, restringido a empresa (tenant isolation).
     */
    @Modifying
    @Query("""
        UPDATE Neumatico n SET n.rtd1 = :rtd1, n.rtd2 = :rtd2, n.rtd3 = :rtd3, n.rtdActual = :rtdActual
        WHERE n.id = :neumaticoId AND n.empresaId = :empresaId
        """)
    int updateRtdMeasurements(
        @Param("neumaticoId") Integer neumaticoId,
        @Param("empresaId") Integer empresaId,
        @Param("rtd1") BigDecimal rtd1,
        @Param("rtd2") BigDecimal rtd2,
        @Param("rtd3") BigDecimal rtd3,
        @Param("rtdActual") BigDecimal rtdActual
    );
}