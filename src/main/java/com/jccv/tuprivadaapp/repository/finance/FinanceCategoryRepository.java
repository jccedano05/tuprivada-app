package com.jccv.tuprivadaapp.repository.finance;

import com.jccv.tuprivadaapp.model.finance.FinanceCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceCategoryRepository extends JpaRepository<FinanceCategory, Long> {
    List<FinanceCategory> findByCondominiumId(Long condominiumId);
}
