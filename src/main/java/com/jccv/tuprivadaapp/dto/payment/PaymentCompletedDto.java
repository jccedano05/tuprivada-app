package com.jccv.tuprivadaapp.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PaymentCompletedDto {
    private Long paymentId;
    private LocalDateTime datePaid;
    private Boolean isPaid;
}
