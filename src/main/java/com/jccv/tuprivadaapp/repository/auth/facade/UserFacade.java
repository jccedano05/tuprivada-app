package com.jccv.tuprivadaapp.repository.auth.facade;

import com.jccv.tuprivadaapp.dto.auth.UserDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.worker.Worker;
import com.jccv.tuprivadaapp.repository.admin.facade.AdminFacade;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.repository.resident.facade.ResidentFacade;
import com.jccv.tuprivadaapp.repository.worker.facade.WorkerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class UserFacade {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AdminFacade adminFacade;

    @Autowired
    private ResidentFacade residentFacade;
    @Autowired
    private WorkerFacade workerFacade;

    @Value("${admin.specialSecretKey}")
    private String SECRET_KEY;

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    public User save(User user) {
        return userRepository.save(user);
    }

    public User save(User user, UserDto request) {

        String requestSecretKey = (request.getSecretKey() != null) ? request.getSecretKey() : "";
        if(checkAuthorityContext() != "SUPERADMIN" && !requestSecretKey.equals(SECRET_KEY) ){
            throw new BadRequestException("No tienes los privilegios para esta operacion");
        }
        return userRepository.save(user);
    }

    public Admin saveAdmin(Admin user) {
        if(checkAuthorityContext() != "SUPERADMIN"){
            throw new BadRequestException("No tienes los privilegios para esta operacion");
        }
        return adminFacade.save(user);
    }

    public Resident saveResident(Resident user) {
        if(checkAuthorityContext() != "ADMIN" && checkAuthorityContext() != "SUPERADMIN"){
            throw new BadRequestException("No tienes los privilegios para esta operacion");
        }
        return residentFacade.save(user);
    }

    public Worker saveWorker(Worker user) {
        if(checkAuthorityContext() != "ADMIN" && checkAuthorityContext() != "SUPERADMIN"){
            throw new BadRequestException("No tienes los privilegios para esta operacion");
        }
        return workerFacade.save(user);
    }

    private String checkAuthorityContext(){
        String authority = "";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList().get(0).toString();
        }
        System.out.println("authority");
        System.out.println(authority);
        return authority;
    }
}
