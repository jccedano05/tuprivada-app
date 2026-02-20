package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

import java.math.BigDecimal;

/**
 * DTO para cálculo de comisiones
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeeCalculation {
    
    private BigDecimal originalAmount;
    private BigDecimal gatewayFeePercentage;
    private BigDecimal gatewayFixedFee;
    private BigDecimal gatewayFeeAmount;
    private BigDecimal platformFeePercentage;
    private BigDecimal platformFeeAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private BigDecimal totalFees;
    private BigDecimal netAmount;
    private BigDecimal totalAmountToCharge;
    
    // Desglose detallado
    private String feeBreakdown;
    private String currency;
}
