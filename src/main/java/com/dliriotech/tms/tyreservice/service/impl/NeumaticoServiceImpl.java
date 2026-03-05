package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
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

import java.util.List;
import java.util.stream.Collectors;

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

        return neumaticoRepository.findAllByEquipoIdAndEmpresaIdOrderByPosicionDesc(equipoId, empresaId)
                .stream()
                .map(this::enrichNeumaticoWithRelations)
                .collect(Collectors.toList());
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
        existingNeumatico.setCatalogoNeumaticoId(request.getCatalogoNeumaticoId());
        existingNeumatico.setEquipoId(request.getEquipoId());
        existingNeumatico.setPosicion(request.getPosicion());
        existingNeumatico.setSerieCodigo(request.getSerieCodigo());
        existingNeumatico.setCostoInicial(request.getCostoInicial());
        existingNeumatico.setProveedorCompraId(request.getProveedorCompraId());
        existingNeumatico.setKmInstalacion(request.getKmInstalacion());
        existingNeumatico.setFechaInstalacion(request.getFechaInstalacion());
        existingNeumatico.setRtd1(request.getRtd1());
        existingNeumatico.setRtd2(request.getRtd2());
        existingNeumatico.setRtd3(request.getRtd3());
        existingNeumatico.setRtdActual(request.getRtdActual());
        existingNeumatico.setKmAcumulados(request.getKmAcumulados());
        existingNeumatico.setNumeroReencauches(request.getNumeroReencauches());
        existingNeumatico.setDisenoReencaucheActualId(request.getDisenoReencaucheActualId());
        existingNeumatico.setClasificacionId(request.getClasificacionId());

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

    private NeumaticoResponse enrichNeumaticoWithRelations(Neumatico neumatico) {
        CatalogoNeumaticoResponse catalogoResponse = catalogoNeumaticoRepository
                .findById(neumatico.getCatalogoNeumaticoId())
                .map(catalogo -> {
                    MarcaNeumaticoResponse marcaResp = marcaNeumaticoRepository.findById(catalogo.getMarcaId())
                            .map(m -> MarcaNeumaticoResponse.builder().id(m.getId()).nombre(m.getNombre()).build())
                            .orElse(null);
                    MedidaNeumaticoResponse medidaResp = medidaNeumaticoRepository.findById(catalogo.getMedidaId())
                            .map(m -> MedidaNeumaticoResponse.builder().id(m.getId()).descripcion(m.getDescripcion())
                                    .tipoConstruccion(m.getTipoConstruccion()).indiceCarga(m.getIndiceCarga())
                                    .simboloVelocidad(m.getSimboloVelocidad()).plyRating(m.getPlyRating()).build())
                            .orElse(null);
                    return CatalogoNeumaticoResponse.builder()
                            .id(catalogo.getId()).marcaNeumaticoResponse(marcaResp).medidaNeumaticoResponse(medidaResp)
                            .modeloDiseno(catalogo.getModeloDiseno()).tipoUso(catalogo.getTipoUso())
                            .rtdOriginal(catalogo.getRtdOriginal()).presionMaximaPsi(catalogo.getPresionMaximaPsi())
                            .treadwear(catalogo.getTreadwear()).traccion(catalogo.getTraccion())
                            .temperatura(catalogo.getTemperatura()).build();
                })
                .orElse(CatalogoNeumaticoResponse.builder().build());

        ProveedorResponse proveedorResponse = neumatico.getProveedorCompraId() != null
                ? proveedorRepository.findById(neumatico.getProveedorCompraId())
                    .map(p -> ProveedorResponse.builder().id(p.getId()).nombre(p.getNombre())
                            .tipo(p.getTipo()).ruc(p.getRuc()).build())
                    .orElse(null)
                : null;

        DisenoReencaucheResponse disenoResponse = neumatico.getDisenoReencaucheActualId() != null
                ? disenoReencaucheRepository.findById(neumatico.getDisenoReencaucheActualId())
                    .map(d -> DisenoReencaucheResponse.builder().id(d.getId()).nombreDiseno(d.getNombreDiseno())
                            .proveedorReencauche(d.getProveedorReencauche()).build())
                    .orElse(null)
                : null;

        ClasificacionNeumaticoResponse clasificacionResponse = neumatico.getClasificacionId() != null
                ? clasificacionNeumaticoRepository.findById(neumatico.getClasificacionId())
                    .map(c -> ClasificacionNeumaticoResponse.builder().id(c.getId()).nombre(c.getNombre())
                            .descripcion(c.getDescripcion()).build())
                    .orElse(null)
                : null;

        RtdThresholdsResponse rtdThresholds = rtdThresholdService
                .calculateRtdThresholds(neumatico, catalogoResponse.getRtdOriginal());

        // Lógica de diseño vigente y estado reencauche
        String disenoVigente;
        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            disenoVigente = disenoResponse != null ? disenoResponse.getNombreDiseno() : null;
        } else {
            disenoVigente = catalogoResponse.getModeloDiseno();
        }

        String estadoReencauche = "Nuevo";
        if (neumatico.getNumeroReencauches() != null && neumatico.getNumeroReencauches() > 0) {
            estadoReencauche = "R" + neumatico.getNumeroReencauches();
        }

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
        return Neumatico.builder()
                .empresaId(request.getEmpresaId())
                .catalogoNeumaticoId(request.getCatalogoNeumaticoId())
                .equipoId(request.getEquipoId())
                .posicion(request.getPosicion())
                .serieCodigo(request.getSerieCodigo())
                .costoInicial(request.getCostoInicial())
                .proveedorCompraId(request.getProveedorCompraId())
                .kmInstalacion(request.getKmInstalacion())
                .fechaInstalacion(request.getFechaInstalacion())
                .rtd1(request.getRtd1())
                .rtd2(request.getRtd2())
                .rtd3(request.getRtd3())
                .rtdActual(request.getRtdActual())
                .kmAcumulados(request.getKmAcumulados())
                .kmCicloActual(request.getKmCicloActual())
                .numeroReencauches(request.getNumeroReencauches())
                .disenoReencaucheActualId(request.getDisenoReencaucheActualId())
                .clasificacionId(request.getClasificacionId())
                .build();
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

