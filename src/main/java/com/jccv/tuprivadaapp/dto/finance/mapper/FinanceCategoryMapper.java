package com.jccv.tuprivadaapp.dto.finance.mapper;

import com.jccv.tuprivadaapp.dto.finance.FinanceCategoryDto;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.finance.FinanceCategory;
import org.springframework.stereotype.Component;

@Component
public class FinanceCategoryMapper {

    public FinanceCategory toEntity(FinanceCategoryDto dto, Condominium condominium) {
        return FinanceCategory.builder()
                .id((dto.getId()))
                .category(dto.getCategory())
                .condominium(condominium)
                .description(dto.getDescription())
                .isExpense(dto.isExpense())
                .build();
    }

    public FinanceCategoryDto toDto(FinanceCategory category) {
        return FinanceCategoryDto.builder()
                .id((category.getId()))
                .category(category.getCategory())
                .condominiumId(category.getCondominium().getId())
                .description(category.getDescription())
                .isExpense(category.isExpense())
                .build();
    }
}
