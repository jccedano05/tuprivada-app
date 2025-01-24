package com.jccv.tuprivadaapp.repository.finance;

import com.jccv.tuprivadaapp.model.finance.FinanceDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceDetailRepository extends JpaRepository<FinanceDetail, Long> {
    List<FinanceDetail> findByFinanceId(Long financeId);
}
