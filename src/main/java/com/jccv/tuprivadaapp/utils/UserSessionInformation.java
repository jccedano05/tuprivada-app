package com.jccv.tuprivadaapp.utils;

import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserSessionInformation {

    private final UserRepository userRepository;

    @Autowired
    public UserSessionInformation(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserInformationFromSecurityContext() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();




            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
