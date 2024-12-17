package com.jccv.tuprivadaapp.service;

import com.jccv.tuprivadaapp.dto.auth.*;
import com.jccv.tuprivadaapp.dto.auth.mapper.UserMapper;
import com.jccv.tuprivadaapp.exception.BadRequestException;
import com.jccv.tuprivadaapp.exception.ResourceNotFoundException;
import com.jccv.tuprivadaapp.model.Role;
import com.jccv.tuprivadaapp.model.Token;
import com.jccv.tuprivadaapp.model.User;
import com.jccv.tuprivadaapp.model.admin.Admin;
import com.jccv.tuprivadaapp.model.condominium.Condominium;
import com.jccv.tuprivadaapp.model.resident.Resident;
import com.jccv.tuprivadaapp.repository.TokenRepository;
import com.jccv.tuprivadaapp.service.admin.AdminService;
import com.jccv.tuprivadaapp.service.condominium.CondominiumService;
import com.jccv.tuprivadaapp.repository.auth.facade.UserFacade;
import com.jccv.tuprivadaapp.service.resident.ResidentService;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuthenticationService {


     private final PasswordEncoder passwordEncoder;

     private final JwtService jwtService;

     private final UserDto userDto;


     private final UserFacade userFacade;

     private final CondominiumService condominiumService;

     private final TokenRepository tokenRepository;

    private final AuthenticationManager authenticationManager;
    private final AuthorizationService authorizationService;

    private final ResidentService residentService;
    private final AdminService adminService;

    private final UserMapper userMapper;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder, JwtService jwtService, UserDto userDto, UserFacade userFacade, CondominiumService condominiumService, TokenRepository tokenRepository, AuthenticationManager authenticationManager, AuthorizationService authorizationService, ResidentService residentService, AdminService adminService, UserMapper userMapper) {

        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDto = userDto;
        this.userFacade = userFacade;
        this.condominiumService = condominiumService;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
        this.authorizationService = authorizationService;
        this.residentService = residentService;
        this.adminService = adminService;
        this.userMapper = userMapper;
    }

    public AuthenticatedUserDto register(UserDto request) {
        //validar que solo SuperAdmins, Admins puedan crear usuarios
//        authorizationService.verifyAuthorityToUpdate(userDto.convertUserDtoToUser(request));
        validateRequest(request);
        User user = createUserByRole(request);
        String token = jwtService.generateToken(user);
        saveToken(user, token);
        return authenticatedUserResponse(user, token);
    }

    private void validateRequest(UserDto request) {
        if (request.getUsername() == null) {
            throw new ResourceNotFoundException("El campo usuario no debe quedar vacío");
        }

        if (userFacade.isUsernameExist(request.getUsername())) {
            throw new BadRequestException("El usuario ya existe");
        }

        if (request.getRole() != Role.SUPERADMIN && request.getCondominiumId() == null) {
            throw new BadRequestException("Para crear este tipo de usuario debes asignarle un condominiumId");
        }
    }

    @Transactional
    private User createUserByRole(UserDto request) {
        try{
        User user = userFacade.save(buildUser(request, new User()));
            switch (request.getRole()) {
                case RESIDENT: {
                    residentService.saveResident(Resident.builder()
                            .user(user)
                                    .isActiveResident(true)
                                    .condominium(condominiumService.findById(request.getCondominiumId()))
                            .build());
                    return user;
                }
                case ADMIN: {
                    adminService.saveAdmin(Admin.builder()
                            .user(user)
                            .isActive(true)
                            .condominium(condominiumService.findById(request.getCondominiumId()))
                            .build());
                    return user;
                }
                default:
                {
                    throw new BadRequestException("Rol no soportado, elegir rol correcto");
                }
            }
        }catch (Exception e) {
                // Si ocurre un error, lanzamos una excepción para que la transacción sea revertida
            System.out.println(e);
            throw new BadRequestException("Rol no soportado, elegir rol correcto");
            }
    }


    // AuthenticationService.java

    @Transactional
    public UserDto updatePasswordUser(Long userId, String newPassword) {
        User user = userFacade.findById(userId);  // Busca al usuario en la base de datos

        if (user == null) {
            throw new ResourceNotFoundException("Usuario no encontrado");
        }
        System.out.println("New password: " + newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        System.out.println("password enconde: " + user.getPassword());


        // Guardar los cambios en la base de datos
        user = userFacade.save(user);

        return userMapper.convertUserToUserDto(user);
    }


//    public AuthenticatedUserDto register(UserDto request) {
//        if (request.getUsername() == null) {
//            throw new ResourceNotFoundException("El campo usuario no debe quedar vacio");
//        }
//        Optional<User> userFounded = repository.findByUsername(request.getUsername());
//
//        if (userFounded.isPresent()) {
//            throw new BadRequestException("El usuario ya existe");
//        }
//        if(request.getRole() != Role.SUPERADMIN && request.getCondominiumId() == null){
//            throw new BadRequestException("Para crear este tipo de usuario debes asignarle un condominiumId");
//        }
//        User user;
//        switch (request.getRole()) {
//            case SUPERADMIN: {
//                user = buildUser(request, new User());
//                user = userFacade.save(user, request);
//                break;
//            }
//            case ADMIN: {
//                Admin admin = buildUser(request, new Admin());
//                user = userFacade.saveAdmin(admin);
//                break;
//            }
//            case RESIDENT: {
//                Resident resident = buildUser(request, new Resident());
//                user = userFacade.saveResident(resident);
//                break;
//            }
//            case WORKER: {
//                Worker worker = buildUser(request, new Worker());
//                user = userFacade.saveWorker(worker);
//                break;
//            }
//            default: {
//                throw new BadRequestException("Rol no soportado, elegir rol correcto");
//            }
//        }
//
//        String token = jwtService.generateToken(user);
//        saveToken(user, token);
//
//        return authenticatedUserResponse(user, token);
////        return new AuthenticationResponse(token);
//    }


    private <T extends User> T buildUser(UserDto request, T user) {
        user.setId(request.getId());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        if (request.getCondominiumId() != null ) {
            Condominium condominium = condominiumService.findById(request.getCondominiumId());
            user.setCondominium(condominium);
        }
        return user;
    }


    public AuthenticatedUserDto authenticate(User request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        User user = userFacade.findByUsername(request.getUsername());
        String token = jwtService.generateToken(user);
//        revokeAllTokensByUser(user);
        saveToken(user, token);
        return authenticatedUserResponse(user, token);
//        return new AuthenticationResponse(token);
    }

    private AuthenticatedUserDto authenticatedUserResponse(User user, String token){
        return AuthenticatedUserDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .token(token)
                .role(user.getRole())
                .condominium(user.getCondominium())
                .username(user.getUsername())
                .build();
    }


    @Transactional
    public UserDto updateUser(Long userId, UserUpdateDto request) {

        User user = userFacade.findById(userId);

        authorizationService.verifyAuthorityToUpdate(user);

        if(request.getFirstName() != null){
            user.setFirstName(request.getFirstName());
        }
        if(request.getLastName() != null){
            user.setLastName(request.getLastName());
        }
        return userMapper.convertUserToUserDto(userFacade.save(user));
    }



    @Transactional
    public UserDto updateUser(Long userId, UserUpdateByAdminDto request) {

        User user = userFacade.findById(userId);


        authorizationService.verifyAuthorityToUpdate(user);

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

        return userMapper.convertUserToUserDto(userFacade.save(user));
    }

    @Transactional
    public UserDto updateUser(Long userId, UserUpdateBySuperadminDto request) {

        User user = userFacade.findById(userId);

        authorizationService.verifyAuthorityToUpdate(user);

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
        return userMapper.convertUserToUserDto(userFacade.save(user));
    }


//    private void verifyAuthorityToUpdate(User userToUpdate){
//        if (SecurityContextHolder.getContext().getAuthentication() != null) {
//            User user= repository.findByUsername( SecurityContextHolder.getContext().getAuthentication().getName()).orElseThrow(() -> new BadRequestException("No se encontro el user de la cuenta"));
//            //If your condominium is not the same than the user to update, you must be a super admin, also condominium must not be null
//            if(user.getCondominium() !=userToUpdate.getCondominium() && user.getCondominium() == null) {
//                if(!user.getRole().equals(Role.SUPERADMIN)) {
//                    throw new BadRequestException("Credenciales no validas para hacer el update");
//                }
//            }
//            // If you are not a SuperAdmin or Admin, only could modify you
//            if(user.getUsername() !=userToUpdate.getUsername() && !user.getRole().equals(Role.ADMIN)) {
//                if(!user.getRole().equals(Role.SUPERADMIN)){
//                    throw new BadRequestException("Credenciales no validas para hacer el update");
//                }
//            }
//            //If you no are SUPER ADMIN, Can't modify a SuperAdmin user
//            if(Role.SUPERADMIN ==userToUpdate.getRole() && user.getRole() != Role.SUPERADMIN) {
//                throw new BadRequestException("Credenciales no validas para hacer el update");
//            }
//        }
//    }


    public AuthenticatedUserDto validateToken(String token) {
        if(jwtService.isValid(token)){
            String username = jwtService.extractUsername(token);
           User user =  userFacade.findByUsername(username);
           return authenticatedUserResponse(user, token);
        }
        throw new JwtException("Token no valid");
//        return jwtService.isValid(token.getToken());

    }

//    private void revokeAllTokensByUser(User user) {
//        List<Token> validTokenListByUser = tokenRepository.findAllTokensByUser(user.getId());
//        if (!validTokenListByUser.isEmpty()) {
//            validTokenListByUser.forEach(t -> t.setLoggedOut(true));
//        }
//        tokenRepository.saveAll(validTokenListByUser);
//    }

    private void saveToken(User user, String token) {
        Token tokenToSave = Token.builder()
                .token(token)
                .loggedOut(false)
                .user(user)
                .build();
        tokenRepository.save(tokenToSave);

    }



}
