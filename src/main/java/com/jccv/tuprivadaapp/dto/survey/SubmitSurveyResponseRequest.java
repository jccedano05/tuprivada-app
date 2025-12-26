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
public class SubmitSurveyResponseRequest {
    
    @NotNull(message = "El ID del usuario es requerido")
    private Long userId;
    
    private Long residentId;
    
    @NotEmpty(message = "Debe incluir al menos una respuesta")
    @Valid
    private List<SurveyAnswerRequest> answers;
}
