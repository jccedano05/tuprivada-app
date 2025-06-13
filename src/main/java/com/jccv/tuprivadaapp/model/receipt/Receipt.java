package com.jccv.tuprivadaapp.model.receipt;

import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.payment.DepositPayment;
import com.jccv.tuprivadaapp.model.payment.Payment;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "receipts")
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String receiptName;

    private String title;

    private String description;

    private String residentName;

    private String residentAddress;
    private Double amount;

    private LocalDateTime datePaid;

    private String operationCode;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id", nullable = false)
    private Condominium condominium;


    @Lob
    private byte[] qrCode;

//    @Lob
//    @Basic(fetch = FetchType.LAZY)
//    private byte[] pdfData;

    @OneToOne
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @OneToOne
    @JoinColumn(name = "deposit_payment_id")
    private DepositPayment depositPayment;
}
