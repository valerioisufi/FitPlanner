package com.example.fitplannerserver.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringIdentityProvider implements IdentityProvider {

    public String getEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Utente non autenticato"); // TODO da modificare ECCEZIONE
        }
        return authentication.getName();
    }
}
