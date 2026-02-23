package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.TipoMovimientoConstants;
import com.dliriotech.tms.tyreservice.dto.ClasificacionNeumaticoResponse;
import com.dliriotech.tms.tyreservice.dto.FinalizarInspeccionRequest;
import com.dliriotech.tms.tyreservice.dto.NeumaticoInspeccionadoRequest;
import com.dliriotech.tms.tyreservice.entity.CatalogoServiciosEmpresa;
import com.dliriotech.tms.tyreservice.entity.Equipo;
import com.dliriotech.tms.tyreservice.entity.MovimientoNeumatico;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.exception.EquipoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.InspeccionProcessingException;
import com.dliriotech.tms.tyreservice.exception.NeumaticoNotFoundException;
import com.dliriotech.tms.tyreservice.exception.RtdInvalidIncrementException;
import com.dliriotech.tms.tyreservice.repository.EquipoRepository;
import com.dliriotech.tms.tyreservice.repository.MovimientoNeumaticoRepository;
import com.dliriotech.tms.tyreservice.repository.NeumaticoRepository;
import com.dliriotech.tms.tyreservice.service.InspeccionNeumaticoService;
import com.dliriotech.tms.tyreservice.service.MasterDataCacheService;
import com.dliriotech.tms.tyreservice.service.NeumaticoEntityCacheService;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InspeccionNeumaticoServiceImpl implements InspeccionNeumaticoService {

    private final EquipoRepository equipoRepository;
    private final NeumaticoRepository neumaticoRepository;
    private final MovimientoNeumaticoRepository movimientoNeumaticoRepository;
    private final MasterDataCacheService masterDataCacheService;
    private final NeumaticoEntityCacheService neumaticoEntityCacheService;
    private final ObservacionNeumaticoService observacionNeumaticoService;

    // ID fijo del proveedor de servicio "DLirio Tyre"
    private static final Integer DLIRIO_TYRE_PROVEEDOR_ID = 3;

    @Override
    @Transactional
    public Mono<Void> finalizarInspeccion(Integer equipoId, Integer empresaId, FinalizarInspeccionRequest request) {
        log.info("Iniciando finalización de inspección transaccional para equipo: {}", equipoId);
        // El empresaId del header prevalece sobre el del body
        request.setEmpresaId(empresaId);

        return validarDatosEntrada(equipoId, request)
            .then(ejecutarInspeccionTransaccional(equipoId, empresaId, request))
            .doOnSuccess(result -> log.info("Inspección finalizada exitosamente para equipo: {}", equipoId))
            .onErrorMap(throwable -> {
                if (throwable instanceof InspeccionProcessingException) {
                    return throwable;
                }
                log.error("Error inesperado al finalizar inspección para equipo {}: {}", equipoId, throwable.getMessage(), throwable);
                return new InspeccionProcessingException(
                    equipoId.toString(), 
                    "procesamiento_general", 
                    "Error inesperado durante el procesamiento de la inspección", 
                    throwable
                );
            })
            .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                .filter(throwable -> !(throwable instanceof InspeccionProcessingException))
                .doBeforeRetry(retrySignal -> 
                    log.warn("Reintentando operación de inspección para equipo {}, intento: {}", 
                        equipoId, retrySignal.totalRetries() + 1)
                )
            );
    }

    /**
     * Valida los datos de entrada antes de procesar la inspección
     */
    private Mono<Void> validarDatosEntrada(Integer equipoId, FinalizarInspeccionRequest request) {
        return Mono.fromRunnable(() -> {
            if (equipoId == null || equipoId <= 0) {
                throw new InspeccionProcessingException(
                    String.valueOf(equipoId), 
                    "validacion_entrada", 
                    "El ID del equipo debe ser un número positivo"
                );
            }
            if (request == null) {
                throw new InspeccionProcessingException(
                    equipoId.toString(), 
                    "validacion_entrada", 
                    "Los datos de la inspección son requeridos"
                );
            }
            if (request.getNeumaticosInspeccionados() == null || request.getNeumaticosInspeccionados().isEmpty()) {
                throw new InspeccionProcessingException(
                    equipoId.toString(), 
                    "validacion_entrada", 
                    "Debe incluir al menos un neumático para inspeccionar"
                );
            }
        });
    }

    /**
     * Ejecuta la inspección completa de forma transaccional
     */
    private Mono<Void> ejecutarInspeccionTransaccional(Integer equipoId, Integer empresaId, FinalizarInspeccionRequest request) {
        return validarEquipoExiste(equipoId)
            .then(validarRtdNoIncremento(equipoId, empresaId, request))
            .then(actualizarFechaInspeccionEquipo(equipoId))
            .then(procesarNeumaticosinspeccionados(request, empresaId))
            .onErrorMap(throwable -> {
                if (throwable instanceof InspeccionProcessingException) {
                    return throwable;
                }
                String operacion = determinarOperacionFallida(throwable);
                return new InspeccionProcessingException(
                    equipoId.toString(), 
                    operacion, 
                    throwable.getMessage(), 
                    throwable
                );
            });
    }

    /**
     * Determina qué operación falló basándose en el tipo de excepción
     */
    private String determinarOperacionFallida(Throwable throwable) {
        if (throwable instanceof EquipoNotFoundException) {
            return "validacion_equipo";
        } else if (throwable instanceof NeumaticoNotFoundException) {
            return "validacion_neumatico";
        } else if (throwable instanceof RtdInvalidIncrementException) {
            return "validacion_rtd_incremento";
        } else if (throwable.getMessage() != null) {
            if (throwable.getMessage().contains("fecha_inspeccion")) {
                return "actualizacion_fecha_inspeccion";
            } else if (throwable.getMessage().contains("rtd")) {
                return "actualizacion_mediciones_rtd";
            } else if (throwable.getMessage().contains("movimiento")) {
                return "creacion_movimiento";
            } else if (throwable.getMessage().contains("observacion")) {
                return "procesamiento_observaciones";
            }
        }
        return "operacion_desconocida";
    }

    private Mono<Void> validarEquipoExiste(Integer equipoId) {
        return masterDataCacheService.getEquipo(equipoId)
            .switchIfEmpty(Mono.error(new EquipoNotFoundException(equipoId.toString())))
            .then();
    }

    /**
     * Valida que ningún RTD actual calculado sea mayor al RTD actual existente.
     * Regla de negocio: El RTD (profundidad de rodadura) solo puede disminuir con el uso, nunca aumentar.
     */
    private Mono<Void> validarRtdNoIncremento(Integer equipoId, Integer empresaId, FinalizarInspeccionRequest request) {
        log.debug("Validando que RTD no incremente para equipo: {}", equipoId);
        
        // Obtener todos los neumáticos del equipo filtrados por empresa (tenant isolation)
        return neumaticoRepository.getAllByEquipoIdAndEmpresaIdOrderByPosicionDesc(equipoId, empresaId)
            .collectMap(Neumatico::getId) // Crear un mapa por ID para búsqueda eficiente
            .flatMap(neumaticosExistentes -> {
                // Validar cada neumático inspeccionado
                return Flux.fromIterable(request.getNeumaticosInspeccionados())
                    .flatMap(neumaticoInspeccionado -> {
                        Integer neumaticoId = neumaticoInspeccionado.getNeumaticoId();
                        Neumatico neumaticoExistente = neumaticosExistentes.get(neumaticoId);
                        
                        if (neumaticoExistente == null) {
                            return Mono.error(new NeumaticoNotFoundException(neumaticoId.toString()));
                        }
                        
                        BigDecimal rtdActualExistente = neumaticoExistente.getRtdActual();
                        BigDecimal rtdActualCalculado = calcularRtdActual(
                            neumaticoInspeccionado.getRtd1(),
                            neumaticoInspeccionado.getRtd2(),
                            neumaticoInspeccionado.getRtd3()
                        );
                        
                        // Validar que el RTD calculado no sea mayor al existente
                        if (rtdActualExistente != null && rtdActualCalculado.compareTo(rtdActualExistente) > 0) {
                            log.warn("RTD inválido detectado para neumático {}: existente={}, calculado={}", 
                                neumaticoId, rtdActualExistente, rtdActualCalculado);
                            return Mono.error(new RtdInvalidIncrementException(
                                neumaticoId, rtdActualExistente, rtdActualCalculado));
                        }
                        
                        log.debug("RTD válido para neumático {}: existente={}, calculado={}", 
                            neumaticoId, rtdActualExistente, rtdActualCalculado);
                        return Mono.empty();
                    })
                    .then();
            })
            .doOnSuccess(result -> log.debug("Validación de RTD completada exitosamente para equipo: {}", equipoId));
    }

    private Mono<Void> actualizarFechaInspeccionEquipo(Integer equipoId) {
        LocalDate fechaInspeccion = LocalDate.now(ZoneId.of("America/Lima"));
        log.debug("Actualizando fecha de inspección para equipo: {} a fecha: {}", equipoId, fechaInspeccion);
        
        return equipoRepository.updateFechaInspeccion(equipoId, fechaInspeccion)
            .doOnNext(rowsUpdated -> log.debug("Filas actualizadas en equipos: {}", rowsUpdated))
            .flatMap(rowsUpdated -> {
                if (rowsUpdated > 0) {
                    return Mono.just(rowsUpdated);
                } else {
                    // Verificamos si el equipo existe antes de lanzar error
                    return equipoRepository.findById(equipoId)
                        .flatMap(equipo -> {
                            log.error("El equipo {} existe pero no se pudo actualizar su fecha de inspección", equipoId);
                            return Mono.<Integer>error(new InspeccionProcessingException(
                                equipoId.toString(),
                                "actualizacion_fecha_inspeccion",
                                "El equipo existe pero no se pudo actualizar su fecha de inspección. Posible problema de permisos o bloqueo de base de datos."
                            ));
                        })
                        .switchIfEmpty(Mono.error(new EquipoNotFoundException(equipoId.toString())));
                }
            })
            .doOnSuccess(result -> log.debug("Fecha de inspección actualizada para equipo: {}", equipoId))
            .then()
            .onErrorMap(throwable -> {
                if (throwable instanceof InspeccionProcessingException || throwable instanceof EquipoNotFoundException) {
                    return throwable;
                }
                log.error("Error inesperado al actualizar fecha de inspección para equipo {}: {}", equipoId, throwable.getMessage(), throwable);
                return new InspeccionProcessingException(
                    equipoId.toString(),
                    "actualizacion_fecha_inspeccion",
                    "Error al actualizar fecha de inspección: " + throwable.getMessage(),
                    throwable
                );
            });
    }

    private Mono<Void> procesarNeumaticosinspeccionados(FinalizarInspeccionRequest request, Integer empresaId) {
        return Flux.fromIterable(request.getNeumaticosInspeccionados())
            .flatMap(neumatico -> procesarNeumaticoIndividual(neumatico, request, empresaId))
            .then();
    }

    private Mono<Void> procesarNeumaticoIndividual(NeumaticoInspeccionadoRequest neumaticoRequest,
                                                   FinalizarInspeccionRequest request,
                                                   Integer empresaId) {
        Integer neumaticoId = neumaticoRequest.getNeumaticoId();
        log.debug("Procesando neumático individual: {}", neumaticoId);

        return validarNeumaticoExiste(neumaticoId, empresaId)
            .then(actualizarMedicionesRtdNeumatico(neumaticoRequest, empresaId))
            .then(crearMovimientoInspeccion(neumaticoRequest, request, empresaId))
            .then(procesarObservaciones(neumaticoRequest, request))
            .doOnSuccess(result -> log.debug("Neumático {} procesado exitosamente", neumaticoId))
            .onErrorMap(throwable -> {
                log.error("Error procesando neumático {}: {}", neumaticoId, throwable.getMessage(), throwable);
                if (throwable instanceof InspeccionProcessingException) {
                    return throwable;
                }
                return new InspeccionProcessingException(
                    request.getEquipoId().toString(),
                    "procesamiento_neumatico_" + neumaticoId,
                    String.format("Error procesando neumático %d: %s", neumaticoId, throwable.getMessage()),
                    throwable
                );
            });
    }

    private Mono<Neumatico> validarNeumaticoExiste(Integer neumaticoId, Integer empresaId) {
        return neumaticoRepository.findByIdAndEmpresaId(neumaticoId, empresaId)
            .switchIfEmpty(Mono.error(new NeumaticoNotFoundException(neumaticoId.toString())));
    }

    private Mono<Void> actualizarMedicionesRtdNeumatico(NeumaticoInspeccionadoRequest request, Integer empresaId) {
        BigDecimal rtdActual = calcularRtdActual(request.getRtd1(), request.getRtd2(), request.getRtd3());
        Integer neumaticoId = request.getNeumaticoId();

        log.debug("Actualizando mediciones RTD para neumático: {} - RTD1: {}, RTD2: {}, RTD3: {}, RTD Actual: {}",
            neumaticoId, request.getRtd1(), request.getRtd2(), request.getRtd3(), rtdActual);

        return neumaticoRepository.updateRtdMeasurements(
                neumaticoId,
                empresaId,
                request.getRtd1(),
                request.getRtd2(),
                request.getRtd3(),
                rtdActual
            )
            .doOnNext(rowsUpdated -> log.debug("Filas actualizadas en neumáticos para ID {}: {}", neumaticoId, rowsUpdated))
            .flatMap(rowsUpdated -> {
                if (rowsUpdated > 0) {
                    return Mono.just(rowsUpdated);
                } else {
                    // Verificamos si el neumático existe antes de lanzar error
                    return neumaticoRepository.findById(neumaticoId)
                        .flatMap(neumatico -> {
                            log.error("El neumático {} existe pero no se pudieron actualizar sus mediciones RTD. RTD actuales: RTD1={}, RTD2={}, RTD3={}, RTD_Actual={}", 
                                neumaticoId, neumatico.getRtd1(), neumatico.getRtd2(), neumatico.getRtd3(), neumatico.getRtdActual());
                            return Mono.<Integer>error(new InspeccionProcessingException(
                                neumaticoId.toString(),
                                "actualizacion_mediciones_rtd",
                                String.format("El neumático %d existe pero no se pudieron actualizar sus mediciones RTD. Posible problema de permisos o bloqueo de base de datos.", neumaticoId)
                            ));
                        })
                        .switchIfEmpty(Mono.error(new NeumaticoNotFoundException(neumaticoId.toString())));
                }
            })
            .doOnSuccess(result -> log.debug("Mediciones RTD actualizadas para neumático: {}", neumaticoId))
            .then()
            .onErrorMap(throwable -> {
                if (throwable instanceof InspeccionProcessingException || throwable instanceof NeumaticoNotFoundException) {
                    return throwable;
                }
                log.error("Error inesperado al actualizar mediciones RTD para neumático {}: {}", neumaticoId, throwable.getMessage(), throwable);
                return new InspeccionProcessingException(
                    neumaticoId.toString(),
                    "actualizacion_mediciones_rtd",
                    "Error al actualizar mediciones RTD: " + throwable.getMessage(),
                    throwable
                );
            });
    }

    private BigDecimal calcularRtdActual(BigDecimal rtd1, BigDecimal rtd2, BigDecimal rtd3) {
        return rtd1.min(rtd2).min(rtd3);
    }

    private Mono<Void> crearMovimientoInspeccion(NeumaticoInspeccionadoRequest neumaticoRequest,
                                                FinalizarInspeccionRequest request,
                                                Integer empresaId) {
        return validarNeumaticoExiste(neumaticoRequest.getNeumaticoId(), empresaId)
            .flatMap(neumatico -> construirMovimientoInspeccion(neumatico, neumaticoRequest, request))
            .flatMap(movimientoNeumaticoRepository::save)
            .doOnSuccess(movimiento -> log.debug("Movimiento de inspección creado con ID: {}", movimiento.getId()))
            .then();
    }

    private Mono<MovimientoNeumatico> construirMovimientoInspeccion(Neumatico neumatico,
                                                                   NeumaticoInspeccionadoRequest neumaticoRequest,
                                                                   FinalizarInspeccionRequest request) {
        return Mono.zip(
                obtenerClasificacionRodando(),
                obtenerTipoMovimientoInspeccion(),
                obtenerKilometraje(request),
                obtenerCostoMovimiento(request)
            )
            .map(tuple -> {
                Integer clasificacionId = tuple.getT1();
                Integer tipoMovimientoId = tuple.getT2();
                Integer kilometraje = tuple.getT3();
                BigDecimal costoMovimiento = tuple.getT4();
                
                return MovimientoNeumatico.builder()
                    .neumaticoId(neumaticoRequest.getNeumaticoId())
                    .fechaMovimiento(LocalDateTime.now())
                    .equipoOrigenId(request.getEquipoId())
                    .equipoDestinoId(request.getEquipoId())
                    .posicionOrigen(neumatico.getPosicion())
                    .posicionDestino(neumatico.getPosicion())
                    .clasificacionOrigenId(clasificacionId)
                    .clasificacionDestinoId(clasificacionId)
                    .tipoMovimientoId(tipoMovimientoId)
                    .kilometraje(kilometraje)
                    .rtd1(neumaticoRequest.getRtd1())
                    .rtd2(neumaticoRequest.getRtd2())
                    .rtd3(neumaticoRequest.getRtd3())
                    .rtdActual(calcularRtdActual(neumaticoRequest.getRtd1(), neumaticoRequest.getRtd2(), neumaticoRequest.getRtd3()))
                    .rtdPostReencauche(null) // NULL para inspecciones
                    .usuarioId(request.getUsuarioId())
                    .costoMovimiento(costoMovimiento)
                    .proveedorServicioId(DLIRIO_TYRE_PROVEEDOR_ID)
                    .comentario(null) // Vacío para inspecciones
                    .build();
            });
    }

    private Mono<Integer> obtenerClasificacionRodando() {
        return neumaticoEntityCacheService.getClasificacionNeumatico(1) // Asumiendo que ID 1 es "Rodando"
            .map(ClasificacionNeumaticoResponse::getId)
            .doOnError(error -> log.error("Error al obtener clasificación 'Rodando': {}", error.getMessage()));
    }

    private Mono<Integer> obtenerTipoMovimientoInspeccion() {
        return masterDataCacheService.getTipoMovimientoIdByNombre(TipoMovimientoConstants.INSPECCION_PERIODICA)
            .doOnError(error -> log.error("Error al obtener tipo de movimiento 'Inspección Periódica': {}", error.getMessage()));
    }

    private Mono<Integer> obtenerKilometraje(FinalizarInspeccionRequest request) {
        if (request.getKilometraje() != null) {
            return Mono.just(request.getKilometraje());
        }
        
        return masterDataCacheService.getEquipo(request.getEquipoId())
            .map(Equipo::getKilometraje)
            .switchIfEmpty(Mono.just(0)); // Valor por defecto si no hay kilometraje
    }

    private Mono<BigDecimal> obtenerCostoMovimiento(FinalizarInspeccionRequest request) {
        return obtenerTipoMovimientoInspeccion()
            .flatMap(tipoMovimientoId ->
                masterDataCacheService.getCatalogoServiciosEmpresa(
                    request.getEmpresaId(),
                    request.getTipoEquipoId(),
                    tipoMovimientoId
                ))
            .map(CatalogoServiciosEmpresa::getCostoServicio)
            .onErrorReturn(BigDecimal.ZERO) // Costo 0 si no se encuentra configuración
            .doOnNext(costo -> log.info("Costo de movimiento obtenido: {}", costo));
    }

    private Mono<Void> procesarObservaciones(NeumaticoInspeccionadoRequest neumaticoRequest, 
                                           FinalizarInspeccionRequest request) {
        if (neumaticoRequest.getObservacionNeumaticoList() == null || 
            neumaticoRequest.getObservacionNeumaticoList().isEmpty()) {
            return Mono.empty();
        }

        return Flux.fromIterable(neumaticoRequest.getObservacionNeumaticoList())
            .flatMap(observacion -> {
                // Siempre inyectados desde el contexto de seguridad (header X-User-Id y path /{equipoId})
                observacion.setUsuarioCreacionId(request.getUsuarioId());
                observacion.setEquipoId(request.getEquipoId());
                return observacionNeumaticoService.saveObservacion(observacion);
            })
            .then()
            .doOnSuccess(result -> log.debug("Observaciones procesadas para neumático: {}", neumaticoRequest.getNeumaticoId()));
    }
}