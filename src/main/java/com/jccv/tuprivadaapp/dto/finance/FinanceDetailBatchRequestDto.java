package com.jccv.tuprivadaapp.dto.finance;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinanceDetailBatchRequestDto {

    @NotNull(message = "El financeId es obligatorio")
    private Long financeId;

    @NotNull(message = "El categoryId es obligatorio")
    private Long categoryId;

    @Valid
    @NotEmpty(message = "Debes proporcionar al menos un detalle")
    private List<FinanceDetailBatchItemDto> details;
}
