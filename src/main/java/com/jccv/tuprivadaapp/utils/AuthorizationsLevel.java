package com.jccv.tuprivadaapp.utils;

import com.jccv.tuprivadaapp.model.Role;
import org.springframework.stereotype.Component;

public class AuthorizationsLevel {

    public static final String MAX_LEVEL = "hasAuthority('SUPERADMIN')";
    public static final String CONDOMINIUM_LEVEL = "hasAuthority('SUPERADMIN') or hasAuthority('ADMIN')";
    public static final String USER_LEVEL = "hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('WORKER') or hasAuthority('RESIDENT')";
    public static final String WORKER_LEVEL = "hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('WORKER')";
    public static final String RESIDENT_LEVEL = "hasAuthority('SUPERADMIN') or hasAuthority('ADMIN') or hasAuthority('RESIDENT')";


}