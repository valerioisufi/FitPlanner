package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.view.library.ExerciseLibraryView;
import com.example.fitplannerclient.util.ValidationUtils;
import javafx.application.Platform;
import javafx.scene.layout.Pane;

import com.example.fitplannerclient.controller.session.NotificationManager;

public class ExerciseLibraryViewController implements GuiController {

    private final ExerciseLibraryView view;
    private final HeaderViewController headerViewController;
    private final GuiManager guiManager;
    private final ExerciseLibraryManager manager;

    public ExerciseLibraryViewController(Navigator navigator, GuiManager guiManager, ExerciseLibraryManager manager, NotificationManager notificationManager) {
        this.guiManager = guiManager;
        this.manager = manager;
        this.headerViewController = new HeaderViewController(navigator, notificationManager, 1, HeaderViewController.Type.TRAINER);
        this.view = new ExerciseLibraryView();

        this.view.setHeaderView(this.headerViewController.getView());

        bindValidators();

        this.view.setOnAddAction(() -> showEditModal(null));
        this.view.setOnEditAction(this::showEditModal);
        this.view.setOnDeleteAction(this::deleteExercise);
        
        this.view.getEditModal().setOnCloseAction(guiManager::hideModal);
        this.view.getEditModal().setOnCancelAction(guiManager::hideModal);
        this.view.getEditModal().setOnSaveAction(this::saveExercise);
    }

    private void showEditModal(ExerciseDescriptionBean exercise) {
        this.view.getEditModal().setExercise(exercise);
        this.guiManager.showModal(this.view.getEditModal());
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
        headerViewController.start();
        loadExercises();
    }

    @Override
    public void stop() {
        headerViewController.stop();
        this.guiManager.hideModal();
    }

    private void loadExercises() {
        manager.getExercisesAsync(null)
                .thenAccept(exercises -> Platform.runLater(() -> view.setExerciseList(exercises)))
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel caricamento esercizi:", ex);
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
                            guiManager.hideModal();
                            guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Esercizio salvato con successo.");
                        });
                        loadExercises();
                    })
                    .exceptionally(ex -> {
                        guiManager.showExceptionError("Errore durante il salvataggio:", ex);
                        return null;
                    });
        } else {
            manager.updateExerciseAsync(bean)
                    .thenRun(() -> {
                        Platform.runLater(() -> {
                            guiManager.hideModal();
                            guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Esercizio aggiornato con successo.");
                        });
                        loadExercises();
                    })
                    .exceptionally(ex -> {
                        guiManager.showExceptionError("Errore durante il salvataggio:", ex);
                        return null;
                    });
        }
    }


    private void deleteExercise(ExerciseDescriptionBean exercise) {
        manager.removeExerciseAsync(exercise.getExerciseId())
                .thenRun(() -> {
                    loadExercises();
                    Platform.runLater(() -> guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Esercizio eliminato con successo."));
                })
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nell'eliminazione dell'esercizio:", ex);
                    return null;
                });
    }
}
