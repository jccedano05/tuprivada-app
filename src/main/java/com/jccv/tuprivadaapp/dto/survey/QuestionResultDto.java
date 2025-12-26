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
public class QuestionResultDto {
    private Long questionId;
    private String question;
    private QuestionType type;
    private Integer totalAnswers;
    private List<SurveyOptionDto> options;
    private List<String> textAnswers;
    private Double averageRating;
}
