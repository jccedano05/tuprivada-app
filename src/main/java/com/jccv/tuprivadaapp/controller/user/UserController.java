package com.jccv.tuprivadaapp.controller.user;


import com.jccv.tuprivadaapp.dto.user.UserDataToShowDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.service.user.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users-administration")
public class UserController {

    @Autowired
    private UserService userService;

    @PutMapping("/users/{userId}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody UserDataToShowDto userDataToShowDto) {


        System.out.println("Backend user");
        System.out.println(userDataToShowDto.toString());
        try {
            User updatedUser = userService.updateUser(userId, userDataToShowDto);
            return new ResponseEntity<>(updatedUser, HttpStatus.OK);
        } catch (ResourceNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (BadRequestException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("condominiums/{condominiumId}")
    public ResponseEntity<List<UserDataToShowDto>> getAllUsers(@PathVariable Long condominiumId) {
        try {
            List<UserDataToShowDto> users = userService.getAllUsersByCondominiumId(condominiumId);
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
