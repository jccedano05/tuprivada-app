package com.jccv.tuprivadaapp.dto.acoountBank.mapper;

import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;
import com.jccv.tuprivadaapp.model.account_bank.AccountBank;
import com.jccv.tuprivadaapp.repository.condominium.facade.CondominiumFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountBankMapper {

    private final CondominiumFacade condominiumFacade;

    @Autowired
    public AccountBankMapper(CondominiumFacade condominiumFacade) {
        this.condominiumFacade = condominiumFacade;
    }

    public  AccountBank convertToAccountBankModel(AccountBankDto accountBankDto){
        return  AccountBank.builder()
                .id(accountBankDto.getId())
                .accountName(accountBankDto.getAccountName())
                .bankName(accountBankDto.getBankName())
                .numberAccount(accountBankDto.getNumberAccount())
                .clabe(accountBankDto.getClabe())
                .reference(accountBankDto.getReference())
                .condominium(condominiumFacade.getAccountBankById(accountBankDto.getCondominiumId()))
                .build();
    }
    public  AccountBankDto convertToAccountBankDto(AccountBank accountBank){
        return  AccountBankDto.builder()
                .id(accountBank.getId())
                .accountName(accountBank.getAccountName())
                .bankName(accountBank.getBankName())
                .numberAccount(accountBank.getNumberAccount())
                .clabe(accountBank.getClabe())
                .reference(accountBank.getReference())
                .condominiumId(accountBank.getCondominium().getId())
                .build();
    }
}
