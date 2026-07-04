package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.dashboard.TrainerHomeView;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class TrainerHomeViewController implements GuiController {
    private final TrainerHomeView view;
    private final HeaderViewController headerViewController;
    private final Navigator navigator;
    private final ProfileManager profileManager;
    private final GuiManager guiManager;

    public TrainerHomeViewController(Navigator navigator, ProfileManager profileManager, GuiManager guiManager) {
        this.navigator = navigator;
        this.profileManager = profileManager;
        this.guiManager = guiManager;
        this.headerViewController = new HeaderViewController(navigator, 0, profileManager);
        this.view = new TrainerHomeView(headerViewController.getView());
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        ProfileBean profile = profileManager.getCacheProfileInfo();
        view.setWelcomeMessage("Benvenuto, " + profile.getFirstName() + "!", "Gestisci la tua libreria e i piani dei tuoi atleti.");
        view.showTrainerDashboard(
                navigator::goToExerciseLibrary,
                navigator::goToPlanManagement
        );

        // Fetch and set invite code
        profileManager.getInvitationCodeAsync()
                .thenAccept(code -> Platform.runLater(() -> view.setInviteCode(code)))
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel recupero del codice di invito:", ex);
                    return null;
                });

        // Fetch and set athletes
        profileManager.getMyAthletesAsync()
                .thenAccept(athletes -> Platform.runLater(() ->
                        view.showAthleteList(athletes, navigator::goToAthleteDashboard)
                ))
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel caricamento degli atleti:", ex);
                    return null;
                });
    }

    @Override
    public void stop() {
        // Metodo intenzionalmente vuoto
    }
}
