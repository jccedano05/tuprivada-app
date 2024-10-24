package com.jccv.tuprivadaapp.dto.auth;

import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {


    private final UserRepository userRepository;

    public AuthorizationService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void verifyAuthorityToUpdate(User userToUpdate) {
        User currentUser = getCurrentUser();

        if (!hasValidAuthority(currentUser, userToUpdate)) {
            throw new BadRequestException("Credenciales no válidas para hacer el update");
        }
    }

    private User getCurrentUser() {
        return userRepository.findByUsername(
                SecurityContextHolder.getContext().getAuthentication().getName()
        ).orElseThrow(() -> new BadRequestException("No se encontró el user de la cuenta"));
    }

    private boolean hasValidAuthority(User currentUser, User userToUpdate) {
        return currentUser.getCondominium() != userToUpdate.getCondominium() &&
                currentUser.getCondominium() == null &&
                Role.SUPERADMIN.equals(currentUser.getRole()) ||
                currentUser.getUsername().equals(userToUpdate.getUsername()) ||
                Role.ADMIN.equals(currentUser.getRole());
    }
}
