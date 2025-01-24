package com.jccv.tuprivadaapp.dto.acoountBank.mapper;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.model.account_bank.AccountBank;
import com.jccv.tuprivadaapp.repository.condominium.facade.CondominiumFacade;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class AccountBankMapper {

    private final CondominiumService condominiumService;

    public AccountBankMapper(@Lazy CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public AccountBank convertToAccountBankModel(AccountBankDto dto) {
        return AccountBank.builder()
                .id(dto.getId())
                .paymentType(dto.getPaymentType())
                .accountName(dto.getAccountName())
                .bankName(dto.getBankName())
                .numberAccount(dto.getNumberAccount())
                .clabe(dto.getClabe())
                .reference(dto.getReference())
                .isGlobalReference(dto.getIsGlobalReference())
                .condominium(condominiumService.findById(dto.getCondominiumId()))
                .build();
    }

    public AccountBankDto convertToAccountBankDto(AccountBank entity) {
        return AccountBankDto.builder()
                .id(entity.getId())
                .paymentType(entity.getPaymentType())
                .accountName(entity.getAccountName())
                .bankName(entity.getBankName())
                .numberAccount(entity.getNumberAccount())
                .clabe(entity.getClabe())
                .reference(entity.getReference())
                .condominiumId(entity.getCondominium().getId())
                .isGlobalReference(entity.getIsGlobalReference())  // Nuevo campo
                .build();
    }

}
