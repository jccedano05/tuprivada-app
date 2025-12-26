package com.jccv.tuprivadaapp.dto.survey;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyStatsDto {
    private Long totalSurveys;
    private Long activeSurveys;
    private Long totalResponses;
    private Double averageResponseRate;
    private Map<String, Long> surveysByType;
}
