package com.jccv.tuprivadaapp.model.survey;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Table(name = "survey_options", indexes = {
        @Index(name = "idx_survey_option_question_id", columnList = "question_id")
})
public class SurveyOption {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    @JsonBackReference
    @ToString.Exclude
    private SurveyQuestion question;

    @Column(nullable = false, length = 500)
    private String text;

    @Column(name = "option_order", nullable = false)
    private Integer order;

    @Column(nullable = false)
    private Integer votes = 0;

    @Transient
    public Double getPercentage(Integer totalVotes) {
        if (totalVotes == null || totalVotes == 0) {
            return 0.0;
        }
        return (votes * 100.0) / totalVotes;
    }
}
