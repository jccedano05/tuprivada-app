package com.jccv.tuprivadaapp.controller.payment.gateway;

import com.jccv.tuprivadaapp.dto.payment.gateway.*;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentAuthorizationService;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayRegistry;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayService;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentReceiptService;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentTransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador REST para operaciones de pasarelas de pago.
 * Maneja creación de pagos, confirmaciones, reembolsos y consultas.
 * 
 * FASE 2: Incluye endpoints para historial de transacciones y generación de comprobantes.
 * 
 * @author TuPrivada Development Team
 * @version 2.0
 * @since 2026-01-21
 */
@RestController
@RequestMapping("/api/v1/payment-gateway")
@RequiredArgsConstructor
@Validated
@Slf4j
public class PaymentGatewayController {

    private final PaymentGatewayService gatewayService;
    private final PaymentGatewayRegistry gatewayRegistry;
    private final PaymentTransactionService transactionService;
    private final PaymentAuthorizationService authorizationService;
    private final PaymentReceiptService receiptService;
    private final UserRepository userRepository;
    
    // Configuración de paginación por defecto
    private static final int DEFAULT_PAGE_SIZE = 15;
    private static final int MAX_PAGE_SIZE = 100;

    /**
     * Crea una intención de pago con la pasarela especificada
     */
    @PostMapping("/payment-intent")
    public ResponseEntity<PaymentIntentResponse> createPaymentIntent(
            @Valid @RequestBody PaymentIntentRequest request) {
        
        log.info("[PaymentGateway] Creando intención de pago para {} con monto {} {}", 
                request.getProvider(), request.getAmount(), request.getCurrency());
        
        try {
            // Si no se especifica proveedor, usar Conekta por defecto
            if (request.getProvider() == null) {
                request = request.toBuilder()
                        .provider(PaymentGatewayAccount.PaymentProvider.CONEKTA)
                        .build();
            }
            
            PaymentIntentResponse response = gatewayService.createPaymentIntent(request);
            
            log.info("[PaymentGateway] Intención de pago creada: {}", 
                    response.getTransactionReference());
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentGatewayException e) {
            log.error("[PaymentGateway] Error creando intención de pago: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Confirma un pago pendiente
     */
    @PostMapping("/payment/{transactionReference}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'USER')")
    public ResponseEntity<PaymentConfirmationResponse> confirmPayment(
            @PathVariable String transactionReference,
            @Valid @RequestBody PaymentConfirmationRequest request) {
        
        log.info("[PaymentGateway] Confirmando pago: {}", transactionReference);
        
        try {
            PaymentConfirmationResponse response = gatewayService.confirmPayment(
                    transactionReference, request);
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentGatewayException e) {
            log.error("[PaymentGateway] Error confirmando pago: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Obtiene el estado de una transacción
     */
    @GetMapping("/payment/{transactionReference}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'USER')")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(
            @PathVariable String transactionReference) {
        
        log.debug("[PaymentGateway] Consultando estado de: {}", transactionReference);
        
        try {
            TransactionStatusResponse response = gatewayService.getTransactionStatus(
                    transactionReference);
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentGatewayException e) {
            log.error("[PaymentGateway] Error consultando estado: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Procesa un reembolso
     */
    @PostMapping("/payment/{transactionReference}/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RefundResponse> refundPayment(
            @PathVariable String transactionReference,
            @Valid @RequestBody RefundRequest request) {
        
        log.info("[PaymentGateway] Procesando reembolso para: {} por monto: {}", 
                transactionReference, request.getAmount());
        
        try {
            RefundResponse response = gatewayService.refundPayment(
                    transactionReference, request);
            
            log.info("[PaymentGateway] Reembolso procesado exitosamente");
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentGatewayException e) {
            log.error("[PaymentGateway] Error procesando reembolso: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Inicia un pago para un Payment existente usando una pasarela
     */
    @PostMapping("/initiate")
    public ResponseEntity<PaymentIntentResponse> initiatePayment(
            @Valid @RequestBody PaymentInitiationRequest request) {
        
        log.info("[PaymentGateway] Iniciando pago para paymentId: {} con método: {}", 
                request.getPaymentId(), request.getPaymentMethod());
        
        try {
            PaymentIntentResponse response = gatewayService.initiatePaymentFromExisting(request);
            
            log.info("[PaymentGateway] Pago iniciado exitosamente: {}", response.getTransactionReference());
            
            return ResponseEntity.ok(response);
            
        } catch (PaymentGatewayException e) {
            log.error("[PaymentGateway] Error iniciando pago: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("[PaymentGateway] Error inesperado iniciando pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error iniciando pago", e);
        }
    }
    
    /**
     * Genera una referencia de pago en efectivo (OXXO) - LEGACY
     * @deprecated Use /initiate instead
     */
    @Deprecated
    @PostMapping("/cash-payment/reference")
    public ResponseEntity<CashPaymentReference> generateCashReference(
            @Valid @RequestBody CashPaymentRequest request) {
        
        log.info("[PaymentGateway] Generando referencia de pago en efectivo para: {}", 
                request.getPayerEmail());
        
        try {
            // Por defecto usar CONEKTA como proveedor para pagos en efectivo
            PaymentGatewayAccount.PaymentProvider provider = PaymentGatewayAccount.PaymentProvider.CONEKTA;
            
            // Obtener estrategia de Conekta desde el registry
            CashPaymentReference reference = gatewayRegistry.getStrategy(provider)
                .generateCashPaymentReference(request);
            
            log.info("[PaymentGateway] Referencia de pago en efectivo generada exitosamente");
            
            return ResponseEntity.ok(reference);
            
        } catch (PaymentGatewayException e) {
            log.error("[PaymentGateway] Error generando referencia OXXO: {}", e.getMessage());
            throw new RuntimeException(e.getMessage(), e);
        } catch (Exception e) {
            log.error("[PaymentGateway] Error inesperado generando referencia de pago: {}", e.getMessage(), e);
            throw new RuntimeException("Error generando referencia de pago", e);
        }
    }

    /**
     * Endpoint de salud para verificar conectividad con pasarelas
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("service", "PaymentGateway");
        health.put("providers", PaymentGatewayAccount.PaymentProvider.values());
        health.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(health);
    }

    // ==================== NUEVOS ENDPOINTS FASE 2 ====================
    
    /**
     * Obtiene todas las transacciones de un Payment específico con paginación.
     * Soporta filtrado opcional por estado.
     * 
     * Seguridad:
     * - RESIDENT: Solo puede consultar transacciones de sus propios pagos
     * - ADMIN/SUPERADMIN: Puede consultar transacciones del condominio
     * 
     * @param paymentId ID del Payment
     * @param status Estado opcional para filtrar (PENDING, SUCCEEDED, FAILED, etc.)
     * @param condominiumId ID del condominio (solo para admins, query param)
     * @param page Número de página (0-indexed)
     * @param size Tamaño de página (default: 15, max: 100)
     * @param userDetails Usuario autenticado
     * @return Respuesta paginada con transacciones
     */
    @GetMapping("/transactions/payment/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SUPERADMIN')")
    public ResponseEntity<PagedTransactionResponse> getPaymentTransactions(
            @PathVariable Long paymentId,
            @RequestParam(required = false) PaymentTransaction.TransactionStatus status,
            @RequestParam(required = false) Long condominiumId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE_SIZE) int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("[PaymentGateway] Consultando transacciones del payment {} - página {} - usuario: {}",
                paymentId, page, userDetails.getUsername());
        
        try {
            // Obtener usuario autenticado (buscar por email o username)
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .or(() -> userRepository.findByUsername(userDetails.getUsername()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            // Validar permisos de acceso al Payment
            authorizationService.validateUserCanAccessPaymentTransactions(paymentId, user);
            
            // Validar y ajustar tamaño de página
            if (size > MAX_PAGE_SIZE) {
                log.warn("[PaymentGateway] Tamaño de página {} excede el máximo permitido {}, ajustando",
                        size, MAX_PAGE_SIZE);
                size = MAX_PAGE_SIZE;
            }
            
            // Crear configuración de paginación (ordenar por fecha descendente)
            Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
            
            // Consultar transacciones
            Page<TransactionListItemDTO> transactionsPage = transactionService
                    .findByPaymentIdPaginated(paymentId, status, pageable);
            
            // Convertir a respuesta estandarizada
            PagedTransactionResponse response = PagedTransactionResponse.fromPage(transactionsPage);
            
            log.info("[PaymentGateway] Encontradas {} transacciones (total: {}) para payment {}",
                    response.getContent().size(), response.getTotalElements(), paymentId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("[PaymentGateway] Error consultando transacciones del payment {}: {}",
                    paymentId, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Obtiene el detalle de una transacción por su referencia.
     * 
     * Seguridad:
     * - RESIDENT: Solo puede consultar transacciones de sus propios pagos
     * - ADMIN/SUPERADMIN: Puede consultar transacciones del condominio
     * 
     * @param transactionReference Referencia única de la transacción
     * @param userDetails Usuario autenticado
     * @return Detalle de la transacción
     */
    @GetMapping("/transactions/{transactionReference}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SUPERADMIN')")
    public ResponseEntity<PaymentTransaction> getTransactionDetails(
            @PathVariable String transactionReference,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("[PaymentGateway] Consultando detalles de transacción {} - usuario: {}",
                transactionReference, userDetails.getUsername());
        
        try {
            // Obtener usuario autenticado (buscar por email o username)
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .or(() -> userRepository.findByUsername(userDetails.getUsername()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            // Validar permisos de acceso a la transacción
            authorizationService.validateUserCanAccessTransaction(transactionReference, user);
            
            // Obtener transacción
            PaymentTransaction transaction = transactionService.findByReference(transactionReference)
                    .orElseThrow(() -> new ResourceNotFoundException("Transacción no encontrada"));
            
            log.info("[PaymentGateway] Detalles de transacción {} obtenidos exitosamente", 
                    transactionReference);
            
            return ResponseEntity.ok(transaction);
            
        } catch (Exception e) {
            log.error("[PaymentGateway] Error consultando detalles de transacción {}: {}",
                    transactionReference, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Genera y descarga un comprobante PDF de una transacción.
     * 
     * Seguridad:
     * - RESIDENT: Solo puede descargar comprobantes de sus propios pagos
     * - ADMIN/SUPERADMIN: Puede descargar comprobantes del condominio
     * 
     * @param transactionReference Referencia única de la transacción
     * @param userDetails Usuario autenticado
     * @return PDF del comprobante como byte array
     */
    @GetMapping("/transactions/{transactionReference}/receipt")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESIDENT', 'SUPERADMIN')")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable String transactionReference,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        log.info("[PaymentGateway] Generando comprobante para {} - usuario: {}",
                transactionReference, userDetails.getUsername());
        
        try {
            // Obtener usuario autenticado (buscar por email o username)
            User user = userRepository.findByEmail(userDetails.getUsername())
                    .or(() -> userRepository.findByUsername(userDetails.getUsername()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
            
            // Validar permisos de acceso a la transacción
            authorizationService.validateUserCanAccessTransaction(transactionReference, user);
            
            // Generar PDF del comprobante
            byte[] pdfBytes = receiptService.generateReceipt(transactionReference);
            
            // Configurar headers para descarga
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData(
                    "attachment",
                    String.format("comprobante_%s.pdf", transactionReference)
            );
            headers.setContentLength(pdfBytes.length);
            
            log.info("[PaymentGateway] Comprobante PDF generado exitosamente para {} ({} bytes)",
                    transactionReference, pdfBytes.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
            
        } catch (Exception e) {
            log.error("[PaymentGateway] Error generando comprobante para transacción {}: {}",
                    transactionReference, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Manejo global de excepciones de pasarela
     */
    @ExceptionHandler(PaymentGatewayException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentGatewayException(
            PaymentGatewayException ex) {
        
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", ex.getMessage());
        error.put("code", ex.getErrorCode());
        error.put("timestamp", System.currentTimeMillis());
        
        log.error("[PaymentGateway] Error procesado: {} - {}", 
                ex.getErrorCode(), ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
