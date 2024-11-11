package com.jccv.tuprivadaapp.dto.auth.mapper;

import com.jccv.tuprivadaapp.dto.auth.UserDto;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {


    private final CondominiumService condominiumService;

    public UserMapper(@Lazy CondominiumService condominiumService) {
        this.condominiumService = condominiumService;
    }

    public UserDto convertUserToUserDto(User user) {

        UserDto.UserDtoBuilder userDto = UserDto.builder()
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
