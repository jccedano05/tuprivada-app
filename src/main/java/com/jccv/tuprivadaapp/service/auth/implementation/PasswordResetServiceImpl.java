package com.jccv.tuprivadaapp.service.auth.implementation;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.auth.PasswordResetToken;
import com.jccv.tuprivadaapp.repository.auth.PasswordResetTokenRepository;
import com.jccv.tuprivadaapp.service.auth.PasswordResetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;

    public PasswordResetServiceImpl(PasswordResetTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(LocalDateTime.now().plusMinutes(30))
                .build();
        tokenRepository.save(passwordResetToken);
    }

    @Override
    public PasswordResetToken validatePasswordResetToken(String token) {
        return tokenRepository.findByToken(token)
                .filter(resetToken -> !resetToken.isExpired())
                .orElseThrow(() -> new IllegalArgumentException("Token inválido o expirado"));
    }

    @Override
    public String generateResetToken() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder();
        int tokenLength = 10; // Puedes cambiar la longitud del token según lo necesites

        for (int i = 0; i < tokenLength; i++) {
            int index = random.nextInt(characters.length());
            token.append(characters.charAt(index));
        }
        return token.toString();
    }

}
