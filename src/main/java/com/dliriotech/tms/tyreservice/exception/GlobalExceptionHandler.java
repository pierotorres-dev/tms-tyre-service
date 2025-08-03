package com.dliriotech.tms.tyreservice.exception;

import com.dliriotech.tms.tyreservice.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.*;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Manejador global de excepciones para el servicio de neumáticos.
 * Proporciona un manejo centralizado y consistente de todas las excepciones.
 */
@Component
@Order(-2)
@Slf4j
public class GlobalExceptionHandler extends AbstractErrorWebExceptionHandler {

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties.Resources resources,
                                  ApplicationContext applicationContext,
                                  ServerCodecConfigurer configurer) {
        super(errorAttributes, resources, applicationContext);
        this.setMessageWriters(configurer.getWriters());
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(ServerRequest request) {
        Throwable error = getError(request);
        log.error("Error procesando solicitud en path '{}': {} - Tipo: {}", 
            request.path(), error.getMessage(), error.getClass().getSimpleName(), error);

        // Buscar el handler más específico (evaluando desde las clases más específicas hacia las generales)
        ErrorDetails errorDetails = null;
        
        // Excepciones específicas primero
        if (error instanceof PosicionAlreadyOccupiedException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof NeumaticoNotFoundException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof NeumaticoAlreadyAssignedException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof InvalidNeumaticoStateException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof CatalogoNeumaticoNotFoundException || 
                 error instanceof ProveedorNotFoundException ||
                 error instanceof DisenoReencaucheNotFoundException ||
                 error instanceof ClasificacionNeumaticoNotFoundException ||
                 error instanceof MarcaNeumaticoNotFoundException ||
                 error instanceof MedidaNeumaticoNotFoundException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof ValidationException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof DataIntegrityException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof CacheOperationException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof NeumaticoException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof ResourceNotFoundException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof BaseException) {
            BaseException baseEx = (BaseException) error;
            errorDetails = new ErrorDetails(baseEx.getStatus(), baseEx.getCode(), baseEx.getMessage());
        }
        else if (error instanceof WebExchangeBindException) {
            WebExchangeBindException bindEx = (WebExchangeBindException) error;
            String message = bindEx.getBindingResult().getFieldErrors().stream()
                    .map(errorField -> String.format("Campo '%s': %s", errorField.getField(), errorField.getDefaultMessage()))
                    .reduce((msg1, msg2) -> msg1 + "; " + msg2)
                    .orElse("Error de validación en los datos de entrada");
            errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, "TYR-VAL-004", message);
        }
        else if (error instanceof ServerWebInputException) {
            errorDetails = new ErrorDetails(HttpStatus.BAD_REQUEST, "TYR-INPUT-001", 
                    "Datos de entrada inválidos: " + error.getMessage());
        }
        else if (error instanceof DataIntegrityViolationException) {
            errorDetails = new ErrorDetails(HttpStatus.CONFLICT, "TYR-DB-001", 
                    "Error de integridad de datos: " + error.getMessage());
        }
        else {
            errorDetails = new ErrorDetails(HttpStatus.INTERNAL_SERVER_ERROR, "TYR-SYS-ERR-001",
                    "Error interno del servidor: " + error.getMessage());
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(errorDetails.code())
                .message(errorDetails.message())
                .path(request.path())
                .timestamp(LocalDateTime.now(ZoneId.of("America/Lima")))
                .build();

        return ServerResponse.status(errorDetails.status())
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }

    private record ErrorDetails(HttpStatus status, String code, String message) {
    }
}