package com.jccv.tuprivadaapp.dto.transaction.mapper;


import com.jccv.tuprivadaapp.dto.transaction.DepositDto;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.transaction.Deposit;
import org.springframework.stereotype.Component;

@Component
public class DepositMapper {

    public DepositDto convertToDto(Deposit entity){
        return DepositDto.builder()
                .id(entity.getId())
                .residentId(entity.getResident().getId())
                .amount(entity.getAmount())
                .depositDate(entity.getDepositDate())
                .bankTrackingKey(entity.getBankTrackingKey())
                .issuingBank(entity.getIssuingBank())
                .build();
    }

    public Deposit convertToEntity(DepositDto dto, Resident resident){
        return Deposit.builder()
                .id(dto.getId())
                .resident(resident)
                .amount(dto.getAmount())
                .depositDate(dto.getDepositDate())
                .bankTrackingKey(dto.getBankTrackingKey())
                .issuingBank(dto.getIssuingBank())
                .build();
    }
}
