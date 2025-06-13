package com.jccv.tuprivadaapp.model.payment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stripe_payment_intents", indexes = {
        @Index(name = "idx_payment_intent_id", columnList = "payment_intent_id"),
        @Index(name = "idx_paymentid_status", columnList = "payment_id, status")
})
public class StripePaymentIntent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_intent_id", nullable = false, unique = true)
    private String paymentIntentId;

    @Column(name = "client_secret")
    private String clientSecret;

    @Column(name = "amount")
    private Long amount;

    @Column(name = "currency")
    private String currency;

    @Column(name = "status")
    private String status;

    @Column(name = "application_fee_amount")
    private Long applicationFeeAmount;

    @Column(name = "destination_account_id")
    private String destinationAccountId;

    @Column(name = "payment_method_id")
    private String paymentMethodId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Payment payment;
}
