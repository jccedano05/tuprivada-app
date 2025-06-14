package com.jccv.tuprivadaapp.dto.payment;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class PaymentDetailsDto {
    private Long paymentId;
    private Long depositId;
    private boolean isPaid;
    private boolean isDeleted;
    private LocalDateTime chargeDate;
    private String titleTypePayment;
    private double amount;
    private String description;  // Descripción del cargo
    private boolean isActive;     // Indica si el cargo está activo
    private LocalDateTime dueDate;
    private Double penaltyValue;
    private LocalDateTime datePaid;

}
