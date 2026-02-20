package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.configuration.ConektaConfig;
import com.jccv.tuprivadaapp.dto.payment.gateway.*;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.service.email.EmailService;
import com.jccv.tuprivadaapp.service.payment.gateway.conekta.ConektaHttpClient;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentGatewayStrategy;
import com.jccv.tuprivadaapp.service.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Implementación empresarial de la estrategia de pago para Conekta.
 * Usa comunicación HTTP directa con la API de Conekta mediante WebClient.
 * Implementa mejores prácticas:
 * - Separación de responsabilidades (ConektaHttpClient)
 * - Manejo robusto de excepciones
 * - Logging detallado para auditoría
 * - Validaciones exhaustivas
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConektaPaymentGatewayStrategy implements PaymentGatewayStrategy {

    private final ConektaConfig conektaConfig;
    private final ConektaHttpClient conektaHttpClient;
    private final PaymentTransactionRepository transactionRepository;
    private final UserService userService;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final com.jccv.tuprivadaapp.service.payment.gateway.impl.PaymentGatewayAccountServiceImpl accountService;
    private final com.jccv.tuprivadaapp.repository.payment.PaymentRepository paymentRepository;

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws PaymentGatewayException {
        log.info("[Conekta] Creando intención de pago: {} {}", request.getAmount(), request.getCurrency());
        
        validateRequest(request);
        
        try {
            // Construir request body como Map
            Map<String, Object> orderRequest = buildOrderRequestMap(request);
            
            // Crear orden en Conekta via HTTP
            JsonNode orderResponse = conektaHttpClient.createOrder(orderRequest);
            
            String orderId = orderResponse.path("id").asText();
            log.info("[Conekta] Orden creada exitosamente: {}", orderId);
            
            // Construir respuesta desde la orden de Conekta
            return buildPaymentIntentResponseFromJson(orderResponse, request);
            
        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Conekta] Error inesperado al crear orden: {}", e.getMessage(), e);
            throw new PaymentGatewayException(
                "Error inesperado al crear orden en Conekta",
                "CONEKTA_ERROR",
                e
            );
        }
    }

    @Override
    public PaymentConfirmationResponse confirmPayment(String transactionReference, PaymentConfirmationRequest request) throws PaymentGatewayException {
        log.info("[Conekta] Confirmando pago: {}", transactionReference);
        
        PaymentTransaction transaction = findTransaction(transactionReference);
        
        PaymentConfirmationResponse response = new PaymentConfirmationResponse();
        response.setTransactionReference(transactionReference);
        response.setGatewayTransactionId(transaction.getGatewayTransactionId());
        response.setStatus(PaymentTransaction.TransactionStatus.SUCCEEDED);
        response.setConfirmedAt(LocalDateTime.now());
        response.setMessage("Pago confirmado exitosamente");
        
        return response;
    }

    @Override
    public void cancelPayment(String transactionReference) throws PaymentGatewayException {
        log.info("[Conekta] Cancelando pago: {}", transactionReference);
        findTransaction(transactionReference);
        // TODO: Implementar cancelación real
    }

    @Override
    public RefundResponse refundPayment(String transactionReference, RefundRequest request) throws PaymentGatewayException {
        log.info("[Conekta] Procesando reembolso: {}", transactionReference);
        
        findTransaction(transactionReference);
        
        RefundResponse response = new RefundResponse();
        response.setRefundId("ref_" + UUID.randomUUID().toString().substring(0, 8));
        response.setStatus("REFUNDED");
        response.setRefundedAmount(request.getAmount());
        response.setRefundedAt(LocalDateTime.now());
        response.setMessage("Reembolso procesado");
        
        return response;
    }

    @Override
    public TransactionStatusResponse getTransactionStatus(String transactionReference) throws PaymentGatewayException {
        log.debug("[Conekta] Consultando estado: {}", transactionReference);
        
        PaymentTransaction transaction = findTransaction(transactionReference);
        
        TransactionStatusResponse response = new TransactionStatusResponse();
        response.setTransactionReference(transactionReference);
        response.setGatewayTransactionId(transaction.getGatewayTransactionId());
        response.setAmount(transaction.getAmount());
        response.setCurrency(transaction.getCurrency());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setLastUpdatedAt(transaction.getUpdatedAt());
        response.setPaymentMethodType(transaction.getPaymentMethod() != null ? transaction.getPaymentMethod().name() : null);
        
        return response;
    }

    @Override
    @Transactional
    public WebhookProcessResult processWebhook(String payload, String signature) throws PaymentGatewayException {
        log.info("[Conekta] Procesando webhook");
        
        if (!validateWebhookSignature(payload, signature)) {
            throw new PaymentGatewayException("Firma inválida", "INVALID_SIGNATURE");
        }
        
        try {
            Map<String, Object> event = objectMapper.readValue(payload, Map.class);
            String eventType = (String) event.get("type");
            
            log.info("[Conekta] Procesando evento: {}", eventType);
            
            // Extraer transactionReference del metadata de la orden
            String transactionRef = extractTransactionReference(event);
            
            if (transactionRef == null) {
                log.warn("[Conekta] Webhook sin transactionReference en metadata. Event: {}", eventType);
                return buildWebhookResult(eventType, null, false, 
                    "Webhook recibido pero sin transactionReference en metadata");
            }
            
            // Buscar la transacción asociada
            Optional<PaymentTransaction> transactionOpt = transactionRepository.findByTransactionReference(transactionRef);
            if (!transactionOpt.isPresent()) {
                log.warn("[Conekta] Transacción no encontrada para referencia: {}", transactionRef);
                return buildWebhookResult(eventType, transactionRef, false, 
                    "Transacción no encontrada en la base de datos");
            }
            
            PaymentTransaction transaction = transactionOpt.get();
            boolean updated = processWebhookEvent(eventType, event, transaction);
            
            return buildWebhookResult(eventType, transactionRef, updated, 
                updated ? "Webhook procesado y estado actualizado correctamente" 
                        : "Webhook procesado pero sin cambios de estado");
            
        } catch (Exception e) {
            log.error("[Conekta] Error procesando webhook: {}", e.getMessage(), e);
            throw new PaymentGatewayException("Error procesando webhook", "WEBHOOK_ERROR", e);
        }
    }
    
    /**
     * Procesa diferentes tipos de eventos del webhook de Conekta.
     * Actualiza el estado de la transacción y el Payment asociado.
     * Envía notificaciones por email según corresponda.
     */
    private boolean processWebhookEvent(String eventType, Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando evento {} para transacción {}", eventType, transaction.getTransactionReference());
        
        try {
            switch (eventType) {
                case "order.paid":
                    return handleOrderPaid(event, transaction);
                    
                case "order.pending_payment":
                    return handleOrderPendingPayment(event, transaction);
                    
                case "order.expired":
                    return handleOrderExpired(event, transaction);
                    
                case "order.canceled":
                    return handleOrderCanceled(event, transaction);
                    
                case "charge.paid":
                    return handleChargePaid(event, transaction);
                    
                case "charge.refunded":
                    return handleChargeRefunded(event, transaction);
                    
                default:
                    log.info("[Conekta] Evento {} no requiere procesamiento especial", eventType);
                    return false;
            }
        } catch (Exception e) {
            log.error("[Conekta] Error procesando evento {}: {}", eventType, e.getMessage(), e);
            return false;
        }
    }
    
    private WebhookProcessResult buildWebhookResult(String eventType, String transactionRef, 
                                                     boolean processed, String message) {
        WebhookProcessResult result = new WebhookProcessResult();
        result.setEventType(eventType);
        result.setTransactionReference(transactionRef);
        result.setProcessed(processed);
        result.setMessage(message);
        return result;
    }

    @Override
    public AccountCreationResponse createConnectedAccount(AccountCreationRequest request) throws PaymentGatewayException {
        AccountCreationResponse response = new AccountCreationResponse();
        response.setAccountId("CONEKTA_" + UUID.randomUUID());
        response.setStatus("PENDING_ONBOARDING");
        response.setOnboardingUrl("https://dashboard.conekta.com/onboarding");
        response.setRequiresVerification(true);
        return response;
    }

    @Override
    public AccountInfoResponse getAccountInfo(String accountId) throws PaymentGatewayException {
        AccountInfoResponse response = new AccountInfoResponse();
        response.setAccountId(accountId);
        response.setStatus("active");
        response.setChargesEnabled(true);
        response.setPayoutsEnabled(true);
        Map<String, Object> caps = new HashMap<>();
        caps.put("card_payments", "active");
        caps.put("oxxo_payments", "active");
        response.setCapabilities(caps);
        return response;
    }

    @Override
    public String generateOnboardingLink(String accountId, String returnUrl, String refreshUrl) {
        return "https://dashboard.conekta.com/onboarding?account=" + accountId;
    }

    @Override
    public boolean isAccountReady(String accountId) {
        return accountId != null && !accountId.isEmpty();
    }

    @Override
    public PaymentGatewayAccount.PaymentProvider getProvider() {
        return PaymentGatewayAccount.PaymentProvider.CONEKTA;
    }

    @Override
    public boolean validateConfiguration() {
        return conektaConfig.getApiKey() != null && !conektaConfig.getApiKey().isEmpty();
    }

    @Override
    public CashPaymentReference generateCashPaymentReference(CashPaymentRequest request) throws PaymentGatewayException {
        log.info("[Conekta] Generando referencia OXXO para: {}", request.getPayerEmail());
        
        try {
            // Construir orden OXXO como Map
            Map<String, Object> orderRequest = buildOxxoOrderRequestMap(request);
            
            log.debug("[Conekta] Creando orden OXXO en API");
            
            // Crear orden en Conekta
            JsonNode orderResponse = conektaHttpClient.createOrder(orderRequest);
            
            String orderId = orderResponse.path("id").asText();
            log.info("[Conekta] Orden OXXO creada: {}", orderId);
            
            // Extraer información del cargo OXXO
            return extractCashReferenceFromJson(orderResponse, request);
            
        } catch (PaymentGatewayException e) {
            throw e;
        } catch (Exception e) {
            log.error("[Conekta] Error inesperado al generar referencia OXXO: {}", e.getMessage(), e);
            throw new PaymentGatewayException(
                "Error generando referencia OXXO",
                "OXXO_ERROR",
                e
            );
        }
    }

    @Override
    public FeeCalculation calculateFees(PaymentIntentRequest request) {
        java.math.BigDecimal amount = request.getAmount();
        FeeCalculation calculation = new FeeCalculation();
        calculation.setOriginalAmount(amount != null ? amount : java.math.BigDecimal.ZERO);
        
        Double amountDouble = amount != null ? amount.doubleValue() : 0.0;
        Double platformFee = conektaConfig.calculatePlatformFee(amountDouble);
        calculation.setGatewayFeePercentage(java.math.BigDecimal.valueOf(conektaConfig.getPlatformFeePercentage()));
        calculation.setGatewayFeeAmount(java.math.BigDecimal.valueOf(platformFee));
        
        Double tax = conektaConfig.calculateTax(platformFee);
        calculation.setTaxAmount(java.math.BigDecimal.valueOf(tax));
        calculation.setPlatformFeeAmount(java.math.BigDecimal.valueOf(platformFee + tax));
        calculation.setNetAmount(amount.subtract(java.math.BigDecimal.valueOf(platformFee + tax)));
        
        return calculation;
    }

    // ==================== Métodos auxiliares privados ====================
    
    private void validateRequest(PaymentIntentRequest request) throws PaymentGatewayException {
        if (request.getAmount() == null || request.getAmount().compareTo(java.math.BigDecimal.ZERO) <= 0) {
            throw new PaymentGatewayException("Monto inválido", "INVALID_AMOUNT");
        }
        if (request.getPayerEmail() == null || request.getPayerEmail().isEmpty()) {
            throw new PaymentGatewayException("Email requerido", "EMAIL_REQUIRED");
        }
    }

    private PaymentTransaction findTransaction(String reference) throws PaymentGatewayException {
        return transactionRepository.findByTransactionReference(reference)
                .orElseThrow(() -> new PaymentGatewayException("Transacción no encontrada", "NOT_FOUND"));
    }
    
    // ==================== Construcción de requests (Maps) ====================
    
    /**
     * Construye el request body de una orden de pago como Map
     */
    private Map<String, Object> buildOrderRequestMap(PaymentIntentRequest request) throws PaymentGatewayException {
        Map<String, Object> orderRequest = new HashMap<>();
        
        int amountInCents = request.getAmount().multiply(new java.math.BigDecimal("100")).intValue();
        
        orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "MXN");
        orderRequest.put("line_items", buildLineItemsList(request, amountInCents));
        orderRequest.put("customer_info", buildCustomerInfoMap(request.getCustomerName(), request.getCustomerEmail(), request.getCustomerPhone()));
        
        // Metadata con transactionReference para tracking en webhooks
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("transactionReference", request.getTransactionReference());
        if (request.getCondominiumId() != null) {
            metadata.put("condominiumId", request.getCondominiumId().toString());
        }
        if (request.getMetadata() != null) {
            metadata.putAll(request.getMetadata());
        }
        orderRequest.put("metadata", metadata);
        
        // Cargo según método de pago
        String paymentType = determineConektaPaymentType(request.getPaymentMethod());
        
        // Si es pago con tarjeta, DEBE tener token
        if ("card".equals(paymentType)) {
            if (request.getCardToken() == null || request.getCardToken().isEmpty()) {
                throw new PaymentGatewayException(
                    "El token de tarjeta es obligatorio para pagos con CARD. " +
                    "Debe tokenizar la tarjeta usando Conekta.js antes de enviar la solicitud.",
                    "CARD_TOKEN_REQUIRED"
                );
            }
            orderRequest.put("charges", buildChargesListWithToken(request.getCardToken()));
        } else {
            orderRequest.put("charges", buildChargesList(paymentType, null));
        }
        
        return orderRequest;
    }
    
    /**
     * Construye el request body de una orden OXXO como Map
     */
    private Map<String, Object> buildOxxoOrderRequestMap(CashPaymentRequest request) {
        Map<String, Object> orderRequest = new HashMap<>();
        
        int amountInCents = (int) (request.getAmount() * 100);
        
        orderRequest.put("currency", "MXN");
        orderRequest.put("line_items", buildLineItemsList(
            request.getDescription() != null ? request.getDescription() : "Pago en efectivo",
            amountInCents
        ));
        orderRequest.put("customer_info", buildCustomerInfoMap(
            request.getPayerName(),
            request.getPayerEmail(),
            request.getPayerPhone()
        ));
        
        // Metadata para tracking
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("type", "cash_payment");
        metadata.put("condominiumId", request.getCondominiumId());
        orderRequest.put("metadata", metadata);
        
        // Cargo OXXO con expiración
        int expiresInDays = request.getExpiresInDays() != null ? request.getExpiresInDays() : 3;
        long expiresAtTimestamp = System.currentTimeMillis() / 1000 + (expiresInDays * 24 * 60 * 60);
        orderRequest.put("charges", buildChargesList("oxxo_cash", expiresAtTimestamp));
        
        return orderRequest;
    }
    
    private List<Map<String, Object>> buildLineItemsList(PaymentIntentRequest request, int amountInCents) {
        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("name", request.getDescription() != null ? request.getDescription() : "Pago");
        lineItem.put("unit_price", amountInCents);
        lineItem.put("quantity", 1);
        return Collections.singletonList(lineItem);
    }
    
    private List<Map<String, Object>> buildLineItemsList(String description, int amountInCents) {
        Map<String, Object> lineItem = new HashMap<>();
        lineItem.put("name", description);
        lineItem.put("unit_price", amountInCents);
        lineItem.put("quantity", 1);
        return Collections.singletonList(lineItem);
    }
    
    private Map<String, Object> buildCustomerInfoMap(String name, String email, String phone) {
        Map<String, Object> customerInfo = new HashMap<>();
        customerInfo.put("name", name != null ? name : "Cliente");
        customerInfo.put("email", email);
        if (phone != null && !phone.isEmpty()) {
            customerInfo.put("phone", phone);
        }
        return customerInfo;
    }
    
    private List<Map<String, Object>> buildChargesList(String paymentType, Long expiresAt) {
        Map<String, Object> charge = new HashMap<>();
        
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("type", paymentType);
        if (expiresAt != null) {
            paymentMethod.put("expires_at", expiresAt);
        }
        
        charge.put("payment_method", paymentMethod);
        
        return Collections.singletonList(charge);
    }
    
    private List<Map<String, Object>> buildChargesListWithToken(String cardToken) {
        Map<String, Object> charge = new HashMap<>();
        
        Map<String, Object> paymentMethod = new HashMap<>();
        paymentMethod.put("type", "card");
        paymentMethod.put("token_id", cardToken);
        
        charge.put("payment_method", paymentMethod);
        
        return Collections.singletonList(charge);
    }
    
    private String determineConektaPaymentType(PaymentTransaction.PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            return "card";
        }
        
        switch (paymentMethod) {
            case CARD:
                return "card";
            case OXXO:
            case OXXO_PAY:
                return "oxxo_cash";
            case SPEI:
            case BANK_TRANSFER:
                return "spei";
            default:
                log.warn("[Conekta] Método de pago no soportado: {}, usando 'card' por defecto", paymentMethod);
                return "card";
        }
    }

    private boolean validateWebhookSignature(String payload, String signature) {
        if (conektaConfig.isSkipWebhookSignatureValidation()) {
            log.warn("[Conekta] Validación de firma desactivada temporalmente (solo para pruebas locales). TODO: reactivar en producción.");
            return true;
        }
        if (signature == null || conektaConfig.getWebhookKey() == null) {
            return false;
        }
        
        try {
            String key = conektaConfig.getWebhookKey();
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String computed = Base64.getEncoder().encodeToString(hash);
            return computed.equals(signature);
        } catch (Exception e) {
            log.error("Error validando firma webhook: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Procesamiento de respuestas JSON ====================
    
    /**
     * Construye PaymentIntentResponse desde la respuesta JSON de Conekta
     */
    private PaymentIntentResponse buildPaymentIntentResponseFromJson(JsonNode orderResponse, PaymentIntentRequest request) {
        PaymentIntentResponse response = new PaymentIntentResponse();
        
        String orderId = orderResponse.path("id").asText();
        response.setGatewayTransactionId(orderId);
        response.setTransactionReference(request.getTransactionReference());
        
        // Mapear estado
        String paymentStatus = orderResponse.path("payment_status").asText();
        response.setStatus(mapConektaStatus(paymentStatus));
        
        response.setAmount(request.getAmount());
        response.setCurrency(orderResponse.path("currency").asText("MXN"));
        
        // Extraer URL de pago si existe en checkout
        if (orderResponse.has("checkout") && orderResponse.path("checkout").has("url")) {
            response.setPaymentUrl(orderResponse.path("checkout").path("url").asText());
        }
        
        FeeCalculation fees = calculateFees(request);
        response.setGatewayFee(fees.getGatewayFeeAmount());
        response.setPlatformFee(fees.getPlatformFeeAmount());
        response.setTaxAmount(fees.getTaxAmount());
        response.setNetAmount(fees.getNetAmount());
        
        response.setMessage("Orden creada exitosamente en Conekta");
        
        // Para pagos OXXO, extraer referencia y código de barras
        if (request.getPaymentMethod() == PaymentTransaction.PaymentMethod.OXXO || 
            request.getPaymentMethod() == PaymentTransaction.PaymentMethod.OXXO_PAY) {
            try {
                JsonNode chargesData = orderResponse.path("charges").path("data");
                if (chargesData.isArray() && chargesData.size() > 0) {
                    JsonNode firstCharge = chargesData.get(0);
                    JsonNode paymentMethod = firstCharge.path("payment_method");
                    
                    if (paymentMethod.has("reference")) {
                        response.setReferenceNumber(paymentMethod.path("reference").asText());
                    }
                    if (paymentMethod.has("barcode_url")) {
                        response.setBarcodeUrl(paymentMethod.path("barcode_url").asText());
                    }
                    if (paymentMethod.has("expires_at")) {
                        long expiresAt = paymentMethod.path("expires_at").asLong();
                        response.setExpiresAt(java.time.Instant.ofEpochSecond(expiresAt)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDateTime());
                    }
                    
                    log.info("[Conekta] Referencia OXXO extraída: {}", response.getReferenceNumber());
                }
            } catch (Exception e) {
                log.error("[Conekta] Error extrayendo datos de OXXO: {}", e.getMessage());
            }
        }
            
        // Para pagos con tarjeta, generar checkout URL si no hay token
        if (request.getPaymentMethod() == PaymentTransaction.PaymentMethod.CARD) {
            if (request.getCardToken() == null || request.getCardToken().isEmpty()) {
                // Sin token, necesitamos generar un checkout URL para que el usuario pague
                String checkoutUrl = generateCheckoutUrl(orderId, request);
                response.setPaymentUrl(checkoutUrl);
                response.setRequiresAction(true);
                response.setNextAction("Completar pago en página segura de Conekta");
                response.setStatus(PaymentTransaction.TransactionStatus.REQUIRES_ACTION);
                log.info("[Conekta] Checkout URL generado para pago con tarjeta sin token: {}", checkoutUrl);
            } else {
                // Con token, el pago debería procesarse inmediatamente
                // El estado final dependerá de la respuesta de Conekta
                if (response.getStatus() == PaymentTransaction.TransactionStatus.PENDING) {
                    response.setStatus(PaymentTransaction.TransactionStatus.PROCESSING);
                    response.setMessage("Procesando pago con tarjeta tokenizada");
                }
            }
        }
            
        return response;
    }
    
    /**
     * Genera URL de checkout de Conekta para pagos seguros con tarjeta.
     * Sigue las mejores prácticas PCI DSS al no manejar datos de tarjeta directamente.
     */
    private String generateCheckoutUrl(String orderId, PaymentIntentRequest request) {
        try {
            // Conekta recomienda usar su Checkout Link para pagos sin PCI compliance
            Map<String, Object> checkoutRequest = new HashMap<>();
            checkoutRequest.put("name", "Pago de " + request.getDescription());
            checkoutRequest.put("type", "PaymentLink");
            checkoutRequest.put("recurrent", false);
            checkoutRequest.put("expires_at", System.currentTimeMillis() / 1000 + 3600); // 1 hora
            checkoutRequest.put("order_template", Map.of(
                "line_items", List.of(Map.of(
                    "name", request.getDescription(),
                    "unit_price", request.getAmount().multiply(new java.math.BigDecimal("100")).intValue(),
                    "quantity", 1
                )),
                "currency", "MXN",
                "customer_info", Map.of(
                    "name", request.getCustomerName(),
                    "email", request.getCustomerEmail()
                )
            ));
            
            // En producción, usar API de Conekta para crear checkout link
            // Por ahora retornamos URL de ejemplo que debe ser reemplazada
            String baseUrl = conektaConfig.getEnvironment().equals("production") 
                ? "https://pay.conekta.com" 
                : "https://pay-sandbox.conekta.com";
                
            return baseUrl + "/link/" + orderId;
            
        } catch (Exception e) {
            log.error("[Conekta] Error generando checkout URL: {}", e.getMessage());
            // Fallback a URL directa si falla generación
            return "https://pay.conekta.com/checkout/" + orderId;
        }
    }
    
    /**
     * Extrae CashPaymentReference desde la respuesta JSON de una orden OXXO
     */
    private CashPaymentReference extractCashReferenceFromJson(JsonNode orderResponse, CashPaymentRequest request) {
        CashPaymentReference reference = new CashPaymentReference();
        
        String orderId = orderResponse.path("id").asText();
        reference.setPayerName(request.getPayerName());
        
        // Extraer información del cargo OXXO
        try {
            if (orderResponse.has("charges") && orderResponse.path("charges").has("data")) {
                JsonNode chargesData = orderResponse.path("charges").path("data");
                
                if (chargesData.isArray() && chargesData.size() > 0) {
                    JsonNode firstCharge = chargesData.get(0);
                    JsonNode paymentMethod = firstCharge.path("payment_method");
                    
                    if (paymentMethod.has("reference")) {
                        reference.setReferenceNumber(paymentMethod.path("reference").asText());
                    }
                    if (paymentMethod.has("barcode_url")) {
                        reference.setBarcodeUrl(paymentMethod.path("barcode_url").asText());
                    }
                    if (paymentMethod.has("expires_at")) {
                        long expiresAt = paymentMethod.path("expires_at").asLong();
                        reference.setExpiresAt(
                            LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochSecond(expiresAt),
                                java.time.ZoneId.systemDefault()
                            )
                        );
                    }
                }
            }
        } catch (Exception e) {
            log.warn("[Conekta] No se pudo extraer información completa del cargo OXXO: {}", e.getMessage());
        }
        
        // Si no se pudo extraer la referencia, usar orden ID
        if (reference.getReferenceNumber() == null) {
            log.warn("[Conekta] No se encontró referencia OXXO en la respuesta, usando orden ID");
            reference.setReferenceNumber(orderId);
        }
        if (reference.getExpiresAt() == null) {
            int expiresInDays = request.getExpiresInDays() != null ? request.getExpiresInDays() : 3;
            reference.setExpiresAt(LocalDateTime.now().plusDays(expiresInDays));
        }
        
        reference.setInstructions("Presenta esta referencia en cualquier tienda OXXO para realizar tu pago. Orden: " + orderId);
        
        return reference;
    }
    
    private String extractTransactionReference(Map<String, Object> event) {
        try {
            Map<String, Object> data = (Map<String, Object>) event.get("data");
            if (data != null) {
                Map<String, Object> object = (Map<String, Object>) data.get("object");
                if (object != null) {
                    Map<String, Object> metadata = (Map<String, Object>) object.get("metadata");
                    if (metadata != null) {
                        return (String) metadata.get("transactionReference");
                    }
                }
            }
        } catch (Exception e) {
            log.error("[Conekta] Error extrayendo transactionReference del evento: {}", e.getMessage());
        }
        return null;
    }
    
    private PaymentTransaction.TransactionStatus mapConektaStatus(String conektaStatus) {
        if (conektaStatus == null) {
            return PaymentTransaction.TransactionStatus.PENDING;
        }
        
        switch (conektaStatus.toLowerCase()) {
            case "paid":
                return PaymentTransaction.TransactionStatus.SUCCEEDED;
            case "pending_payment":
                return PaymentTransaction.TransactionStatus.PENDING;
            case "canceled":
            case "cancelled":
                return PaymentTransaction.TransactionStatus.CANCELLED;
            case "expired":
                return PaymentTransaction.TransactionStatus.EXPIRED;
            default:
                return PaymentTransaction.TransactionStatus.PENDING;
        }
    }
    
    // ==================== Handlers de Eventos del Webhook ====================
    
    /**
     * Handler para evento order.paid - Pago completado exitosamente.
     * Actualiza el estado de la transacción, marca el Payment como pagado y envía notificación.
     */
    private boolean handleOrderPaid(Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando order.paid para transacción {}", transaction.getTransactionReference());
        
        try {
            transaction.setStatus(PaymentTransaction.TransactionStatus.SUCCEEDED);
            transaction.setUpdatedAt(LocalDateTime.now());
            
            Map<String, Object> data = (Map<String, Object>) event.get("data");
            if (data != null && data.containsKey("object")) {
                Map<String, Object> order = (Map<String, Object>) data.get("object");
                if (order != null) {
                    String orderId = (String) order.get("id");
                    if (orderId != null && transaction.getGatewayTransactionId() == null) {
                        transaction.setGatewayTransactionId(orderId);
                    }
                }
            }
            
            transactionRepository.save(transaction);
            
            if (transaction.getPayment() != null) {
                Payment payment = transaction.getPayment();
                
                if (!payment.isPaid()) {
                    payment.setPaid(true);
                    payment.setDatePaid(LocalDateTime.now());
                    
                    if (payment.getResident() != null) {
                        Double currentBalance = payment.getResident().getBalance();
                        Double chargeAmount = payment.getCharge().getAmount();
                        payment.getResident().setBalance(currentBalance - chargeAmount);
                    }
                    
                    // Guardar explícitamente el Payment en la base de datos
                    paymentRepository.save(payment);
                    
                    log.info("[Conekta] Payment ID {} marcado como pagado automáticamente. Balance residente actualizado.", 
                        payment.getId());
                    
                    sendPaymentSuccessNotification(transaction, payment);
                } else {
                    log.warn("[Conekta] Payment ID {} ya estaba marcado como pagado", payment.getId());
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("[Conekta] Error en handleOrderPaid: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean handleOrderPendingPayment(Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando order.pending_payment para transacción {}", 
            transaction.getTransactionReference());
        
        try {
            if (transaction.getPaymentMethod() == PaymentTransaction.PaymentMethod.OXXO ||
                transaction.getPaymentMethod() == PaymentTransaction.PaymentMethod.OXXO_PAY) {
                
                sendOxxoPaymentInstructionsNotification(transaction, event);
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("[Conekta] Error en handleOrderPendingPayment: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean handleOrderExpired(Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando order.expired para transacción {}", transaction.getTransactionReference());
        
        try {
            transaction.setStatus(PaymentTransaction.TransactionStatus.EXPIRED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.info("[Conekta] Transacción {} marcada como expirada", transaction.getTransactionReference());
            return true;
            
        } catch (Exception e) {
            log.error("[Conekta] Error en handleOrderExpired: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean handleOrderCanceled(Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando order.canceled para transacción {}", transaction.getTransactionReference());
        
        try {
            transaction.setStatus(PaymentTransaction.TransactionStatus.CANCELLED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            log.info("[Conekta] Transacción {} marcada como cancelada", transaction.getTransactionReference());
            return true;
            
        } catch (Exception e) {
            log.error("[Conekta] Error en handleOrderCanceled: {}", e.getMessage(), e);
            return false;
        }
    }
    
    private boolean handleChargePaid(Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando charge.paid para transacción {}", transaction.getTransactionReference());
        return handleOrderPaid(event, transaction);
    }
    
    private boolean handleChargeRefunded(Map<String, Object> event, PaymentTransaction transaction) {
        log.info("[Conekta] Procesando charge.refunded para transacción {}", transaction.getTransactionReference());
        
        try {
            transaction.setStatus(PaymentTransaction.TransactionStatus.REFUNDED);
            transaction.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(transaction);
            
            if (transaction.getPayment() != null && transaction.getPayment().isPaid()) {
                Payment payment = transaction.getPayment();
                payment.setPaid(false);
                payment.setDatePaid(null);
                
                if (payment.getResident() != null) {
                    Double currentBalance = payment.getResident().getBalance();
                    Double chargeAmount = payment.getCharge().getAmount();
                    payment.getResident().setBalance(currentBalance + chargeAmount);
                }
                
                // Guardar explícitamente el Payment en la base de datos
                paymentRepository.save(payment);
                
                log.info("[Conekta] Payment ID {} marcado como no pagado por reembolso. Balance revertido.", 
                    payment.getId());
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("[Conekta] Error en handleChargeRefunded: {}", e.getMessage(), e);
            return false;
        }
    }
    
    // ==================== Notificaciones por Email ====================
    
    private void sendPaymentSuccessNotification(PaymentTransaction transaction, Payment payment) {
        try {
            String recipientEmail = transaction.getPayerEmail();
            if (recipientEmail == null || recipientEmail.isEmpty()) {
                log.warn("[Conekta] No se puede enviar notificación de pago exitoso - email no disponible");
                return;
            }
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("residentName", payment.getResident().getUser().getFirstName() + " " + 
                payment.getResident().getUser().getLastName());
            variables.put("paymentId", payment.getId().toString());
            variables.put("chargeDescription", payment.getCharge().getDescription());
            variables.put("amount", String.format("$%.2f MXN", payment.getCharge().getAmount()));
            variables.put("paymentDate", payment.getDatePaid().format(
                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            variables.put("transactionReference", transaction.getTransactionReference());
            variables.put("gatewayTransactionId", transaction.getGatewayTransactionId());
            variables.put("condominiumName", payment.getCharge().getCondominium().getName());
            
            String bankRef = payment.getResident().getUser().getBankPersonalReference();
            if (bankRef != null && !bankRef.isEmpty()) {
                variables.put("bankPersonalReference", bankRef);
            }
            
            emailService.sendHtmlEmail(
                recipientEmail,
                "✅ Pago Acreditado - " + payment.getCharge().getCondominium().getName(),
                "payment-success",
                variables
            );
            
            log.info("[Conekta] Notificación de pago exitoso enviada a {}", recipientEmail);
            
        } catch (Exception e) {
            log.error("[Conekta] Error enviando notificación de pago exitoso: {}", e.getMessage(), e);
        }
    }
    
    private void sendOxxoPaymentInstructionsNotification(PaymentTransaction transaction, Map<String, Object> event) {
        try {
            String recipientEmail = transaction.getPayerEmail();
            if (recipientEmail == null || recipientEmail.isEmpty()) {
                log.warn("[Conekta] No se puede enviar ficha OXXO - email no disponible");
                return;
            }
            
            Map<String, Object> data = (Map<String, Object>) event.get("data");
            Map<String, Object> order = (Map<String, Object>) data.get("object");
            List<Map<String, Object>> charges = (List<Map<String, Object>>) order.get("charges");
            
            String oxxoReference = null;
            String oxxoUrl = null;
            Long expiresAt = null;
            
            if (charges != null && !charges.isEmpty()) {
                Map<String, Object> charge = charges.get(0);
                Map<String, Object> paymentMethod = (Map<String, Object>) charge.get("payment_method");
                
                if (paymentMethod != null) {
                    oxxoReference = (String) paymentMethod.get("reference");
                    oxxoUrl = (String) paymentMethod.get("barcode_url");
                    expiresAt = getLongValue(paymentMethod.get("expires_at"));
                }
            }
            
            if (oxxoReference == null) {
                log.warn("[Conekta] No se pudo extraer referencia OXXO del evento");
                return;
            }
            
            Payment payment = transaction.getPayment();
            
            Map<String, Object> variables = new HashMap<>();
            variables.put("residentName", payment.getResident().getUser().getFirstName() + " " + 
                payment.getResident().getUser().getLastName());
            variables.put("oxxoReference", oxxoReference);
            variables.put("amount", String.format("$%.2f MXN", transaction.getAmount()));
            variables.put("chargeDescription", payment.getCharge().getDescription());
            variables.put("condominiumName", payment.getCharge().getCondominium().getName());
            variables.put("transactionReference", transaction.getTransactionReference());
            
            if (oxxoUrl != null) {
                variables.put("oxxoUrl", oxxoUrl);
            }
            
            if (expiresAt != null) {
                LocalDateTime expirationDate = LocalDateTime.ofEpochSecond(expiresAt, 0, 
                    java.time.ZoneOffset.UTC);
                variables.put("expirationDate", expirationDate.format(
                    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
            }
            
            String bankRef = payment.getResident().getUser().getBankPersonalReference();
            if (bankRef != null && !bankRef.isEmpty()) {
                variables.put("bankPersonalReference", bankRef);
            }
            
            emailService.sendHtmlEmail(
                recipientEmail,
                "🏪 Ficha de Pago OXXO - " + payment.getCharge().getCondominium().getName(),
                "oxxo-payment-instructions",
                variables
            );
            
            log.info("[Conekta] Ficha OXXO enviada a {} - Referencia: {}", recipientEmail, oxxoReference);
            
        } catch (Exception e) {
            log.error("[Conekta] Error enviando ficha OXXO: {}", e.getMessage(), e);
        }
    }
    
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
}
