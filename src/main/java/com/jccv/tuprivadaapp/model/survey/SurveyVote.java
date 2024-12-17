package com.jccv.tuprivadaapp.model.survey;

import com.jccv.tuprivadaapp.model.resident.Resident;
import jakarta.persistence.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
public class SurveyVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resident_id")
    private Resident resident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_option_id")
    private SurveyOption surveyOption;

    // Getters y setters
}
