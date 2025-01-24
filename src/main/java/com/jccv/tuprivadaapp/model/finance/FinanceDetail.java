package com.jccv.tuprivadaapp.model.finance;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "finance_details")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String concept;  // Concepto del trabajo o depósito

    @Column(nullable = false)
    private double amount;  // Monto del trabajo o depósito

    private String description;  // Descripción opcional

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_category_id")
    private FinanceCategory financeCategory;  // Relación con la categoría

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "finance_id")
    private Finance finance;  // Relación con la finanza
}
