package com.jccv.tuprivadaapp.dto.payment;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepositPaymentDto {
    private Long id;
    // Se incluye el id del Payment al que pertenece el abono
    private Long paymentId;
    private double amount;
    private String description;
    private String title;
    private LocalDateTime depositDate;
}
