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
@Table(name = "charges")
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titleTypePayment;  // Ej. "Mantenimiento Junio", "Mantenimiento Julio"

    private LocalDateTime chargeDate;  // Fecha del cargo

    private boolean isActive = true;  // Indica si el cargo está activo o no

    private double amount;           // Monto del cargo
    private String description;      // Descripción del cargo
    private PenaltyTypeEnum penaltyType;
    private Double penaltyValue;
    private LocalDateTime dueDate;

    @OneToMany(mappedBy = "charge", cascade = CascadeType.ALL)
    @JsonManagedReference
    @ToString.Exclude
    private List<Payment> payments;  // Lista de pagos asociados a este cargo

    @ManyToOne
    @JoinColumn(name = "condominium_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;  // Referencia al condominio

}
