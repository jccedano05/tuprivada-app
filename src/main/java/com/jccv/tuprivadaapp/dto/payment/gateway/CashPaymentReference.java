package com.jccv.tuprivadaapp.dto.payment.gateway;

import lombok.*;

import java.time.LocalDateTime;

/**
 * DTO para referencia de pago en efectivo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CashPaymentReference {

    private String referenceNumber;
    private String barcodeUrl;
    private LocalDateTime expiresAt;
    private String payerName;
    private String paymentInstructions;
    private String instructions;
    private String pdfUrl;
}
