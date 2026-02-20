package com.jccv.tuprivadaapp.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDetailsSummaryDto {
    // Payment info
    private Long paymentId;
    private boolean isPaid;
    private boolean isDeleted;
    private LocalDateTime datePaid;
    private Double remainingAmount;
    
    // Charge info
    private Long chargeId;
    private String titleTypePayment;
    private Double amount;
    private String description;
    private LocalDateTime chargeDate;
    private LocalDateTime dueDate;
    private Double penaltyValue;
    
    // Resident info (solo lo necesario)
    private Long residentId;
    private String residentFirstName;
    private String residentLastName;
    private String residentStreet;
    private String residentExtNumber;
    private String residentIntNumber;
    private Double residentBalance;
    
    // Condominium info (solo lo mínimo)
    private Long condominiumId;
    private String condominiumName;
}
