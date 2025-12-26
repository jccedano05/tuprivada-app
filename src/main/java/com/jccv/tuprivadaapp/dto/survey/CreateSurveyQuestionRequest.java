package com.jccv.tuprivadaapp.dto.survey;

import com.jccv.tuprivadaapp.model.survey.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
public class CreateSurveyQuestionRequest {
    
    @NotBlank(message = "La pregunta es requerida")
    private String question;
    
    @NotNull(message = "El tipo de pregunta es requerido")
    private QuestionType type;
    
    private Boolean isRequired = false;
    
    @NotNull(message = "El orden es requerido")
    private Integer order;
    
    @Valid
    private List<CreateSurveyOptionRequest> options;
}
