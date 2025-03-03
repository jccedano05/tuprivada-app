package com.jccv.tuprivadaapp.dto.transaction;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AnnualDepositSummaryDto {
    private double totalAmountYear;
    private int totalDeposits;
    private int totalResidents;
    private List<MonthlyDepositDto> monthlyBreakdown;
    private List<RecentDepositDto> recentDeposits;
}
