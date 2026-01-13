package com.example.fitplannerserver.security;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.TokenBean;
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
    public TokenBean login(@RequestBody LoginBean loginBean) {
        return authenticationController.login(loginBean);
    }
}
