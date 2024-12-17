package com.jccv.tuprivadaapp.dto.auth;

import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder

public class AuthenticatedUserDto {

    private Long id;
    private String token;
    private String firstName;
    private String lastName;
    private String username;
    private Role role;
    private Condominium condominium;
}
