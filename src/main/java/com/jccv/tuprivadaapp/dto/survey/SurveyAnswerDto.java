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
public class SurveyAnswerDto {
    private Long id;
    private Long questionId;
    private String questionText;
    private List<Long> selectedOptionIds;
    private String textAnswer;
    private Integer ratingValue;
}
