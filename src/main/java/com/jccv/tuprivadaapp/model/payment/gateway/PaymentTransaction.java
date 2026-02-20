package com.jccv.tuprivadaapp.model.payment.gateway;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entidad que representa una transacción de pago procesada a través de cualquier pasarela.
 * Implementa el patrón de registro inmutable para auditoría.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "payment_transactions", indexes = {
        @Index(name = "idx_transaction_reference", columnList = "transaction_reference", unique = true),
        @Index(name = "idx_transaction_gateway_ref", columnList = "gateway_transaction_id"),
        @Index(name = "idx_transaction_payment", columnList = "payment_id"),
        @Index(name = "idx_transaction_status", columnList = "status"),
        @Index(name = "idx_transaction_created", columnList = "created_at"),
        @Index(name = "idx_transaction_bank_ref", columnList = "bank_personal_reference")
})
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100)
    private String transactionReference; // Referencia única interna

    @Column(name = "gateway_transaction_id", length = 255)
    private String gatewayTransactionId; // ID de transacción en la pasarela

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gateway_account_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private PaymentGatewayAccount gatewayAccount;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency = "MXN";

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_method_details", columnDefinition = "TEXT")
    private String paymentMethodDetails; // JSON con detalles del método de pago

    // Datos del pagador
    @Column(name = "payer_email")
    private String payerEmail;

    @Column(name = "payer_name")
    private String payerName;

    @Column(name = "payer_phone")
    private String payerPhone;

    @Column(name = "bank_personal_reference")
    private String bankPersonalReference;

    // Dirección del residente
    @Column(name = "street")
    private String street;

    @Column(name = "house_number")
    private String houseNumber;

    @Column(name = "interior_number")
    private String interiorNumber;

    // Comisiones y fees
    @Column(name = "gateway_fee", precision = 19, scale = 2)
    private BigDecimal gatewayFee; // Comisión de la pasarela

    @Column(name = "platform_fee", precision = 19, scale = 2)
    private BigDecimal platformFee; // Comisión de la plataforma

    @Column(name = "tax_amount", precision = 19, scale = 2)
    private BigDecimal taxAmount; // IVA u otros impuestos

    @Column(name = "net_amount", precision = 19, scale = 2)
    private BigDecimal netAmount; // Monto neto después de comisiones

    // Control de intentos y errores
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "response_raw", columnDefinition = "TEXT")
    private String responseRaw; // Respuesta completa de la pasarela (para debugging)

    // Metadatos adicionales
    @ElementCollection
    @CollectionTable(name = "payment_transaction_metadata",
            joinColumns = @JoinColumn(name = "transaction_id"))
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value", columnDefinition = "TEXT")
    private Map<String, String> metadata = new HashMap<>();

    // URLs de confirmación y webhook
    @Column(name = "confirmation_url", columnDefinition = "TEXT")
    private String confirmationUrl;

    @Column(name = "webhook_url", columnDefinition = "TEXT")
    private String webhookUrl;

    // Campos para OXXO Pay y otros métodos offline
    @Column(name = "reference_number")
    private String referenceNumber; // Número de referencia para pago en OXXO

    @Column(name = "barcode_url", columnDefinition = "TEXT")
    private String barcodeUrl; // URL del código de barras

    @Column(name = "client_secret", columnDefinition = "TEXT")
    private String clientSecret; // Para métodos que requieren client_secret (ej. tarjetas)

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Fecha de expiración para pagos offline

    // Auditoría
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdByUser;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "confirmed_at")
    private LocalDateTime confirmedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // IP y dispositivo para seguridad
    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_fingerprint")
    private String deviceFingerprint;

    // Estados de la transacción
    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        REQUIRES_ACTION,
        REQUIRES_PAYMENT_METHOD,
        REQUIRES_CONFIRMATION,
        AUTHORIZED,
        CAPTURED,
        SUCCEEDED,
        FAILED,
        CANCELLED,
        REFUNDED,
        PARTIALLY_REFUNDED,
        EXPIRED,
        DISPUTED
    }

    // Métodos de pago soportados
    public enum PaymentMethod {
        CARD,
        OXXO,
        OXXO_PAY,
        SPEI,
        BANK_TRANSFER,
        CASH,
        PAYPAL,
        WALLET
    }

    // Método helper para verificar si la transacción fue exitosa
    public boolean isSuccessful() {
        return status == TransactionStatus.SUCCEEDED || 
               status == TransactionStatus.CAPTURED;
    }

    // Método helper para verificar si la transacción puede ser reintentada
    public boolean canRetry() {
        return status == TransactionStatus.FAILED && 
               retryCount < 3;
    }
}
