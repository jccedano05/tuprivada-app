package com.jccv.tuprivadaapp.service.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceCategoryDto;

import java.util.List;

public interface FinanceCategoryService {
    FinanceCategoryDto createFinanceCategory(FinanceCategoryDto financeCategoryDto);
    FinanceCategoryDto getFinanceCategoryById(Long id);
    List<FinanceCategoryDto> getFinanceCategoriesByCondominium(Long condominiumId);
    FinanceCategoryDto updateFinanceCategory(Long id, FinanceCategoryDto financeCategoryDto);
    void deleteFinanceCategory(Long id);
}
