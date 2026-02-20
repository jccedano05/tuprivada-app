package com.jccv.tuprivadaapp.model.payment.gateway;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad que representa una cuenta de pasarela de pago asociada a un condominio.
 * Diseñada para soportar múltiples proveedores (Conekta, Stripe, OpenPay, etc.)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_gateway_accounts", indexes = {
        @Index(name = "idx_gateway_account_condominium", columnList = "condominium_id"),
        @Index(name = "idx_gateway_account_provider", columnList = "provider"),
        @Index(name = "idx_gateway_account_active", columnList = "is_active")
})
public class PaymentGatewayAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "provider", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private PaymentProvider provider;

    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId; // ID de la cuenta en el proveedor (ej: acct_xxx en Conekta)

    @Column(name = "account_name")
    private String accountName;

    @Column(name = "account_email")
    private String accountEmail;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "is_verified", nullable = false)
    private boolean isVerified = false;

    @Column(name = "onboarding_status")
    @Enumerated(EnumType.STRING)
    private OnboardingStatus onboardingStatus = OnboardingStatus.PENDING;

    @Column(name = "capabilities", columnDefinition = "TEXT")
    private String capabilities; // JSON con las capacidades habilitadas

    @ElementCollection
    @CollectionTable(name = "payment_gateway_account_metadata",
            joinColumns = @JoinColumn(name = "gateway_account_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private Map<String, String> metadata = new HashMap<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;

    @Column(name = "commission_percentage")
    private Double commissionPercentage; // Porcentaje de comisión acordado

    @Column(name = "fixed_fee")
    private Double fixedFee; // Tarifa fija por transacción

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "deactivated_at")
    private LocalDateTime deactivatedAt;

    // Enumeración para proveedores de pago
    public enum PaymentProvider {
        CONEKTA,
        STRIPE,
        OPENPAY,
        MERCADO_PAGO,
        PAYPAL
    }

    // Estado del proceso de onboarding
    public enum OnboardingStatus {
        PENDING,
        IN_PROGRESS,
        REQUIRES_ACTION,
        COMPLETED,
        REJECTED,
        SUSPENDED
    }
}
