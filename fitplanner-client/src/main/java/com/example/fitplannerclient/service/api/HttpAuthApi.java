package com.example.fitplannerclient.service.api;

import com.example.fitplannerclient.exception.NotAuthenticatedException;
import com.example.fitplannerclient.service.HttpService;
import com.example.fitplannerclient.service.TokenStore;
import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.RegisterDTO;
import com.example.fitplannercommon.TokenDTO;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class HttpAuthApi implements AuthApi {

    private final HttpService httpService;
    private final TokenStore tokenStore;

    public HttpAuthApi(HttpService httpService, TokenStore tokenStore) {
        this.httpService = httpService;
        this.tokenStore = tokenStore;
    }

    @Override
    public CompletableFuture<Void> loginAsync(LoginDTO loginDTO) {

        return httpService.postAsync("/auth/login", loginDTO, TokenDTO.class)
                .thenAccept(tokenBean -> {
                    // Successfully logged in
                    tokenStore.setAccessToken(tokenBean.getAccessToken());
                    tokenStore.setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(throwable -> {
                    String msg = HttpService.extractErrorMessage(throwable);

                    throw new CompletionException(new NotAuthenticatedException(msg));
                });
    }

    @Override
    public CompletableFuture<Void> registerAsync(RegisterDTO registerDTO) {

        return httpService.postAsync("/auth/register", registerDTO, TokenDTO.class)
                .thenAccept(tokenBean -> {
                    // Successfully registered
                    tokenStore.setAccessToken(tokenBean.getAccessToken());
                    tokenStore.setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(throwable -> {
                    String msg = HttpService.extractErrorMessage(throwable);

                    throw new CompletionException(new NotAuthenticatedException(msg));
                });
    }
}