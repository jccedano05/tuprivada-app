package com.jccv.tuprivadaapp.repository.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction;
import com.jccv.tuprivadaapp.model.payment.gateway.PaymentTransaction.TransactionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestión de transacciones de pago
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    
    /**
     * Busca una transacción por referencia única
     */
    Optional<PaymentTransaction> findByTransactionReference(String transactionReference);
    
    /**
     * Busca una transacción por ID de la pasarela
     */
    Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId);
    
    /**
     * Busca transacciones por ID de pago
     */
    List<PaymentTransaction> findByPaymentId(Long paymentId);
    
    /**
     * Busca transacciones por ID de pago con paginación
     */
    @Query("SELECT t FROM PaymentTransaction t WHERE t.payment.id = :paymentId ORDER BY t.createdAt DESC")
    Page<PaymentTransaction> findByPaymentIdPaginated(@Param("paymentId") Long paymentId, Pageable pageable);
    
    /**
     * Busca transacciones por ID de pago y estado con paginación
     */
    Page<PaymentTransaction> findByPaymentIdAndStatus(
            Long paymentId, 
            TransactionStatus status, 
            Pageable pageable
    );
    
    /**
     * Busca transacciones exitosas por ID de pago
     */
    @Query("SELECT t FROM PaymentTransaction t " +
           "WHERE t.payment.id = :paymentId " +
           "AND t.status IN ('SUCCEEDED', 'CAPTURED')")
    List<PaymentTransaction> findSuccessfulByPaymentId(@Param("paymentId") Long paymentId);
    
    /**
     * Busca transacciones por referencia bancaria personal
     */
    List<PaymentTransaction> findByBankPersonalReference(String bankPersonalReference);
    
    /**
     * Busca transacciones por estado
     */
    Page<PaymentTransaction> findByStatus(
            PaymentTransaction.TransactionStatus status, 
            Pageable pageable
    );
    
    /**
     * Busca transacciones pendientes de expiración
     */
    @Query("SELECT t FROM PaymentTransaction t " +
           "WHERE t.status = 'PENDING' " +
           "AND t.expiresAt IS NOT NULL " +
           "AND t.expiresAt <= :now")
    List<PaymentTransaction> findExpiredTransactions(@Param("now") LocalDateTime now);
    
    /**
     * Calcula el total de transacciones exitosas en un período
     */
    @Query("SELECT SUM(t.amount) FROM PaymentTransaction t " +
           "WHERE t.gatewayAccount.id = :accountId " +
           "AND t.status IN ('SUCCEEDED', 'CAPTURED') " +
           "AND t.createdAt BETWEEN :startDate AND :endDate")
    BigDecimal calculateTotalSuccessfulAmount(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Cuenta transacciones por estado en un período
     */
    @Query("SELECT t.status, COUNT(t) FROM PaymentTransaction t " +
           "WHERE t.gatewayAccount.id = :accountId " +
           "AND t.createdAt BETWEEN :startDate AND :endDate " +
           "GROUP BY t.status")
    List<Object[]> countTransactionsByStatus(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    /**
     * Busca transacciones que pueden ser reintentadas
     */
    @Query("SELECT t FROM PaymentTransaction t " +
           "WHERE t.status = 'FAILED' " +
           "AND t.retryCount < 3 " +
           "AND t.createdAt >= :since")
    List<PaymentTransaction> findRetriableTransactions(@Param("since") LocalDateTime since);
    
    /**
     * Busca transacciones por email del pagador
     */
    Page<PaymentTransaction> findByPayerEmail(String payerEmail, Pageable pageable);
    
    /**
     * Verifica si existe una transacción exitosa para un pago
     */
    boolean existsByPaymentIdAndStatusIn(Long paymentId, List<PaymentTransaction.TransactionStatus> statuses);
    
    /**
     * Busca la primera transacción de un pago con estados específicos
     */
    Optional<PaymentTransaction> findFirstByPaymentIdAndStatusIn(Long paymentId, List<PaymentTransaction.TransactionStatus> statuses);
}
