package com.jccv.tuprivadaapp.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserUpdateDto {
    private String firstName;
    private String lastName;
}
