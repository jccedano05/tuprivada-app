package com.jccv.tuprivadaapp.model.payment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.charge.Charge;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_resident_paid_deleted", columnList = "resident_id, is_paid, is_deleted")})
//        @Index(name = "idx_charge_date", columnList = "charge_date")})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "is_paid")
    private boolean isPaid = false;


    @Column(name = "date_paid")
    private LocalDateTime datePaid = null;



    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "is_penalty_applied")
    private boolean isPenaltyApplied = false;  // Control para saber si ya se aplicó el recargo

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Charge charge;

    @OneToMany(mappedBy = "payment")
    @JsonManagedReference
    @ToString.Exclude
    private List<DepositPayment> depositPayments;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    @ToString.Exclude
    private List<StripePaymentIntent> stripePaymentIntents;
}
