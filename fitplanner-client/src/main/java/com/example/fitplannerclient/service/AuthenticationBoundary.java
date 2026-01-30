package com.example.fitplannerclient.service;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;
import com.example.fitplannercommon.TokenBean;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class AuthenticationBoundary {

    public CompletableFuture<Void> loginAsync(LoginBean loginBean) {
        return HttpService.getInstance().postAsync("/auth/login", loginBean, TokenBean.class)
                .thenAccept(tokenBean -> {
                    SessionManager sessionManager = SessionManager.getInstance();
                    sessionManager.setAccessToken(tokenBean.getAccessToken());
                    sessionManager.setRefreshToken(tokenBean.getRefreshToken());
                });
    }

    public CompletableFuture<Void> registerAsync(RegisterBean registerBean) {
        return HttpService.getInstance().postAsync("/auth/register", registerBean, TokenBean.class)
                .thenAccept(tokenBean -> {
                    SessionManager sessionManager = SessionManager.getInstance();
                    sessionManager.setAccessToken(tokenBean.getAccessToken());
                    sessionManager.setRefreshToken(tokenBean.getRefreshToken());
                });
    }


}
