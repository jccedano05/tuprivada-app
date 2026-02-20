package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de estado de transacción
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionStatusResponse {
    
    private String transactionReference;
    private String gatewayTransactionId;
    private PaymentTransaction.TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private String failureReason;
    private String paymentMethodType;
    private boolean canRetry;
}
