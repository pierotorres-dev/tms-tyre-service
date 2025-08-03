package com.dliriotech.tms.tyreservice.service;

import com.dliriotech.tms.tyreservice.entity.ConfiguracionEmpresaEquipo;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.TipoEquipo;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

/**
 * Servicio de cache para datos maestros relacionados con neumáticos.
 * Implementa estrategias de cache para datos que cambian con poca frecuencia
 * y son consultados frecuentemente.
 */
public interface MasterDataCacheService {
    
    /**
     * Obtiene un equipo por ID con cache.
     * TTL: 4 horas (datos semi-estáticos)
     */
    Mono<Equipo> getEquipo(Integer equipoId);
    
    /**
     * Obtiene configuración específica empresa-tipo_equipo con cache.
     * TTL: 24 horas (datos maestros)
     */
    Mono<ConfiguracionEmpresaEquipo> getConfiguracionEmpresaEquipo(Integer empresaId, Integer tipoEquipoId);
    
    /**
     * Obtiene tipo de equipo por ID con cache.
     * TTL: 24 horas (datos maestros)
     */
    Mono<TipoEquipo> getTipoEquipo(Integer tipoEquipoId);
    
    /**
     * Obtiene el último movimiento de reencauche para un neumático.
     * TTL: 1 hora (datos operacionales que pueden cambiar)
     */
    Mono<MovimientoNeumatico> getLatestReencaucheMovement(Integer neumaticoId);
    
    /**
     * Calcula directamente el RTD mínimo con cache optimizado.
     * Combina las consultas de equipo, configuración y tipo de equipo.
     */
    Mono<BigDecimal> getRtdMinimo(Integer equipoId);
    
    /**
     * Invalida cache relacionado con un equipo específico.
     */
    Mono<Void> invalidateEquipoCache(Integer equipoId);
    
    /**
     * Invalida cache de configuración empresa-equipo.
     */
    Mono<Void> invalidateConfiguracionCache(Integer empresaId, Integer tipoEquipoId);
    
    /**
     * Invalida cache de movimientos de un neumático específico.
     */
    Mono<Void> invalidateMovimientoCache(Integer neumaticoId);
}