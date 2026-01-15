package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.AuthenticationController;
import com.example.fitplannerserver.controller.WorkoutPlanController;
import com.example.fitplannerserver.security.JwtUtil;
import com.example.fitplannerserver.security.SpringSessionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllerConfig {

    private final JwtUtil jwtUtil;
    private final SpringSessionProvider sessionManager;

    public ControllerConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        this.sessionManager = new SpringSessionProvider();
    }

    @Bean
    public WorkoutPlanController workoutPlanController() {
        return new WorkoutPlanController(sessionManager);
    }

    @Bean
    public AuthenticationController authenticationController() {
        return new AuthenticationController(jwtUtil);
    }
}
