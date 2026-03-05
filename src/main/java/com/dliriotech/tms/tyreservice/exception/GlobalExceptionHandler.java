package com.dliriotech.tms.tyreservice.exception;

import com.dliriotech.tms.tyreservice.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Manejador global de excepciones para el tyre-service (Spring MVC).
 * <p>
 * Orden de resolución:
 * <ol>
 *   <li>Excepciones de dominio → {@link BaseException} y subclases</li>
 *   <li>Validación de @Valid → {@link MethodArgumentNotValidException}</li>
 *   <li>Body malformado / JSON inválido → {@link HttpMessageNotReadableException}</li>
 *   <li>Tipo de parámetro incorrecto → {@link MethodArgumentTypeMismatchException}</li>
 *   <li>Header requerido faltante → {@link MissingRequestHeaderException}</li>
 *   <li>@RequestParam requerido faltante → {@link MissingServletRequestParameterException}</li>
 *   <li>Violaciones de BD → {@link DataIntegrityViolationException}</li>
 *   <li>Catch-all → {@link Exception}</li>
 * </ol>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja todas las excepciones de dominio que extienden BaseException.
     */
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("Error de negocio [{}] en '{}': {}", ex.getCode(), request.getRequestURI(), ex.getMessage());
        return buildResponse(ex.getStatus(), ex.getCode(), ex.getMessage(), request);
    }

    /**
     * Validaciones de @Valid en el body del request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> String.format("Campo '%s': %s", fe.getField(), fe.getDefaultMessage()))
                .reduce((a, b) -> a + "; " + b)
                .orElse(ErrorCode.VALIDATION_ERROR.getDefaultMessage());
        log.warn("Error de validación en '{}': {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR.getCode(), message, request);
    }

    /**
     * Body malformado, JSON inválido, tipo incompatible en deserialización.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBadInput(HttpMessageNotReadableException ex, HttpServletRequest request) {
        log.warn("Datos de entrada inválidos en '{}': {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_INPUT_ERROR.getCode(),
                ErrorCode.VALIDATION_INPUT_ERROR.getDefaultMessage(), request);
    }

    /**
     * Tipo de parámetro incorrecto (e.g. equipoId="abc" en path variable Integer).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String message = String.format("El parámetro '%s' debe ser de tipo %s",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "desconocido");
        log.warn("Tipo de parámetro inválido en '{}': {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_INPUT_ERROR.getCode(), message, request);
    }

    /**
     * Header requerido faltante (e.g. X-Empresa-Id, X-User-Id).
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponse> handleMissingHeader(MissingRequestHeaderException ex, HttpServletRequest request) {
        String message = String.format("Header requerido '%s' no encontrado", ex.getHeaderName());
        log.warn("Header faltante en '{}': {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_HEADER.getCode(), message, request);
    }

    /**
     * Parámetro de query requerido faltante (e.g. ?tipoMovimientoId obligatorio).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        String message = String.format("Parámetro requerido '%s' de tipo %s no encontrado",
                ex.getParameterName(), ex.getParameterType());
        log.warn("Parámetro faltante en '{}': {}", request.getRequestURI(), message);
        return buildResponse(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_INPUT_ERROR.getCode(), message, request);
    }

    /**
     * Violaciones de constraints a nivel de BD no capturadas por el dominio.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("Violación de integridad de datos en '{}': {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ErrorCode.DATA_INTEGRITY_CONFLICT.getCode(),
                ErrorCode.DATA_INTEGRITY_CONFLICT.getDefaultMessage(), request);
    }

    /**
     * Cualquier otro error no controlado.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en '{}': {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR.getCode(),
                ErrorCode.INTERNAL_ERROR.getDefaultMessage(), request);
    }

    // ── Helper ───────────────────────────────────────────────────────

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String code, String message,
                                                         HttpServletRequest request) {
        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(request.getRequestURI())
                .timestamp(LocalDateTime.now(ZoneId.of("America/Lima")))
                .build();
        return ResponseEntity.status(status).body(errorResponse);
    }
}