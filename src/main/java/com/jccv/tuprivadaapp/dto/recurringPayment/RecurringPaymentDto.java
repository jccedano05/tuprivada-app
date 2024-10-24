package com.jccv.tuprivadaapp.dto.recurringPayment;


import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class RecurringPaymentDto {

    private Long id;
    private double amount;
    private LocalDate startDate;
    private LocalDate nextPaymentDate;
    private String frequency;
    private boolean isRecurringPaymentActive;
    private String title;
    private String description;
    private Long condominiumId;
    private List<Long> residentIds;


}
