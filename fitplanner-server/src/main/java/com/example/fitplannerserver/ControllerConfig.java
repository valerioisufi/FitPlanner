package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.AuthenticationController;
import com.example.fitplannerserver.controller.WorkoutPlanController;
import com.example.fitplannerserver.security.JwtUtil;
import com.example.fitplannerserver.security.SessionProvider;
import com.example.fitplannerserver.security.SpringSessionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class ControllerConfig {

    @Bean
    public SessionProvider sessionProvider() {
        return new SpringSessionProvider();
    }

    @Bean
    public WorkoutPlanController workoutPlanController(SessionProvider sessionProvider) {
        return new WorkoutPlanController(sessionProvider);
    }

    @Bean
    public AuthenticationController authenticationController(JwtUtil jwtUtil, PasswordEncoder passwordEncoder) {
        return new AuthenticationController(jwtUtil, passwordEncoder);
    }
}
