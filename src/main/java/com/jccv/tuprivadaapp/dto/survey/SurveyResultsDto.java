package com.jccv.tuprivadaapp.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyResultsDto {
    private Long surveyId;
    private Integer totalResponses;
    private Double responseRate;
    private List<QuestionResultDto> questions;
}
