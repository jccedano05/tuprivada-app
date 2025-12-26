package com.jccv.tuprivadaapp.dto.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStatusRequest {
    
    @NotNull(message = "El estado es requerido")
    private SurveyStatus status;
}
