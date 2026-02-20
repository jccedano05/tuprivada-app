package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para representación ligera de transacciones en listados paginados.
 * Diseñado para optimizar transferencia de datos en el frontend.
 * 
 * @author TuPrivada Development Team
 * @version 1.0
 * @since 2026-01-21
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionListItemDTO {
    
    /**
     * Referencia única interna de la transacción (ej: TXN-20260121-ABC123)
     */
    private String transactionReference;
    
    /**
     * ID de la transacción en la pasarela de pago (ej: ord_2tP8xYzGkL3mN4pQ)
     */
    private String gatewayTransactionId;
    
    /**
     * ID del Payment asociado
     */
    private Long paymentId;
    
    /**
     * Estado actual de la transacción
     */
    private PaymentTransaction.TransactionStatus status;
    
    /**
     * Método de pago utilizado (OXXO, CARD, SPEI, etc.)
     */
    private PaymentTransaction.PaymentMethod paymentMethod;
    
    /**
     * Monto total de la transacción
     */
    private BigDecimal amount;
    
    /**
     * Moneda de la transacción (usualmente MXN)
     */
    private String currency;
    
    /**
     * Número de referencia para pagos en efectivo (OXXO)
     */
    private String referenceNumber;
    
    /**
     * URL del código de barras para pagos en efectivo
     */
    private String barcodeUrl;
    
    /**
     * Fecha de expiración para pagos en efectivo
     */
    private LocalDateTime expiresAt;
    
    /**
     * Fecha de creación de la transacción
     */
    private LocalDateTime createdAt;
    
    /**
     * Fecha de última actualización
     */
    private LocalDateTime lastUpdatedAt;
    
    /**
     * Comisión cobrada por la pasarela de pago
     */
    private BigDecimal gatewayFee;
    
    /**
     * Monto neto después de comisiones
     */
    private BigDecimal netAmount;
    
    /**
     * Email del pagador
     */
    private String payerEmail;
    
    /**
     * Nombre del pagador
     */
    private String payerName;
    
    /**
     * Código de error si la transacción falló
     */
    private String errorCode;
    
    /**
     * Mensaje de error descriptivo
     */
    private String errorMessage;
    
    /**
     * Número de intentos de procesamiento
     */
    private Integer retryCount;
    
    /**
     * Indica si la transacción puede ser reintentada
     */
    private Boolean canRetry;
    
    /**
     * Factory method para crear DTO desde entidad PaymentTransaction.
     * Implementa mapeo seguro sin exponer información sensible.
     * 
     * @param transaction Entidad PaymentTransaction a convertir
     * @return DTO mapeado o null si la entidad es null
     */
    public static TransactionListItemDTO fromEntity(PaymentTransaction transaction) {
        if (transaction == null) {
            return null;
        }
        
        return TransactionListItemDTO.builder()
                .transactionReference(transaction.getTransactionReference())
                .gatewayTransactionId(transaction.getGatewayTransactionId())
                .paymentId(transaction.getPayment() != null ? transaction.getPayment().getId() : null)
                .status(transaction.getStatus())
                .paymentMethod(transaction.getPaymentMethod())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .referenceNumber(transaction.getReferenceNumber())
                .barcodeUrl(transaction.getBarcodeUrl())
                .expiresAt(transaction.getExpiresAt())
                .createdAt(transaction.getCreatedAt())
                .lastUpdatedAt(transaction.getUpdatedAt())
                .gatewayFee(transaction.getGatewayFee())
                .netAmount(transaction.getNetAmount())
                .payerEmail(transaction.getPayerEmail())
                .payerName(transaction.getPayerName())
                .errorCode(transaction.getErrorCode())
                .errorMessage(transaction.getErrorMessage())
                .retryCount(transaction.getRetryCount())
                .canRetry(transaction.canRetry())
                .build();
    }
}
