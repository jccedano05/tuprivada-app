package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para respuesta de reembolso
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundResponse {
    
    private String refundId;
    private String originalTransactionReference;
    private BigDecimal refundedAmount;
    private String status;
    private LocalDateTime refundedAt;
    private String message;
}
