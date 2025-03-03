package com.jccv.tuprivadaapp.dto.transaction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecentDepositDto {
    private Long id;
    private String residentName;
    private Double amount;
    private String date;
}