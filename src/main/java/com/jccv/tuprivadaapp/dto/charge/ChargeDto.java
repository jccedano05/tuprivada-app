package com.jccv.tuprivadaapp.dto.charge;

import com.jccv.tuprivadaapp.model.payment.PenaltyTypeEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChargeDto {
    private Long id;
    private List<Long> residentIds;  // IDs de los residentes a los que se les aplicará el cargo
    private double amount;           // Monto del cargo
    private String titleTypePayment;
    private String description;      // Descripción del cargo
    private Long condominiumId;
    private PenaltyTypeEnum penaltyType;
    private Double penaltyValue;
    private LocalDateTime chargeDate;
    private LocalDateTime dueDate;
}

