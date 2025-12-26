package com.jccv.tuprivadaapp.dto.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyStatus;
import com.jccv.tuprivadaapp.model.survey.SurveyType;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSurveyRequest {
    
    private String title;
    private String description;
    private SurveyType type;
    private SurveyStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isAnonymous;
    private Boolean allowMultipleVotes;
    
    @Valid
    private List<CreateSurveyQuestionRequest> questions;
}
