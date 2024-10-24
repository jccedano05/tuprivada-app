package com.jccv.tuprivadaapp.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnualFinanceDto {
    private int year;
    private double totalIncome;
    private double totalBills;
}
