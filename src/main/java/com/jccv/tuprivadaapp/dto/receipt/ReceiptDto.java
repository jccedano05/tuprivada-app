package com.jccv.tuprivadaapp.dto.receipt;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReceiptDto {
    private Long id;
    private Long condominiumId;
    private Long paymentId;
    private Long depositPaymentId;
    private String receiptName;
    private String operationCode;
    private LocalDateTime createdAt;
    private String title;
    private String description;
    private String residentName;
    private String residentAddress;
    private double amount;
    private LocalDateTime datePaid;

}
