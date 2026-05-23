package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.AppControllerFactory;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.AuthManager;
import com.example.fitplannerclient.service.SessionManager;
import com.example.fitplannerclient.ui.fx.guicontroller.*;
import javafx.application.Platform;

public class Navigator {

    private static Navigator instance;

    private final GuiManager guiManager;
    private final AppControllerFactory appControllerFactory;
    private final SessionManager sessionManager;

    private GuiController currentGuiController;

    public Navigator(GuiManager guiManager, AppControllerFactory factory, SessionManager sessionManager) {
        this.guiManager = guiManager;
        this.appControllerFactory = factory;
        this.sessionManager = sessionManager;
        instance = this;
    }

    public static Navigator getInstance() {
        return instance;
    }

    public GuiManager getGuiManager() {
        return guiManager;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    private void navigateTo(GuiController nextController) {
        if (currentGuiController != null) {
            currentGuiController.stop();
        }
        currentGuiController = nextController;
        guiManager.setView(nextController.getView());
        nextController.start();
    }

    private void handleLoginSuccess(Runnable onSuccess) {
        if (appControllerFactory.createProfileManager().didUserChange()) {
            appControllerFactory.resetDataManagers();
            HomeViewController homeController = new HomeViewController(appControllerFactory.createProfileManager(), appControllerFactory.createWorkoutPlanManager());
            navigateTo(homeController);
        } else {
            if (onSuccess != null) {
                onSuccess.run();
            } else {
                HomeViewController homeController = new HomeViewController(appControllerFactory.createProfileManager(), appControllerFactory.createWorkoutPlanManager());
                navigateTo(homeController);
            }
        }
    }

    public void requireAuthentication(Runnable onSuccess) {
        AuthManager authAppController = appControllerFactory.createAuthManager();

        Runnable finalSuccessAction = () -> {
            appControllerFactory.createProfileManager().getProfileInfoAsync()
                .thenAccept(profile -> {
                    Platform.runLater(() -> handleLoginSuccess(onSuccess));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> requireAuthentication(onSuccess));
                    return null;
                });
        };

        AuthenticationViewController authGuiController = new AuthenticationViewController(
                guiManager,
                authAppController,
                finalSuccessAction
        );

        Platform.runLater(() -> navigateTo(authGuiController));
    }

    public void requireAuthenticationOverlay(Runnable onSuccess) {
        AuthManager authAppController = appControllerFactory.createAuthManager();

        Runnable onLoginSuccess = () -> {
            appControllerFactory.createProfileManager().getProfileInfoAsync()
                .thenAccept(profile -> {
                    Platform.runLater(() -> {
                        guiManager.hideOverlay();
                        handleLoginSuccess(onSuccess);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> requireAuthenticationOverlay(onSuccess));
                    return null;
                });
        };

        AuthenticationViewController authGuiController = new AuthenticationViewController(
                guiManager, authAppController, onLoginSuccess
        );

        guiManager.showOverlay(authGuiController.getView());
        authGuiController.start();
    }

    public void logout() {
        sessionManager.logout();
        appControllerFactory.resetManagers();
        requireAuthentication(null);
    }

    public void goHome() {
        if (!sessionManager.isLoggedIn()) {
            requireAuthentication(null);
        } else {
            appControllerFactory.createProfileManager().getProfileInfoAsync()
                .thenAccept(profile -> {
                    Platform.runLater(() -> {
                        HomeViewController homeController = new HomeViewController(appControllerFactory.createProfileManager(), appControllerFactory.createWorkoutPlanManager());
                        navigateTo(homeController);
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> requireAuthentication(null));
                    return null;
                });
        }
    }

    public void goToProfile() {
        if (appControllerFactory.createProfileManager().getCachedProfile() == null) {
            goHome();
            return;
        }
        ProfileViewController profileController = new ProfileViewController(appControllerFactory.createProfileManager());
        Platform.runLater(() -> navigateTo(profileController));
    }

    public void goToExerciseLibrary() {
        ExerciseLibraryViewController controller = new ExerciseLibraryViewController(guiManager, appControllerFactory.createExerciseLibraryManager(), appControllerFactory.createProfileManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToPlanManagement() {
        PlanManagementViewController controller = new PlanManagementViewController(appControllerFactory.createWorkoutPlanManager(), appControllerFactory.createProfileManager(), guiManager);
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToWorkoutPlanEditor(com.example.fitplannerclient.bean.plan.WorkoutPlanBean planToEdit) {
        WorkoutPlanEditorViewController controller = new WorkoutPlanEditorViewController(planToEdit, appControllerFactory.createWorkoutPlanManager(), appControllerFactory.createExerciseLibraryManager(), appControllerFactory.createProfileManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToWorkoutExecution(WorkoutSessionBean session) {
        WorkoutExecutionViewController controller = new WorkoutExecutionViewController(session, appControllerFactory.createSessionLogFacade(), appControllerFactory.createProfileManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToAthleteDashboard(ProfileBean athlete) {
        AthleteDashboardViewController controller = new AthleteDashboardViewController(athlete, appControllerFactory.createProfileManager(), appControllerFactory.createWorkoutPlanManager(), guiManager);
        Platform.runLater(() -> navigateTo(controller));
    }
}
