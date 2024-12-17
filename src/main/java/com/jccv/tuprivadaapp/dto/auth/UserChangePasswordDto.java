package com.jccv.tuprivadaapp.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
public class UserChangePasswordDto {
    private Long userId;
    private String newPassword;
}
