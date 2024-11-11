package com.jccv.tuprivadaapp.dto.charge;

import com.jccv.tuprivadaapp.model.payment.PenaltyTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChargeSummaryDto {
    private Long id;                             // ID del cargo
    private String titleTypePayment;             // Tipo de pago
    private LocalDateTime chargeDate;            // Fecha del cargo
    private double amount;                        // Monto del cargo
    private String description;                   // Descripción del cargo
    private PenaltyTypeEnum penaltyType;         // Tipo de penalización
    private Double penaltyValue;                  // Valor de la penalización
    private LocalDateTime dueDate;               // Fecha de vencimiento
    private int totalPayments;                    // Total de pagos
    private int paymentsCompleted;                // Pagos completados
    private boolean active;                       // Estado del cargo
    private List<Long> residentIds;
}
