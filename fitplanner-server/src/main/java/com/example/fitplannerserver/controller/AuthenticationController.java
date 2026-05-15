package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.LoginBean;
import com.example.fitplannercommon.ProfileBean;
import com.example.fitplannercommon.RegisterBean;
import com.example.fitplannercommon.TokenBean;
import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.InvalidCredentialsException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.exception.WrongArgumentsException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.security.JwtUtil;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class AuthenticationController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
    }

    public TokenBean login(LoginBean loginBean) {
        if (loginBean == null || loginBean.getEmail() == null || loginBean.getPassword() == null)
            throw new WrongArgumentsException("");


        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        try {
            Optional<Account> accountFound = accountDao.findByEmail(loginBean.getEmail());

            if (accountFound.isEmpty() || !passwordEncoder.matches(loginBean.getPassword(), accountFound.get().getPasswordHash())) {
                throw new InvalidCredentialsException("Credenziali non valide");
            }

            Account account = accountFound.get();

            String refreshToken = JwtUtil.generateRefreshToken();
            account.setRefreshToken(refreshToken);
            accountDao.save(account);

            TokenBean tokenBean = new TokenBean();

            tokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUserId(), account.getProfileType()));
            tokenBean.setRefreshToken(refreshToken);

            return tokenBean;
        } catch (DaoException e) {
            throw new SystemException("Errore durante il login");
        }
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

        try {
            if (accountDao.create(account)) {
                TokenBean tokenBean = new TokenBean();

                tokenBean.setAccessToken(jwtUtil.generateAccessToken(account.getUserId(), account.getProfileType()));
                tokenBean.setRefreshToken(account.getRefreshToken());
                return tokenBean;
            } else {
                throw new InvalidCredentialsException("Email già utilizzata");
            }
        } catch (DaoException e) {
            throw new RuntimeException(e);
        }
    }

    public TokenBean refreshToken(TokenBean tokenBean) {
        AccountDao accountDao = DaoFactory.getInstance().getAccountDao();

        try {
            Optional<Account> account = accountDao.findByRefreshToken(tokenBean.getRefreshToken());

            if (account.isPresent()) {
                TokenBean newTokenBean = new TokenBean();

                newTokenBean.setAccessToken(jwtUtil.generateAccessToken(account.get().getUserId(), account.get().getProfileType()));
                return newTokenBean;
            }

            throw new InvalidCredentialsException("Refresh token non valido");

        } catch (DaoException e) {
            throw new SystemException("");
        }
    }
}