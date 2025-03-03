package com.jccv.tuprivadaapp.dto.transaction;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonthlyDepositDto {
    private String month;
    private double amount;
    private int depositCount;
    private int residentCount;
}