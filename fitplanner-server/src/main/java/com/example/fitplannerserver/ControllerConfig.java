package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.*;
import com.example.fitplannerserver.security.JwtUtil;
import com.example.fitplannerserver.security.IdentityProvider;
import com.example.fitplannerserver.security.SpringIdentityProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ControllerConfig {

    @Bean
    public IdentityProvider sessionProvider() {
        return new SpringIdentityProvider();
    }

    @Bean
    public AuthenticationController authenticationController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        return new AuthenticationController(jwtUtil, passwordEncoder);
    }

    @Bean
    public ProfileController profileController(IdentityProvider identityProvider) {
        return new ProfileController(identityProvider);
    }

    @Bean
    public EditWorkoutPlanController editWorkoutPlanController(IdentityProvider identityProvider) {
        return new EditWorkoutPlanController(identityProvider);
    }

    @Bean
    public ManageExerciseLibraryController manageExerciseLibraryController(IdentityProvider identityProvider) {
        return new ManageExerciseLibraryController(identityProvider);
    }

    @Bean
    public SessionLogController sessionLogController(IdentityProvider identityProvider) {
        return new SessionLogController(identityProvider);
    }

    @Bean
    public NotificationController notificationController(IdentityProvider identityProvider) {
        return new NotificationController(identityProvider);
    }

}
