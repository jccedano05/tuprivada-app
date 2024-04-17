package com.jccv.tuprivadaapp.dto.auth;

import com.jccv.tuprivadaapp.model.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserUpdateByAdminDto extends UserUpdateDto{
    private String firstName;
    private String lastName;
    private Role role;
}
