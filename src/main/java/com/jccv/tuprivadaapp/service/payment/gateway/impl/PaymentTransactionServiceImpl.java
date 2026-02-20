package com.jccv.tuprivadaapp.service.payment.gateway.impl;

import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentIntentRequest;
import com.jccv.tuprivadaapp.dto.payment.gateway.PaymentIntentResponse;
import com.jccv.tuprivadaapp.dto.payment.gateway.TransactionDetailResponse;
import com.jccv.tuprivadaapp.dto.payment.gateway.TransactionListItemDTO;
import com.jccv.tuprivadaapp.dto.payment.gateway.TransactionStatusHistoryDTO;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentAuditLog;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentGatewayAccount;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jccv.tuprivadaapp.repository.payment.PaymentRepository;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentAuditLogRepository;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentGatewayAccountRepository;
import com.jccv.tuprivadaapp.repository.payment.gateway.PaymentTransactionRepository;
import com.jccv.tuprivadaapp.service.payment.gateway.PaymentTransactionService;
import com.jccv.tuprivadaapp.service.payment.gateway.reference.TransactionReferenceGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentTransactionServiceImpl implements PaymentTransactionService {

    private final PaymentRepository paymentRepository;
    private final PaymentTransactionRepository transactionRepository;
    private final PaymentAuditLogRepository auditLogRepository;
    private final PaymentGatewayAccountRepository gatewayAccountRepository;
    private final TransactionReferenceGenerator referenceGenerator;
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    // Acciones de auditoría relacionadas con cambios de estado de transacciones
    private static final Set<PaymentAuditLog.AuditAction> TRANSACTION_STATUS_ACTIONS = Set.of(
            PaymentAuditLog.AuditAction.TRANSACTION_INITIATED,
            PaymentAuditLog.AuditAction.TRANSACTION_AUTHORIZED,
            PaymentAuditLog.AuditAction.TRANSACTION_CAPTURED,
            PaymentAuditLog.AuditAction.TRANSACTION_FAILED,
            PaymentAuditLog.AuditAction.TRANSACTION_CANCELLED,
            PaymentAuditLog.AuditAction.TRANSACTION_REFUNDED
    );

    @Override
    @Transactional
    public PaymentTransaction createPendingTransaction(PaymentIntentRequest request,
                                                       PaymentGatewayAccount gatewayAccount,
                                                       String transactionReference) {

        Payment payment = null;
        if (request.getPaymentId() != null) {
            Long paymentId = request.getPaymentId();
            payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el pago con id: " + paymentId));
        }

        String reference = transactionReference != null ? transactionReference : referenceGenerator.generate();
        log.info("[Payments] Creando transacción {} para pago {} y condominio {}",
                reference,
                payment != null ? payment.getId() : "N/A",
                request.getCondominiumId());

        PaymentTransaction.PaymentTransactionBuilder builder = PaymentTransaction.builder()
                .transactionReference(reference)
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentTransaction.TransactionStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .payerEmail(request.getPayerEmail())
                .payerName(request.getPayerName())
                .payerPhone(request.getPayerPhone())
                .bankPersonalReference(request.getBankPersonalReference())
                .street(request.getStreet())
                .houseNumber(request.getHouseNumber())
                .interiorNumber(request.getInteriorNumber())
                .confirmationUrl(request.getSuccessUrl())
                .webhookUrl(request.getWebhookUrl())
                .ipAddress(request.getClientIp())
                .userAgent(request.getUserAgent())
                .deviceFingerprint(request.getDeviceFingerprint())
                .createdAt(LocalDateTime.now());

        if (payment != null) {
            builder.payment(payment);
        }
        if (gatewayAccount.getId() != null) {
            builder.gatewayAccount(gatewayAccount);
        } else {
            // evitar referenciar entidad sin ID
            final String accountIdRef = gatewayAccount.getAccountId();
            gatewayAccount = gatewayAccountRepository.findByAccountId(accountIdRef)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró cuenta de pasarela con accountId: " + accountIdRef));
            builder.gatewayAccount(gatewayAccount);
        }

        PaymentTransaction transaction = builder.build();

        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public PaymentTransaction updateStatus(String transactionReference,
                                           PaymentTransaction.TransactionStatus status,
                                           String gatewayTransactionId,
                                           String message,
                                           String responseRaw) {
        PaymentTransaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transacción no encontrada con referencia: " + transactionReference));

        transaction.setStatus(status);
        transaction.setGatewayTransactionId(gatewayTransactionId);
        transaction.setResponseRaw(responseRaw);
        transaction.setErrorMessage(message);

        if (status == PaymentTransaction.TransactionStatus.SUCCEEDED
                || status == PaymentTransaction.TransactionStatus.CAPTURED) {
            transaction.setConfirmedAt(LocalDateTime.now());
        } else if (status == PaymentTransaction.TransactionStatus.FAILED) {
            transaction.setRetryCount(Optional.ofNullable(transaction.getRetryCount()).orElse(0) + 1);
        }

        log.info("[Payments] Transacción {} actualizada a estatus {}", transactionReference, status);
        return transactionRepository.save(transaction);
    }

    @Override
    @Transactional
    public void recordError(String transactionReference, String errorCode, String errorMessage) {
        transactionRepository.findByTransactionReference(transactionReference).ifPresent(tx -> {
            tx.setErrorCode(errorCode);
            tx.setErrorMessage(errorMessage);
            tx.setUpdatedAt(LocalDateTime.now());
            transactionRepository.save(tx);
            log.warn("[Payments] Error en transacción {}: {} - {}", transactionReference, errorCode, errorMessage);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PaymentTransaction> findByReference(String transactionReference) {
        return transactionRepository.findByTransactionReference(transactionReference);
    }

    @Override
    @Transactional
    public void incrementRetryCount(String transactionReference) {
        transactionRepository.findByTransactionReference(transactionReference).ifPresent(tx -> {
            int newRetryCount = Optional.ofNullable(tx.getRetryCount()).orElse(0) + 1;
            tx.setRetryCount(newRetryCount);
            transactionRepository.save(tx);
            log.info("[Payments] Incrementando reintentos para transacción {} a {}", transactionReference, newRetryCount);
        });
    }

    @Override
    @Transactional
    public PaymentTransaction syncWithGatewayResponse(String transactionReference, PaymentIntentResponse response) {
        PaymentTransaction transaction = transactionRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transacción no encontrada con referencia: " + transactionReference));

        if (response.getStatus() != null) {
            transaction.setStatus(response.getStatus());
            if (response.getStatus() == PaymentTransaction.TransactionStatus.SUCCEEDED
                    || response.getStatus() == PaymentTransaction.TransactionStatus.CAPTURED) {
                transaction.setConfirmedAt(LocalDateTime.now());
            }
        }

        if (response.getGatewayTransactionId() != null) {
            transaction.setGatewayTransactionId(response.getGatewayTransactionId());
        }
        if (response.getReferenceNumber() != null) {
            transaction.setReferenceNumber(response.getReferenceNumber());
        }
        if (response.getBarcodeUrl() != null) {
            transaction.setBarcodeUrl(response.getBarcodeUrl());
        }
        if (response.getClientSecret() != null) {
            transaction.setClientSecret(response.getClientSecret());
        }
        if (response.getConfirmationUrl() != null) {
            transaction.setConfirmationUrl(response.getConfirmationUrl());
        }
        if (response.getPaymentUrl() != null) {
            transaction.getMetadata().put("paymentUrl", response.getPaymentUrl());
        }
        if (response.getGatewayFee() != null) {
            transaction.setGatewayFee(response.getGatewayFee());
        }
        if (response.getPlatformFee() != null) {
            transaction.setPlatformFee(response.getPlatformFee());
        }
        if (response.getTaxAmount() != null) {
            transaction.setTaxAmount(response.getTaxAmount());
        }
        if (response.getNetAmount() != null) {
            transaction.setNetAmount(response.getNetAmount());
        }
        if (response.getExpiresAt() != null) {
            transaction.setExpiresAt(response.getExpiresAt());
        }
        if (response.getAdditionalData() != null) {
            transaction.setResponseRaw(writeAsJson(response.getAdditionalData()));
        } else if (response.getMessage() != null) {
            transaction.setResponseRaw(response.getMessage());
        }

        return transactionRepository.save(transaction);
    }

    private String writeAsJson(Object data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            log.warn("[Payments] No se pudo serializar la respuesta de la pasarela: {}", e.getMessage());
            return data != null ? data.toString() : null;
        }
    }
    
    @Override
    public String generateTransactionReference() {
        return referenceGenerator.generate();
    }
    
    // ==================== IMPLEMENTACIÓN DE NUEVOS MÉTODOS FASE 2 ====================
    
    @Override
    @Transactional(readOnly = true)
    public Page<TransactionListItemDTO> findByPaymentIdPaginated(
            Long paymentId,
            PaymentTransaction.TransactionStatus status,
            Pageable pageable) {
        
        log.debug("[Payments] Consultando transacciones del payment {} con status {} - página {}",
                paymentId, status, pageable.getPageNumber());
        
        // Validar que el Payment existe
        if (!paymentRepository.existsById(paymentId)) {
            log.warn("[Payments] Payment {} no encontrado al consultar transacciones", paymentId);
            throw new ResourceNotFoundException("No se encontró el pago con ID: " + paymentId);
        }
        
        Page<PaymentTransaction> transactionsPage;
        
        if (status != null) {
            // Filtrar por estado específico
            log.debug("[Payments] Aplicando filtro de estado: {}", status);
            transactionsPage = transactionRepository.findByPaymentIdAndStatus(
                    paymentId, status, pageable);
        } else {
            // Obtener todas las transacciones del payment
            transactionsPage = transactionRepository.findByPaymentIdPaginated(
                    paymentId, pageable);
        }
        
        // Mapear entidades a DTOs
        Page<TransactionListItemDTO> dtoPage = transactionsPage
                .map(TransactionListItemDTO::fromEntity);
        
        log.info("[Payments] Encontradas {} transacciones para payment {} (total: {})",
                dtoPage.getNumberOfElements(), paymentId, dtoPage.getTotalElements());
        
        return dtoPage;
    }
    
    @Override
    @Transactional(readOnly = true)
    public TransactionDetailResponse getTransactionWithDetails(String transactionReference) {
        
        log.debug("[Payments] Obteniendo detalle completo de transacción {}", transactionReference);
        
        // Buscar transacción
        PaymentTransaction transaction = transactionRepository
                .findByTransactionReference(transactionReference)
                .orElseThrow(() -> {
                    log.warn("[Payments] Transacción {} no encontrada", transactionReference);
                    return new ResourceNotFoundException(
                            "No se encontró la transacción con referencia: " + transactionReference);
                });
        
        // Mapear datos básicos de la transacción
        TransactionListItemDTO transactionDTO = TransactionListItemDTO.fromEntity(transaction);
        
        // Obtener historial de estados
        List<TransactionStatusHistoryDTO> statusHistory = getStatusHistory(transaction.getId());
        
        // Construir información del residente
        String residentEmail = null;
        String residentName = null;
        String bankPersonalReference = null;
        
        if (transaction.getPayment() != null && 
            transaction.getPayment().getResident() != null) {
            
            var resident = transaction.getPayment().getResident();
            if (resident.getUser() != null) {
                residentEmail = resident.getUser().getEmail();
                residentName = resident.getUser().getFirstName() + " " + 
                              resident.getUser().getLastName();
                bankPersonalReference = resident.getUser().getBankPersonalReference();
            }
        }
        
        // Construir información del cargo
        TransactionDetailResponse.ChargeInfoDTO chargeInfo = null;
        if (transaction.getPayment() != null && 
            transaction.getPayment().getCharge() != null) {
            
            var charge = transaction.getPayment().getCharge();
            chargeInfo = TransactionDetailResponse.ChargeInfoDTO.builder()
                    .chargeId(charge.getId())
                    .description(charge.getDescription())
                    .titleTypePayment(charge.getTitleTypePayment())
                    .dueDate(charge.getDueDate() != null ? 
                            charge.getDueDate().format(DATE_FORMATTER) : null)
                    .build();
        }
        
        // Construir información de la cuenta de pasarela
        TransactionDetailResponse.GatewayAccountInfoDTO gatewayInfo = null;
        if (transaction.getGatewayAccount() != null) {
            var account = transaction.getGatewayAccount();
            gatewayInfo = TransactionDetailResponse.GatewayAccountInfoDTO.builder()
                    .accountId(account.getId())
                    .provider(account.getProvider().name())
                    .accountName(account.getAccountName())
                    .build();
        }
        
        // Construir respuesta completa
        TransactionDetailResponse response = TransactionDetailResponse.builder()
                .transaction(transactionDTO)
                .residentEmail(residentEmail)
                .residentName(residentName)
                .bankPersonalReference(bankPersonalReference)
                .statusHistory(statusHistory)
                .chargeInfo(chargeInfo)
                .gatewayAccountInfo(gatewayInfo)
                .build();
        
        log.info("[Payments] Detalle completo de transacción {} generado con {} eventos en historial",
                transactionReference, statusHistory.size());
        
        return response;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<TransactionStatusHistoryDTO> getStatusHistory(Long transactionId) {
        
        log.debug("[Payments] Obteniendo historial de estados para transacción ID {}", transactionId);
        
        // Obtener logs de auditoría ordenados cronológicamente
        List<PaymentAuditLog> auditLogs = auditLogRepository
                .findByTransactionIdOrderByCreatedAtAsc(transactionId);
        
        // Filtrar solo logs relacionados con cambios de estado y mapear a DTO
        List<TransactionStatusHistoryDTO> history = auditLogs.stream()
                .filter(log -> TRANSACTION_STATUS_ACTIONS.contains(log.getAction()))
                .map(this::mapAuditLogToHistoryDTO)
                .collect(Collectors.toList());
        
        // Si no hay historial en audit log, crear entrada inicial basada en la transacción
        if (history.isEmpty()) {
            log.debug("[Payments] No se encontró historial en audit log para transacción {}, " +
                    "creando entrada básica", transactionId);
            
            Optional<PaymentTransaction> txOpt = transactionRepository.findById(transactionId);
            if (txOpt.isPresent()) {
                PaymentTransaction tx = txOpt.get();
                TransactionStatusHistoryDTO initialStatus = TransactionStatusHistoryDTO.builder()
                        .status(tx.getStatus())
                        .timestamp(tx.getCreatedAt())
                        .message(generateMessageForStatus(tx.getStatus()))
                        .performedBy("Sistema")
                        .build();
                history.add(initialStatus);
            }
        }
        
        log.debug("[Payments] Historial de transacción {} contiene {} eventos", 
                transactionId, history.size());
        
        return history;
    }
    
    /**
     * Mapea un registro de auditoría a un DTO de historial de estados.
     * 
     * @param auditLog Registro de auditoría
     * @return DTO de evento de historial
     */
    private TransactionStatusHistoryDTO mapAuditLogToHistoryDTO(PaymentAuditLog auditLog) {
        PaymentTransaction.TransactionStatus status = extractStatusFromAction(auditLog.getAction());
        String performedBy = determinePerformedBy(auditLog);
        
        return TransactionStatusHistoryDTO.builder()
                .status(status)
                .timestamp(auditLog.getCreatedAt())
                .message(auditLog.getDescription() != null ? 
                        auditLog.getDescription() : generateMessageForStatus(status))
                .performedBy(performedBy)
                .additionalInfo(auditLog.getNewValue())
                .build();
    }
    
    /**
     * Extrae el estado de transacción desde la acción de auditoría.
     * 
     * @param action Acción de auditoría
     * @return Estado de transacción correspondiente
     */
    private PaymentTransaction.TransactionStatus extractStatusFromAction(
            PaymentAuditLog.AuditAction action) {
        
        return switch (action) {
            case TRANSACTION_INITIATED -> PaymentTransaction.TransactionStatus.PENDING;
            case TRANSACTION_AUTHORIZED -> PaymentTransaction.TransactionStatus.AUTHORIZED;
            case TRANSACTION_CAPTURED -> PaymentTransaction.TransactionStatus.CAPTURED;
            case TRANSACTION_FAILED -> PaymentTransaction.TransactionStatus.FAILED;
            case TRANSACTION_CANCELLED -> PaymentTransaction.TransactionStatus.CANCELLED;
            case TRANSACTION_REFUNDED -> PaymentTransaction.TransactionStatus.REFUNDED;
            default -> PaymentTransaction.TransactionStatus.PROCESSING;
        };
    }
    
    /**
     * Determina quién realizó la acción basándose en el log de auditoría.
     * 
     * @param auditLog Registro de auditoría
     * @return Descripción de quién realizó la acción
     */
    private String determinePerformedBy(PaymentAuditLog auditLog) {
        if (auditLog.getUser() != null) {
            return auditLog.getUser().getFirstName() + " " + auditLog.getUser().getLastName();
        }
        
        // Si no hay usuario, probablemente fue un webhook o proceso automático
        if (auditLog.getDescription() != null && 
            auditLog.getDescription().toLowerCase().contains("webhook")) {
            return "Webhook Conekta";
        }
        
        return "Sistema";
    }
    
    /**
     * Genera un mensaje descriptivo para un estado de transacción.
     * 
     * @param status Estado de la transacción
     * @return Mensaje descriptivo
     */
    private String generateMessageForStatus(PaymentTransaction.TransactionStatus status) {
        return switch (status) {
            case PENDING -> "Transacción iniciada, esperando procesamiento";
            case PROCESSING -> "Transacción en proceso de validación";
            case REQUIRES_ACTION -> "Requiere acción del usuario (ej. 3D Secure)";
            case REQUIRES_PAYMENT_METHOD -> "Se requiere método de pago";
            case REQUIRES_CONFIRMATION -> "Se requiere confirmación del usuario";
            case AUTHORIZED -> "Pago autorizado por la pasarela";
            case CAPTURED -> "Pago capturado y acreditado";
            case SUCCEEDED -> "Pago completado exitosamente";
            case FAILED -> "La transacción falló";
            case CANCELLED -> "Transacción cancelada";
            case REFUNDED -> "Pago reembolsado";
            case PARTIALLY_REFUNDED -> "Pago reembolsado parcialmente";
            case EXPIRED -> "Transacción expirada (referencia OXXO no pagada a tiempo)";
            case DISPUTED -> "Pago en disputa";
        };
    }
}
