package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

import java.util.Map;

/**
 * DTO para confirmación de pago
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmationRequest {
    
    private String paymentMethodId;
    private String paymentToken;
    private String cvv;
    private Map<String, String> additionalData;
    private boolean savePaymentMethod;
    private String returnUrl;
}
