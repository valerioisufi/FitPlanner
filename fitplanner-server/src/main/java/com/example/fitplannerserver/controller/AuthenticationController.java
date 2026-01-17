package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.RegisterBean;
import com.example.fitplannercommon.TokenBean;
import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

public class AuthenticationController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder bCryptPasswordEncoder;

    public AuthenticationController(JwtUtil jwtUtil, PasswordEncoder bCryptPasswordEncoder) {
        this.jwtUtil = jwtUtil;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public TokenBean login(LoginBean loginBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        Account account = accountDao.findByUsername(loginBean.getUsername());
        if (account != null) {
            String refreshToken = JwtUtil.generateRefreshToken();
            account.setRefreshToken(refreshToken);
            accountDao.save(account);

            TokenBean tokenBean = new TokenBean();
            tokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUsername()));
            tokenBean.setRefreshToken(refreshToken);

            return tokenBean;
        }

        return null;

    }

    public TokenBean register(RegisterBean registerBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        Account account = new Account(
                registerBean.getUsername(),
                bCryptPasswordEncoder.encode(registerBean.getPassword()),
                JwtUtil.generateRefreshToken()
        );

        if (accountDao.create(account)) {
            TokenBean tokenBean = new TokenBean();
            tokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUsername()));
            tokenBean.setRefreshToken(account.getRefreshToken());
            return tokenBean;
        }

        return null;
    }

    public TokenBean refreshToken(TokenBean tokenBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        Account account = accountDao.findByRefreshToken(tokenBean.getRefreshToken());
        if (account != null) {
            TokenBean newTokenBean = new TokenBean();
            newTokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUsername()));
            return newTokenBean;
        }

        return null;
    }
}
