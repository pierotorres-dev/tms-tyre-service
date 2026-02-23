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
 * Manejador global de excepciones para el tyre-service.
 * Gracias al polimorfismo de {@link BaseException}, ya no requiere un if-else por cada tipo:
 * todas las excepciones de dominio se resuelven en una sola rama.
 * Solo se tratan de forma especial las excepciones de infraestructura (Spring/DB) que
 * no extienden {@link BaseException}.
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

        HttpStatus status;
        String code;
        String message;

        if (error instanceof BaseException baseEx) {
            // Todas las excepciones de dominio: neumáticos, observaciones, inspecciones, caché, etc.
            status = baseEx.getStatus();
            code = baseEx.getCode();
            message = baseEx.getMessage();
            log.warn("Error de negocio [{}] en '{}': {}", code, request.path(), message);

        } else if (error instanceof WebExchangeBindException bindEx) {
            // Validaciones de @Valid en el body del request
            status = HttpStatus.BAD_REQUEST;
            code = ErrorCode.VALIDATION_ERROR.getCode();
            message = bindEx.getBindingResult().getFieldErrors().stream()
                    .map(fe -> String.format("Campo '%s': %s", fe.getField(), fe.getDefaultMessage()))
                    .reduce((a, b) -> a + "; " + b)
                    .orElse(ErrorCode.VALIDATION_ERROR.getDefaultMessage());
            log.warn("Error de validación en '{}': {}", request.path(), message);

        } else if (error instanceof ServerWebInputException) {
            // Body malformado, tipo incorrecto, etc.
            status = HttpStatus.BAD_REQUEST;
            code = ErrorCode.VALIDATION_INPUT_ERROR.getCode();
            message = ErrorCode.VALIDATION_INPUT_ERROR.getDefaultMessage();
            log.warn("Datos de entrada inválidos en '{}': {}", request.path(), error.getMessage());

        } else if (error instanceof DataIntegrityViolationException) {
            // Violaciones de constraints a nivel de BD no capturadas por el dominio
            status = HttpStatus.CONFLICT;
            code = ErrorCode.DATA_INTEGRITY_CONFLICT.getCode();
            message = ErrorCode.DATA_INTEGRITY_CONFLICT.getDefaultMessage();
            log.error("Violación de integridad de datos en '{}': {}", request.path(), error.getMessage());

        } else {
            // Cualquier otro error no controlado
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            code = ErrorCode.INTERNAL_ERROR.getCode();
            message = ErrorCode.INTERNAL_ERROR.getDefaultMessage();
            log.error("Error no controlado en '{}': {}", request.path(), error.getMessage(), error);
        }

        ErrorResponse errorResponse = ErrorResponse.builder()
                .code(code)
                .message(message)
                .path(request.path())
                .timestamp(LocalDateTime.now(ZoneId.of("America/Lima")))
                .build();

        return ServerResponse.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(errorResponse));
    }
}