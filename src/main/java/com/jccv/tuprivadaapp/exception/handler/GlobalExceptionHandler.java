package com.jccv.tuprivadaapp.exception.handler;

import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentGatewayException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Centraliza el manejo de errores y garantiza respuestas consistentes.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones de autenticación - Solo para problemas reales de auth
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException ex, WebRequest request) {
        
        log.error("Error de autenticación: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "AUTHENTICATION_FAILED",
            "Credenciales inválidas o sesión expirada",
            HttpStatus.UNAUTHORIZED,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Maneja excepciones de acceso denegado - Solo para problemas de autorización
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        
        log.error("Acceso denegado: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "ACCESS_DENIED",
            "No tienes permisos para realizar esta acción",
            HttpStatus.FORBIDDEN,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Maneja excepciones de pasarela de pago
     */
    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentGatewayException(
            PaymentGatewayException ex, WebRequest request) {
        
        log.error("Error en pasarela de pago [{}]: {}", ex.getErrorCode(), ex.getMessage());
        
        // Mapear códigos de error a status HTTP apropiados
        HttpStatus status = mapPaymentErrorToHttpStatus(ex.getErrorCode());
        
        Map<String, Object> response = buildErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            status,
            request.getDescription(false)
        );
        
        // Agregar detalles adicionales si existen
        if (ex.getCause() != null) {
            response.put("details", ex.getCause().getMessage());
        }
        
        return ResponseEntity.status(status).body(response);
    }
    
    /**
     * Maneja recursos no encontrados
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        log.error("Recurso no encontrado: {}", ex.getMessage());
        
        Map<String, Object> response = buildErrorResponse(
            "RESOURCE_NOT_FOUND",
            ex.getMessage(),
            HttpStatus.NOT_FOUND,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Maneja errores de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = buildErrorResponse(
            "VALIDATION_FAILED",
            "Error de validación en los datos enviados",
            HttpStatus.BAD_REQUEST,
            request.getDescription(false)
        );
        response.put("validation_errors", errors);
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Maneja errores de transacciones
     */
    @ExceptionHandler({TransactionSystemException.class, UnexpectedRollbackException.class})
    public ResponseEntity<Map<String, Object>> handleTransactionException(
            Exception ex, WebRequest request) {
        
        log.error("Error de transacción: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = buildErrorResponse(
            "TRANSACTION_ERROR",
            "Error al procesar la transacción. Por favor, intenta nuevamente",
            HttpStatus.INTERNAL_SERVER_ERROR,
            request.getDescription(false)
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Maneja cualquier excepción no capturada - Default 500
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(
            Exception ex, WebRequest request) {
        
        log.error("Error inesperado: {}", ex.getMessage(), ex);
        
        Map<String, Object> response = buildErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Ha ocurrido un error inesperado. Por favor, contacta al soporte si el problema persiste",
            HttpStatus.INTERNAL_SERVER_ERROR,
            request.getDescription(false)
        );
        
        // En desarrollo, incluir más detalles
        if (isDevEnvironment()) {
            response.put("debug_message", ex.getMessage());
            response.put("exception_type", ex.getClass().getSimpleName());
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    /**
     * Construye una respuesta de error consistente
     */
    private Map<String, Object> buildErrorResponse(
            String errorCode, 
            String message, 
            HttpStatus status,
            String path) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", status.getReasonPhrase());
        response.put("code", errorCode);
        response.put("message", message);
        response.put("path", path);
        
        return response;
    }
    
    /**
     * Mapea códigos de error de pago a status HTTP apropiados
     */
    private HttpStatus mapPaymentErrorToHttpStatus(String errorCode) {
        if (errorCode == null) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
        
        switch (errorCode) {
            case "PAYMENT_ALREADY_PAID":
            case "PENDING_TRANSACTION_EXISTS":
            case "EMAIL_REQUIRED":
            case "INVALID_AMOUNT":
            case "TOKENIZATION_FAILED":
                return HttpStatus.BAD_REQUEST;
                
            case "GATEWAY_CONFIGURATION":
            case "GATEWAY_ACCOUNT_NOT_READY":
                return HttpStatus.SERVICE_UNAVAILABLE;
                
            case "PAYMENT_NOT_FOUND":
            case "TRANSACTION_NOT_FOUND":
                return HttpStatus.NOT_FOUND;
                
            case "GATEWAY_ERROR":
            case "WEBHOOK_ERROR":
            default:
                return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
    
    /**
     * Verifica si estamos en ambiente de desarrollo
     */
    private boolean isDevEnvironment() {
        String env = System.getProperty("spring.profiles.active", "dev");
        return "dev".equalsIgnoreCase(env) || "development".equalsIgnoreCase(env);
    }
}
