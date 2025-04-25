package com.jccv.tuprivadaapp.dto.payment;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentResidentDetailsDto {
    private Long paymentId;
    private Long residentId;
    private Long chargeId;
    private String firstName;
    private String lastName;
    private boolean isPaid;
    private Double amount;
    private String description;

    private String street;
    private String extNumber;
    private String intNumber;
    private LocalDateTime datePaid;
}
