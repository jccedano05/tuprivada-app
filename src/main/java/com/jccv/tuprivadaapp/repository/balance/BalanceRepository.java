package com.jccv.tuprivadaapp.repository.balance;


import com.jccv.tuprivadaapp.model.balance.Balance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BalanceRepository extends JpaRepository<Balance, Long> {
    List<Balance> findAllByResidentId(Long residentId);
    Balance findFirstByResidentIdOrderByDepositDateDesc(Long residentId);  // Obtener el saldo más reciente
}
