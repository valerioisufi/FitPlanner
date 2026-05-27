package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.library.ExerciseLibraryView;
import com.example.fitplannerclient.util.ValidationUtils;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

public class ExerciseLibraryViewController implements GuiController {

    private final ExerciseLibraryView view;
    private final HeaderViewController headerViewController;
    private final GuiManager guiManager;
    private final ExerciseLibraryManager manager;
    private final ProfileManager profileManager;

    public ExerciseLibraryViewController(GuiManager guiManager, ExerciseLibraryManager manager, ProfileManager profileManager) {
        this.guiManager = guiManager;
        this.manager = manager;
        this.profileManager = profileManager;
        this.headerViewController = new HeaderViewController(1, profileManager);
        this.view = new ExerciseLibraryView();

        this.view.setHeaderView(this.headerViewController.getView());

        bindValidators();

        this.view.setOnAddAction(() -> this.view.showModal(null));
        this.view.setOnEditAction(this.view::showModal);
        this.view.setOnDeleteAction(this::deleteExercise);
        
        this.view.getEditModal().setOnCloseAction(this.view::hideModal);
        this.view.getEditModal().setOnCancelAction(this.view::hideModal);
        this.view.getEditModal().setOnSaveAction(this::saveExercise);
    }

    private void bindValidators() {
        view.getEditModal().getNameField().setValidator(name -> ValidationUtils.validateRequired(name, "Nome Esercizio", 50));
        view.getEditModal().getDescField().setValidator(desc -> {
            if (desc != null && desc.length() > 500) return "La descrizione non può superare i 500 caratteri";
            return null;
        });
    }

    @Override
    public Pane getView() {
        return this.view;
    }

    @Override
    public void start() {
        loadExercises();
    }

    @Override
    public void stop() {
        this.view.hideModal();
    }

    private void loadExercises() {
        manager.getExercisesAsync(null)
                .thenAccept(exercises -> Platform.runLater(() -> view.setExerciseList(exercises)))
                .exceptionally(ex -> {
                    Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.ERROR, "Errore nel caricamento esercizi: " + extractErrorMessage(ex)));
                    return null;
                });
    }

    private void saveExercise(ExerciseDescriptionBean bean) {
        boolean isNameValid = view.getEditModal().getNameField().validate();
        boolean isDescValid = view.getEditModal().getDescField().validate();
        boolean isValid = isNameValid && isDescValid;

        if (!isValid) return;
        
        if (bean.getExerciseId() == null) {
            manager.addExerciseAsync(bean)
                    .thenRun(() -> {
                        Platform.runLater(() -> {
                            this.view.hideModal();
                            guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Esercizio salvato con successo.");
                        });
                        loadExercises();
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.ERROR, extractErrorMessage(ex)));
                        return null;
                    });
        } else {
            manager.updateExerciseAsync(bean)
                    .thenRun(() -> {
                        Platform.runLater(() -> {
                            this.view.hideModal();
                            guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Esercizio aggiornato con successo.");
                        });
                        loadExercises();
                    })
                    .exceptionally(ex -> {
                        Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.ERROR, extractErrorMessage(ex)));
                        return null;
                    });
        }
    }

    private String extractErrorMessage(Throwable ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : "Errore durante il salvataggio.";
    }

    private void deleteExercise(ExerciseDescriptionBean exercise) {
        manager.removeExerciseAsync(exercise.getExerciseId())
                .thenRun(() -> {
                    loadExercises();
                    Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Esercizio eliminato con successo."));
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.ERROR, "Errore nell'eliminazione dell'esercizio: " + extractErrorMessage(ex)));
                    return null;
                });
    }
}
