
package com.jccv.tuprivadaapp.dto.resident;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResidentChargeSummaryDto {
    private Long residentId;
    private String firstName;
    private String lastName;
    private String street;
    private String extNumber;
    private Long paidPayments;     // Cambiado a Long para que coincida con SUM(CASE) en JPQL
    private Long unpaidPayments;   // Cambiado a Long para que coincida con SUM(CASE) en JPQL
    private Double totalDue;       // SUM sobre monto (double) → Double  
}
