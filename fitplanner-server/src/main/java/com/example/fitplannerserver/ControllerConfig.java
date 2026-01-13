package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.AuthenticationController;
import com.example.fitplannerserver.controller.WorkoutPlanController;
import com.example.fitplannerserver.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ControllerConfig {

    private final JwtUtil jwtUtil;

    public ControllerConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public WorkoutPlanController workoutPlanController() {
        return new WorkoutPlanController();
    }

    @Bean
    public AuthenticationController authenticationController() {
        return new AuthenticationController(jwtUtil);
    }
}
