package com.jccv.tuprivadaapp.dto.survey;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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
public class UpdateSurveyResponseRequest {
    
    @NotNull(message = "El ID del usuario es requerido")
    private Long userId;
    
    @NotEmpty(message = "Las respuestas no pueden estar vacías")
    @Valid
    private List<SurveyAnswerRequest> answers;
}
