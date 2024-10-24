package com.jccv.tuprivadaapp.controller;


import com.jccv.tuprivadaapp.model.condominium.Condominium;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.MAX_LEVEL;

@RestController
@RequestMapping("users")
@PreAuthorize(MAX_LEVEL)
public class UsersController {

    @GetMapping
    public ResponseEntity<?> getAllUsersByAuthority() {
        try {
//            List<Condominium> condominiums = condominiumService.findAll();
//            return new ResponseEntity<>(condominiums, HttpStatus.OK);
            throw new Exception("Not found");
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

}
