package com.jccv.tuprivadaapp.dto.payment;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {
    private Long transactionId;
    private String transactionType; // "PAYMENT" o "DEPOSIT"
    private LocalDateTime date;
    private double amount;
    private String title;
    private String description;
}