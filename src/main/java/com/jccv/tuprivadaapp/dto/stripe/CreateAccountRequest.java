package com.jccv.tuprivadaapp.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateAccountRequest {
    private String email;
    // más campos si usas Custom
}