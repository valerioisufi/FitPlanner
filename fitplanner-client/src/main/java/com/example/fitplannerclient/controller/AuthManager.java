package com.example.fitplannerclient.controller;

import com.example.fitplannerclient.bean.auth.LoginBean;
import com.example.fitplannerclient.bean.auth.RegisterBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.exception.InvalidInputException;
import com.example.fitplannerclient.service.api.AuthApi;
import com.example.fitplannerclient.util.ValidationUtils;
import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannercommon.RegisterDTO;

import java.util.concurrent.CompletableFuture;

public class AuthManager {

    private final AuthApi authApi;

    public AuthManager(AuthApi authApi) {
        this.authApi = authApi;
    }

    public CompletableFuture<Void> loginAsync(LoginBean loginBean) {
        // Validate login inputs
        String emailError = ValidationUtils.validateEmail(loginBean.getEmail());
        String passError = ValidationUtils.validateRequired(loginBean.getPassword(), "Password", 32);

        if (emailError != null || passError != null) {
            return CompletableFuture.failedFuture(new InvalidInputException("Credenziali non valide"));
        }

        // Map Bean -> DTO
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(loginBean.getEmail());
        loginDTO.setPassword(loginBean.getPassword());

        return authApi.loginAsync(loginDTO);
    }

    public CompletableFuture<Void> registerAsync(RegisterBean registerBean) {
        // Validate auth inputs
        String emailError = ValidationUtils.validateEmail(registerBean.getEmail());
        String passError = ValidationUtils.validatePassword(registerBean.getPassword());

        // Validate profile inputs
        ProfileBean profileBean = registerBean.getProfile();
        String nameError = ValidationUtils.validateName(profileBean.getFirstName(), "Nome", 50);
        String lastNameError = ValidationUtils.validateName(profileBean.getLastName(), "Cognome", 50);
        String contactEmailError = ValidationUtils.validateEmail(profileBean.getContactEmail());
        String phoneError = ValidationUtils.validatePhone(profileBean.getPhoneNumber());

        if (emailError != null || passError != null || nameError != null ||
                lastNameError != null || contactEmailError != null || phoneError != null) {
            return CompletableFuture.failedFuture(new InvalidInputException("Dati di registrazione non validi"));
        }

        // Map Bean -> DTO
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(registerBean.getEmail());
        registerDTO.setPassword(registerBean.getPassword());

        ProfileDTO profileDTO = new ProfileDTO();
        profileDTO.setFirstName(profileBean.getFirstName());
        profileDTO.setLastName(profileBean.getLastName());
        profileDTO.setPhoneNumber(profileBean.getPhoneNumber());
        profileDTO.setContactEmail(profileBean.getContactEmail());

        if (profileBean.getProfileType() != null) {
            profileDTO.setProfileType(ProfileDTO.ProfileType.valueOf(profileBean.getProfileType().name()));
        }

        registerDTO.setProfile(profileDTO);

        return authApi.registerAsync(registerDTO);
    }
}