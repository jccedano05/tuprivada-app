package com.jccv.tuprivadaapp.dto.payment.gateway;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para solicitud de reembolso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {
    
    @NotNull(message = "El monto del reembolso es obligatorio")
    @Positive(message = "El monto debe ser positivo")
    private BigDecimal amount;
    
    @NotNull(message = "La razón del reembolso es obligatoria")
    private String reason;
    
    private RefundType type;
    
    private String notes;
    
    public enum RefundType {
        FULL,
        PARTIAL
    }
}
