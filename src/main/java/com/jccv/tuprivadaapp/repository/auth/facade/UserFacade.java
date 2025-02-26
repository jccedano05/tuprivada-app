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

import java.util.List;

@Component
public class UserFacade {

    private static final String SUPERADMIN = "SUPERADMIN";
    private static final String ADMIN = "ADMIN";
    private static final String MESSAGE_USER_EXCEPTION = "Usuario no encontrado";
    private static final String MESSAGE_BAD_REQUEST_EXCEPTION = "No tienes los privilegios para esta operación";

    private final UserRepository userRepository;
    private final AdminFacade adminFacade;
    private final ResidentFacade residentFacade;
    private final WorkerFacade workerFacade;



    @Autowired
    public UserFacade(UserRepository userRepository, AdminFacade adminFacade, ResidentFacade residentFacade, WorkerFacade workerFacade) {
        this.userRepository = userRepository;
        this.adminFacade = adminFacade;
        this.residentFacade = residentFacade;
        this.workerFacade = workerFacade;
    }

    public User findById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException(MESSAGE_USER_EXCEPTION));
    }

    public User findByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Email no encontrado"));
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("Usuario y/o Contraseña incorrecta"));
    }

    public List<User> findUsersResidentByCondominiumId(Long condominiumId) {
        return userRepository.findResidentsByCondominiumId(condominiumId);
    }
    public List<User> findUsersByUserIds(List<Long> userIds) {
        return userRepository.findAllByUserIds(userIds);
    }

    //Buscar por nombre de usuario
    public boolean isUsernameExist(String username){
        return userRepository.findByUsername(username).isPresent();
    }
    public User save(User user) {
        return userRepository.save(user);
    }

    public User save(User user, UserDto request) {
            if (!checkAuthorityContext().equals(SUPERADMIN) ) {
            throw new BadRequestException(MESSAGE_USER_EXCEPTION);
        }
        return userRepository.save(user);
    }

    public Admin saveAdmin(Admin user) {
        if (!checkAuthorityContext().equals(SUPERADMIN)) {
            throw new BadRequestException(MESSAGE_USER_EXCEPTION);
        }
        return adminFacade.save(user);
    }

    public Resident saveResident(Resident user) {
        if (!checkAuthorityContext().equals(ADMIN) && !checkAuthorityContext().equals(SUPERADMIN)) {
            throw new BadRequestException(MESSAGE_BAD_REQUEST_EXCEPTION);
        }
        return residentFacade.save(user);
    }

    public Worker saveWorker(Worker user) {
        if (!checkAuthorityContext().equals(ADMIN) && !checkAuthorityContext().equals(SUPERADMIN)) {
            throw new BadRequestException(MESSAGE_BAD_REQUEST_EXCEPTION);
        }
        return workerFacade.save(user);
    }

    private String checkAuthorityContext() {
        String authority = "";
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            authority = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream().toList().get(0).toString();
        }
        return authority;
    }

    private List<User> getAllUsersByAuthority(User user){
        //
        return null;
    }
}
