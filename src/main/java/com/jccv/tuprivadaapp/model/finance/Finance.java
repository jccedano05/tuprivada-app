package com.jccv.tuprivadaapp.model.finance;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "finances")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Finance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    private Condominium condominium;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private double incomeQuantity;

    @Column(nullable = false)
    private double billQuantity;  // Gasto o egreso
}
