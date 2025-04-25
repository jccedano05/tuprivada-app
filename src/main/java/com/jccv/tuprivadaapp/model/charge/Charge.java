package com.jccv.tuprivadaapp.model.charge;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.Payment;
import com.jccv.tuprivadaapp.model.payment.PenaltyTypeEnum;
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
@Table(name = "charges", indexes = {
        @Index(name = "idx_charge_date", columnList = "charge_date")
})
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titleTypePayment;

    private LocalDateTime chargeDate;

    private boolean isActive = true;

    private double amount;
    private String description;
    private PenaltyTypeEnum penaltyType;
    private Double penaltyValue;
    private LocalDateTime dueDate;

    @OneToMany(mappedBy = "charge", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private List<Payment> payments;

    @ManyToOne
    @JoinColumn(name = "condominium_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;

}
