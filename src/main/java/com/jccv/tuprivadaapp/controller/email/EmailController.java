package com.jccv.tuprivadaapp.controller.email;

import com.jccv.tuprivadaapp.service.email.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static com.jccv.tuprivadaapp.utils.AuthorizationsLevel.CONDOMINIUM_LEVEL;

@RestController
@RequestMapping("emails")
@PreAuthorize(CONDOMINIUM_LEVEL)
public class EmailController {


    private final EmailService emailService;

    @Autowired
    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

//    @PostMapping("/reset-password")
//    public String registerUser(@RequestParam String email) {
//
//
//
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("codeForReset", codeForReset);
//
//        emailService.sendHtmlEmail(email, "Bienvenido a nuestra plataforma", "welcome", variables);
//
//        return "Usuario registrado";
//    }

//    @PostMapping("/test")
//    public String registerUser(@RequestParam String email, @RequestParam String name) {
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("nombre", name);
//        variables.put("activationLink", "https://tudominio.com/activate?token=abc123");
//
//        emailService.sendHtmlEmail(email, "Bienvenido a nuestra plataforma", "welcome", variables);
//
//        return "Usuario registrado";
//    }
}
