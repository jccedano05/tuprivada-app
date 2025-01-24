package com.jccv.tuprivadaapp.model.finance;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "finance_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinanceCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    private String description;

    @Column(nullable = false)
    private boolean isExpense;  // true si es un gasto, false si es un ingreso

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;
}
