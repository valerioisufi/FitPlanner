package com.example.fitplannerclient.ui.gui1;

import com.example.fitplannerclient.ui.GraphicController;
import com.example.fitplannercommon.ExerciseBean;
import com.example.fitplannerclient.ui.gui1.view.WorkoutExecutionView;

import java.io.IOException;

public class WorkoutExecutionController implements GraphicController {

    private final WorkoutExecutionView view;

    // Bean dell'esercizio corrente (parte del workout session)
    private final ExerciseBean currentExercise;

    public WorkoutExecutionController() {
        this.view = new WorkoutExecutionView();
        this.currentExercise = new ExerciseBean(10,11,4,8, "jksfks", "sjkls");

        // 1. Popola i dati statici (Mockati o presi da DB descrittivo)
        populateExerciseData();

        // 2. Crea le righe dei set in base al Bean
        populateSetsTable();

        // 3. Azioni
        view.setOnFinishAction(() -> {
            System.out.println("Esercizio completato!");
            // Navigator.getInstance().goBackToWorkout();
        });
    }

    @Override
    public void start(GuiManager guiManager) {
        guiManager.setView(view);
    }

    private void populateSetsTable() {
        // Leggiamo dal Bean quanti set dobbiamo fare
        int totalSets = currentExercise.getSet(); // Es. 4
        int targetReps = currentExercise.getRep(); // Es. 8

        for (int i = 1; i <= totalSets; i++) {
            // "0" è il placeholder per il peso, targetReps per le ripetizioni
            view.addSetRow(i, "0", String.valueOf(targetReps));
        }
    }

    private void populateExerciseData() {
        // Simulazione: Nel sistema reale avresti un ExerciseDescriptionBean
        // che contiene video URL e testo lungo, recuperato tramite exercise.getType()

        String exName = "Barbell Squat"; // Risolto da ID
        String history = "Last time: 50kg x 8 reps (4 sets)";

        // Istruzioni lunghe (come nell'immagine)
        String instructions =
                "1. Set Up:\n" +
                        "   • Set a barbell on a squat rack slightly below shoulder height.\n" +
                        "   • Step under the bar and position it across the upper part of your back.\n" +
                        "   • Grip the bar outside shoulder width.\n\n" +
                        "2. Unrack and Stance:\n" +
                        "   • Unrack the bar by extending your legs.\n" +
                        "   • Position your feet slightly wider than hip-width, toes pointed slightly outward.\n\n" +
                        "3. The Descent:\n" +
                        "   • Initiate movement by bending hips and knees.\n" +
                        "   • Keep your chest up and back straight.\n" +
                        "   • Lower until thighs are parallel to the floor.\n\n" +
                        "4. The Ascent:\n" +
                        "   • Drive through your mid-foot to push back up.\n" +
                        "   • Squeeze your glutes at the top.";


        view.setExerciseDetails(exName, history);
        view.setInstructions(exName, instructions);
    }
}