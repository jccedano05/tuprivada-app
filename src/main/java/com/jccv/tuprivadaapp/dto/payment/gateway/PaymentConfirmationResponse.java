package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de confirmación de pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmationResponse {
    
    private String transactionReference;
    private String gatewayTransactionId;
    private PaymentTransaction.TransactionStatus status;
    private String message;
    private LocalDateTime confirmedAt;
    private String authorizationCode;
    private String receiptUrl;
    private boolean requiresAdditionalAction;
    private String additionalActionUrl;
    private java.util.Map<String, Object> paymentMethodDetails;
}
