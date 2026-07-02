package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.context.UserSessionContext;
import com.example.fitplannerclient.controller.SessionController;
import com.example.fitplannerclient.controller.SessionController.LoginOutcome;
import com.example.fitplannerclient.ui.fx.guicontroller.*;
import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;

/**
 * Classe che si occupa della navigazione tra le viste
 */
public class Navigator {

    private final GuiManager guiManager;
    private final SessionController sessionController;

    private GuiController currentGuiController;

    public Navigator(GuiManager guiManager, SessionController sessionController) {
        this.guiManager = guiManager;
        this.sessionController = sessionController;
        sessionController.setReauthenticationHandler(this::promptReauthentication);
    }

    private UserSessionContext session() {
        return sessionController.getSession();
    }

    private void navigateTo(GuiController nextController) {
        if (currentGuiController != null) {
            currentGuiController.stop();
        }
        guiManager.clearModals();

        currentGuiController = nextController;
        guiManager.setView(nextController.getView());
        nextController.start();
    }

    private HomeViewController createHomeController() {
        return new HomeViewController(this, session().createProfileManager(), session().createWorkoutPlanManager(), guiManager);
    }

    public void requireAuthentication(Runnable onSuccess) {
        AuthenticationViewController authGuiController = new AuthenticationViewController(
                guiManager,
                sessionController,
                outcome -> Platform.runLater(() -> {
                    if (outcome == LoginOutcome.SAME_USER && onSuccess != null) {
                        onSuccess.run();
                    } else {
                        navigateTo(createHomeController());
                    }
                })
        );

        Platform.runLater(() -> navigateTo(authGuiController));
    }

    /**
     * Handler di ri-autenticazione per il SessionController: mostra il login in overlay
     * e completa il future con l'esito. Se l'identità è cambiata, la vista corrente
     * (che apparteneva al vecchio utente) viene abbandonata per la home
     */
    private CompletableFuture<LoginOutcome> promptReauthentication() {
        CompletableFuture<LoginOutcome> result = new CompletableFuture<>();

        Platform.runLater(() -> {
            AuthenticationViewController authGuiController = new AuthenticationViewController(
                    guiManager,
                    sessionController,
                    outcome -> Platform.runLater(() -> {
                        guiManager.hideOverlay();
                        if (outcome == LoginOutcome.NEW_USER) {
                            navigateTo(createHomeController());
                        }
                        result.complete(outcome);
                    })
            );

            guiManager.showOverlay(authGuiController.getView());
            authGuiController.start();
        });

        return result;
    }

    public void logout() {
        sessionController.logout();
        requireAuthentication(null);
    }

    public void goHome() {
        if (sessionController.isAuthenticated()) {
            Platform.runLater(() -> navigateTo(createHomeController()));
        } else if (sessionController.hasPersistedTokens()) {
            sessionController.resumeSessionAsync()
                    .thenAccept(outcome -> Platform.runLater(() -> navigateTo(createHomeController())))
                    .exceptionally(ex -> {
                        requireAuthentication(null);
                        return null;
                    });
        } else {
            requireAuthentication(null);
        }
    }

    public void goToProfile() {
        if (!sessionController.isAuthenticated()) {
            goHome();
            return;
        }
        ProfileViewController profileController = new ProfileViewController(this, session().createProfileManager(), guiManager);
        Platform.runLater(() -> navigateTo(profileController));
    }

    public void goToExerciseLibrary() {
        ExerciseLibraryViewController controller = new ExerciseLibraryViewController(this, guiManager, session().createExerciseLibraryManager(), session().createProfileManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToPlanManagement() {
        PlanManagementViewController controller = new PlanManagementViewController(this, session().createWorkoutPlanManager(), session().createProfileManager(), guiManager);
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToWorkoutPlanEditor(String planIdToEdit, boolean copyOfExisting) {
        WorkoutPlanEditorViewController controller = new WorkoutPlanEditorViewController(this, planIdToEdit, copyOfExisting, session().createEditWorkoutPlanManager(), session().createExerciseLibraryManager(), guiManager);
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToWorkoutExecution(String planId, int sessionDay) {
        WorkoutExecutionViewController controller = new WorkoutExecutionViewController(this, planId, sessionDay, session().createWorkoutExecutionManager(), session().createExerciseLibraryManager(), guiManager);
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToAthleteDashboard(ProfileBean athlete) {
        AthleteDashboardViewController controller = new AthleteDashboardViewController(
                this,
                athlete,
                session().createProfileManager(),
                session().createWorkoutPlanManager(),
                session().createWorkoutHistoryManager(),
                guiManager);
        Platform.runLater(() -> navigateTo(controller));
    }
}
