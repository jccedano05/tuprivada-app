package com.jccv.tuprivadaapp.model.resident;

import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "payments_residents")
public class PaymentResident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Digits(integer = 5, fraction = 2, message = "El monto debe tener como máximo 5 dígitos enteros y 2 decimales")
    private BigDecimal amount;
    @Column(columnDefinition = "DATE DEFAULT CURRENT_DATE")
    private LocalDate date;

    @Column(columnDefinition = "DATE DEFAULT CURRENT_DATE + INTERVAL '15 days'")
    private LocalDate expirationDate;

    private boolean isComplete; // Nuevo campo

    @ManyToOne(optional = false)
    @JoinColumn(name = "resident_id", nullable = false)
    @NotNull(message = "El residente no debe estar vacío")
    private Resident resident;

    @Column(columnDefinition = "boolean default false")
    public boolean isComplete() {
        return isComplete;
    }


}
