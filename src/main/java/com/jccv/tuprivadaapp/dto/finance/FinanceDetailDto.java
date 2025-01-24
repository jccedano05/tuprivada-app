package com.jccv.tuprivadaapp.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDetailDto {
    private Long id;
    private Long financeId;
    private Long categoryId;
    private String concept;
    private double amount;
    private String description;
}
