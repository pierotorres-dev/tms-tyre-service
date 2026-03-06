package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.entity.*;
import com.dliriotech.tms.tyreservice.exception.NeumaticoException;
import com.dliriotech.tms.tyreservice.exception.ValidationException;
import com.dliriotech.tms.tyreservice.exception.DataIntegrityException;
import com.dliriotech.tms.tyreservice.exception.ErrorCode;
import com.dliriotech.tms.tyreservice.exception.PosicionAlreadyOccupiedException;
import com.dliriotech.tms.tyreservice.exception.NeumaticoNotFoundException;
import com.dliriotech.tms.tyreservice.repository.*;
import com.dliriotech.tms.tyreservice.service.NeumaticoService;
import com.dliriotech.tms.tyreservice.service.RtdThresholdService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NeumaticoServiceImpl implements NeumaticoService {

    private final NeumaticoRepository neumaticoRepository;
    private final CatalogoNeumaticoRepository catalogoNeumaticoRepository;
    private final ProveedorRepository proveedorRepository;
    private final DisenoReencaucheRepository disenoReencaucheRepository;
    private final ClasificacionNeumaticoRepository clasificacionNeumaticoRepository;
    private final MarcaNeumaticoRepository marcaNeumaticoRepository;
    private final MedidaNeumaticoRepository medidaNeumaticoRepository;
    private final RtdThresholdService rtdThresholdService;

    @Override
    @Transactional(readOnly = true)
    public List<NeumaticoResponse> getAllNeumaticosByEquipoId(Integer equipoId, Integer empresaId) {
        if (equipoId == null || equipoId <= 0) {
            throw new ValidationException("equipoId", "debe ser un número positivo válido");
        }

        log.info("Consultando neumáticos para equipo {} empresa {}", equipoId, empresaId);

        // 1 solo query con JOIN FETCH: neumaticos + catalogo + marca + medida + proveedor + diseno + clasificacion
        List<Neumatico> neumaticos = neumaticoRepository
                .findAllByEquipoWithRelations(equipoId, empresaId);

        if (neumaticos.isEmpty()) {
            return List.of();
        }

        // Batch RTD thresholds: 1 query equipo + 1 query config + 1 query movimientos (vs N*3 antes)
        Map<Integer, BigDecimal> rtdOriginalMap = new HashMap<>();
        for (Neumatico n : neumaticos) {
            CatalogoNeumatico catalogo = n.getCatalogoNeumatico();
            rtdOriginalMap.put(n.getId(), catalogo != null ? catalogo.getRtdOriginal() : BigDecimal.ZERO);
        }

        Map<Integer, RtdThresholdsResponse> rtdThresholdsMap = rtdThresholdService
                .calculateRtdThresholdsBatch(neumaticos, rtdOriginalMap);

        return neumaticos.stream()
                .map(n -> mapToResponseWithPreloadedData(n, rtdThresholdsMap.get(n.getId())))
                .toList();
    }

    @Override
    @Transactional
    public NeumaticoResponse saveNeumatico(NeumaticoRequest request, Integer empresaId) {
        request.setEmpresaId(empresaId);
        validateNeumaticoRequest(request);
        validatePosicionAvailability(request.getEquipoId(), request.getPosicion());

        log.info("Guardando nuevo neumático");

        try {
            Neumatico entity = mapRequestToEntity(request);
            Neumatico saved = neumaticoRepository.save(entity);
            log.info("Neumático guardado exitosamente: {}", saved.getId());
            return enrichNeumaticoWithRelations(saved);
        } catch (DataIntegrityViolationException ex) {
            throw handleDataIntegrityViolation(ex, request);
        }
    }

    @Override
    @Transactional
    public NeumaticoResponse updateNeumatico(Integer id, NeumaticoRequest request, Integer empresaId) {
        if (id == null || id <= 0) {
            throw new ValidationException("id", "debe ser un número positivo válido");
        }
        request.setEmpresaId(empresaId);
        validateNeumaticoRequest(request);

        log.info("Actualizando neumático {}", id);

        Neumatico existingNeumatico = neumaticoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new NeumaticoNotFoundException(id.toString()));

        // Solo validar posición si está cambiando
        if (!existingNeumatico.getEquipoId().equals(request.getEquipoId())
                || !existingNeumatico.getPosicion().equals(request.getPosicion())) {
            validatePosicionAvailabilityForUpdate(request.getEquipoId(), request.getPosicion(), id);
        }

        // Actualizar campos
        existingNeumatico.setEmpresaId(request.getEmpresaId());
        existingNeumatico.setCatalogoNeumatico(catalogoNeumaticoRepository.getReferenceById(request.getCatalogoNeumaticoId()));
        existingNeumatico.setEquipoId(request.getEquipoId());
        existingNeumatico.setPosicion(request.getPosicion());
        existingNeumatico.setSerieCodigo(request.getSerieCodigo());
        existingNeumatico.setCostoInicial(request.getCostoInicial());
        existingNeumatico.setProveedorCompra(request.getProveedorCompraId() != null
                ? proveedorRepository.getReferenceById(request.getProveedorCompraId()) : null);
        existingNeumatico.setKmInstalacion(request.getKmInstalacion());
        existingNeumatico.setFechaInstalacion(request.getFechaInstalacion());
        existingNeumatico.setRtd1(request.getRtd1());
        existingNeumatico.setRtd2(request.getRtd2());
        existingNeumatico.setRtd3(request.getRtd3());
        existingNeumatico.setRtdActual(request.getRtdActual());
        existingNeumatico.setKmAcumulados(request.getKmAcumulados());
        existingNeumatico.setNumeroReencauches(request.getNumeroReencauches());
        existingNeumatico.setDisenoReencaucheActual(request.getDisenoReencaucheActualId() != null
                ? disenoReencaucheRepository.getReferenceById(request.getDisenoReencaucheActualId()) : null);
        existingNeumatico.setClasificacion(request.getClasificacionId() != null
                ? clasificacionNeumaticoRepository.getReferenceById(request.getClasificacionId()) : null);

        try {
            Neumatico saved = neumaticoRepository.save(existingNeumatico);
            log.info("Neumático {} actualizado exitosamente", id);
            return enrichNeumaticoWithRelations(saved);
        } catch (DataIntegrityViolationException ex) {
            throw handleDataIntegrityViolation(ex, request);
        }
    }

    // ── Validaciones ──────────────────────────────────────────────────

    private void validateNeumaticoRequest(NeumaticoRequest request) {
        if (request == null) {
            throw new ValidationException("La solicitud no puede ser nula");
        }
        if (request.getCatalogoNeumaticoId() == null || request.getCatalogoNeumaticoId() <= 0) {
            throw new ValidationException("catalogoNeumaticoId", "debe ser un número positivo válido");
        }
        if (request.getEquipoId() == null || request.getEquipoId() <= 0) {
            throw new ValidationException("equipoId", "debe ser un número positivo válido");
        }
        if (request.getSerieCodigo() == null || request.getSerieCodigo().trim().isEmpty()) {
            throw new ValidationException("serieCodigo", "no puede estar vacío");
        }
        if (request.getPosicion() == null || request.getPosicion() <= 0) {
            throw new ValidationException("posicion", "debe ser un número positivo válido");
        }
    }

    private void validatePosicionAvailability(Integer equipoId, Integer posicion) {
        neumaticoRepository.findByEquipoIdAndPosicion(equipoId, posicion)
                .ifPresent(existing -> {
                    throw new PosicionAlreadyOccupiedException(equipoId, posicion, existing.getSerieCodigo());
                });
    }

    private void validatePosicionAvailabilityForUpdate(Integer equipoId, Integer posicion, Integer excludeId) {
        neumaticoRepository.findByEquipoIdAndPosicionAndIdNot(equipoId, posicion, excludeId)
                .ifPresent(existing -> {
                    throw new PosicionAlreadyOccupiedException(equipoId, posicion, existing.getSerieCodigo());
                });
    }

    // ── Enrichment ────────────────────────────────────────────────────

    /**
     * Mapea un neumático a NeumaticoResponse usando las relaciones JPA ya cargadas por JOIN FETCH.
     * NO ejecuta queries adicionales — todo ya está en memoria.
     * Usado por getAllNeumaticosByEquipoId (flujo optimizado de lista).
     */
    private NeumaticoResponse mapToResponseWithPreloadedData(Neumatico neumatico,
                                                              RtdThresholdsResponse rtdThresholds) {
        CatalogoNeumaticoResponse catalogoResponse = buildCatalogoResponse(neumatico.getCatalogoNeumatico());

        ProveedorResponse proveedorResponse = neumatico.getProveedorCompra() != null
                ? ProveedorResponse.builder()
                    .id(neumatico.getProveedorCompra().getId())
                    .nombre(neumatico.getProveedorCompra().getNombre())
                    .tipo(neumatico.getProveedorCompra().getTipo())
                    .ruc(neumatico.getProveedorCompra().getRuc())
                    .build()
                : null;

        DisenoReencaucheResponse disenoResponse = neumatico.getDisenoReencaucheActual() != null
                ? DisenoReencaucheResponse.builder()
                    .id(neumatico.getDisenoReencaucheActual().getId())
                    .nombreDiseno(neumatico.getDisenoReencaucheActual().getNombreDiseno())
                    .proveedorReencauche(neumatico.getDisenoReencaucheActual().getProveedorReencauche())
                    .build()
                : null;

        ClasificacionNeumaticoResponse clasificacionResponse = neumatico.getClasificacion() != null
                ? ClasificacionNeumaticoResponse.builder()
                    .id(neumatico.getClasificacion().getId())
                    .nombre(neumatico.getClasificacion().getNombre())
                    .descripcion(neumatico.getClasificacion().getDescripcion())
                    .build()
                : null;

        String disenoVigente = buildDisenoVigente(neumatico, disenoResponse, catalogoResponse);
        String estadoReencauche = buildEstadoReencauche(neumatico);

        return buildNeumaticoResponse(neumatico, catalogoResponse, proveedorResponse,
                disenoResponse, clasificacionResponse, rtdThresholds, disenoVigente, estadoReencauche);
    }

    /**
     * Enriquece un neumático individual cargando relaciones desde los objetos JPA o repositorios.
     * Usado por save/update donde se maneja un solo neumático y no se usa JOIN FETCH.
     */
    private NeumaticoResponse enrichNeumaticoWithRelations(Neumatico neumatico) {
        // Resolver catálogo: usar relación JPA si está cargada, sino buscar por ID
        CatalogoNeumatico catalogo = neumatico.getCatalogoNeumatico() != null
                ? neumatico.getCatalogoNeumatico()
                : (neumatico.getCatalogoNeumaticoId() != null
                    ? catalogoNeumaticoRepository.findById(neumatico.getCatalogoNeumaticoId()).orElse(null)
                    : null);

        CatalogoNeumaticoResponse catalogoResponse;
        if (catalogo != null) {
            // Cargar marca y medida si no están ya en la relación
            MarcaNeumaticoResponse marcaResp = null;
            if (catalogo.getMarca() != null) {
                marcaResp = MarcaNeumaticoResponse.builder()
                        .id(catalogo.getMarca().getId()).nombre(catalogo.getMarca().getNombre()).build();
            } else if (catalogo.getMarcaId() != null) {
                marcaResp = marcaNeumaticoRepository.findById(catalogo.getMarcaId())
                        .map(m -> MarcaNeumaticoResponse.builder().id(m.getId()).nombre(m.getNombre()).build())
                        .orElse(null);
            }

            MedidaNeumaticoResponse medidaResp = null;
            if (catalogo.getMedida() != null) {
                medidaResp = MedidaNeumaticoResponse.builder()
                        .id(catalogo.getMedida().getId()).descripcion(catalogo.getMedida().getDescripcion())
                        .tipoConstruccion(catalogo.getMedida().getTipoConstruccion())
                        .indiceCarga(catalogo.getMedida().getIndiceCarga())
                        .simboloVelocidad(catalogo.getMedida().getSimboloVelocidad())
                        .plyRating(catalogo.getMedida().getPlyRating()).build();
            } else if (catalogo.getMedidaId() != null) {
                medidaResp = medidaNeumaticoRepository.findById(catalogo.getMedidaId())
                        .map(m -> MedidaNeumaticoResponse.builder().id(m.getId()).descripcion(m.getDescripcion())
                                .tipoConstruccion(m.getTipoConstruccion()).indiceCarga(m.getIndiceCarga())
                                .simboloVelocidad(m.getSimboloVelocidad()).plyRating(m.getPlyRating()).build())
                        .orElse(null);
            }

            catalogoResponse = CatalogoNeumaticoResponse.builder()
                    .id(catalogo.getId()).marcaNeumaticoResponse(marcaResp).medidaNeumaticoResponse(medidaResp)
                    .modeloDiseno(catalogo.getModeloDiseno()).tipoUso(catalogo.getTipoUso())
                    .rtdOriginal(catalogo.getRtdOriginal()).presionMaximaPsi(catalogo.getPresionMaximaPsi())
                    .treadwear(catalogo.getTreadwear()).traccion(catalogo.getTraccion())
                    .temperatura(catalogo.getTemperatura()).build();
        } else {
            catalogoResponse = CatalogoNeumaticoResponse.builder().build();
        }

        ProveedorResponse proveedorResponse = null;
        if (neumatico.getProveedorCompra() != null) {
            Proveedor p = neumatico.getProveedorCompra();
            proveedorResponse = ProveedorResponse.builder()
                    .id(p.getId()).nombre(p.getNombre()).tipo(p.getTipo()).ruc(p.getRuc()).build();
        } else if (neumatico.getProveedorCompraId() != null) {
            proveedorResponse = proveedorRepository.findById(neumatico.getProveedorCompraId())
                    .map(p -> ProveedorResponse.builder().id(p.getId()).nombre(p.getNombre())
                            .tipo(p.getTipo()).ruc(p.getRuc()).build())
                    .orElse(null);
        }

        DisenoReencaucheResponse disenoResponse = null;
        if (neumatico.getDisenoReencaucheActual() != null) {
            DisenoReencauche d = neumatico.getDisenoReencaucheActual();
            disenoResponse = DisenoReencaucheResponse.builder()
                    .id(d.getId()).nombreDiseno(d.getNombreDiseno())
                    .proveedorReencauche(d.getProveedorReencauche()).build();
        } else if (neumatico.getDisenoReencaucheActualId() != null) {
            disenoResponse = disenoReencaucheRepository.findById(neumatico.getDisenoReencaucheActualId())
                    .map(d -> DisenoReencaucheResponse.builder().id(d.getId()).nombreDiseno(d.getNombreDiseno())
                            .proveedorReencauche(d.getProveedorReencauche()).build())
                    .orElse(null);
        }

        ClasificacionNeumaticoResponse clasificacionResponse = null;
        if (neumatico.getClasificacion() != null) {
            ClasificacionNeumatico c = neumatico.getClasificacion();
            clasificacionResponse = ClasificacionNeumaticoResponse.builder()
                    .id(c.getId()).nombre(c.getNombre()).descripcion(c.getDescripcion()).build();
        } else if (neumatico.getClasificacionId() != null) {
            clasificacionResponse = clasificacionNeumaticoRepository.findById(neumatico.getClasificacionId())
                    .map(c -> ClasificacionNeumaticoResponse.builder().id(c.getId()).nombre(c.getNombre())
                            .descripcion(c.getDescripcion()).build())
                    .orElse(null);
        }

        RtdThresholdsResponse rtdThresholds = rtdThresholdService
                .calculateRtdThresholds(neumatico, catalogoResponse.getRtdOriginal());

        String disenoVigente = buildDisenoVigente(neumatico, disenoResponse, catalogoResponse);
        String estadoReencauche = buildEstadoReencauche(neumatico);

        return buildNeumaticoResponse(neumatico, catalogoResponse, proveedorResponse,
                disenoResponse, clasificacionResponse, rtdThresholds, disenoVigente, estadoReencauche);
    }

    // ── Helpers de Construcción DTO ───────────────────────────────────

    private CatalogoNeumaticoResponse buildCatalogoResponse(CatalogoNeumatico catalogo) {
        if (catalogo == null) {
            return CatalogoNeumaticoResponse.builder().build();
        }

        MarcaNeumaticoResponse marcaResp = catalogo.getMarca() != null
                ? MarcaNeumaticoResponse.builder()
                    .id(catalogo.getMarca().getId())
                    .nombre(catalogo.getMarca().getNombre())
                    .build()
                : null;

        MedidaNeumaticoResponse medidaResp = catalogo.getMedida() != null
                ? MedidaNeumaticoResponse.builder()
                    .id(catalogo.getMedida().getId())
                    .descripcion(catalogo.getMedida().getDescripcion())
                    .tipoConstruccion(catalogo.getMedida().getTipoConstruccion())
                    .indiceCarga(catalogo.getMedida().getIndiceCarga())
                    .simboloVelocidad(catalogo.getMedida().getSimboloVelocidad())
                    .plyRating(catalogo.getMedida().getPlyRating())
                    .build()
                : null;

        return CatalogoNeumaticoResponse.builder()
                .id(catalogo.getId())
                .marcaNeumaticoResponse(marcaResp)
                .medidaNeumaticoResponse(medidaResp)
                .modeloDiseno(catalogo.getModeloDiseno())
                .tipoUso(catalogo.getTipoUso())
                .rtdOriginal(catalogo.getRtdOriginal())
                .presionMaximaPsi(catalogo.getPresionMaximaPsi())
                .treadwear(catalogo.getTreadwear())
                .traccion(catalogo.getTraccion())
                .temperatura(catalogo.getTemperatura())
                .build();
    }

    private String buildDisenoVigente(Neumatico neumatico,
                                       DisenoReencaucheResponse disenoResponse,
                                       CatalogoNeumaticoResponse catalogoResponse) {
        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            return disenoResponse != null ? disenoResponse.getNombreDiseno() : null;
        }
        return catalogoResponse.getModeloDiseno();
    }

    private String buildEstadoReencauche(Neumatico neumatico) {
        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            return "R" + neumatico.getNumeroReencauches();
        }
        return "Nuevo";
    }

    private NeumaticoResponse buildNeumaticoResponse(Neumatico neumatico,
                                                      CatalogoNeumaticoResponse catalogoResponse,
                                                      ProveedorResponse proveedorResponse,
                                                      DisenoReencaucheResponse disenoResponse,
                                                      ClasificacionNeumaticoResponse clasificacionResponse,
                                                      RtdThresholdsResponse rtdThresholds,
                                                      String disenoVigente,
                                                      String estadoReencauche) {
        return NeumaticoResponse.builder()
                .id(neumatico.getId())
                .empresaId(neumatico.getEmpresaId())
                .catalogoNeumaticoResponse(catalogoResponse)
                .equipoId(neumatico.getEquipoId())
                .posicion(neumatico.getPosicion())
                .serieCodigo(neumatico.getSerieCodigo())
                .costoInicial(neumatico.getCostoInicial())
                .proveedorResponse(proveedorResponse)
                .kmInstalacion(neumatico.getKmInstalacion())
                .fechaInstalacion(neumatico.getFechaInstalacion())
                .rtd1(neumatico.getRtd1())
                .rtd2(neumatico.getRtd2())
                .rtd3(neumatico.getRtd3())
                .rtdActual(neumatico.getRtdActual())
                .kmAcumulados(neumatico.getKmAcumulados())
                .kmCicloActual(neumatico.getKmCicloActual())
                .numeroReencauches(neumatico.getNumeroReencauches())
                .disenoReencaucheResponse(disenoResponse)
                .clasificacionNeumaticoResponse(clasificacionResponse)
                .rtdThresholds(rtdThresholds)
                .disenoVigente(disenoVigente)
                .estadoReencauche(estadoReencauche)
                .build();
    }

    // ── Mappers ───────────────────────────────────────────────────────

    private Neumatico mapRequestToEntity(NeumaticoRequest request) {
        Neumatico entity = Neumatico.builder()
                .empresaId(request.getEmpresaId())
                .equipoId(request.getEquipoId())
                .posicion(request.getPosicion())
                .serieCodigo(request.getSerieCodigo())
                .costoInicial(request.getCostoInicial())
                .kmInstalacion(request.getKmInstalacion())
                .fechaInstalacion(request.getFechaInstalacion())
                .rtd1(request.getRtd1())
                .rtd2(request.getRtd2())
                .rtd3(request.getRtd3())
                .rtdActual(request.getRtdActual())
                .kmAcumulados(request.getKmAcumulados())
                .kmCicloActual(request.getKmCicloActual())
                .numeroReencauches(request.getNumeroReencauches())
                .build();

        // Usar referencias JPA (proxy sin query) para las relaciones @ManyToOne
        entity.setCatalogoNeumatico(catalogoNeumaticoRepository.getReferenceById(request.getCatalogoNeumaticoId()));

        if (request.getProveedorCompraId() != null) {
            entity.setProveedorCompra(proveedorRepository.getReferenceById(request.getProveedorCompraId()));
        }
        if (request.getDisenoReencaucheActualId() != null) {
            entity.setDisenoReencaucheActual(disenoReencaucheRepository.getReferenceById(request.getDisenoReencaucheActualId()));
        }
        if (request.getClasificacionId() != null) {
            entity.setClasificacion(clasificacionNeumaticoRepository.getReferenceById(request.getClasificacionId()));
        }

        return entity;
    }

    private RuntimeException handleDataIntegrityViolation(DataIntegrityViolationException ex, NeumaticoRequest request) {
        String errorMessage = ex.getMessage();
        if (errorMessage != null) {
            if (errorMessage.contains("serie_codigo")) {
                return new DataIntegrityException(
                        "Ya existe un neumático con el código de serie '" + request.getSerieCodigo() + "'", ex);
            }
            if (errorMessage.contains("uk_equipo_posicion")) {
                return new PosicionAlreadyOccupiedException(request.getEquipoId(), request.getPosicion());
            }
            if (errorMessage.contains("Duplicate entry")) {
                return new DataIntegrityException("Ya existe un registro con los datos proporcionados", ex);
            }
            if (errorMessage.contains("foreign key constraint") || errorMessage.contains("Cannot add or update")) {
                if (errorMessage.contains("catalogo_neumatico")) {
                    return new ValidationException("catalogoNeumaticoId", "no existe en el catálogo de neumáticos");
                }
                if (errorMessage.contains("proveedor")) {
                    return new ValidationException("proveedorCompraId", "no existe en el registro de proveedores");
                }
                if (errorMessage.contains("clasificacion")) {
                    return new ValidationException("clasificacionId", "no existe en las clasificaciones de neumáticos");
                }
                if (errorMessage.contains("diseno_reencauche")) {
                    return new ValidationException("disenoReencaucheActualId", "no existe en los diseños de reencauche");
                }
                return new DataIntegrityException("Referencia inválida a un registro relacionado", ex);
            }
        }
        return new NeumaticoException(ErrorCode.NEUMATICO_SAVE_ERROR, "Error al guardar neumático", ex);
    }
}

