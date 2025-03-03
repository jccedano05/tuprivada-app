package com.jccv.tuprivadaapp.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import com.jccv.tuprivadaapp.model.Role;

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

    private Role role;
}
