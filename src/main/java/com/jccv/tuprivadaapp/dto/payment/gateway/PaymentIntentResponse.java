package com.jccv.tuprivadaapp.dto.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO para la respuesta de creación de intención de pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentIntentResponse {
    
    private String transactionReference;
    private String gatewayTransactionId;
    private String clientSecret; // Para pagos con tarjeta
    private PaymentTransaction.TransactionStatus status;
    private BigDecimal amount;
    private String currency;
    
    // Para pagos en efectivo
    private String referenceNumber; // Número de referencia OXXO
    private String barcodeUrl;
    private LocalDateTime expiresAt;
    
    // URLs de confirmación
    private String confirmationUrl;
    private String paymentUrl;
    
    // Información de comisiones
    private BigDecimal gatewayFee;
    private BigDecimal platformFee;
    private BigDecimal taxAmount;
    private BigDecimal netAmount;
    private BigDecimal totalAmount;
    
    // Metadatos adicionales
    private Map<String, Object> additionalData;
    
    // Indicadores
    private boolean requiresAction;
    private String nextAction;
    
    // Mensajes
    private String message;
    private String displayMessage;
}
