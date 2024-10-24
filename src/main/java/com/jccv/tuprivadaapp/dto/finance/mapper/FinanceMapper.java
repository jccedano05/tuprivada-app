package com.jccv.tuprivadaapp.dto.finance.mapper;

import com.jccv.tuprivadaapp.dto.finance.FinanceDto;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class FinanceMapper {

    private final CondominiumService condominiumService;

    @Autowired
    public FinanceMapper(CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public FinanceDto toDTO(Finance finance) {
        FinanceDto dto = new FinanceDto();
        dto.setId(finance.getId());
        dto.setCondominiumId(finance.getCondominium().getId());
        dto.setDate(finance.getDate().toString());
        dto.setIncomeQuantity(finance.getIncomeQuantity());
        dto.setBillQuantity(finance.getBillQuantity());
        return dto;
    }

    public Finance toEntity(FinanceDto dto) {
        Finance finance = new Finance();
        finance.setId(dto.getId());
        finance.setCondominium(condominiumService.findById(dto.getCondominiumId()));
        finance.setDate(LocalDate.parse(dto.getDate()));
        finance.setIncomeQuantity(dto.getIncomeQuantity());
        finance.setBillQuantity(dto.getBillQuantity());
        return finance;
    }
}
