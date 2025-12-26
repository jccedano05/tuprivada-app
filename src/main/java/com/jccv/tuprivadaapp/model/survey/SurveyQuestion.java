package com.jccv.tuprivadaapp.model.survey;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "survey_questions", indexes = {
        @Index(name = "idx_survey_question_survey_id", columnList = "survey_id")
})
public class SurveyQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private Survey survey;

    @Column(nullable = false, length = 1000)
    private String question;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionType type;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = false;

    @Column(name = "question_order", nullable = false)
    private Integer order;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<SurveyOption> options = new ArrayList<>();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    @Builder.Default
    private List<SurveyAnswer> answers = new ArrayList<>();
}
