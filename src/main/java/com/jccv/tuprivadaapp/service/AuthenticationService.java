package com.jccv.tuprivadaapp.service;

import com.jccv.tuprivadaapp.jwt.model.AuthenticationResponse;
import com.jccv.tuprivadaapp.model.Token;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.TokenRepository;
import com.jccv.tuprivadaapp.repository.UserRepository;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthenticationService {

    private final UserRepository repository;
    @Autowired
    private ResidentRepository residentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(UserRepository repository, PasswordEncoder passwordEncoder, JwtService jwtService, TokenRepository tokenRepository, AuthenticationManager authenticationManager) {
        this.repository = repository;

        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
    }


    public AuthenticationResponse register(User request){
        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = repository.save(user);

        String token = jwtService.generateToken(user);
        saveToken(user, token);

        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse registerResident(User request){
        Resident user = (Resident) User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        user = residentRepository.save(user);

        String token = jwtService.generateToken(user);
        saveToken(user, token);

        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse authenticate(User request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);
        revokeAllTokensByUser(user);
        saveToken(user, token);
        return new AuthenticationResponse(token);
    }

    public boolean validateToken(Token token) {

        return jwtService.isValid(token.getToken());

    }

    private void revokeAllTokensByUser(User user) {
        List<Token> validTokenListByUser = tokenRepository.findAllTokensByUser(user.getId());
        if(!validTokenListByUser.isEmpty()){
            validTokenListByUser.forEach(t -> t.setLoggedOut(true));
        }
        tokenRepository.saveAll(validTokenListByUser);
    }

    private  void saveToken(User user, String token) {
        Token tokenToSave = Token.builder()
                .token(token)
                .loggedOut(false)
                .user(user)
                .build();
        tokenRepository.save(tokenToSave);

    }



}
