package com.jccv.tuprivadaapp.repository.finance;

import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FinanceRepository extends JpaRepository<Finance, Long> {
    Page<Finance> findByCondominiumIdOrderByDateDesc(Long condominiumId, Pageable pageable);

    @Query("SELECT f FROM Finance f WHERE f.condominium.id = :condominiumId AND YEAR(f.date) = :year ORDER BY f.date DESC")
    List<Finance> findFinancesByYear(Long condominiumId, int year);

    @Query("SELECT new com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto(YEAR(f.date), SUM(f.incomeQuantity), SUM(f.billQuantity)) " +
            "FROM Finance f WHERE f.condominium.id = :condominiumId GROUP BY YEAR(f.date)")
    List<AnnualFinanceDto> findFinancesGroupedByYear(Long condominiumId);
}
