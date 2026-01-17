package com.example.fitplannerclient.service;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.TokenBean;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthenticationBoundary {

    public CompletableFuture<Void> loginAsync(LoginBean loginBean) {
        return HttpService.getInstance().postAsync("/auth/login", loginBean, TokenBean.class)
                .thenAccept(tokenBean -> {
                    SessionManager.getInstance().setAccessToken(tokenBean.getAccessToken());
                    SessionManager.getInstance().setRefreshToken(tokenBean.getRefreshToken());
                })
                .exceptionally(ex -> {
                    Throwable cause_ = ex instanceof CompletionException ? ex.getCause() : ex;
                    Throwable cause = ex.getCause();
//                    if (cause instanceof HttpErrorException && ((HttpErrorException) cause).getStatusCode() == 401) {
//                        throw new CompletionException(new InvalidCredentialsException("User o pass errati"));
//                    }
                    throw new CompletionException(cause);
                });
    }

    public CompletableFuture<Void> refreshAsync() {
        TokenBean tokenBean = new TokenBean();
        // TODO Lanciare un'eccezione se refresh token == null
        tokenBean.setRefreshToken(SessionManager.getInstance().getRefreshToken());

        return HttpService.getInstance().postAsync("/auth/refresh", tokenBean, TokenBean.class)
                .thenAccept(newTokenBean -> {
                    SessionManager.getInstance().setAccessToken(newTokenBean.getAccessToken());
                });
    }

}
