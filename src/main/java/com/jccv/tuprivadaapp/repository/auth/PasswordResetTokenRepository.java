package com.jccv.tuprivadaapp.repository.auth;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.auth.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByUser(User user);
}