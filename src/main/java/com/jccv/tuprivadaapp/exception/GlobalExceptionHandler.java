package com.jccv.tuprivadaapp.exception;

import com.jccv.tuprivadaapp.controller.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleResourceNotFound(ResourceNotFoundException ex) {
        LOGGER.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleBadRequest(BadRequestException ex) {
        LOGGER.warn("Petición inválida: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(StripeServiceException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleStripeError(StripeServiceException ex) {
        LOGGER.error("Error en servicio de pagos: {}", ex.getMessage(), ex);
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage(), null);
    }

    @ExceptionHandler(SurveyValidationException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleSurveyValidation(SurveyValidationException ex) {
        LOGGER.warn("Error de validación de encuesta: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    @ExceptionHandler(SurveyAlreadyVotedException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleSurveyAlreadyVoted(SurveyAlreadyVotedException ex) {
        LOGGER.warn("Usuario ya votó en encuesta: {}", ex.getMessage());
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleValidationErrors(MethodArgumentNotValidException ex) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));
        LOGGER.warn("Errores de validación: {}", validationErrors);
        Map<String, Object> details = new HashMap<>();
        details.put("errors", validationErrors);
        return buildResponse(HttpStatus.BAD_REQUEST, "La petición contiene datos inválidos", details);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleMalformedBody(HttpMessageNotReadableException ex) {
        LOGGER.warn("Cuerpo de la petición no legible: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "El formato del cuerpo de la petición es incorrecto", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Map<String, Object>>> handleGenericException(Exception ex) {
        LOGGER.error("Error inesperado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error interno. Intenta nuevamente más tarde.", null);
    }

    private ResponseEntity<ApiResponse<Map<String, Object>>> buildResponse(HttpStatus status, String message, Map<String, Object> data) {
        ApiResponse<Map<String, Object>> response = ApiResponse.<Map<String, Object>>builder()
                .status(status.getReasonPhrase())
                .code(status.value())
                .message(message)
                .data(data)
                .build();
        return ResponseEntity.status(status).body(response);
    }
}
