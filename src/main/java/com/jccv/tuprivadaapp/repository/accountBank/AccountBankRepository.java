package com.jccv.tuprivadaapp.repository.accountBank;

import com.jccv.tuprivadaapp.model.account_bank.AccountBank;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountBankRepository extends JpaRepository<AccountBank, Long> {

    public Optional<AccountBank> findByCondominiumId(Long condominiumId);
}
