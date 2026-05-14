package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannercommon.RegisterBean;
import com.example.fitplannercommon.TokenBean;
import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.exception.InvalidCredentialsException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.security.JwtUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.password.PasswordEncoder;

public class AuthenticationController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenBean login(LoginBean loginBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        Account account = accountDao.findByEmail(loginBean.getUsername());
        if (account == null || !passwordEncoder.matches(loginBean.getPassword(), account.getPasswordHash())) {
            throw new InvalidCredentialsException("Credenziali non valide");
        }

        String refreshToken = JwtUtil.generateRefreshToken();
        account.setRefreshToken(refreshToken);
        accountDao.save(account);

        TokenBean tokenBean = new TokenBean();

        tokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUserId()));
        tokenBean.setRefreshToken(refreshToken);

        return tokenBean;
    }

    public TokenBean register(RegisterBean registerBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        Account.Role role = (registerBean.getProfile().getProfileType() == ProfileBean.ProfileType.ATHLETE)
                ? Account.Role.ATHLETE
                : Account.Role.TRAINER;

        // Generate a UUIDv7 for the userId
        String newUserId = UuidCreator.getTimeOrderedEpoch().toString();

        Account account = new Account(
                newUserId,
                registerBean.getEmail(),
                passwordEncoder.encode(registerBean.getPassword()),
                JwtUtil.generateRefreshToken(),
                role
        );

        if (accountDao.create(account)) {
            TokenBean tokenBean = new TokenBean();

            tokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUserId()));
            tokenBean.setRefreshToken(account.getRefreshToken());
            return tokenBean;
        } else {
            throw new InvalidCredentialsException("Email già utilizzata");
        }
    }

    public TokenBean refreshToken(TokenBean tokenBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        Account account = accountDao.findByRefreshToken(tokenBean.getRefreshToken());
        if (account != null) {
            TokenBean newTokenBean = new TokenBean();

            newTokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUserId()));
            return newTokenBean;
        }

        throw new InvalidCredentialsException("Refresh token non valido");
    }
}