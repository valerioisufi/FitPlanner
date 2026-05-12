package com.example.fitplannerclient;

import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.service.AuthFacade;

public class AppControllerFactory {

    private final AuthFacade authFacade;

    public AppControllerFactory(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    public AuthManager createAuthManager() {
        return new AuthManager(authFacade);
    }

}
