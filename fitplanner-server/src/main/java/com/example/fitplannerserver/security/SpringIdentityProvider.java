package com.example.fitplannerserver.security;

import com.example.fitplannerserver.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringIdentityProvider implements IdentityProvider {

    public String getEmail(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Utente non autenticato");
        }
        return authentication.getName();
    }
}
