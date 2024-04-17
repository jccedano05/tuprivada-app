package com.jccv.tuprivadaapp.dto.auth;


import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import lombok.*;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder
@Component
public class UserDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String password;
    private Long condominiumId;
    private Role role;
    private String secretKey;

    public UserDto convertUserToUserDto(User user) {

        UserDtoBuilder userDto = UserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole());
        if (user.getCondominium() != null) {
            userDto.condominiumId(user.getCondominium().getId());
        }
        return userDto.build();
    }
}



