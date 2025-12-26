package com.jccv.tuprivadaapp.dto.finance;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FinanceCategoryCreateRequest {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    private String category;

    private String description;

    private boolean isExpense;
}
