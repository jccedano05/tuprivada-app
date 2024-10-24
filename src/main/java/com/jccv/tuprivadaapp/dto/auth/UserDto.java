package com.jccv.tuprivadaapp.dto.auth;


import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import lombok.*;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    CondominiumService condominiumService;

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


    public User convertUserDtoToUser(UserDto userDto) {

        User user = User.builder()
                .id(userDto.getId())
                .firstName(userDto.getFirstName())
                .lastName(userDto.getLastName())
                .role(userDto.getRole()).build();
        if (userDto.getCondominiumId() != null) {
            user.setCondominium(condominiumService.findById(userDto.getCondominiumId()));
        }
        return user;
    }
}



