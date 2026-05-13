package com.example.fitplannerclient.service;

import com.example.fitplannerclient.exception.NotAuthenticatedException;
import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;
import com.example.fitplannercommon.TokenBean;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthFacade {

    // Dependencies explicitly declared
    private final HttpService httpService;
    private final SessionManager sessionManager;

    // Inject dependencies via constructor (No more singletons!)
    public AuthFacade(HttpService httpService, SessionManager sessionManager) {
        this.httpService = httpService;
        this.sessionManager = sessionManager;
    }

    public CompletableFuture<Void> loginAsync(LoginBean loginBean) {
        // Use the injected httpService
        return httpService.postAsync("/auth/login", loginBean, TokenBean.class)
                .thenAccept(tokenBean -> {
                    // Successfully logged in: use the injected sessionManager to store tokens
                    sessionManager.setAccessToken(tokenBean.getAccessToken());
                    sessionManager.setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();
                    String msg = (cause != null) ? cause.getMessage() : throwable.getMessage();

                    throw new CompletionException(new NotAuthenticatedException(msg));
                });
    }

    public CompletableFuture<Void> registerAsync(RegisterBean registerBean) {
        // Use the injected httpService
        return httpService.postAsync("/auth/register", registerBean, TokenBean.class)
                .thenAccept(tokenBean -> {
                    // Successfully registered: save tokens to keep the user logged in
                    sessionManager.setAccessToken(tokenBean.getAccessToken());
                    sessionManager.setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(throwable -> {
                    Throwable cause = throwable.getCause();
                    String msg = (cause != null) ? cause.getMessage() : throwable.getMessage();

                    throw new CompletionException(new NotAuthenticatedException(msg));
                });
    }
}