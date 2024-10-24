package com.jccv.tuprivadaapp.dto.payment;

import lombok.*;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PaymentDto {
    private Long id;
    private double amount;
    private LocalDateTime chargeDate;
    private LocalDateTime dueDate;
    private String typePayment;
    private String description;
    private boolean isPaid;
    private Long residentId;
}
