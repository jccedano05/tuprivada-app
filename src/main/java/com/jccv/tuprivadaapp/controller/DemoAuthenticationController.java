package com.jccv.tuprivadaapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoAuthenticationController {

    @GetMapping("/demo")
    public ResponseEntity<String> demo(){
        return ResponseEntity.ok("Endpoint Url Authenticated correct");
    }
    @GetMapping("/admin_only")
    public ResponseEntity<String> adminDemo(){
        return ResponseEntity.ok("Endpoint Url Authenticated correct ADMIN");
    }
}
