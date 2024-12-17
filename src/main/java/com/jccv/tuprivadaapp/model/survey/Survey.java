package com.jccv.tuprivadaapp.model.survey;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "surveys")
public class Survey {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium; // Relación con Condominium

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<SurveyOption> options;

    // Getters y setters
}

