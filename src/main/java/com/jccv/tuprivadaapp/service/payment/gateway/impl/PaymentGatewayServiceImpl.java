package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.dto.payment.gateway.*;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentAuditLog;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentAuditLogRepository;
import com.jccv.tuprivadaapp.service.email.EmailService;
import com.jccv.tuprivadaapp.service.payment.PaymentService;
import com.jccv.tuprivadaapp.service.payment.gateway.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentGatewayServiceImpl implements PaymentGatewayService {

    private static final String PAYMENT_CONFIRMATION_TEMPLATE = "emails/payment-confirmation";

    private final PaymentGatewayRegistry gatewayRegistry;
    private final PaymentGatewayAccountService gatewayAccountService;
    private final PaymentTransactionService transactionService;
    private final PaymentAuditLogRepository auditLogRepository;
    private final PaymentService paymentService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private final com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository transactionRepository;

    @Override
    @Transactional
    public PaymentIntentResponse createPaymentIntent(PaymentIntentRequest request) throws PaymentGatewayException {
        Assert.notNull(request, "El request de pago no puede ser nulo");

        PaymentTransaction transaction = null;
        try {
            PaymentGatewayAccount.PaymentProvider provider = Optional.ofNullable(request.getProvider())
                    .orElseThrow(() -> new PaymentGatewayException("Debe indicar el proveedor de pago", "PROVIDER_REQUIRED"));

            PaymentGatewayAccount gatewayAccount = gatewayAccountService.getActiveAccount(
                    request.getCondominiumId(), provider);

            PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(provider);
            validateGateway(strategy, gatewayAccount);

            transaction = transactionService.createPendingTransaction(request, gatewayAccount, request.getTransactionReference());
            PaymentIntentRequest enrichedRequest = request.toBuilder()
                    .transactionReference(transaction.getTransactionReference())
                    .build();

            PaymentIntentResponse response = strategy.createPaymentIntent(enrichedRequest);
            if (response.getTransactionReference() == null) {
                response.setTransactionReference(transaction.getTransactionReference());
            }

            transactionService.syncWithGatewayResponse(transaction.getTransactionReference(), response);
            registerAudit(transaction.getId(), PaymentAuditLog.AuditAction.TRANSACTION_INITIATED,
                    "Intención de pago creada en " + provider, null, response);
            log.info("[Payments] Intención {} creada satisfactoriamente en {}", transaction.getTransactionReference(), provider);
            return response;
        } catch (PaymentGatewayException ex) {
            handleFailure(transaction, ex.getMessage(), ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            handleFailure(transaction, ex.getMessage(), "UNEXPECTED_ERROR");
            throw wrapException("Error inesperado al crear la intención de pago", "PAYMENT_INTENT_ERROR", ex);
        }
    }

    @Override
    @Transactional
    public PaymentConfirmationResponse confirmPayment(String transactionReference, PaymentConfirmationRequest request) throws PaymentGatewayException {
        PaymentTransaction transaction = findTransactionOrThrow(transactionReference);
        try {
            PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(transaction.getGatewayAccount().getProvider());

            PaymentConfirmationResponse response = strategy.confirmPayment(transactionReference, request);
            if (response.getStatus() != null) {
                PaymentTransaction updated = transactionService.updateStatus(
                        transactionReference,
                        response.getStatus(),
                        transaction.getGatewayTransactionId(),
                        response.getMessage(),
                        null
                );

                if (updated.isSuccessful()) {
                    finalizeSuccessfulPayment(updated);
                }
            }

            registerAudit(transaction.getId(), PaymentAuditLog.AuditAction.TRANSACTION_CAPTURED,
                    "Transacción confirmada manualmente", null, response);
            return response;
        } catch (PaymentGatewayException ex) {
            handleFailure(transaction, ex.getMessage(), ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            handleFailure(transaction, ex.getMessage(), "UNEXPECTED_ERROR");
            throw wrapException("Error inesperado al confirmar el pago", "PAYMENT_CONFIRMATION_ERROR", ex);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionStatusResponse getTransactionStatus(String transactionReference) throws PaymentGatewayException {
        PaymentTransaction transaction = findTransactionOrThrow(transactionReference);
        try {
            PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(transaction.getGatewayAccount().getProvider());
            TransactionStatusResponse response = strategy.getTransactionStatus(transactionReference);

            if (response.getStatus() != null && response.getStatus() != transaction.getStatus()) {
                transactionService.updateStatus(transactionReference, response.getStatus(),
                        transaction.getGatewayTransactionId(), response.getFailureReason(), null);
            }

            registerAudit(transaction.getId(), PaymentAuditLog.AuditAction.TRANSACTION_AUTHORIZED,
                    "Consulta de estado remota", transaction.getStatus(), response.getStatus());
            return response;
        } catch (PaymentGatewayException ex) {
            throw ex;
        } catch (Exception ex) {
            throw wrapException("Error inesperado al consultar el estado de la transacción",
                    "PAYMENT_STATUS_ERROR", ex);
        }
    }

    @Override
    @Transactional
    public RefundResponse refundPayment(String transactionReference, RefundRequest request) throws PaymentGatewayException {
        PaymentTransaction transaction = findTransactionOrThrow(transactionReference);
        try {
            PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(transaction.getGatewayAccount().getProvider());
            RefundResponse response = strategy.refundPayment(transactionReference, request);

            transactionService.updateStatus(transactionReference,
                    PaymentTransaction.TransactionStatus.REFUNDED,
                    transaction.getGatewayTransactionId(),
                    "Refund: " + response.getMessage(),
                    null);

            registerAudit(transaction.getId(), PaymentAuditLog.AuditAction.TRANSACTION_REFUNDED,
                    "Reembolso procesado", null, response);
            return response;
        } catch (PaymentGatewayException ex) {
            handleFailure(transaction, ex.getMessage(), ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            handleFailure(transaction, ex.getMessage(), "UNEXPECTED_ERROR");
            throw wrapException("Error al procesar el reembolso", "REFUND_ERROR", ex);
        }
    }

    @Override
    @Transactional
    public WebhookProcessResult processWebhook(String provider, String payload, String signature) throws PaymentGatewayException {
        PaymentGatewayAccount.PaymentProvider paymentProvider = resolveProvider(provider);
        try {
            PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(paymentProvider);
            WebhookProcessResult result = strategy.processWebhook(payload, signature);

            if (result != null && result.getTransactionReference() != null) {
                PaymentTransaction transaction = transactionService.findByReference(result.getTransactionReference())
                        .orElse(null);
                if (transaction != null) {
                    registerAudit(transaction.getId(),
                            result.isProcessed() ? PaymentAuditLog.AuditAction.WEBHOOK_PROCESSED : PaymentAuditLog.AuditAction.WEBHOOK_RECEIVED,
                            "Webhook " + result.getEventType(), null, result);
                    // El estado del Payment ya se actualiza dentro de ConektaPaymentGatewayStrategy.handleOrderPaid()
                    // Solo enviamos email de confirmación si es necesario
                    if (result.isProcessed() && transaction.getStatus() == PaymentTransaction.TransactionStatus.SUCCEEDED) {
                        // El email ya se envía en ConektaPaymentGatewayStrategy
                        log.info("[PaymentGateway] Webhook procesado exitosamente para transacción {}", 
                                result.getTransactionReference());
                    }
                }
            }
            return result;
        } catch (PaymentGatewayException ex) {
            throw ex;
        } catch (Exception ex) {
            throw wrapException("Error al procesar el webhook para " + provider, "WEBHOOK_ERROR", ex);
        }
    }

    private void validateGateway(PaymentGatewayStrategy strategy, PaymentGatewayAccount account) throws PaymentGatewayException {
        if (!strategy.validateConfiguration()) {
            throw new PaymentGatewayException("Configuración incompleta para la pasarela " + strategy.getProvider(),
                    "GATEWAY_CONFIGURATION");
        }
        if (!strategy.isAccountReady(account.getAccountId())) {
            throw new PaymentGatewayException("La cuenta conectada aún no está lista para operar",
                    "GATEWAY_ACCOUNT_NOT_READY");
        }
    }

    private PaymentTransaction findTransactionOrThrow(String transactionReference) throws PaymentGatewayException {
        return transactionService.findByReference(transactionReference)
                .orElseThrow(() -> new PaymentGatewayException(
                        "Transacción no encontrada con referencia: " + transactionReference,
                        "TRANSACTION_NOT_FOUND"));
    }

    private void finalizeSuccessfulPayment(PaymentTransaction transaction) {
        Payment payment = transaction.getPayment();
        if (payment == null) {
            log.warn("[Payments] La transacción {} no tiene pago asociado", transaction.getTransactionReference());
            return;
        }

        paymentService.updateIsPaidStatus(payment.getCharge().getId(), payment.getResident().getId(), true);
        sendPaymentConfirmationEmail(payment, transaction);
    }

    private void sendPaymentConfirmationEmail(Payment payment, PaymentTransaction transaction) {
        try {
            String recipient = payment.getResident().getUser().getEmail();
            Map<String, Object> variables = new HashMap<>();
            variables.put("residentName", payment.getResident().getUser().getFirstName());
            variables.put("amount", transaction.getAmount());
            variables.put("currency", transaction.getCurrency());
            variables.put("reference", transaction.getTransactionReference());
            variables.put("method", transaction.getPaymentMethod());
            variables.put("processedAt", LocalDateTime.now());

            emailService.sendHtmlEmail(recipient,
                    "Confirmación de pago - " + payment.getCharge().getTitleTypePayment(),
                    PAYMENT_CONFIRMATION_TEMPLATE,
                    variables);
        } catch (Exception ex) {
            log.error("[Payments] No se pudo enviar el correo de confirmación del pago {}: {}",
                    transaction.getTransactionReference(), ex.getMessage());
        }
    }

    private void registerAudit(Long transactionId,
                               PaymentAuditLog.AuditAction action,
                               String description,
                               Object oldValue,
                               Object newValue) {
        try {
            PaymentAuditLog logEntry = PaymentAuditLog.builder()
                    .transactionId(transactionId)
                    .action(action)
                    .entityType("PaymentTransaction")
                    .entityId(transactionId)
                    .description(description)
                    .oldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null)
                    .newValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null)
                    .build();
            auditLogRepository.save(logEntry);
        } catch (JsonProcessingException e) {
            log.warn("[Payments] No se pudo serializar la información de auditoría: {}", e.getMessage());
        }
    }

    private void handleFailure(PaymentTransaction transaction, String message, String errorCode) {
        if (transaction != null) {
            transactionService.recordError(transaction.getTransactionReference(), errorCode, message);
            registerAudit(transaction.getId(), PaymentAuditLog.AuditAction.TRANSACTION_FAILED, message, null, null);
        }
    }

    private PaymentGatewayException wrapException(String message, String errorCode, Exception ex) {
        PaymentGatewayException exception = new PaymentGatewayException(message, errorCode, ex);
        exception.setGatewayErrorCode(errorCode);
        return exception;
    }

    private PaymentGatewayAccount.PaymentProvider resolveProvider(String provider) throws PaymentGatewayException {
        try {
            return PaymentGatewayAccount.PaymentProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new PaymentGatewayException("Proveedor de webhook no soportado: " + provider,
                    "PROVIDER_NOT_SUPPORTED", ex);
        }
    }
    
    @Override
    @Transactional
    public PaymentIntentResponse initiatePaymentFromExisting(PaymentInitiationRequest request) throws PaymentGatewayException {
        Assert.notNull(request, "El request de pago no puede ser nulo");
        Assert.notNull(request.getPaymentId(), "El ID del pago es obligatorio");
        Assert.notNull(request.getEmail(), "El email es obligatorio para notificaciones");
        
        PaymentTransaction transaction = null;
        try {
            Payment payment = paymentService.getPaymentEntityById(request.getPaymentId());
            
            // Validación estricta: no permitir duplicación de pagos
            if (payment.isPaid()) {
                log.warn("[Payments] Intento de duplicar pago ya completado - Payment ID: {}", payment.getId());
                throw new PaymentGatewayException(
                    "Este pago ya ha sido completado. No se puede generar una nueva orden de pago para evitar duplicación.",
                    "PAYMENT_ALREADY_PAID"
                );
            }
            
            // Buscar transacciones pendientes o en proceso para este pago
            Optional<PaymentTransaction> existingPendingTransaction = 
                transactionRepository.findFirstByPaymentIdAndStatusIn(
                    payment.getId(), 
                    List.of(PaymentTransaction.TransactionStatus.PENDING, 
                           PaymentTransaction.TransactionStatus.PROCESSING)
                );
            
            // Si existe una transacción pendiente, verificar si es válida o expiró
            if (existingPendingTransaction.isPresent()) {
                PaymentTransaction existingTx = existingPendingTransaction.get();
                
                // Si es OXXO y tiene fecha de expiración
                if (existingTx.getPaymentMethod() == PaymentTransaction.PaymentMethod.OXXO && 
                    existingTx.getExpiresAt() != null) {
                    
                    // Si ya expiró, permitir crear una nueva
                    if (existingTx.getExpiresAt().isBefore(LocalDateTime.now())) {
                        log.info("[Payments] Referencia OXXO expirada para Payment ID: {}, permitiendo crear nueva", 
                                payment.getId());
                        // Marcar la transacción expirada como EXPIRED
                        transactionService.updateStatus(
                            existingTx.getTransactionReference(),
                            PaymentTransaction.TransactionStatus.EXPIRED,
                            existingTx.getGatewayTransactionId(),
                            "Referencia OXXO expirada automáticamente",
                            null
                        );
                    } else {
                        // Si aún es válida, retornar la referencia existente
                        log.info("[Payments] Reutilizando referencia OXXO válida para Payment ID: {}", payment.getId());
                        
                        PaymentIntentResponse response = new PaymentIntentResponse();
                        response.setTransactionReference(existingTx.getTransactionReference());
                        response.setGatewayTransactionId(existingTx.getGatewayTransactionId());
                        response.setStatus(existingTx.getStatus());
                        response.setAmount(existingTx.getAmount());
                        response.setCurrency(existingTx.getCurrency());
                        response.setReferenceNumber(existingTx.getReferenceNumber());
                        response.setBarcodeUrl(existingTx.getBarcodeUrl());
                        response.setExpiresAt(existingTx.getExpiresAt());
                        response.setMessage("Referencia OXXO existente reutilizada");
                        
                        return response;
                    }
                } else {
                    // Para otros métodos de pago (CARD, etc.), no permitir duplicados
                    log.warn("[Payments] Ya existe una transacción pendiente para Payment ID: {}", payment.getId());
                    throw new PaymentGatewayException(
                        "Ya existe una orden de pago pendiente para este pago. Por favor, completa o cancela la transacción existente antes de crear una nueva.",
                        "PENDING_TRANSACTION_EXISTS"
                    );
                }
            }
            
            PaymentGatewayAccount.PaymentProvider provider = Optional.ofNullable(request.getProvider())
                    .orElse(PaymentGatewayAccount.PaymentProvider.CONEKTA);
            
            // Intentar obtener cuenta activa, si no existe usar configuración por defecto
            PaymentGatewayAccount gatewayAccount;
            try {
                gatewayAccount = gatewayAccountService.getActiveAccount(
                        payment.getCharge().getCondominium().getId(), provider);
            } catch (ResourceNotFoundException e) {
                log.warn("[Payments] No hay cuenta de pasarela configurada para condominio {}, usando configuración por defecto", 
                        payment.getCharge().getCondominium().getId());
                // Obtener o crear cuenta por defecto (se commitea en transacción separada)
                gatewayAccount = gatewayAccountService.getOrCreateDefaultAccount(
                        payment.getCharge().getCondominium().getId(), provider);
            }
            
            PaymentGatewayStrategy strategy = gatewayRegistry.getStrategy(provider);
            validateGateway(strategy, gatewayAccount);
            
            String transactionRef = transactionService.generateTransactionReference();
            transaction = transactionService.createPendingTransaction(
                    buildPaymentIntentRequest(payment, request, transactionRef),
                    gatewayAccount,
                    transactionRef
            );
            
            transaction.setPayment(payment);
            
            PaymentIntentRequest gatewayRequest = buildPaymentIntentRequest(payment, request, transactionRef);
            PaymentIntentResponse response = strategy.createPaymentIntent(gatewayRequest);
            
            if (response.getTransactionReference() == null) {
                response.setTransactionReference(transactionRef);
            }
            
            transactionService.syncWithGatewayResponse(transactionRef, response);
            registerAudit(transaction.getId(), PaymentAuditLog.AuditAction.TRANSACTION_INITIATED,
                    "Intención de pago creada para Payment ID: " + payment.getId(), null, response);
            
            log.info("[Payments] Intención {} creada para Payment ID {} en {}", 
                    transactionRef, payment.getId(), provider);
            
            return response;
            
        } catch (PaymentGatewayException ex) {
            handleFailure(transaction, ex.getMessage(), ex.getErrorCode());
            throw ex;
        } catch (Exception ex) {
            handleFailure(transaction, ex.getMessage(), "UNEXPECTED_ERROR");
            throw wrapException("Error inesperado al iniciar pago desde Payment existente", 
                    "PAYMENT_INITIATION_ERROR", ex);
        }
    }
    
    private PaymentIntentRequest buildPaymentIntentRequest(Payment payment, PaymentInitiationRequest initiationRequest, 
                                                            String transactionRef) {
        return PaymentIntentRequest.builder()
                .transactionReference(transactionRef)
                .paymentId(payment.getId())
                .amount(java.math.BigDecimal.valueOf(payment.getCharge().getAmount()))
                .currency("MXN")
                .paymentMethod(initiationRequest.getPaymentMethod())
                .provider(Optional.ofNullable(initiationRequest.getProvider())
                        .orElse(PaymentGatewayAccount.PaymentProvider.CONEKTA))
                .condominiumId(payment.getCharge().getCondominium().getId())
                .description(payment.getCharge().getDescription())
                .customerEmail(initiationRequest.getEmail())
                .customerName(payment.getResident().getUser().getFirstName() + " " + 
                             payment.getResident().getUser().getLastName())
                .customerPhone(payment.getResident().getUser().getPhone())
                .cardToken(initiationRequest.getCardToken())
                .deviceFingerprint(initiationRequest.getDeviceFingerprint())
                .ipAddress(initiationRequest.getIpAddress())
                .metadata(buildProfessionalMetadata(payment, transactionRef, initiationRequest))
                .build();
    }
    
    /**
     * Construye metadata profesional para enviar a la pasarela de pago.
     * Incluye información crítica para auditoría, conciliación bancaria y trazabilidad.
     */
    private Map<String, String> buildProfessionalMetadata(Payment payment, String transactionRef, 
                                                          PaymentInitiationRequest request) {
        Map<String, String> metadata = new HashMap<>();
        
        // Identificadores internos para conciliación
        metadata.put("transactionReference", transactionRef);
        metadata.put("paymentId", payment.getId().toString());
        metadata.put("chargeId", payment.getCharge().getId().toString());
        metadata.put("residentId", payment.getResident().getId().toString());
        metadata.put("condominiumId", payment.getCharge().getCondominium().getId().toString());
        
        // Referencia bancaria del residente (crítica para conciliación)
        String bankReference = payment.getResident().getUser().getBankPersonalReference();
        if (bankReference != null && !bankReference.isEmpty()) {
            metadata.put("bankPersonalReference", bankReference);
        }
        
        // Información del período y concepto de pago
        metadata.put("chargeDescription", payment.getCharge().getDescription());
        metadata.put("chargeAmount", String.valueOf(payment.getCharge().getAmount()));
        metadata.put("chargeTitleType", payment.getCharge().getTitleTypePayment() != null ? 
            payment.getCharge().getTitleTypePayment() : "Cargo General");
        
        // Fechas importantes para auditoría
        metadata.put("chargeDueDate", payment.getCharge().getDueDate() != null ? 
            payment.getCharge().getDueDate().toString() : "N/A");
        metadata.put("chargeDate", payment.getCharge().getChargeDate() != null ? 
            payment.getCharge().getChargeDate().toString() : LocalDateTime.now().toString());
        metadata.put("transactionInitiatedAt", LocalDateTime.now().toString());
        
        // Información del residente para identificación
        metadata.put("residentName", payment.getResident().getUser().getFirstName() + " " + 
            payment.getResident().getUser().getLastName());
        metadata.put("residentEmail", request.getEmail());
        
        // Método de pago y ambiente
        metadata.put("paymentMethod", request.getPaymentMethod().toString());
        metadata.put("environment", "sandbox"); // TODO: parametrizar según configuración
        
        // Información adicional para prevención de fraude
        if (request.getIpAddress() != null) {
            metadata.put("clientIp", request.getIpAddress());
        }
        if (request.getDeviceFingerprint() != null) {
            metadata.put("deviceFingerprint", request.getDeviceFingerprint());
        }
        
        log.info("[Payments] Metadata profesional generada para transacción {}: {} campos", 
            transactionRef, metadata.size());
        
        return metadata;
    }
}
