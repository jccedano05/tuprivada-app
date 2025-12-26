package com.jccv.tuprivadaapp.dto.survey;

import com.jccv.tuprivadaapp.model.survey.SurveyStatus;
import com.jccv.tuprivadaapp.model.survey.SurveyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
public class CreateSurveyRequest {
    
    @NotBlank(message = "El título es requerido")
    private String title;
    
    private String description;
    
    @NotNull(message = "El tipo de encuesta es requerido")
    private SurveyType type;
    
    @NotNull(message = "El estado es requerido")
    private SurveyStatus status;
    
    @NotNull(message = "La fecha de inicio es requerida")
    private LocalDateTime startDate;
    
    @NotNull(message = "La fecha de fin es requerida")
    private LocalDateTime endDate;
    
    private Boolean isAnonymous = false;
    
    private Boolean allowMultipleVotes = false;
    
    @NotNull(message = "El ID del condominio es requerido")
    private Long condominiumId;
    
    @NotEmpty(message = "Debe incluir al menos una pregunta")
    @Valid
    private List<CreateSurveyQuestionRequest> questions;
}
