package com.jccv.tuprivadaapp.dto.survey;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSurveyOptionRequest {
    
    @NotBlank(message = "El texto de la opción es requerido")
    private String text;
    
    @NotNull(message = "El orden es requerido")
    private Integer order;
}
