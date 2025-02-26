
package com.jccv.tuprivadaapp.model.transaction;

import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "deposits", indexes = {
        @Index(name = "idx_deposit_resident", columnList = "resident_id"),
        @Index(name = "idx_deposit_date", columnList = "deposit_date")
})
public class Deposit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resident_id", nullable = false)
    @ToString.Exclude
    private Resident resident;

    @Column(name = "amount", nullable = false)
    private Double amount;

    @Column(name = "deposit_date", nullable = false)
    private LocalDateTime depositDate;

    @Column(name = "bank_tracking_key", nullable = false)
    private String bankTrackingKey;
    @Column(name = "issuing_bank", nullable = false)
    private String issuingBank;

    @Column(name = "balance_after_deposit", nullable = false)
    private Double balanceAfterDeposit;
}
