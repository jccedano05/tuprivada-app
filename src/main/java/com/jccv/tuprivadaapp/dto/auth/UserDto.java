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
    private String email;
    private String countryCode;
    private String phone;
    private Role role;
    private String bankPersonalReference;




}



