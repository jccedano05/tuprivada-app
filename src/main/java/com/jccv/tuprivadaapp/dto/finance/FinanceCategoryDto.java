package com.jccv.tuprivadaapp.dto.finance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceCategoryDto {
    private Long id;
    private Long condominiumId;
    private String category;
    private String description;
    private boolean isExpense;
}
