package com.jccv.tuprivadaapp.dto.payment.gateway;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para solicitud de pago en efectivo (OXXO, etc)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashPaymentRequest {
    
    @NotNull(message = "El monto es obligatorio")
    private Double amount;
    
    @NotNull(message = "El tipo de pago en efectivo es obligatorio")
    private CashPaymentType type;
    
    private Integer expiresInDays;
    private String description;
    
    // Datos del pagador
    private String payerName;
    private String payerEmail;
    private String payerPhone;
    
    // Referencia al condominio
    private Long condominiumId;
    
    public enum CashPaymentType {
        OXXO,
        OXXO_PAY,
        SEVEN_ELEVEN,
        WALMART,
        SORIANA,
        CHEDRAUI
    }
}
