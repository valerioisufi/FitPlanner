package com.example.fitplannerclient.service.api;

import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.RegisterDTO;

import java.util.concurrent.CompletableFuture;

public interface AuthApi {
    CompletableFuture<Void> loginAsync(LoginDTO loginDTO);
    CompletableFuture<Void> registerAsync(RegisterDTO registerDTO);
}
