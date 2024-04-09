package com.jccv.tuprivadaapp.controller;

import com.jccv.tuprivadaapp.jwt.model.AuthenticationResponse;
import com.jccv.tuprivadaapp.model.Token;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthenticationController {


private final AuthenticationService authenticationService;


    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody User request){
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/registerResident")
    public ResponseEntity<AuthenticationResponse> registerResident(@RequestBody User request){
        return ResponseEntity.ok(authenticationService.registerResident(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody User request){
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestBody Token token) {
        System.out.println("token Jwt Controller: " + token);
        return ResponseEntity.ok(authenticationService.validateToken(token));
    }
}
