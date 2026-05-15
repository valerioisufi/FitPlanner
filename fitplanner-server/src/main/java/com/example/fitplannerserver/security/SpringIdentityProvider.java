package com.example.fitplannerserver.security;

import com.example.fitplannerserver.exception.UnauthorizedException;
import com.example.fitplannerserver.model.Account;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SpringIdentityProvider implements IdentityProvider {

    @Override
    public String getUserId(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Security context is empty");
        }
        return authentication.getName();
    }

    @Override
    public Account.Role getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Security context is empty");
        }

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            String authName = authority.getAuthority();

            if (authName != null && authName.startsWith("ROLE_")) {
                return Account.Role.valueOf(authName.substring(5));
            }
        }

        // no matching role is found
        throw new IllegalStateException("Valid authentication token found but no valid role is mapped");
    }

    @Override
    public void checkUserRole(Account.Role role){
        if(getUserRole() != role){
            throw new UnauthorizedException("L'utente non dispone del ruolo adeguato");
        }
    }

}