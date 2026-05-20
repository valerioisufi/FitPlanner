package com.example.fitplannerserver.api;

import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.RegisterDTO;
import com.example.fitplannercommon.TokenDTO;
import com.example.fitplannerserver.controller.AuthenticationController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthenticationBoundary {

    private final AuthenticationController authenticationController;

    public AuthenticationBoundary(AuthenticationController authenticationController) {
        this.authenticationController = authenticationController;
    }

    @PostMapping("/login")
    public TokenDTO login(@RequestBody LoginDTO loginDTO) {
        return authenticationController.login(loginDTO);
    }

    @PostMapping("/register")
    public TokenDTO register(@RequestBody RegisterDTO registerDTO) {
        return authenticationController.register(registerDTO);
    }

    @PostMapping("/refresh")
    public TokenDTO refreshToken(@RequestBody TokenDTO tokenDTO) {
        return authenticationController.refreshToken(tokenDTO);
    }

}
