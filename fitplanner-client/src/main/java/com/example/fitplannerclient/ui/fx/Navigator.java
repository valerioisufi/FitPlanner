package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.context.UserSessionContext;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.controller.session.SessionManager;
import com.example.fitplannerclient.controller.session.SessionManager.LoginOutcome;
import com.example.fitplannerclient.ui.fx.guicontroller.*;
import javafx.application.Platform;

import java.util.concurrent.CompletableFuture;

/**
 * Classe che si occupa della navigazione tra le viste
 */
public class Navigator {

    private final GuiManager guiManager;
    private final SessionManager sessionManager;

    private GuiController currentGuiController;

    public Navigator(GuiManager guiManager, SessionManager sessionManager) {
        this.guiManager = guiManager;
        this.sessionManager = sessionManager;
        sessionManager.setReauthenticationHandler(this::promptReauthentication);
    }

    private UserSessionContext session() {
        return sessionManager.getSession();
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

    private GuiController createHomeController() {
        ProfileManager profileManager = session().createProfileManager();
        ProfileBean profile = profileManager.getCacheProfileInfo();

        // la home dipende dal tipo di profilo dell'utente
        if (profile.getProfileType() == ProfileBean.ProfileType.TRAINER) {
            return new TrainerHomeViewController(this, guiManager, profileManager, session().createNotificationManager());
        }
        return new AthleteHomeViewController(this, guiManager, profileManager, session().createWorkoutPlanManager(), session().createNotificationManager());
    }

    public void requireAuthentication(Runnable onSuccess) {
        AuthenticationViewController authGuiController = new AuthenticationViewController(
                guiManager,
                sessionManager,
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
     * Handler di ri-autenticazione per il SessionManager: mostra il login in overlay
     * e completa il future con l'esito. Se l'identità è cambiata, la vista corrente
     * (che apparteneva al vecchio utente) viene abbandonata per la home
     */
    private CompletableFuture<LoginOutcome> promptReauthentication() {
        CompletableFuture<LoginOutcome> result = new CompletableFuture<>();

        Platform.runLater(() -> {
            AuthenticationViewController authGuiController = new AuthenticationViewController(
                    guiManager,
                    sessionManager,
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
        sessionManager.logout();
        requireAuthentication(null);
    }

    public void goHome() {
        if (sessionManager.isAuthenticated()) { // l'utente è già autenticato
            Platform.runLater(() -> navigateTo(createHomeController()));
        } else if (sessionManager.hasPersistedTokens()) { // l'utente possiede dei token con cui provare ad autenticarsi
            sessionManager.resumeSessionAsync()
                    .thenAccept(outcome -> Platform.runLater(() -> navigateTo(createHomeController())))
                    .exceptionally(ex -> {
                        requireAuthentication(null);
                        return null;
                    });
        } else { // l'utente deve fare il login o registrarsi
            requireAuthentication(null);
        }
    }

    public void goToProfile() {
        ProfileViewController profileController = new ProfileViewController(this, guiManager, session().createProfileManager(), session().createNotificationManager());
        Platform.runLater(() -> navigateTo(profileController));
    }

    public void goToExerciseLibrary() {
        ExerciseLibraryViewController controller = new ExerciseLibraryViewController(this, guiManager, session().createExerciseLibraryManager(), session().createNotificationManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToPlanManagement() {
        PlanManagementViewController controller = new PlanManagementViewController(this, guiManager, session().createWorkoutPlanManager(), session().createProfileManager(), session().createNotificationManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToWorkoutPlanEditor(String planIdToEdit, boolean copyOfExisting) {
        WorkoutPlanEditorViewController controller = new WorkoutPlanEditorViewController(this, guiManager, planIdToEdit, copyOfExisting, session().createEditWorkoutPlanManager(), session().createExerciseLibraryManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToWorkoutExecution(String planId, int sessionDay) {
        WorkoutExecutionViewController controller = new WorkoutExecutionViewController(this, guiManager, planId, sessionDay, session().createWorkoutExecutionManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToStatistics() {
        ProgressViewController controller = new ProgressViewController(
                this,
                guiManager,
                session().createWorkoutHistoryManager(),
                session().createNotificationManager());
        Platform.runLater(() -> navigateTo(controller));
    }

    public void goToAthleteDashboard(ProfileBean athlete) {
        AthleteDetailsViewController controller = new AthleteDetailsViewController(
                this,
                guiManager,
                athlete,
                session().createWorkoutPlanManager(),
                session().createWorkoutHistoryManagerFor(athlete.getUserId()),
                session().createNotificationManager());
        Platform.runLater(() -> navigateTo(controller));
    }
}
