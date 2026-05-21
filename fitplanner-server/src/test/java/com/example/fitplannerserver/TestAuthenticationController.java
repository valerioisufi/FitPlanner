package com.example.fitplannerserver;

import com.example.fitplannercommon.LoginDTO;
import com.example.fitplannercommon.ProfileDTO;
import com.example.fitplannercommon.RegisterDTO;
import com.example.fitplannercommon.TokenDTO;
import com.example.fitplannerserver.controller.AuthenticationController;
import com.example.fitplannerserver.dao.AccountDao;
import com.example.fitplannerserver.dao.ProfileDao;
import com.example.fitplannerserver.dao.inmemory.InMemoryAccountDao;
import com.example.fitplannerserver.dao.inmemory.InMemoryProfileDao;
import com.example.fitplannerserver.exception.InvalidCredentialsException;
import com.example.fitplannerserver.model.Account;
import com.example.fitplannerserver.model.User;
import com.example.fitplannerserver.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test della classe AuthenticationController che gestisce la registrazione
 * e il successivo login degli utenti
 * @author Valerio Isufi
 */

class TestAuthenticationController {

    private PasswordEncoder passwordEncoder;

    private AuthenticationController controller;
    private AccountDao accountDao;
    private ProfileDao profileDao;

    @BeforeEach
    void setup(){
        JwtUtil jwtUtil = new JwtUtil();
        passwordEncoder = new BCryptPasswordEncoder();
        accountDao = new InMemoryAccountDao();
        profileDao = new InMemoryProfileDao();

        controller = new AuthenticationController(
                jwtUtil,
                passwordEncoder,
                accountDao,
                profileDao
        );
    }

    @Test
    @DisplayName("Date delle credenziali valide, il login deve avere successo e restituire un TokenBean")
    void testLoginSuccess() throws Exception {
        // Arrange
        String email = "test@example.com";
        String password = "Password123";

        createAndSaveMockAccount(email, password, Account.Role.ATHLETE);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(password);

        // Act
        TokenDTO result = controller.login(loginDTO);

        // Assert
        assertNotNull(result, "Un login avvenuto con successo deve restituire un TokenBean valido");
    }

    @Test
    @DisplayName("Data una password errata, il login deve fallire e lanciare InvalidCredentialsException")
    void testLoginInvalidCredentials() throws Exception {
        // Arrange
        String email = "test@example.com";
        String wrongPassword = "Wrongpass123";

        createAndSaveMockAccount(email, "Correctpass123", Account.Role.ATHLETE);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail(email);
        loginDTO.setPassword(wrongPassword);

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            controller.login(loginDTO);
        });
    }

    @Test
    @DisplayName("Data un'email che già esiste, la registrazione deve lanciare InvalidCredentialsException")
    void testRegisterEmailAlreadyUsed() throws Exception {
        // Arrange
        String email = "duplicate@example.com";
        String password = "Password123";

        createAndSaveMockAccount(email, "Otherpass123", Account.Role.ATHLETE);

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(email);
        registerDTO.setPassword(password);

        registerDTO.setProfile(createMockProfileBean());

        // Act & Assert
        assertThrows(InvalidCredentialsException.class, () -> {
            controller.register(registerDTO);
        });
    }

    @Test
    @DisplayName("Dati utente validi, la registrazione deve salvare l'account e il profilo nel database")
    void testRegisterSuccess() throws Exception {
        // Arrange
        String newEmail = "new@example.com";

        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setEmail(newEmail);
        registerDTO.setPassword("Password123");

        registerDTO.setProfile(createMockProfileBean());

        // Act
        TokenDTO result = controller.register(registerDTO);

        // Assert
        assertNotNull(result, "La registrazione deve restituire un TokenBean valido");

        // 1. Verifichiamo che l'account sia stato effettivamente salvato
        Optional<Account> savedAccountOpt = accountDao.findByEmail(newEmail);
        assertTrue(savedAccountOpt.isPresent(), "L'account deve essere stato salvato nel database");

        // 2. Verifichiamo che anche il profilo sia stato salvato e collegato correttamente
        String generatedUserId = savedAccountOpt.get().getUserId();
        Optional<User> savedProfileOpt = profileDao.findById(generatedUserId);

        assertTrue(savedProfileOpt.isPresent(), "Il profilo utente deve essere stato salvato nel database");
    }

    @Test
    @DisplayName("Dato un refresh token valido, deve restituire un nuovo TokenBean con successo")
    void testRefreshTokenSuccess() throws Exception {
        // Arrange
        Account savedAccount = createAndSaveMockAccount("testrefresh@example.com", "Password123", Account.Role.TRAINER);

        TokenDTO requestTokenDTO = new TokenDTO();
        requestTokenDTO.setRefreshToken(savedAccount.getRefreshToken());

        // Act
        TokenDTO result = controller.refreshToken(requestTokenDTO);

        // Assert
        assertNotNull(result, "Il refresh deve restituire un nuovo TokenBean");
        assertNotNull(result.getAccessToken(), "Il nuovo TokenBean deve contenere un access token generato");
    }

    private Account createAndSaveMockAccount(String email, String rawPassword, Account.Role role) throws Exception {
        String encodedPassword = passwordEncoder.encode(rawPassword);

        Account mockAccount = new Account(
                "uuid-" + System.currentTimeMillis(), // safe dummy UUID
                email,
                encodedPassword,
                "dummy_refresh_token",
                role
        );

        accountDao.create(mockAccount);

        return mockAccount;
    }

    private ProfileDTO createMockProfileBean(){
        return new ProfileDTO(
                null,
                "Mario",
                "Rossi",
                "1234567890",
                "contact@example.com",
                ProfileDTO.ProfileType.ATHLETE
        );
    }

}