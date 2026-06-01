package com.example.fitplannerserver;

import com.example.fitplannerserver.controller.*;
import com.example.fitplannerserver.dao.DaoFactory;
import com.example.fitplannerserver.security.IdentityProvider;
import com.example.fitplannerserver.security.JwtUtil;
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
        return new AuthenticationController(
                jwtUtil,
                passwordEncoder,
                DaoFactory.getInstance().getAccountDao(),
                DaoFactory.getInstance().getProfileDao()
        );
    }

    @Bean
    public ProfileController profileController(IdentityProvider identityProvider) {
        return new ProfileController(
                identityProvider,
                DaoFactory.getInstance().getProfileDao(),
                DaoFactory.getInstance().getCoachingDao()
        );
    }

    @Bean
    public WorkoutPlanManagementController workoutPlanManagementController(IdentityProvider identityProvider) {
        return new WorkoutPlanManagementController(
                identityProvider,
                DaoFactory.getInstance().getWorkoutPlanDao(),
                DaoFactory.getInstance().getCoachingDao()
        );
    }

    @Bean
    public WorkoutScheduleController workoutScheduleController(IdentityProvider identityProvider) {
        return new WorkoutScheduleController(
                identityProvider,
                DaoFactory.getInstance().getWorkoutPlanDao(),
                DaoFactory.getInstance().getSessionLogDao()
        );
    }

    @Bean
    public ManageExerciseLibraryController manageExerciseLibraryController(IdentityProvider identityProvider) {
        return new ManageExerciseLibraryController(
                identityProvider,
                DaoFactory.getInstance().getExerciseLibraryDao(),
                DaoFactory.getInstance().getCoachingDao()
        );
    }

    @Bean
    public SessionLogController sessionLogController(IdentityProvider identityProvider) {
        return new SessionLogController(
                identityProvider,
                DaoFactory.getInstance().getSessionLogDao(),
                DaoFactory.getInstance().getCoachingDao()
        );
    }

    @Bean
    public NotificationController notificationController(IdentityProvider identityProvider) {
        return new NotificationController(identityProvider);
    }

}
