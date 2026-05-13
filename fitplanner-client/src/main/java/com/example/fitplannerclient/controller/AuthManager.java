package com.example.fitplannerclient.controller;

import com.example.fitplannerclient.service.AuthFacade;
import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;

import java.util.concurrent.CompletableFuture;

public class AuthManager {

    // Best practice: make injected dependencies final
    private final AuthFacade authFacade;

    public AuthManager(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    public CompletableFuture<Void> loginAsync(LoginBean loginBean) {
        // Here you could add client-side validation in the future.
        // For example: Check if the username is not empty before making the network call.
        if (loginBean.getUsername() == null || loginBean.getUsername().isBlank()) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Username cannot be empty"));
        }

        // Delegate the actual network request to the Facade
        return authFacade.loginAsync(loginBean);
    }

    public CompletableFuture<Void> registerAsync(RegisterBean registerBean) {
        // Future business logic: Check if the password meets security requirements
        if (registerBean.getPassword().length() < 8) {
            return CompletableFuture.failedFuture(new IllegalArgumentException("Password must be at least 8 characters long"));
        }

        // Delegate to the Facade
        return authFacade.registerAsync(registerBean);
    }
}