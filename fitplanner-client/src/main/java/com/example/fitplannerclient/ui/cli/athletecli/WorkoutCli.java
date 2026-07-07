package com.example.fitplannerclient.ui.cli.athletecli;

import com.example.fitplannerclient.bean.exercise.CurrentExerciseBean;
import com.example.fitplannerclient.bean.log.ExerciseSetBean;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;

import java.util.List;

public class WorkoutCli extends AbstractCliView implements WorkoutExecutionObserver {

    private WorkoutExecutionManager executionManager;

    private final String planId;
    private final int sessionDay;

    private WorkoutExecutionPhase currentPhase;
    private CurrentExerciseBean currentExercise;
    private boolean isCompleted = false;

    public WorkoutCli(String planId, int sessionDay) {
        this.planId = planId;
        this.sessionDay = sessionDay;
    }

    @Override
    protected CliView render() {
        this.executionManager = engine.getSessionContext().createWorkoutExecutionManager();

        executionManager.attachObserver(this);

        try {
            executionManager.startSessionAsync(planId, sessionDay).join();
            executionManager.play();

            while (!isCompleted) {
                showMenuAndProcessInput();
            }

        } catch (Exception e) {
            printer.printException("Errore durante l'esecuzione dell'allenamento", e);
        } finally {
            executionManager.detachObserver(this);
            executionManager.stop();
        }

        return new DashboardCli();
    }

    private void showMenuAndProcessInput() {
        while (currentPhase == null && !isCompleted) {
            try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }

        if (isCompleted) return;

        printer.printHeader("ALLENAMENTO IN CORSO - " + currentPhase);

        if (currentPhase == WorkoutExecutionPhase.EXERCISE) {
            if (currentExercise != null) {
                printer.printInfo("Esercizio: " + currentExercise.getExerciseDescription().getName());
                printer.printInfo("Istruzioni: " + currentExercise.getExerciseDescription().getExecution());

                if (currentExercise.getLastWeightLog() != null && currentExercise.getLastWeightLog().getSets() != null && !currentExercise.getLastWeightLog().getSets().isEmpty()) {
                    printer.printInfo("Ultimi log:");
                    for (ExerciseSetBean set : currentExercise.getLastWeightLog().getSets()) {
                        printer.printInfo("- " + set.getLoad() + "kg x " + set.getReps() + " (RPE: " + set.getRpe() + ")");
                    }
                }
            }

            printer.printMenu(null, List.of(
                    "Registra Serie",
                    "Aggiungi Note Esercizio",
                    "Salta Esercizio Corrente (Avanti)",
                    "Salta Esercizio Precedente (Indietro)",
                    "Termina Allenamento e Salva"
            )); // todo gestire anche tasti play/pause

            processExerciseMenu(reader.readInt("Scegli un'opzione: ", 1, 5));

        } else if (currentPhase == WorkoutExecutionPhase.REST) {
            printer.printInfo("Fase di Recupero in corso...");
            printer.printMenu(null, List.of(
                    "Salta Recupero",
                    "Termina Allenamento e Salva"
            ));

            processRestMenu(reader.readInt("Scegli un'opzione: ", 1, 2));
        }
    }

    private void processExerciseMenu(int scelta) {
        if (currentPhase != WorkoutExecutionPhase.EXERCISE) {
            printer.printInfo("La fase è cambiata. Scelta ignorata.");
            return;
        }

        switch (scelta) {
            case 1 -> {
                double weight = reader.readDouble("Peso (kg): ");
                int reps = reader.readInt("Ripetizioni: ", 1, 999);
                int rpe = reader.readInt("RPE (1-10): ", 1, 10);
                if (currentExercise != null) {
                    executionManager.logExerciseSet(currentExercise.getExerciseDescription().getExerciseId(), new ExerciseSetBean(reps, weight, rpe));
                    printer.printInfo("Serie registrata con successo.");
                }
            }
            case 2 -> {
                String note = reader.readString("Inserisci nota per l'esercizio: ");
                if (currentExercise != null) {
                    executionManager.updateExerciseNotes(currentExercise.getExerciseDescription().getExerciseId(), note);
                    printer.printInfo("Note aggiornate con successo.");
                }
            }
            case 3 -> executionManager.skipNext();
            case 4 -> executionManager.skipPrevious();
            case 5 -> finishWorkout();
            default -> printer.printInfo("Scelta non valida.");
        }
    }

    private void processRestMenu(int scelta) {
        if (currentPhase != WorkoutExecutionPhase.REST) {
            printer.printInfo("La fase è cambiata. Scelta ignorata.");
            return;
        }

        if (scelta == 1) {
            executionManager.skipPrevious();
        } else {
            finishWorkout();
        }
    }

    private void finishWorkout() {
        String sessionNotes = reader.readString("Note finali sessione (premi invio per saltare): ");
        try {
            executionManager.finishAndSaveSession(sessionNotes).join();
            printer.printInfo("Allenamento salvato con successo.");
        } catch (Exception e) {
            printer.printException("Errore nel salvataggio dell'allenamento", e);
        }
        isCompleted = true;
    }

    @Override
    public void updateExecutionPhase(WorkoutExecutionPhase phase) {
        this.currentPhase = phase;
        if (phase == WorkoutExecutionPhase.COMPLETED) {
            printer.printInfo("\nL'allenamento è stato completato dal motore.");
            isCompleted = true;
        } else if (phase == WorkoutExecutionPhase.EXERCISE) {
            printer.printInfo("\nPassaggio alla fase EXERCISE! (Premi invio se sei bloccato nel prompt)");
        }
    }

    @Override
    public void updateCurrentExercise(CurrentExerciseBean currentExercise) {
        this.currentExercise = currentExercise;
    }

    @Override
    public void updateCurrentWorkoutEngineState(WorkoutExecutionState state) {
        // Stato del motore non utilizzato dalla CLI.
        // todo forse andrebbe utilizzato
    }

    @Override
    public void updateCurrentRestTime(int restTimeSeconds) {
        // Ignorato per evitare spam nella console
    }

    @Override
    public void stop() {
        if (executionManager != null) {
            executionManager.detachObserver(this);
            executionManager.stop();
        }
    }
}
