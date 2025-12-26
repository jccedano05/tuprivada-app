package com.jccv.tuprivadaapp.dto.user;

import com.jccv.tuprivadaapp.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDataToShowDto {

    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String firstName;

    @NotBlank(message = "El apellido no puede estar vacío")
    private String lastName;

    @NotBlank(message = "El email no puede estar vacío")
    private String email;

    private String bankPersonalReference;

    @Pattern(regexp = "^[+]?\\d{7,15}$", message = "El teléfono debe contener entre 7 y 15 dígitos y puede iniciar con +")
    private String phone;

    @Pattern(regexp = "^\\+\\d{1,4}$", message = "El código de país debe iniciar con + y contener de 1 a 4 dígitos")
    private String countryCode;

    private Role role;
}
