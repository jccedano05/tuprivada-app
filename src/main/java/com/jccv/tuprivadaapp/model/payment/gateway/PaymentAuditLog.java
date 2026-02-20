package com.jccv.tuprivadaapp.model.payment.gateway;

import com.jccv.tuprivadaapp.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Entidad para auditoría de todas las operaciones de pago.
 * Registro inmutable para cumplir con requisitos de compliance.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_audit_logs", indexes = {
        @Index(name = "idx_audit_transaction", columnList = "transaction_id"),
        @Index(name = "idx_audit_created", columnList = "created_at"),
        @Index(name = "idx_audit_action", columnList = "action"),
        @Index(name = "idx_audit_user", columnList = "user_id")
})
public class PaymentAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id")
    private Long transactionId;

    @Column(name = "action", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private AuditAction action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue; // JSON con el valor anterior

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue; // JSON con el nuevo valor

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id")
    private String sessionId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "risk_score")
    private Integer riskScore; // 0-100, para análisis de fraude

    @Column(name = "risk_factors", columnDefinition = "TEXT")
    private String riskFactors; // JSON con factores de riesgo detectados

    public enum AuditAction {
        // Acciones de transacción
        TRANSACTION_INITIATED,
        TRANSACTION_AUTHORIZED,
        TRANSACTION_CAPTURED,
        TRANSACTION_FAILED,
        TRANSACTION_CANCELLED,
        TRANSACTION_REFUNDED,
        
        // Acciones de cuenta
        ACCOUNT_CREATED,
        ACCOUNT_UPDATED,
        ACCOUNT_ACTIVATED,
        ACCOUNT_DEACTIVATED,
        ACCOUNT_VERIFIED,
        
        // Acciones de pago
        PAYMENT_METHOD_ADDED,
        PAYMENT_METHOD_REMOVED,
        PAYMENT_METHOD_UPDATED,
        
        // Acciones de webhook
        WEBHOOK_RECEIVED,
        WEBHOOK_PROCESSED,
        WEBHOOK_FAILED,
        
        // Acciones de seguridad
        FRAUD_DETECTED,
        SUSPICIOUS_ACTIVITY,
        SECURITY_ALERT,
        
        // Acciones administrativas
        MANUAL_REVIEW,
        MANUAL_APPROVAL,
        MANUAL_REJECTION,
        CONFIGURATION_CHANGED
    }
}
