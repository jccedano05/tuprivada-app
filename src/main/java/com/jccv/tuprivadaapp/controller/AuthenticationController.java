
package com.jccv.tuprivadaapp.controller;

import com.jccv.tuprivadaapp.dto.auth.*;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.auth.PasswordResetToken;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.service.AuthenticationService;


import com.jccv.tuprivadaapp.service.auth.PasswordResetService;
import com.jccv.tuprivadaapp.service.email.EmailService;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository;

    private final Bucket loginBucket;
    private final Bucket registerBucket;
    private final Bucket validateTokenBucket;
    private final Bucket validateEmailResetPassword;
    private final Bucket validateTokenResetPassword;

    private final PasswordResetService passwordResetService;
    private final EmailService emailService;


    @Autowired
    public AuthenticationController(AuthenticationService authenticationService, UserRepository userRepository, PasswordResetService passwordResetService, EmailService emailService) {
        this.authenticationService = authenticationService;
        this.userRepository = userRepository;
        this.passwordResetService = passwordResetService;
        this.emailService = emailService;

        // Limitar a 5 solicitudes cada 10 minutos para el login
        Bandwidth loginLimit = Bandwidth.simple(50, Duration.ofMinutes(10));
        this.loginBucket = Bucket4j.builder().addLimit(loginLimit).build();

        // Limitar a 3 solicitudes cada 15 minutos para el registro
        Bandwidth registerLimit = Bandwidth.simple(10, Duration.ofMinutes(15));
        this.registerBucket = Bucket4j.builder().addLimit(registerLimit).build();

        // Limitar a 50 solicitudes cada 5 minutos para validar tokens
        Bandwidth validateTokenLimit = Bandwidth.simple(100, Duration.ofMinutes(5));
        this.validateTokenBucket = Bucket4j.builder().addLimit(validateTokenLimit).build();


        // Limitar a 3 solicitudes cada 15 minutos para el registro
        Bandwidth validateEmailResetPassword = Bandwidth.simple(10, Duration.ofMinutes(15));
        this.validateEmailResetPassword = Bucket4j.builder().addLimit(validateEmailResetPassword).build();


        // Limitar a 3 solicitudes cada 15 minutos para el registro
        Bandwidth validateTokenResetPassword = Bandwidth.simple(60, Duration.ofMinutes(30));
        this.validateTokenResetPassword = Bucket4j.builder().addLimit(validateTokenResetPassword).build();
    }

    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody User request) {
        if (loginBucket.tryConsume(1)) {
            try {
                AuthenticatedUserDto user = authenticationService.authenticate(request);
                return ResponseEntity.ok(user);
            } catch (BadRequestException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (ResourceNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Demasiadas solicitudes de login, intenta más tarde.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @PostMapping("registerUser")
    public ResponseEntity<?> register(@RequestBody UserDto request) {
        if (registerBucket.tryConsume(1)) {
            try {
                return ResponseEntity.ok(authenticationService.register(request));
            } catch (BadRequestException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
            } catch (ResourceNotFoundException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
            } catch (Exception e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } else {
            return new ResponseEntity<>("Demasiadas solicitudes de registro, intenta más tarde.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @PostMapping("updatePasswordUser/{userId}")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId, @RequestBody UserChangePasswordDto resp) {
        try {
            UserDto updatedUser = authenticationService.updatePasswordUser(userId, resp.getNewPassword());
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();  // Esto te dará más información sobre la excepción

            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("changePassword")
    public ResponseEntity<?> changePassword(@RequestBody UserChangePasswordDto resp) {
        try {
            UserDto updatedUser = authenticationService.changePassword( resp);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();  // Esto te dará más información sobre la excepción
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
       try{
           if (validateTokenBucket.tryConsume(1)) {
               String token = "";
               if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                   token = authorizationHeader.substring(7); // Elimina el prefijo "Bearer "
               }
               return ResponseEntity.ok(authenticationService.validateToken(token));
           } else {
               return new ResponseEntity<>("Demasiadas solicitudes para validar token, intenta más tarde.", HttpStatus.TOO_MANY_REQUESTS);
           }
       }catch (JwtException e){
           return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
       }catch (Exception e){
           return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
       }
    }



    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email) {

        if (validateEmailResetPassword.tryConsume(1)) {
        // Validar si el correo existe
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new ResponseEntity<>("El correo no está registrado", HttpStatus.NOT_FOUND);
        }

        User user = optionalUser.get();

        // Generar token de recuperación
        String resetToken = passwordResetService.generateResetToken();
        passwordResetService.createPasswordResetTokenForUser(user, resetToken);

        // Enviar correo con el código
        Map<String, Object> variables = Map.of("codeForReset", resetToken);
        emailService.sendHtmlEmail(user.getEmail(), "Cambio de contraseña Ayni comunidad", "resetPassword", variables);

        return ResponseEntity.ok("Se ha enviado un correo con las instrucciones para cambiar la contraseña");
    }else {
            return new ResponseEntity<>("Demasiadas solicitudes para validar token, intenta más tarde.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }

    @GetMapping("/reset-password/validate-token")
    public ResponseEntity<?> validateResetToken(@RequestParam("token") String token) {
        try {
            if (validateTokenResetPassword.tryConsume(1)) {
                PasswordResetToken resetToken = passwordResetService.validatePasswordResetToken(token);

                return ResponseEntity.ok("Token válido.");
            } else {
                return new ResponseEntity<>("Demasiadas solicitudes para validar token, intenta más tarde.", HttpStatus.TOO_MANY_REQUESTS);
            }

        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al validar el token.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset-password/update")
    public ResponseEntity<?> updatePasswordWithoutOld(@RequestBody UserChangePasswordDto passwordDto, @RequestParam("token") String token) {
        try {
            // Validar token antes de proceder
            PasswordResetToken resetToken = passwordResetService.validatePasswordResetToken(token);
            User user = resetToken.getUser();

            // Validar si las contraseñas coinciden
            if (!passwordDto.getNewPassword().equals(passwordDto.getConfirmPassword())) {
                throw new BadRequestException("Las contraseñas no coinciden.");
            }

            // Actualizar la contraseña del usuario
            UserDto updatedUser = authenticationService.updatePasswordUser(user.getId(), passwordDto.getNewPassword());
            return ResponseEntity.ok(updatedUser);
        } catch (BadRequestException | IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al actualizar la contraseña.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }




}
