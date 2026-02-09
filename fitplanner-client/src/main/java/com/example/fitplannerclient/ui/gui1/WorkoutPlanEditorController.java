package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannerclient.ui.gui1.view.WorkoutPlanEditorView;
import com.example.fitplannercommon.*;
import javafx.stage.Stage;

import java.util.*;

public class WorkoutPlanEditorController implements GraphicController {

    private final WorkoutPlanEditorView view;
    private final WorkoutPlanBean currentPlan;

    // Mappa per simulare un "Database" locale degli esercizi disponibili
    // Chiave: Nome Esercizio, Valore: Bean Descrizione
    private final Map<String, ExerciseDescriptionBean> exerciseLibraryMap = new HashMap<>();

    // Mappa inversa per risolvere ID -> Nome (Simulazione)
    private final Map<Integer, String> typeIdToNameMap = new HashMap<>();

    public WorkoutPlanEditorController() {
        this.view = new WorkoutPlanEditorView();
        this.currentPlan = new WorkoutPlanBean();

        initializeMockData();
        setupController();

        view.populateLibrary(new ArrayList<>(exerciseLibraryMap.values()));
        view.renderPlan(currentPlan.getWorkoutSessions());
    }

    private void setupController() {
        // Logica per risolvere i nomi nella view
        view.setNameResolver(typeId -> typeIdToNameMap.getOrDefault(typeId, "Unknown"));

        // Logica quando un esercizio viene droppato
        view.setOnExerciseDropped((exerciseName, dayNumber) -> {
            addExerciseToDay(exerciseName, dayNumber);
        });
    }

    private void addExerciseToDay(String exerciseName, int dayNumber) {
        System.out.println("Droppato: " + exerciseName + " su Giorno " + dayNumber);

        // 1. Trova la sessione corrispondente al giorno
        Optional<WorkoutSessionBean> sessionOpt = currentPlan.getWorkoutSessions().stream()
                .filter(s -> s.getDay() == dayNumber)
                .findFirst();

        if (sessionOpt.isPresent()) {
            WorkoutSessionBean session = sessionOpt.get();

            // 2. Trova l'ID (type) basato sul nome (Simulazione logica DB)
            // Nel mondo reale useresti un ID vero
            int exerciseTypeID = getTypeIdByName(exerciseName);

            // 3. Crea il nuovo ExerciseBean (Istanza nel piano)
            // Default: 3 serie, 10 rep, 60s rest
            ExerciseBean newExercise = new ExerciseBean(exerciseTypeID, 10, 3, 60, "", "");

            // 4. Aggiungi alla lista
            if (session.getExercises() == null) {
                session.setExercises(new ArrayList<>());
            }
            session.getExercises().add(newExercise);

            // 5. Ricarica la vista per mostrare il nuovo elemento
            view.renderPlan(currentPlan.getWorkoutSessions());
        }
    }

    private int getTypeIdByName(String name) {
        // Cerca nella mappa inversa
        for (Map.Entry<Integer, String> entry : typeIdToNameMap.entrySet()) {
            if (entry.getValue().equals(name)) return entry.getKey();
        }
        return 0; // Default o Error code
    }

    private void initializeMockData() {
        // 1. Creiamo la libreria
        createLibraryEntry(1, "Barbell Squat", "Legs", "Quads");
        createLibraryEntry(2, "Bench Press", "Chest", "Triceps");
        createLibraryEntry(3, "Deadlift", "Back", "Hamstrings");
        createLibraryEntry(4, "Pull-ups", "Back", "Biceps");
        createLibraryEntry(5, "Overhead Press", "Shoulders");
        createLibraryEntry(6, "Leg Press", "Legs");

        // 2. Creiamo una struttura vuota per il piano (es. 7 giorni)
        List<WorkoutSessionBean> sessions = new ArrayList<>();
        for (int i = 1; i <= 7; i++) { // Creiamo 7 giorni vuoti
            sessions.add(new WorkoutSessionBean("Day " + i, new ArrayList<>(), i));
        }
        currentPlan.setWorkoutSessions(sessions);
        currentPlan.setName("My Custom Split");
    }

    private void createLibraryEntry(int id, String name, String... muscles) {
        ExerciseDescriptionBean desc = new ExerciseDescriptionBean(name, "Desc...", Arrays.asList(muscles));
        exerciseLibraryMap.put(name, desc);
        typeIdToNameMap.put(id, name);
    }

    @Override
    public void start(GuiManager guiManager) {
        guiManager.setView(this.view);
    }
}