package com.jccv.tuprivadaapp.repository.transaction;

import com.jccv.tuprivadaapp.model.transaction.Deposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, Long> {

    List<Deposit> findAllByResidentId(Long residentId);

    // Depósitos del mes actual para un condominio
    @Query("SELECT d FROM Deposit d WHERE d.resident.condominium.id = :condominiumId AND d.depositDate BETWEEN :startOfMonth AND :endOfMonth")
    List<Deposit> findDepositsByCondominiumIdInCurrentMonth(@Param("condominiumId") Long condominiumId,
                                                            @Param("startOfMonth") LocalDateTime startOfMonth,
                                                            @Param("endOfMonth") LocalDateTime endOfMonth);

    // Depósitos del año actual para un condominio
    @Query("SELECT d FROM Deposit d WHERE d.resident.condominium.id = :condominiumId AND d.depositDate BETWEEN :startOfYear AND :endOfYear")
    List<Deposit> findDepositsByCondominiumIdInCurrentYear(@Param("condominiumId") Long condominiumId,
                                                           @Param("startOfYear") LocalDateTime startOfYear,
                                                           @Param("endOfYear") LocalDateTime endOfYear);

    // Depósitos por mes para un condominio
    @Query("SELECT d FROM Deposit d WHERE d.resident.condominium.id = :condominiumId AND MONTH(d.depositDate) = :month AND YEAR(d.depositDate) = :year")
    List<Deposit> findByCondominiumIdAndMonth(@Param("condominiumId") Long condominiumId,
                                              @Param("month") int month,
                                              @Param("year") int year);

    // Depósitos por año para un condominio
    @Query("SELECT d FROM Deposit d WHERE d.resident.condominium.id = :condominiumId AND YEAR(d.depositDate) = :year")
    List<Deposit> findByCondominiumIdAndYear(@Param("condominiumId") Long condominiumId,
                                             @Param("year") int year);
}
