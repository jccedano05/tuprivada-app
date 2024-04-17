package com.jccv.tuprivadaapp.service;

import com.jccv.tuprivadaapp.dto.auth.UserDto;
import com.jccv.tuprivadaapp.dto.auth.UserUpdateByAdminDto;
import com.jccv.tuprivadaapp.dto.auth.UserUpdateBySuperadminDto;
import com.jccv.tuprivadaapp.dto.auth.UserUpdateDto;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.jwt.model.AuthenticationResponse;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.Token;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.model.worker.Worker;
import com.jccv.tuprivadaapp.repository.TokenRepository;
import com.jccv.tuprivadaapp.repository.admin.facade.AdminFacade;
import com.jccv.tuprivadaapp.repository.auth.UserRepository;
import com.jccv.tuprivadaapp.repository.admin.AdminRepository;
import com.jccv.tuprivadaapp.repository.resident.ResidentRepository;
import com.jccv.tuprivadaapp.repository.resident.facade.ResidentFacade;
import com.jccv.tuprivadaapp.repository.worker.WorkerRepository;
import com.jccv.tuprivadaapp.repository.worker.facade.WorkerFacade;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private UserDto userDto;

    @Autowired
    private UserFacade userFacade;

    @Autowired
    private CondominiumService condominiumService;
    @Autowired
    private TokenRepository tokenRepository;
    @Autowired
    private AuthenticationManager authenticationManager;




    public AuthenticationResponse register(UserDto request) {
        if (request.getUsername() == null) {
            throw new ResourceNotFoundException("El campo usuario no debe quedar vacio");
        }
        Optional<User> userFounded = repository.findByUsername(request.getUsername());

        if (userFounded.isPresent()) {
            throw new BadRequestException("El usuario ya existe");
        }
        if(request.getRole() != Role.SUPERADMIN && request.getCondominiumId() == null){
            throw new BadRequestException("Para crear este tipo de usuario debes asignarle un condominiumId");
        }
        User user;
        switch (request.getRole()) {
            case SUPERADMIN: {
                user = buildUser(request, new User());
                user = userFacade.save(user, request);
                break;
            }
            case ADMIN: {
                Admin admin = buildUser(request, new Admin());
                user = userFacade.saveAdmin(admin);
                break;
            }
            case RESIDENT: {
                Resident resident = buildUser(request, new Resident());
                user = userFacade.saveResident(resident);
                break;
            }
            case WORKER: {
                Worker worker = buildUser(request, new Worker());
                user = userFacade.saveWorker(worker);
                break;
            }
            default: {
                throw new BadRequestException("Rol no soportado, elegir rol correcto");
            }
        }

        String token = jwtService.generateToken(user);
        saveToken(user, token);

        return new AuthenticationResponse(token);
    }


    private <T extends User> T buildUser(UserDto request, T user) {
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        if (request.getCondominiumId() != null ) {
            Condominium condominium = condominiumService.findById(request.getCondominiumId());
            user.setCondominium(condominium);
        }
        System.out.println("user");
        System.out.println(user.toString());
        return user;
    }


    public AuthenticationResponse authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = repository.findByUsername(request.getUsername()).orElseThrow(() -> new ResourceNotFoundException("Usuario y/o Contraseña incorrecta"));
        String token = jwtService.generateToken(user);
        revokeAllTokensByUser(user);
        saveToken(user, token);
        return new AuthenticationResponse(token);
    }


    @Transactional
    public UserDto updateUser(Long userId, UserUpdateDto request) {

        User user = userFacade.findById(userId);

        verifyAuthorityToUpdate(user);

        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        return userDto.convertUserToUserDto(userFacade.save(user));
    }



    @Transactional
    public UserDto updateUser(Long userId, UserUpdateByAdminDto request) {

        User user = userFacade.findById(userId);


        verifyAuthorityToUpdate(user);

        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        if(request.getRole() != null){
            if(request.getRole() == Role.SUPERADMIN){
                throw new BadRequestException("No puedes proporcionar un Rol mayor a tu autoridad");
            }
            user.setRole(request.getRole());
        }

        return userDto.convertUserToUserDto(userFacade.save(user));
    }

    @Transactional
    public UserDto updateUser(Long userId, UserUpdateBySuperadminDto request) {

        User user = userFacade.findById(userId);

        verifyAuthorityToUpdate(user);

        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        if(request.getCondominiumId() != null){
            Condominium condominium = condominiumService.findById(request.getCondominiumId());
            user.setCondominium(condominium);
        }
        if(request.getRole() != null){
            user.setRole(request.getRole());
        }
        return userDto.convertUserToUserDto(userFacade.save(user));
    }


    private void verifyAuthorityToUpdate(User userToUpdate){
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            User user= repository.findByUsername( SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new BadRequestException("No se encontro el user de la cuenta"));
            //If your condominium is not the same than the user to update, you must be a super admin, also condominium must not be null
            if(user.getCondominium() !=userToUpdate.getCondominium() && user.getCondominium() == null) {
                if(!user.getRole().equals(Role.SUPERADMIN)) {
                    throw new BadRequestException("Credenciales no validas para hacer el update");
                }
            }
            // If you are not a SuperAdmin or Admin, only could modify you
            if(user.getUsername() !=userToUpdate.getUsername() && !user.getRole().equals(Role.ADMIN)) {
                if(!user.getRole().equals(Role.SUPERADMIN)){
                    throw new BadRequestException("Credenciales no validas para hacer el update");
                }
            }
            //If you no are SUPER ADMIN, Can't modify a SuperAdmin user
            if(Role.SUPERADMIN ==userToUpdate.getRole() && user.getRole() != Role.SUPERADMIN) {
                throw new BadRequestException("Credenciales no validas para hacer el update");
            }
        }
    }


    public boolean validateToken(Token token) {

        return jwtService.isValid(token.getToken());

    }

    private void revokeAllTokensByUser(User user) {
        List<Token> validTokenListByUser = tokenRepository.findAllTokensByUser(user.getId());
        if (!validTokenListByUser.isEmpty()) {
            validTokenListByUser.forEach(t -> t.setLoggedOut(true));
        }
        tokenRepository.saveAll(validTokenListByUser);
    }

    private void saveToken(User user, String token) {
        Token tokenToSave = Token.builder()
                .token(token)
                .loggedOut(false)
                .user(user)
                .build();
        tokenRepository.save(tokenToSave);

    }



}
