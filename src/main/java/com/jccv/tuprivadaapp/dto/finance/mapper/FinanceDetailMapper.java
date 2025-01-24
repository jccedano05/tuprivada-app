package com.jccv.tuprivadaapp.dto.finance.mapper;

import com.jccv.tuprivadaapp.dto.finance.FinanceDetailDto;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.model.finance.FinanceCategory;
import com.jccv.tuprivadaapp.model.finance.FinanceDetail;
import org.springframework.stereotype.Component;

@Component
public class FinanceDetailMapper {

    public  FinanceDetail toEntity(FinanceDetailDto dto, Finance finance, FinanceCategory category) {
        return  FinanceDetail.builder()
                .id(dto.getId())
                .amount(dto.getAmount())
                .concept(dto.getConcept())
                .description(dto.getDescription())
                .financeCategory(category)
                .finance(finance)
                .build();
    }

    public FinanceDetailDto toDto(FinanceDetail detail) {
        return new FinanceDetailDto(
                detail.getId(),
                detail.getFinance().getId(),
                detail.getFinanceCategory().getId(),
                detail.getConcept(),
                detail.getAmount(),
                detail.getDescription()
        );
    }
}
