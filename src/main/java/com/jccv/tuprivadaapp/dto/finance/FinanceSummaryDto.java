package com.jccv.tuprivadaapp.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceSummaryDto {

        private Long id;
        private Long condominiumId;
        private String date;
        private double income;
        private double bill;
}


