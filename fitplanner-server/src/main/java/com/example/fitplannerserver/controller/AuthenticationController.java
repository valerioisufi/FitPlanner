package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.TokenBean;
import com.example.fitplannercommon.UserBean;
import com.example.fitplannerserver.security.JwtUtil;
import org.springframework.stereotype.Service;

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
            tokenBean.setToken(jwtUtil.generateToken(loginBean.getUsername()));
            return tokenBean;
        }

        return null;

    }

    public TokenBean register(UserBean userBean) {
        return null;
    }
}
