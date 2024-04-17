package com.jccv.tuprivadaapp.controller;

import com.jccv.tuprivadaapp.dto.auth.UserDto;
import com.jccv.tuprivadaapp.dto.auth.UserUpdateByAdminDto;
import com.jccv.tuprivadaapp.dto.auth.UserUpdateBySuperadminDto;
import com.jccv.tuprivadaapp.dto.auth.UserUpdateDto;
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
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }



    @PutMapping("updateBySuperadmin/{userId}")
    @PreAuthorize(MAX_LEVEL)
    public ResponseEntity<?> updateUserBySuperAdmin(@PathVariable Long userId,
                                        @RequestBody UserUpdateBySuperadminDto request) {
        try {
            return ResponseEntity.ok(authenticationService.updateUser(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("updateByAdmin/{userId}")
    @PreAuthorize(CONDOMINIUM_LEVEL)
    public ResponseEntity<?> updateUserByAdmin(@PathVariable Long userId,
                                        @RequestBody UserUpdateByAdminDto request) {
        try {
            return ResponseEntity.ok(authenticationService.updateUser(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("updateByNormalUser/{userId}")
    @PreAuthorize(USER_LEVEL)
    public ResponseEntity<?> updateUser(@PathVariable Long userId,
                                        @RequestBody UserUpdateDto request) {
        try {
            return ResponseEntity.ok(authenticationService.updateUser(userId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

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



    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request) {
        try {

            return ResponseEntity.ok(authenticationService.authenticate(request));

        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/validateToken")
    public ResponseEntity<?> validateToken(@RequestBody Token token) {
        System.out.println("token Jwt Controller: " + token);
        return ResponseEntity.ok(authenticationService.validateToken(token));
    }
}
