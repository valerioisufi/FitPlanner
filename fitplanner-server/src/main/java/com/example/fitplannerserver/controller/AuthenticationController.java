package com.example.fitplannerserver.controller;

import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannercommon.RegisterDTO;
import com.example.fitplannercommon.TokenDTO;
import com.example.fitplannerserver.beanvalidator.AuthValidator;
import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.exception.DaoException;
import com.example.fitplannerserver.exception.InvalidCredentialsException;
import com.example.fitplannerserver.exception.SystemException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.example.fitplannerserver.security.JwtUtil;
import com.example.fitplannerserver.util.InvitationCodeGenerator;
import com.github.f4b6a3.uuid.UuidCreator;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

public class AuthenticationController {
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    private final AccountDao accountDao;
    private final ProfileDao profileDao;

    public AuthenticationController(
            JwtUtil jwtUtil,
            PasswordEncoder passwordEncoder,
            AccountDao accountDao,
            ProfileDao profileDao
    ) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;

        this.accountDao = accountDao;
        this.profileDao = profileDao;
    }

    public TokenDTO login(LoginDTO loginDTO) {
        AuthValidator.validateLoginBean(loginDTO);

        try {
            Optional<Account> accountFound = accountDao.findByEmail(loginDTO.getEmail());

            if (accountFound.isEmpty() || !passwordEncoder.matches(loginDTO.getPassword(), accountFound.get().getPasswordHash())) {
                throw new InvalidCredentialsException("Credenziali non valide");
            }

            Account account = accountFound.get();

            String refreshToken = JwtUtil.generateRefreshToken();
            account.setRefreshToken(refreshToken);
            accountDao.save(account);

            TokenDTO tokenDTO = new TokenDTO();

            tokenDTO.setAccessToken(jwtUtil.generateAccessToken(account.getUserId(), account.getProfileType()));
            tokenDTO.setRefreshToken(refreshToken);

            return tokenDTO;
        } catch (DaoException e) {
            throw new SystemException("Errore durante il login");
        }
    }

    public TokenDTO register(RegisterDTO registerDTO) {
        AuthValidator.validateRegisterBean(registerDTO);

        Account.Role role = (registerDTO.getProfile().getProfileType() == ProfileDTO.ProfileType.ATHLETE)
                ? Account.Role.ATHLETE
                : Account.Role.TRAINER;

        // Generate a UUIDv7 for the userId
        String newUserId = UuidCreator.getTimeOrderedEpoch().toString();

        Account account = new Account(
                newUserId,
                registerDTO.getEmail(),
                passwordEncoder.encode(registerDTO.getPassword()),
                JwtUtil.generateRefreshToken(),
                role
        );

        try {
            if (accountDao.create(account)) {
                // l'account dell'utente è stato creato correttamente
                ProfileDTO profileDTO = registerDTO.getProfile();

                User user = new User(newUserId);
                user.setUserProfileInfo(
                        profileDTO.getFirstName(),
                        profileDTO.getLastName(),
                        profileDTO.getContactEmail(),
                        profileDTO.getPhoneNumber()
                );
                if(role == Account.Role.TRAINER)
                    user.setInvitationCode(InvitationCodeGenerator.generateCode());

                profileDao.save(user);

                // genero i token jwt per l'utente
                TokenDTO tokenDTO = new TokenDTO();

                tokenDTO.setAccessToken(jwtUtil.generateAccessToken(account.getUserId(), account.getProfileType()));
                tokenDTO.setRefreshToken(account.getRefreshToken());
                return tokenDTO;
            } else {
                throw new InvalidCredentialsException("Email già utilizzata");
            }
        } catch (DaoException e) {
            throw new SystemException("Errore durante la registrazione");
        }
    }

    public TokenDTO refreshToken(TokenDTO tokenDTO) {
        AuthValidator.validateRefreshTokenBean(tokenDTO);

        try {
            Optional<Account> account = accountDao.findByRefreshToken(tokenDTO.getRefreshToken());

            if (account.isPresent()) {
                TokenDTO newTokenDTO = new TokenDTO();

                newTokenDTO.setAccessToken(jwtUtil.generateAccessToken(account.get().getUserId(), account.get().getProfileType()));
                return newTokenDTO;
            }

            throw new InvalidCredentialsException("Refresh token non valido");

        } catch (DaoException e) {
            throw new SystemException("Errore durante il recupero dell'account");
        }
    }


}