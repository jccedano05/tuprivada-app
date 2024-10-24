package com.jccv.tuprivadaapp.utils;

import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UserFacade userFacade;
    private final PasswordEncoder passwordEncoder;


    @Autowired
    public DataInitializer(UserFacade userFacade, PasswordEncoder passwordEncoder) {
        this.userFacade = userFacade;
        this.passwordEncoder = passwordEncoder;
    }

    @Bean
    public CommandLineRunner createSuperAdmin() {
        return args -> {
            // Verifica si el usuario superadmin ya existe
            //verificar si es nulo (por si no quiero agregar esa variable, si tiene el key string para que no cualquiera pueda agregar
            // si no conoce eso y que sea en variables de entorno
            if (!userFacade.isUsernameExist("superadmin")) {
                // Si no existe, lo creamos
                User superAdmin = new User();
                superAdmin.setUsername("superadmin");
                superAdmin.setPassword(passwordEncoder.encode("123321")); // usa una contraseña segura o obtenla de una variable de entorno
                superAdmin.setRole(Role.SUPERADMIN);
                superAdmin.setLastName("test lastname");
                superAdmin.setFirstName("test firstname");

                // Guardar el usuario
                userFacade.save(superAdmin);
                System.out.println("Superadmin creado");
            }
        };
    }
}
