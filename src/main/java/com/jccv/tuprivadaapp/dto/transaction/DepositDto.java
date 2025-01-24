package com.jccv.tuprivadaapp.dto.transaction;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class DepositDto {

    private Long id;
    private Long residentId;
    private Double amount;
    private LocalDateTime depositDate;
    private String bankTrackingKey;
    private String issuingBank;
}
