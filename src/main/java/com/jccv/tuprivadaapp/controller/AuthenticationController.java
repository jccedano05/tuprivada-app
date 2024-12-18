package com.jccv.tuprivadaapp.controller;

import com.jccv.tuprivadaapp.dto.auth.*;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.Token;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.*;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;


    @PostMapping("registerUser")
    public ResponseEntity<?> register(@RequestBody UserDto request) {
        try {
            return ResponseEntity.ok(authenticationService.register(request));
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
        catch (Exception e){
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("updatePasswordUser/{userId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updatePassword(@PathVariable Long userId,
                                                    @RequestBody UserChangePasswordDto resp) {
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


//
//    @PutMapping("updateBySuperadmin/{userId}")
//    @PreAuthorize(MAX_LEVEL)
//    public ResponseEntity<?> updateUserBySuperAdmin(@PathVariable Long userId,
//                                        @RequestBody UserUpdateBySuperadminDto request) {
//        try {
//            return ResponseEntity.ok(authenticationService.updateUser(userId, request));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//
//    @PutMapping("updateByAdmin/{userId}")
//    @PreAuthorize(CONDOMINIUM_LEVEL)
//    public ResponseEntity<?> updateUserByAdmin(@PathVariable Long userId,
//                                        @RequestBody UserUpdateByAdminDto request) {
//        try {
//            return ResponseEntity.ok(authenticationService.updateUser(userId, request));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }
//
//    @PutMapping("updateByNormalUser/{userId}")
//    @PreAuthorize(USER_LEVEL)
//    public ResponseEntity<?> updateUser(@PathVariable Long userId,
//                                        @RequestBody UserUpdateDto request) {
//        try {
//            return ResponseEntity.ok(authenticationService.updateUser(userId, request));
//        } catch (RuntimeException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//    }

//
//    @PostMapping("registerAdmin")
////    @PreAuthorize(MAX_LEVEL)
//    public ResponseEntity<?> registerAdmin(@RequestBody UserDto request) {
//        try {
//            System.out.println("entro");
//            return ResponseEntity.ok(authenticationService.register(request, Role.ADMIN));
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }
//
//    @PostMapping("registerResident")
////    @PreAuthorize(CONDOMINIUM_LEVEL)
//    public ResponseEntity<?> registerResident(@RequestBody UserDto request) {
//        try {
//            return ResponseEntity.ok(authenticationService.register(request, Role.RESIDENT));
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }
//
//
//    @PostMapping("registerWorker")
////    @PreAuthorize(CONDOMINIUM_LEVEL)
//    public ResponseEntity<?> registerWorker(@RequestBody UserDto request) {
//        System.out.println("entro worker controller");
//        try {
//            return ResponseEntity.ok(authenticationService.register(request, Role.WORKER));
//        } catch (Exception e) {
//            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
//        }
//    }



    @PostMapping("login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authorizationHeader) {
        String token = "";
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
             token = authorizationHeader.substring(7); // Elimina el prefijo "Bearer "
        }
            return ResponseEntity.ok(authenticationService.validateToken(token));
    }

//    @GetMapping("/validateToken")
//    public ResponseEntity<?> validateToken(@RequestBody Token token) {
//        System.out.println("token Jwt Controller: " + token);
//        return ResponseEntity.ok(authenticationService.validateToken(token));
//    }
}
