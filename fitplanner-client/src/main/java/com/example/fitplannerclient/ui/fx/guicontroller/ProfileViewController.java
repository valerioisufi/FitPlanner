package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.profile.ProfileView;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import com.example.fitplannerclient.controller.session.NotificationManager;

public class ProfileViewController implements GuiController {

    private final BorderPane mainPane;
    private final ProfileView profileView;
    private final HeaderViewController headerViewController;
    private final ProfileManager profileManager;
    private final GuiManager guiManager;

    public ProfileViewController(Navigator navigator, GuiManager guiManager, ProfileManager profileManager, NotificationManager notificationManager) {
        this.profileManager = profileManager;
        this.guiManager = guiManager;

        ProfileBean profile = profileManager.getCacheProfileInfo();
        HeaderViewController.Type type = profile.getProfileType() == ProfileBean.ProfileType.TRAINER 
            ? HeaderViewController.Type.TRAINER : HeaderViewController.Type.ATHLETE;

        this.headerViewController = new HeaderViewController(navigator, notificationManager, -1, type);
        this.profileView = new ProfileView();

        this.mainPane = new BorderPane();
        this.mainPane.setTop(headerViewController.getView());
        this.mainPane.setCenter(profileView);
    }

    @Override
    public void start() {
        headerViewController.start();
        ProfileBean profile = profileManager.getCacheProfileInfo();
        profileView.setProfileData(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getContactEmail(),
                profile.getPhoneNumber()
        );
        this.profileView.setRoleIndicator(profile.getProfileType().name());

        if (profile.getProfileType() == ProfileBean.ProfileType.ATHLETE) {
            profileView.showTrainerLinkSection(true);
            profileView.setLinkTrainerAction(code ->
                profileManager.linkTrainerAsync(code).thenRun(() ->
                    Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Trainer collegato con successo!"))
                ).exceptionally(ex -> {
                    guiManager.showExceptionError("Codice invito non valido:", ex);
                    return null;
                })
            );
        }

        this.profileView.setSaveAction(this::handleSave);
    }

    private void handleSave() {
        ProfileBean profile = profileManager.getCacheProfileInfo();
        if (profile == null) return;
        profile.setFirstName(profileView.getFirstName());
        profile.setLastName(profileView.getLastName());
        profile.setContactEmail(profileView.getContactEmail());
        profile.setPhoneNumber(profileView.getPhoneNumber());

        profileManager.updateProfileInfoAsync(profile)
                .thenAccept(v -> Platform.runLater(() -> {
                    profileView.finishEditing();
                    guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Profilo aggiornato con successo.");
                }))
                .exceptionally(ex -> {
                    guiManager.showExceptionError(
                            "Errore nell'aggiornamento del profilo:", ex);
                    return null;
                });
    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void stop() {
        headerViewController.stop();
    }
}
