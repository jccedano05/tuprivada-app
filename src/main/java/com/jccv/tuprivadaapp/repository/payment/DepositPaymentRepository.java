package com.jccv.tuprivadaapp.repository.payment;

import com.jccv.tuprivadaapp.model.payment.DepositPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DepositPaymentRepository extends JpaRepository<DepositPayment, Long> {

    List<DepositPayment> findByPaymentId(Long paymentId);

    //Sumatoria de abonos a un payment
    @Query("SELECT COALESCE(SUM(d.amount), 0) FROM DepositPayment d WHERE d.payment.id = :paymentId")
    double getTotalDepositsAmountByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT dp FROM DepositPayment dp JOIN dp.payment p WHERE p.resident.id = :residentId AND dp.depositDate BETWEEN :startDate AND :endDate")
    List<DepositPayment> findByResidentIdAndDepositDateBetween(
            @Param("residentId") Long residentId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT dp FROM DepositPayment dp JOIN dp.payment p WHERE p.resident.id = :residentId")
    List<DepositPayment> findAllByResidentId(@Param("residentId") Long residentId);




}
