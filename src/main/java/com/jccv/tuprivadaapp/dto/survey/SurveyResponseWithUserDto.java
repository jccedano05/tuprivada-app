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
public class SurveyResponseWithUserDto {
    private Long id;
    private Long surveyId;
    private String surveyTitle;
    private Long userId;
    private String userFirstName;
    private String userLastName;
    private String userEmail;
    private Long residentId;
    private List<SurveyAnswerDto> answers;
    private LocalDateTime submittedAt;
}
