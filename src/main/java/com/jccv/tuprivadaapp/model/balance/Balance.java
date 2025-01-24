package com.jccv.tuprivadaapp.model.balance;


import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "balances", indexes = {
        @Index(name = "idx_resident_balance", columnList = "resident_id")})
public class Balance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "amount", nullable = false)
    private double amount;  // El saldo actual del residente

    @Column(name = "deposit_date", nullable = false)
    private LocalDateTime depositDate;  // Fecha de depósito

    @Column(name = "tracking_key", nullable = false)
    private String trackingKey;  // Clave de rastreo del depósito

    @Column(name = "issuer_bank", nullable = false)
    private String issuerBank;  // Banco emisor

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    private Resident resident;

    // Otros campos relevantes, como comentarios u observaciones
}
