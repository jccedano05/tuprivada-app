package com.jccv.tuprivadaapp.service.auth;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.auth.PasswordResetToken;

public interface PasswordResetService {
    void createPasswordResetTokenForUser(User user, String token);
    PasswordResetToken validatePasswordResetToken(String token);
    String generateResetToken();
}
