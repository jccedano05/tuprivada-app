package com.jccv.tuprivadaapp.dto.auth;


import com.jccv.tuprivadaapp.model.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserUpdateBySuperadminDto {
    private String firstName;
    private String lastName;
    private Long condominiumId;
    private Role role;
}
