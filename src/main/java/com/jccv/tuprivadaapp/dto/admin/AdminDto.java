package com.jccv.tuprivadaapp.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDto {

    private Long id;
    private boolean isActive;
    private Long userId;  // Referencia al ID del usuario
}
