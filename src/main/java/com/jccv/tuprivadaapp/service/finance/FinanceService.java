package com.jccv.tuprivadaapp.service.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceDto;
import com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto;
import com.jccv.tuprivadaapp.model.finance.Finance;
import org.springframework.data.domain.Page;

import java.util.List;

public interface FinanceService {
    FinanceDto createFinance(FinanceDto financeDto);
    FinanceDto getFinanceById(Long id);
    Page<FinanceDto> getFinancesByCondominium(Long condominiumId, int page, int size);

    public List<FinanceDto> getFinancesCondominiumByYear(Long condominiumId, int year);
    List<AnnualFinanceDto> getAnnualFinances(Long condominiumId);
    FinanceDto updateFinance(Long id, FinanceDto financeDto);
    void deleteFinance(Long id);
}
