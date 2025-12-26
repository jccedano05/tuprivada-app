package com.jccv.tuprivadaapp.dto.survey;

import com.jccv.tuprivadaapp.model.survey.QuestionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyQuestionDto {
    private Long id;
    private String question;
    private QuestionType type;
    private Boolean isRequired;
    private Integer order;
    private List<SurveyOptionDto> options;
}
