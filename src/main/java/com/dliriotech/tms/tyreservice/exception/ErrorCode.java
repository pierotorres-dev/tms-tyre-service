package com.dliriotech.tms.tyreservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Fuente única de verdad para todos los códigos de error del tyre-service.
 * Cada entrada define: código único, HttpStatus y mensaje por defecto.
 *
 * <p>Convención de códigos: TYR-{categoría}-{secuencial}</p>
 * <ul>
 *   <li>TYR-NEU — Neumáticos</li>
 *   <li>TYR-OBS — Observaciones</li>
 *   <li>TYR-INSP — Inspecciones</li>
 *   <li>TYR-CAT — Catálogos</li>
 *   <li>TYR-CAC — Caché</li>
 *   <li>TYR-DAT — Integridad de datos</li>
 *   <li>TYR-VAL — Validación</li>
 *   <li>TYR-HDR — Headers</li>
 *   <li>TYR-SYS — Errores de sistema</li>
 * </ul>
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── Neumáticos ──
    NEUMATICO_NOT_FOUND("TYR-NEU-001", HttpStatus.NOT_FOUND, "Neumático no encontrado"),
    NEUMATICO_POSICION_OCCUPIED("TYR-NEU-002", HttpStatus.CONFLICT, "La posición ya está ocupada por otro neumático"),
    NEUMATICO_ALREADY_ASSIGNED("TYR-NEU-003", HttpStatus.CONFLICT, "El neumático ya está asignado a un equipo"),
    NEUMATICO_INVALID_STATE("TYR-NEU-004", HttpStatus.BAD_REQUEST, "Operación no permitida en el estado actual del neumático"),
    NEUMATICO_OPERATION_ERROR("TYR-NEU-005", HttpStatus.INTERNAL_SERVER_ERROR, "Error en operación de neumático"),
    NEUMATICO_SAVE_ERROR("TYR-NEU-006", HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar neumático"),
    NEUMATICO_UPDATE_ERROR("TYR-NEU-007", HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar neumático"),

    // ── Observaciones ──
    OBSERVACION_NOT_FOUND("TYR-OBS-001", HttpStatus.NOT_FOUND, "Observación no encontrada"),
    OBSERVACION_INVALID_NEUMATICO("TYR-OBS-002", HttpStatus.BAD_REQUEST, "ID de neumático inválido"),
    OBSERVACION_INVALID_EQUIPO("TYR-OBS-003", HttpStatus.BAD_REQUEST, "ID de equipo inválido"),
    OBSERVACION_INVALID_TIPO_MOVIMIENTO("TYR-OBS-004", HttpStatus.BAD_REQUEST, "Tipo de movimiento inválido"),
    OBSERVACION_CREATION_INVALID("TYR-OBS-005", HttpStatus.BAD_REQUEST, "Datos inválidos para crear observación"),
    OBSERVACION_TIPO_NOT_FOUND("TYR-OBS-006", HttpStatus.NOT_FOUND, "Tipo de observación no encontrado"),
    OBSERVACION_ESTADO_NOT_FOUND("TYR-OBS-007", HttpStatus.NOT_FOUND, "Estado de observación no encontrado"),
    OBSERVACION_DB_ERROR("TYR-OBS-008", HttpStatus.INTERNAL_SERVER_ERROR, "Error de base de datos en observación"),
    OBSERVACION_MASTER_DATA_ERROR("TYR-OBS-009", HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener datos maestros de observación"),
    OBSERVACION_ENRICH_ERROR("TYR-OBS-010", HttpStatus.INTERNAL_SERVER_ERROR, "Error al enriquecer observación con datos relacionados"),
    OBSERVACION_UPDATE_INVALID("TYR-OBS-011", HttpStatus.BAD_REQUEST, "Datos inválidos para actualizar observación"),
    OBSERVACION_NO_FIELDS("TYR-OBS-012", HttpStatus.BAD_REQUEST, "No se proporcionaron campos para actualizar"),
    OBSERVACION_STATE_TRANSITION("TYR-OBS-013", HttpStatus.BAD_REQUEST, "Transición de estado no permitida"),
    OBSERVACION_FINAL_STATE("TYR-OBS-014", HttpStatus.BAD_REQUEST, "No se puede modificar una observación en estado final"),

    // ── Inspecciones ──
    INSPECCION_PROCESSING_ERROR("TYR-INSP-001", HttpStatus.UNPROCESSABLE_ENTITY, "Error al procesar la inspección"),
    INSPECCION_RTD_INVALID("TYR-INSP-002", HttpStatus.UNPROCESSABLE_ENTITY, "RTD inválido: el valor no puede ser mayor al actual"),
    INSPECCION_EQUIPO_NOT_FOUND("TYR-INSP-003", HttpStatus.NOT_FOUND, "Equipo no encontrado para la inspección"),

    // ── Catálogos y entidades relacionadas ──
    CATALOGO_NEUMATICO_NOT_FOUND("TYR-CAT-001", HttpStatus.NOT_FOUND, "Catálogo de neumático no encontrado"),
    PROVEEDOR_NOT_FOUND("TYR-CAT-002", HttpStatus.NOT_FOUND, "Proveedor no encontrado"),
    DISENO_REENCAUCHE_NOT_FOUND("TYR-CAT-003", HttpStatus.NOT_FOUND, "Diseño de reencauche no encontrado"),
    CLASIFICACION_NEUMATICO_NOT_FOUND("TYR-CAT-004", HttpStatus.NOT_FOUND, "Clasificación de neumático no encontrada"),
    MARCA_NEUMATICO_NOT_FOUND("TYR-CAT-005", HttpStatus.NOT_FOUND, "Marca de neumático no encontrada"),
    MEDIDA_NEUMATICO_NOT_FOUND("TYR-CAT-006", HttpStatus.NOT_FOUND, "Medida de neumático no encontrada"),
    EQUIPO_NOT_FOUND("TYR-CAT-007", HttpStatus.NOT_FOUND, "Equipo no encontrado"),

    // ── Caché ──
    CACHE_OPERATION_ERROR("TYR-CAC-001", HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar operación de caché"),

    // ── Integridad de datos ──
    DATA_INTEGRITY_CONFLICT("TYR-DAT-001", HttpStatus.CONFLICT, "Conflicto de integridad de datos"),

    // ── Validación ──
    VALIDATION_FIELD_ERROR("TYR-VAL-001", HttpStatus.BAD_REQUEST, "Error de validación en un campo"),
    VALIDATION_ERROR("TYR-VAL-002", HttpStatus.BAD_REQUEST, "Error de validación"),
    VALIDATION_INPUT_ERROR("TYR-VAL-003", HttpStatus.BAD_REQUEST, "Datos de entrada inválidos"),

    // ── Headers ──
    MISSING_HEADER("TYR-HDR-001", HttpStatus.BAD_REQUEST, "Header requerido no encontrado"),

    // ── Sistema ──
    INTERNAL_ERROR("TYR-SYS-001", HttpStatus.INTERNAL_SERVER_ERROR, "Error interno del servidor");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;
}