package com.jccv.tuprivadaapp.service.user.implementation;

import com.jccv.tuprivadaapp.dto.user.UserDataToShowDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User updateUser(Long userId, UserDataToShowDto userDataToShowDto) {
        // Buscamos al usuario por su ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // Validamos que el email no esté en uso por otro usuario
        userRepository.findByEmail(userDataToShowDto.getEmail()).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(userId)) {
                throw new BadRequestException("El email ya está en uso por otro usuario");
            }
        });

        // Actualizamos los campos permitidos
        user.setFirstName(userDataToShowDto.getFirstName());
        user.setLastName(userDataToShowDto.getLastName());
        user.setEmail(userDataToShowDto.getEmail());
        user.setBankPersonalReference(userDataToShowDto.getBankPersonalReference());

        // Guardamos los cambios
        return userRepository.save(user);
    }

    @Override
    public List<UserDataToShowDto> getAllUsersByCondominiumId(Long condominiumId) {
        return userRepository.findUsersByCondominiumId(condominiumId);

    }

}

