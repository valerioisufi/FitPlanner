package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;
import com.example.fitplannercommon.TokenBean;
import com.example.fitplannerserver.security.JwtUtil;

public class AuthenticationController {
    private final JwtUtil jwtUtil;

    public AuthenticationController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public TokenBean login(LoginBean loginBean) {

        // Controllo hardcoded (solo esempio)
        if ("admin".equals(loginBean.getUsername()) &&
                "password".equals(loginBean.getPassword())) {

            TokenBean tokenBean = new TokenBean();
            tokenBean.setAccessToken(jwtUtil.generateToken(loginBean.getUsername()));
            return tokenBean;
        }

        return null;

    }

    public TokenBean register(RegisterBean registerBean) {
        return null;
    }

    public TokenBean refreshToken(TokenBean tokenBean) {
        return null;
    }
}
