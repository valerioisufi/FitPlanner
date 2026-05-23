package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.profile.ProfileBean;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.ProfileView;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

public class ProfileViewController implements GuiController {

    private final BorderPane mainPane;
    private final ProfileView profileView;
    private final HeaderViewController headerViewController;
    private final ProfileManager profileManager;

    public ProfileViewController(ProfileManager profileManager) {
        this.profileManager = profileManager;
        this.headerViewController = new HeaderViewController(-1, profileManager);
        this.profileView = new ProfileView();

        this.mainPane = new BorderPane();
        this.mainPane.setTop(headerViewController.getView());
        this.mainPane.setCenter(profileView);
    }

    @Override
    public void start() {
        ProfileBean profile = profileManager.getCachedProfile();
        profileView.setProfileData(
                profile.getFirstName(),
                profile.getLastName(),
                profile.getContactEmail(),
                profile.getPhoneNumber()
        );
        this.profileView.setRoleIndicator(profile.getProfileType().name());

        if (profile.getProfileType() == ProfileBean.ProfileType.ATHLETE) {
            profileView.showTrainerLinkSection(true);
            profileView.setLinkTrainerAction(code -> {
                profileManager.linkTrainerAsync(code).thenRun(() -> {
                    Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.SUCCESS, "Trainer collegato con successo!"));
                }).exceptionally(ex -> {
                    Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.ERROR, "Codice invito non valido"));
                    return null;
                });
            });
        }

        this.profileView.setSaveAction(this::handleSave);
    }

    private void handleSave() {
        ProfileBean profile = profileManager.getCachedProfile();
        if (profile == null) return;
        profile.setFirstName(profileView.getFirstName());
        profile.setLastName(profileView.getLastName());
        profile.setContactEmail(profileView.getContactEmail());
        profile.setPhoneNumber(profileView.getPhoneNumber());

        profileManager.updateProfileInfoAsync(profile)
                .thenAccept(v -> Platform.runLater(() -> {
                    profileView.finishEditing();
                    Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.SUCCESS, "Profilo aggiornato con successo.");
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(
                            GuiManager.NotificationType.ERROR, 
                            "Errore nell'aggiornamento del profilo: " + ex.getMessage()));
                    return null;
                });
    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void stop() {}
}
