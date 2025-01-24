package com.jccv.tuprivadaapp.service.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceDetailDto;

import java.util.List;

public interface FinanceDetailService {
    FinanceDetailDto createFinanceDetail(FinanceDetailDto financeDetailDto);
    FinanceDetailDto getFinanceDetailById(Long id);
    List<FinanceDetailDto> getFinanceDetailsByFinance(Long financeId);
    FinanceDetailDto updateFinanceDetail(Long id, FinanceDetailDto financeDetailDto);
    void deleteFinanceDetail(Long id);
}
