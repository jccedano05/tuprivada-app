package com.jccv.tuprivadaapp.model.payment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
//@Table(name = "payments")
@Table(name = "payments", indexes = {
        @Index(name = "idx_resident_paid_deleted", columnList = "resident_id, is_paid, is_deleted"),
        @Index(name = "idx_charge_date", columnList = "charge_date")})
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double amount;

    private LocalDateTime chargeDate;

    private LocalDateTime dueDate;

    private String typePayment;

    private String description;

    @Column(name = "is_paid")
    private boolean isPaid = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;


    @Column(name = "penalty_value")
    private double penaltyValue;  // Valor del recargo (fijo o porcentaje)

    @Enumerated(EnumType.STRING)
    @Column(name = "penalty_type")
    private PenaltyTypeEnum penaltyType;  // Tipo de recargo (fijo o porcentaje)

    @Column(name = "is_penalty_applied")
    private boolean isPenaltyApplied = false;  // Control para saber si ya se aplicó el recargo


    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    @JsonBackReference
    private Resident resident;

}
