package com.jccv.tuprivadaapp.dto.transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepositSummaryDto {
    private double amountInMonth;
    private double amountInYear;

}
