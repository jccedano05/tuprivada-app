package com.jccv.tuprivadaapp.dto.survey;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveyAnswerRequest {
    
    @NotNull(message = "El ID de la pregunta es requerido")
    private Long questionId;
    
    private List<Long> selectedOptionIds;
    private String textAnswer;
    private Integer ratingValue;
}
