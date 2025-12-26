package com.jccv.tuprivadaapp.model.survey;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
@Table(name = "survey_answers", indexes = {
        @Index(name = "idx_survey_answer_response_id", columnList = "response_id"),
        @Index(name = "idx_survey_answer_question_id", columnList = "question_id")
})
public class SurveyAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private SurveyResponse response;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @ToString.Exclude
    private SurveyQuestion question;

    @ElementCollection
    @CollectionTable(name = "survey_answer_selected_options", 
        joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "option_id")
    @Builder.Default
    private List<Long> selectedOptionIds = new ArrayList<>();

    @Column(name = "text_answer", length = 2000)
    private String textAnswer;

    @Column(name = "rating_value")
    private Integer ratingValue;
}
