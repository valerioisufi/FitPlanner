package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.AuthenticationController;
import com.example.fitplannerserver.controller.WorkoutPlanController;
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
    public WorkoutPlanController workoutPlanController(IdentityProvider identityProvider) {
        return new WorkoutPlanController(identityProvider);
    }

    @Bean
    public AuthenticationController authenticationController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        return new AuthenticationController(jwtUtil, passwordEncoder);
    }
}
