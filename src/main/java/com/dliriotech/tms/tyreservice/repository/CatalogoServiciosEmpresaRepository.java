package com.dliriotech.tms.tyreservice.repository;

import com.dliriotech.tms.tyreservice.entity.CatalogoServiciosEmpresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CatalogoServiciosEmpresaRepository extends JpaRepository<CatalogoServiciosEmpresa, Integer> {

    /**
     * Busca el costo de servicio por empresa, tipo de equipo y tipo de movimiento.
     */
    Optional<CatalogoServiciosEmpresa> findByEmpresaIdAndTipoEquipoIdAndTipoMovimientoId(
        Integer empresaId,
        Integer tipoEquipoId,
        Integer tipoMovimientoId
    );
}
