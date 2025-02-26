package com.jccv.tuprivadaapp.service.transaction;


import com.jccv.tuprivadaapp.dto.transaction.AnnualDepositSummaryDto;
import com.jccv.tuprivadaapp.dto.transaction.DepositDto;
import com.jccv.tuprivadaapp.dto.transaction.DepositSummaryDto;
import com.jccv.tuprivadaapp.model.transaction.Deposit;

import java.util.List;

public interface DepositService {
    DepositDto createDeposit(DepositDto depositDTO);

    DepositDto updateDeposit(Long id, DepositDto depositDTO);

    boolean deleteDeposit(Long id);

    Deposit findById(Long id);

    List<DepositDto> findDepositsByResidentId(Long residentId);

    List<DepositDto> getDepositsByCondominiumIdAndYear(Long condominiumId, int year);

    public List<DepositDto> getDepositsByCondominiumIdAndMonth(Long condominiumId, int month, int year);

     DepositSummaryDto getDepositSummaryForMonthAndYear(Long condominiumId, int month, int year);

    AnnualDepositSummaryDto getAnnualDepositSummary(Long condominiumId, int year);
}
