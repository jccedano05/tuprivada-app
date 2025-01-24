//package com.jccv.tuprivadaapp.controller;
//
//import com.jccv.tuprivadaapp.dto.auth.*;
//import com.jccv.tuprivadaapp.exception.BadRequestException;
//import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
//import com.jccv.tuprivadaapp.model.Role;
//import com.jccv.tuprivadaapp.model.Token;
//import com.jccv.tuprivadaapp.model.User;
//import com.jccv.tuprivadaapp.service.AuthenticationService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.*;
//
//@RestController
//@RequestMapping("auth")
//public class AuthenticationController {
//
//    @Autowired
//    private AuthenticationService authenticationService;
//
//
//    @PostMapping("registerUser")
//    public ResponseEntity<?> register(@RequestBody UserDto request) {
//        try {
//            return ResponseEntity.ok(authenticationService.register(request));
//        } catch (BadRequestException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }catch (ResourceNotFoundException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//        }
//        catch (Exception e){
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @PostMapping("updatePasswordUser/{userId}")
////    @PreAuthorize("hasRole('ADMIN')")
//    public ResponseEntity<?> updatePassword(@PathVariable Long userId,
//                                                    @RequestBody UserChangePasswordDto resp) {
//        try {
//            UserDto updatedUser = authenticationService.updatePasswordUser(userId, resp.getNewPassword());
//            return ResponseEntity.ok(updatedUser);
//        } catch (ResourceNotFoundException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//        } catch (BadRequestException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//
//
//
//    @PostMapping("login")
//    public ResponseEntity<?> login(@RequestBody User request) {
//        try {
//
//            AuthenticatedUserDto user =authenticationService.authenticate(request);
//            System.out.println("******** USER  SERVice**********");
//            System.out.println(user.toString());
//            return ResponseEntity.ok(user);
//
//        } catch (BadRequestException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        } catch (ResourceNotFoundException e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }
//
//    @GetMapping("/validateToken")
//    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
//        String token = "";
//        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
//             token = authorizationHeader.substring(7); // Elimina el prefijo "Bearer "
//        }
//            return ResponseEntity.ok(authenticationService.validateToken(token));
//    }
//
//}


package com.jccv.tuprivadaapp.controller;

import com.jccv.tuprivadaapp.dto.auth.*;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.service.AuthenticationService;


import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    private final Bucket loginBucket;
    private final Bucket registerBucket;
    private final Bucket validateTokenBucket;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;

        // Limitar a 5 solicitudes cada 10 minutos para el login
        Bandwidth loginLimit = Bandwidth.simple(50, Duration.ofMinutes(10));
        this.loginBucket = Bucket4j.builder().addLimit(loginLimit).build();

        // Limitar a 3 solicitudes cada 15 minutos para el registro
        Bandwidth registerLimit = Bandwidth.simple(10, Duration.ofMinutes(15));
        this.registerBucket = Bucket4j.builder().addLimit(registerLimit).build();

        // Limitar a 50 solicitudes cada 5 minutos para validar tokens
        Bandwidth validateTokenLimit = Bandwidth.simple(100, Duration.ofMinutes(5));
        this.validateTokenBucket = Bucket4j.builder().addLimit(validateTokenLimit).build();
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
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("changePassword")
    public ResponseEntity<?> changePassword(@RequestBody UserChangePasswordDto resp) {
        try {
            System.out.println("Entro al change password");
            UserDto updatedUser = authenticationService.changePassword( resp);
            return ResponseEntity.ok(updatedUser);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        if (validateTokenBucket.tryConsume(1)) {
            String token = "";
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                token = authorizationHeader.substring(7); // Elimina el prefijo "Bearer "
            }
            return ResponseEntity.ok(authenticationService.validateToken(token));
        } else {
            return new ResponseEntity<>("Demasiadas solicitudes para validar token, intenta más tarde.", HttpStatus.TOO_MANY_REQUESTS);
        }
    }
}
