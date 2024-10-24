package com.jccv.tuprivadaapp.repository.recurring_payment;

import com.jccv.tuprivadaapp.model.recurring_payment.RecurringPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RecurringPaymentRepository extends JpaRepository<RecurringPayment, Long> {

//    List<RecurringPayment> findByNextPaymentDate(LocalDate nextPaymentDate);

    @Query("SELECT DISTINCT r FROM RecurringPayment r JOIN r.residents res " +
            "WHERE r.nextPaymentDate = CURRENT_DATE " +
            "AND res.isActiveResident = true " +
            "AND r.isRecurringPaymentActive = true")
    List<RecurringPayment> findByNextPaymentDate();
}