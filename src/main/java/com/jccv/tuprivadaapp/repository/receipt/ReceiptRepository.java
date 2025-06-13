package com.jccv.tuprivadaapp.repository.receipt;

import com.jccv.tuprivadaapp.model.receipt.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
    @Query("SELECT r FROM Receipt r WHERE r.payment.id = :paymentId")
    Optional<Receipt> findByPaymentId(@Param("paymentId") Long paymentId);

    @Query("SELECT r FROM Receipt r WHERE r.depositPayment.id = :depositPaymentId")
    Optional<Receipt> findByDepositPaymentId(@Param("depositPaymentId") Long depositPaymentId);

}
