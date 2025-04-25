package com.jccv.tuprivadaapp.model.payment;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "deposit_payments", indexes = {
        @Index(name = "idx_deposit_date", columnList = "depositDate")
})
public class DepositPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;

    // Monto del abono
    private double amount;

    // Fecha en que se realizó el abono
    private LocalDateTime depositDate;

    // Relación ManyToOne con Payment (un Payment puede tener muchos abonos)
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "payment_id", nullable = false)
//    @ToString.Exclude
//    private Payment payment;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonBackReference
    @ToString.Exclude
    private Payment payment;
}
