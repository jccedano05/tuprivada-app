package com.jccv.tuprivadaapp.model.faq;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "faqs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Faq {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "condominium_id")
    @JsonBackReference
    @ToString.Exclude
    private Condominium condominium;

    @NotBlank(message = "La pregunta no puede estar vacía")
    private String question;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String answer;
}
