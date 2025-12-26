package com.jccv.tuprivadaapp.dto.survey;

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
public class SurveyResponseDto {
    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private Long userId;
    private Long residentId;
    private List<SurveyAnswerDto> answers;
    private LocalDateTime submittedAt;
}
