package com.jccv.tuprivadaapp.jwt.model;


public class AuthenticationResponse {

    private final String token;
    public String getToken() {
        return token;
    }

    public AuthenticationResponse(String token) {
        this.token = token;
    }

}
