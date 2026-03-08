package com.dliriotech.tms.tyreservice.service.impl;

import com.dliriotech.tms.tyreservice.constants.EstadoObservacionConstants;
import com.dliriotech.tms.tyreservice.constants.HeaderConstants;
import com.dliriotech.tms.tyreservice.dto.*;
import com.dliriotech.tms.tyreservice.entity.Neumatico;
import com.dliriotech.tms.tyreservice.entity.ObservacionNeumatico;
import com.dliriotech.tms.tyreservice.exception.ObservacionCreationException;
import com.dliriotech.tms.tyreservice.exception.ObservacionProcessingException;
import com.dliriotech.tms.tyreservice.exception.ObservacionUpdateException;
import com.dliriotech.tms.tyreservice.repository.*;
import com.dliriotech.tms.tyreservice.service.ObservacionNeumaticoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ObservacionNeumaticoServiceImpl implements ObservacionNeumaticoService {

    private final MapaObservacionSolucionRepository mapaObservacionSolucionRepository;
    private final ObservacionNeumaticoRepository observacionNeumaticoRepository;
    private final TipoObservacionRepository tipoObservacionRepository;
    private final AuthUserRepository authUserRepository;
    private final NeumaticoRepository neumaticoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoIdAndTipoMovimientoId(
            Integer neumaticoId, Integer tipoMovimientoId, Integer empresaId) {
        log.info("Obteniendo observaciones solucionables para neumático {} y tipo movimiento {}", neumaticoId, tipoMovimientoId);

        if (neumaticoId == null || neumaticoId <= 0) {
            throw ObservacionProcessingException.invalidNeumaticoId(neumaticoId);
        }
        if (tipoMovimientoId == null || tipoMovimientoId <= 0) {
            throw ObservacionProcessingException.invalidTipoMovimiento(tipoMovimientoId);
        }

        List<Integer> tipoObservacionIds = mapaObservacionSolucionRepository
                .findTipoObservacionIdsByTipoMovimientoId(tipoMovimientoId);
        if (tipoObservacionIds.isEmpty()) {
            return List.of();
        }

        return observacionNeumaticoRepository
                .findByNeumaticoIdAndEmpresaIdAndTipoObservacionIdsAndEstadoObservacionId(
                        neumaticoId, empresaId, tipoObservacionIds, EstadoObservacionConstants.ID_PENDIENTE)
                .stream()
                .map(this::enrichObservacionWithRelations)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionNeumaticoResponse> getAllObservacionesByNeumaticoId(Integer neumaticoId, Integer empresaId) {
        log.info("Obteniendo todas las observaciones para neumático {}", neumaticoId);

        if (neumaticoId == null || neumaticoId <= 0) {
            throw ObservacionProcessingException.invalidNeumaticoId(neumaticoId);
        }

        return observacionNeumaticoRepository
                .findByNeumaticoIdAndEmpresaIdOrderByFechaCreacionDesc(neumaticoId, empresaId)
                .stream()
                .map(this::enrichObservacionWithRelations)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByNeumaticoId(Integer neumaticoId, Integer empresaId) {
        log.info("Obteniendo observaciones pendientes para neumático {}", neumaticoId);

        if (neumaticoId == null || neumaticoId <= 0) {
            throw ObservacionProcessingException.invalidNeumaticoId(neumaticoId);
        }

        return observacionNeumaticoRepository
                .findByNeumaticoIdAndEmpresaIdAndEstadoObservacionIdOrderByFechaCreacionDesc(
                        neumaticoId, empresaId, EstadoObservacionConstants.ID_PENDIENTE)
                .stream()
                .map(this::enrichObservacionWithRelations)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObservacionNeumaticoResponse> getAllObservacionesPendientesAndByEquipoId(Integer equipoId, Integer empresaId) {
        log.info("Obteniendo observaciones pendientes de neumáticos del equipo {}", equipoId);

        if (equipoId == null || equipoId <= 0) {
            throw ObservacionProcessingException.invalidEquipoId(equipoId);
        }

        return observacionNeumaticoRepository
                .findByEquipoIdAndEmpresaIdAndEstadoObservacionId(equipoId, empresaId, EstadoObservacionConstants.ID_PENDIENTE)
                .stream()
                .map(this::enrichObservacionWithRelations)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ObservacionNeumaticoResponse saveObservacion(ObservacionNeumaticoNuevoRequest request) {
        log.info("Creando nueva observación para neumático {}", request.getNeumaticoId());

        validateObservacionRequest(request);

        // Validar que el tipo de observación existe
        tipoObservacionRepository.findById(request.getTipoObservacionId())
                .orElseThrow(() -> ObservacionCreationException.tipoObservacionNotFound(request.getTipoObservacionId()));

        ObservacionNeumatico entity = buildObservacionEntity(request);
        ObservacionNeumatico saved = observacionNeumaticoRepository.save(entity);

        log.info("Observación creada exitosamente con ID: {}", saved.getId());
        return enrichObservacionWithRelations(saved);
    }

    @Override
    @Transactional
    public ObservacionNeumaticoResponse updateObservacion(Integer id, ObservacionNeumaticoUpdateRequest request, Integer empresaId) {
        log.info("Actualizando observación con ID: {}", id);

        validateUpdateRequest(id, request);

        // Buscar la observación y verificar que pertenece a la empresa
        ObservacionNeumatico existingObservacion = observacionNeumaticoRepository.findById(id)
                .orElseThrow(() -> ObservacionUpdateException.notFound(id));

        Neumatico neumatico = neumaticoRepository.findById(existingObservacion.getNeumaticoId()).orElse(null);
        if (neumatico == null || !empresaId.equals(neumatico.getEmpresaId())) {
            throw ObservacionUpdateException.notFound(id);
        }

        // Validar que el nuevo estado es un ID conocido (si se está actualizando)
        if (request.getEstadoObservacionId() != null) {
            int newId = request.getEstadoObservacionId();
            if (newId != EstadoObservacionConstants.ID_PENDIENTE
                    && newId != EstadoObservacionConstants.ID_RESUELTA
                    && newId != EstadoObservacionConstants.ID_CANCELADA) {
                throw ObservacionUpdateException.estadoNotFound(newId);
            }
        }

        // Validar reglas de negocio y aplicar cambios
        validateBusinessRules(existingObservacion, request);
        ObservacionNeumatico updatedObservacion = applyUpdates(existingObservacion, request);

        ObservacionNeumatico saved = observacionNeumaticoRepository.save(updatedObservacion);

        log.info("Observación actualizada exitosamente con ID: {}", saved.getId());
        return enrichObservacionWithRelations(saved);
    }

    // ── Helpers privados ─────────────────────────────────────────────

    /**
     * Mapa estático de estados de observación.
     * Evita queries a BD para datos de referencia que no cambian.
     * Si se agregan nuevos estados en BD, actualizar aquí y en EstadoObservacionConstants.
     */
    private static final Map<Integer, EstadoObservacionResponse> ESTADO_OBSERVACION_MAP = Map.of(
            EstadoObservacionConstants.ID_PENDIENTE,
            EstadoObservacionResponse.builder()
                    .id(EstadoObservacionConstants.ID_PENDIENTE)
                    .nombre(EstadoObservacionConstants.PENDIENTE)
                    .descripcion("Observación registrada pero aún no atendida").build(),
            EstadoObservacionConstants.ID_RESUELTA,
            EstadoObservacionResponse.builder()
                    .id(EstadoObservacionConstants.ID_RESUELTA)
                    .nombre(EstadoObservacionConstants.RESUELTO)
                    .descripcion("Observación completamente resuelta").build(),
            EstadoObservacionConstants.ID_CANCELADA,
            EstadoObservacionResponse.builder()
                    .id(EstadoObservacionConstants.ID_CANCELADA)
                    .nombre(EstadoObservacionConstants.CANCELADO)
                    .descripcion("Observación cancelada o no aplicable").build()
    );

    private EstadoObservacionResponse resolveEstadoObservacion(Integer estadoId) {
        if (estadoId == null) return null;
        return ESTADO_OBSERVACION_MAP.get(estadoId);
    }

    private void validateObservacionRequest(ObservacionNeumaticoNuevoRequest request) {
        if (request == null) {
            throw ObservacionCreationException.invalidRequest("request", "null");
        }
        if (request.getNeumaticoId() == null || request.getNeumaticoId() <= 0) {
            throw ObservacionCreationException.invalidRequest("neumaticoId", request.getNeumaticoId());
        }
        if (request.getEquipoId() == null || request.getEquipoId() <= 0) {
            throw ObservacionCreationException.invalidRequest("equipoId", request.getEquipoId());
        }
        if (request.getPosicion() == null || request.getPosicion() <= 0) {
            throw ObservacionCreationException.invalidRequest("posicion", request.getPosicion());
        }
        if (request.getTipoObservacionId() == null || request.getTipoObservacionId() <= 0) {
            throw ObservacionCreationException.invalidRequest("tipoObservacionId", request.getTipoObservacionId());
        }
        if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
            throw ObservacionCreationException.invalidRequest("descripcion", request.getDescripcion());
        }
        if (request.getUsuarioCreacionId() == null || request.getUsuarioCreacionId() <= 0) {
            throw ObservacionCreationException.invalidRequest("usuarioCreacionId",
                    "no fue provisto por el header " + HeaderConstants.HEADER_USER_ID);
        }
    }

    private void validateUpdateRequest(Integer id, ObservacionNeumaticoUpdateRequest request) {
        if (id == null || id <= 0) {
            throw ObservacionUpdateException.invalidRequest("id", id);
        }
        if (request == null) {
            throw ObservacionUpdateException.invalidRequest("request", "null");
        }
        if (request.getEstadoObservacionId() == null
                && request.getUsuarioResolucionId() == null
                && (request.getComentarioResolucion() == null || request.getComentarioResolucion().trim().isEmpty())) {
            throw ObservacionUpdateException.invalidRequest("request", "no hay campos para actualizar");
        }
        if (request.getEstadoObservacionId() != null && request.getEstadoObservacionId() <= 0) {
            throw ObservacionUpdateException.invalidRequest("estadoObservacionId", request.getEstadoObservacionId());
        }
        if (request.getUsuarioResolucionId() != null && request.getUsuarioResolucionId() <= 0) {
            throw ObservacionUpdateException.invalidRequest("usuarioResolucionId", request.getUsuarioResolucionId());
        }
    }

    private void validateBusinessRules(ObservacionNeumatico existing, ObservacionNeumaticoUpdateRequest request) {
        int currentStateId = existing.getEstadoObservacionId();

        // Estados finales: no se pueden modificar
        if (currentStateId == EstadoObservacionConstants.ID_RESUELTA) {
            throw ObservacionUpdateException.alreadyResolved(existing.getId());
        }
        if (currentStateId == EstadoObservacionConstants.ID_CANCELADA) {
            throw ObservacionUpdateException.invalidStateTransition(
                    EstadoObservacionConstants.CANCELADO, "cualquier estado");
        }

        // Transiciones válidas desde Pendiente: solo a Resuelta o Cancelada
        if (request.getEstadoObservacionId() != null
                && !request.getEstadoObservacionId().equals(existing.getEstadoObservacionId())) {
            int newStateId = request.getEstadoObservacionId();

            if (currentStateId == EstadoObservacionConstants.ID_PENDIENTE) {
                if (newStateId != EstadoObservacionConstants.ID_RESUELTA
                        && newStateId != EstadoObservacionConstants.ID_CANCELADA) {
                    throw ObservacionUpdateException.invalidStateTransition(
                            EstadoObservacionConstants.PENDIENTE, "estado id=" + newStateId);
                }
            } else {
                throw ObservacionUpdateException.invalidStateTransition(
                        "estado id=" + currentStateId, "estado id=" + newStateId);
            }
        }
    }

    private ObservacionNeumatico applyUpdates(ObservacionNeumatico existing, ObservacionNeumaticoUpdateRequest request) {
        ObservacionNeumatico.ObservacionNeumaticoBuilder builder = existing.toBuilder();

        if (request.getEstadoObservacionId() != null) {
            builder.estadoObservacionId(request.getEstadoObservacionId());

            if (!request.getEstadoObservacionId().equals(existing.getEstadoObservacionId())) {
                builder.fechaResolucion(LocalDateTime.now(ZoneId.of("America/Lima")));
                if (request.getUsuarioResolucionId() != null) {
                    builder.usuarioResolucionId(request.getUsuarioResolucionId());
                } else if (existing.getUsuarioResolucionId() == null) {
                    builder.usuarioResolucionId(existing.getUsuarioCreacionId());
                }
            }
        } else if (request.getUsuarioResolucionId() != null) {
            builder.usuarioResolucionId(request.getUsuarioResolucionId());
        }

        if (request.getComentarioResolucion() != null) {
            builder.comentarioResolucion(request.getComentarioResolucion().trim());
        }

        return builder.build();
    }

    private ObservacionNeumatico buildObservacionEntity(ObservacionNeumaticoNuevoRequest request) {
        return ObservacionNeumatico.builder()
                .neumaticoId(request.getNeumaticoId())
                .equipoId(request.getEquipoId())
                .posicion(request.getPosicion())
                .tipoObservacionId(request.getTipoObservacionId())
                .descripcion(request.getDescripcion().trim())
                .estadoObservacionId(EstadoObservacionConstants.ID_PENDIENTE)
                .fechaCreacion(LocalDateTime.now(ZoneId.of("America/Lima")))
                .usuarioCreacionId(request.getUsuarioCreacionId())
                .fechaResolucion(null)
                .usuarioResolucionId(null)
                .comentarioResolucion(null)
                .build();
    }

    private ObservacionNeumaticoResponse enrichObservacionWithRelations(ObservacionNeumatico observacion) {
        TipoObservacionResponse tipoObservacionResponse = null;
        if (observacion.getTipoObservacionId() != null) {
            tipoObservacionResponse = tipoObservacionRepository.findById(observacion.getTipoObservacionId())
                    .map(tipo -> TipoObservacionResponse.builder()
                            .id(tipo.getId()).nombre(tipo.getNombre())
                            .ambito(tipo.getAmbito()).descripcion(tipo.getDescripcion())
                            .activo(tipo.getActivo()).build())
                    .orElse(null);
        }

        EstadoObservacionResponse estadoObservacionResponse = resolveEstadoObservacion(observacion.getEstadoObservacionId());

        UserInfoResponse usuarioInfo = null;
        if (observacion.getUsuarioCreacionId() != null) {
            usuarioInfo = authUserRepository.findById(observacion.getUsuarioCreacionId())
                    .map(user -> UserInfoResponse.builder()
                            .id(user.getId()).name(user.getName())
                            .lastName(user.getLastName())
                            .fullName(user.getName() + " " + user.getLastName()).build())
                    .orElse(null);
        }

        NeumaticoSummaryResponse neumaticoSummary = null;
        if (observacion.getNeumaticoId() != null) {
            neumaticoSummary = neumaticoRepository.findById(observacion.getNeumaticoId())
                    .map(n -> NeumaticoSummaryResponse.builder()
                            .id(n.getId()).empresaId(n.getEmpresaId())
                            .serieCodigo(n.getSerieCodigo()).build())
                    .orElse(NeumaticoSummaryResponse.builder()
                            .id(observacion.getNeumaticoId()).serieCodigo("N/A").build());
        }

        return ObservacionNeumaticoResponse.builder()
                .id(observacion.getId())
                .neumaticoSummary(neumaticoSummary)
                .equipoId(observacion.getEquipoId())
                .posicion(observacion.getPosicion())
                .tipoObservacionResponse(tipoObservacionResponse)
                .descripcion(observacion.getDescripcion())
                .estadoObservacionResponse(estadoObservacionResponse)
                .fechaCreacion(observacion.getFechaCreacion())
                .userInfoResponseCreacion(usuarioInfo)
                .fechaResolucion(observacion.getFechaResolucion())
                .usuarioResolucionId(observacion.getUsuarioResolucionId())
                .comentarioResolucion(observacion.getComentarioResolucion())
                .build();
    }
}


