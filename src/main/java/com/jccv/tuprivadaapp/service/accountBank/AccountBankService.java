package com.jccv.tuprivadaapp.service.accountBank;


import com.jccv.tuprivadaapp.dto.acoountBank.AccountBankDto;

import java.util.List;

public interface AccountBankService {

    public AccountBankDto create(AccountBankDto accountBankDto);

    public AccountBankDto findById(Long id);

    public AccountBankDto findAccountBankByResident(Long residentId);

    public List<AccountBankDto> findAll();

    public AccountBankDto update(AccountBankDto accountBankDto);

    public void deleteById(Long id);


    public AccountBankDto findAccountBankByCondominium(Long accountBankId);

}
