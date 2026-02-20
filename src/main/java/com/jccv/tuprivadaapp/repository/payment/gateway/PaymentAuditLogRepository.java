package com.jccv.tuprivadaapp.repository.payment.gateway;

import com.jccv.tuprivadaapp.model.payment.gateway.PaymentAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para auditoría de pagos
 */
@Repository
public interface PaymentAuditLogRepository extends JpaRepository<PaymentAuditLog, Long> {
    
    /**
     * Busca logs por ID de transacción
     */
    List<PaymentAuditLog> findByTransactionIdOrderByCreatedAtDesc(Long transactionId);
    
    /**
     * Busca logs por ID de transacción ordenados cronológicamente (ascendente)
     * Útil para construir timeline de eventos
     */
    List<PaymentAuditLog> findByTransactionIdOrderByCreatedAtAsc(Long transactionId);
    
    /**
     * Busca logs por tipo de entidad e ID
     */
    List<PaymentAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, 
            Long entityId
    );
    
    /**
     * Busca logs por usuario
     */
    Page<PaymentAuditLog> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Busca logs por acción
     */
    Page<PaymentAuditLog> findByAction(PaymentAuditLog.AuditAction action, Pageable pageable);
    
    /**
     * Busca logs en un rango de fechas
     */
    @Query("SELECT l FROM PaymentAuditLog l " +
           "WHERE l.createdAt BETWEEN :startDate AND :endDate " +
           "ORDER BY l.createdAt DESC")
    Page<PaymentAuditLog> findByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable
    );
    
    /**
     * Busca actividades sospechosas
     */
    @Query("SELECT l FROM PaymentAuditLog l " +
           "WHERE l.riskScore >= :minRiskScore " +
           "OR l.action IN ('FRAUD_DETECTED', 'SUSPICIOUS_ACTIVITY', 'SECURITY_ALERT') " +
           "ORDER BY l.createdAt DESC")
    List<PaymentAuditLog> findSuspiciousActivities(@Param("minRiskScore") Integer minRiskScore);
    
    /**
     * Busca logs por IP
     */
    List<PaymentAuditLog> findByIpAddressOrderByCreatedAtDesc(String ipAddress);
}
