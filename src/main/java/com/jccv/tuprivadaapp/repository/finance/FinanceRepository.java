package com.jccv.tuprivadaapp.repository.finance;

import com.jccv.tuprivadaapp.dto.finance.FinanceDto;
import com.jccv.tuprivadaapp.dto.finance.FinanceSummaryDto;
import com.jccv.tuprivadaapp.model.finance.Finance;
import com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FinanceRepository extends JpaRepository<Finance, Long> {
    Page<Finance> findByCondominiumIdOrderByDateDesc(Long condominiumId, Pageable pageable);

//    @Query("SELECT f FROM Finance f WHERE f.condominium.id = :condominiumId AND YEAR(f.date) = :year ORDER BY f.date DESC")
//    List<Finance> findFinancesByYear(Long condominiumId, int year);

    @Query("SELECT new com.jccv.tuprivadaapp.dto.finance.FinanceSummaryDto( " +
            "f.id, f.condominium.id, FUNCTION('TO_CHAR', f.date, 'YYYY-MM-DD'), " +  // Conversión de LocalDate a String
            "COALESCE(SUM(CASE WHEN fc.isExpense = false THEN fd.amount ELSE 0 END), 0), " +
            "COALESCE(SUM(CASE WHEN fc.isExpense = true THEN fd.amount ELSE 0 END), 0)) " +
            "FROM Finance f " +
            "LEFT JOIN FinanceDetail fd ON fd.finance.id = f.id " +
            "LEFT JOIN FinanceCategory fc ON fc.id = fd.financeCategory.id " +
            "WHERE f.condominium.id = :condominiumId " +
            "AND YEAR(f.date) = :year " +
            "GROUP BY f.id, f.condominium.id, f.date " +
            "ORDER BY f.date DESC")
    List<FinanceSummaryDto> findFinanceSummaryByCondominiumAndYear(@Param("condominiumId") Long condominiumId,
                                                                   @Param("year") int year);






    @Query("SELECT new com.jccv.tuprivadaapp.dto.finance.AnnualFinanceDto(YEAR(f.date), SUM(f.incomeQuantity), SUM(f.billQuantity)) " +
            "FROM Finance f WHERE f.condominium.id = :condominiumId GROUP BY YEAR(f.date)")
    List<AnnualFinanceDto> findFinancesGroupedByYear(Long condominiumId);
}
