package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannercommon.ExerciseDescriptionBean;
import com.example.fitplannerclient.ui.gui1.view.ExerciseLibraryView;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExerciseLibraryController implements GraphicController {

    private final ExerciseLibraryView view;

    public ExerciseLibraryController() {
        this.view = new ExerciseLibraryView();

        // 1. Carica i dati (Mock)
        List<ExerciseDescriptionBean> data = loadMockData();

        // 2. Passa i dati alla view
        view.setExerciseList(data);

        // 3. Imposta le azioni dei bottoni
        setupActions();
    }

    private void setupActions() {
        view.setOnAddAction(() -> {
            System.out.println("Navigazione verso: Creazione Nuovo Esercizio");
            // Navigator.getInstance().goToCreateExercise();
        });

        view.setOnEditAction(bean -> {
            System.out.println("Modifica esercizio: " + bean.getName());
        });

        view.setOnDeleteAction(bean -> {
            System.out.println("Eliminazione esercizio: " + bean.getName());
            // Qui potresti ricaricare la lista rimuovendo l'elemento
        });
    }

    // Metodo che simula il recupero dei Bean da un servizio
    private List<ExerciseDescriptionBean> loadMockData() {
        List<ExerciseDescriptionBean> list = new ArrayList<>();

        list.add(new ExerciseDescriptionBean(
                "Barbell Squat",
                "A compound lower body exercise targeting quads, glutes, and hamstrings",
                Arrays.asList("Quadriceps", "Glutes", "Hamstrings")
        ));

        list.add(new ExerciseDescriptionBean(
                "Bench Press",
                "Upper body pressing movement for chest, shoulders, and triceps",
                Arrays.asList("Chest", "Shoulders", "Triceps")
        ));

        list.add(new ExerciseDescriptionBean(
                "Deadlift",
                "Full body compound movement focusing on posterior chain",
                Arrays.asList("Back", "Glutes", "Hamstrings")
        ));

        list.add(new ExerciseDescriptionBean(
                "Pull-ups",
                "Bodyweight exercise for back and arm development",
                Arrays.asList("Back", "Biceps")
        ));

        list.add(new ExerciseDescriptionBean(
                "Face Pulls",
                "Isolation exercise for rear delts and rotator cuff health",
                Arrays.asList("Shoulders", "Upper Back")
        ));

        return list;
    }

    @Override
    public void start(GuiManager guiManager) {
        guiManager.setView(this.view);
    }
}