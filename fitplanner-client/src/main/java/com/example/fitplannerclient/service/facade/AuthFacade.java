package com.example.fitplannerclient.service.facade;

import com.example.fitplannerclient.exception.NotAuthenticatedException;
import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.RegisterDTO;
import com.example.fitplannercommon.TokenDTO;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthFacade {

    private final HttpService httpService;
    private final SessionManager sessionManager;

    public AuthFacade(HttpService httpService, SessionManager sessionManager) {
        this.httpService = httpService;
        this.sessionManager = sessionManager;
    }

    public CompletableFuture<Void> loginAsync(LoginDTO loginDTO) {

        return httpService.postAsync("/auth/login", loginDTO, TokenDTO.class)
                .thenAccept(tokenBean -> {
                    // Successfully logged in
                    sessionManager.setAccessToken(tokenBean.getAccessToken());
                    sessionManager.setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(throwable -> {
                    String msg = HttpService.extractErrorMessage(throwable);

                    throw new CompletionException(new NotAuthenticatedException(msg));
                });
    }

    public CompletableFuture<Void> registerAsync(RegisterDTO registerDTO) {

        return httpService.postAsync("/auth/register", registerDTO, TokenDTO.class)
                .thenAccept(tokenBean -> {
                    // Successfully registered
                    sessionManager.setAccessToken(tokenBean.getAccessToken());
                    sessionManager.setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(throwable -> {
                    String msg = HttpService.extractErrorMessage(throwable);

                    throw new CompletionException(new NotAuthenticatedException(msg));
                });
    }
}