package com.example.fitplannerclient.controller;

import com.example.fitplannerclient.bean.auth.LoginBean;
import com.example.fitplannerclient.bean.auth.RegisterBean;
import com.example.fitplannerclient.service.AuthFacade;
import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannercommon.RegisterDTO;

import java.util.concurrent.CompletableFuture;

public class AuthManager {

    private final AuthFacade authFacade;

    public AuthManager(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    public CompletableFuture<Void> loginAsync(LoginBean loginBean) {
        if (loginBean.getEmail() == null || loginBean.getEmail().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Email cannot be empty"));
        }

        // Map Bean -> DTO
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(loginBean.getEmail());
        loginDTO.setPassword(loginBean.getPassword());

        return authFacade.loginAsync(loginDTO);
    }

    public CompletableFuture<Void> registerAsync(RegisterBean registerBean) {
        if (registerBean.getPassword() == null || registerBean.getPassword().length() < 8) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Password must be at least 8 characters long"));
        }

        // Map Bean -> DTO
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(registerBean.getEmail());
        registerDTO.setPassword(registerBean.getPassword());

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFirstName(registerBean.getFirstName());
        profileDTO.setLastName(registerBean.getLastName());
        profileDTO.setPhoneNumber(registerBean.getPhoneNumber());
        profileDTO.setContactEmail(registerBean.getContactEmail());
        registerDTO.setProfile(profileDTO);

        // Assuming RegisterDTO has a String or Enum field for ProfileType
        if (registerBean.getProfileType() != null) {
            // registerDTO.setProfileType(registerBean.getProfileType().name());
        }

        return authFacade.registerAsync(registerDTO);
    }
}