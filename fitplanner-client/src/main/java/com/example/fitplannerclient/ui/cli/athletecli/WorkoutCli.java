package com.example.fitplannerclient.ui.cli.athletecli;

import com.example.fitplannerclient.bean.exercise.CurrentExerciseBean;
import com.example.fitplannerclient.bean.log.ExerciseSetBean;
import com.example.fitplannerclient.controller.plan.execution.WorkoutExecutionManager;
import com.example.fitplannerclient.controller.plan.execution.observer.WorkoutExecutionObserver;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;

import java.util.List;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class WorkoutCli extends AbstractCliView implements WorkoutExecutionObserver {

    private final CountDownLatch initLatch = new CountDownLatch(1);
    private CountDownLatch transitionLatch;

    private WorkoutExecutionManager executionManager;

    private final String planId;
    private final int sessionDay;

    private volatile WorkoutExecutionPhase currentPhase;
    private volatile CurrentExerciseBean currentExercise;
    private volatile WorkoutExecutionState engineState = WorkoutExecutionState.PLAYING;
    private volatile boolean isCompleted = false;

    public WorkoutCli(String planId, int sessionDay) {
        this.planId = planId;
        this.sessionDay = sessionDay;
    }

    @Override
    protected CliView render() {
        this.executionManager = engine.getSessionContext().createWorkoutExecutionManager();

        executionManager.attachObserver(this);
        executionManager.startSessionAsync(planId, sessionDay)
                .exceptionally(e -> {
                    printer.printException("Errore nel caricamento dell'allenamento: ", e);
                    return null;
                }).join();

        executionManager.play();

        while (!isCompleted) {
            showMenuAndProcessInput();
        }

        return new DashboardCli();
    }

    @Override
    public void stop() {
        if (executionManager != null) {
            executionManager.detachObserver(this);
            executionManager.stop();
        }
    }

    private void waitForTransition(Runnable action) {
        transitionLatch = new CountDownLatch(1);
        action.run();
        try {
            transitionLatch.await(500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void showMenuAndProcessInput() {
        if (currentPhase == null && !isCompleted) {
            try {
                initLatch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (isCompleted) return;

        printer.printHeader("ALLENAMENTO IN CORSO - " + currentPhase);

        if (currentPhase == WorkoutExecutionPhase.EXERCISE) {
            handleExercisePhase();
        } else if (currentPhase == WorkoutExecutionPhase.REST) {
            handleRestPhase();
        } else if (currentPhase == WorkoutExecutionPhase.COMPLETED) {
            printer.printInfo("L'allenamento è terminato.");
            finishWorkout();
        }
    }

    private void handleExercisePhase() {
        int waitTime = 0;
        while (currentExercise == null && waitTime < 30) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            waitTime++;
        }

        if (currentExercise != null) {
            printExerciseInfo();
        }

        String playPauseOption = (engineState == WorkoutExecutionState.PAUSED) ? "Riprendi (Play)" : "Metti in Pausa";

        printer.printMenu(null, List.of(
                "Registra Serie",
                "Aggiungi Note Esercizio",
                "Esercizio successivo ",
                "Esercizio precedente ",
                playPauseOption,
                "Termina Allenamento e Salva"
        ));

        processExerciseMenu(reader.readInt("Scegli un'opzione: ", 1, 6));
    }

    private void handleRestPhase() {
        printer.printInfo("Fase di Recupero in corso...");
        String playPauseOption = (engineState == WorkoutExecutionState.PAUSED) ? "Riprendi (Play)" : "Metti in Pausa";

        printer.printMenu(null, List.of(
                "Salta Recupero",
                playPauseOption,
                "Termina Allenamento e Salva"
        ));

        processRestMenu(reader.readInt("Scegli un'opzione: ", 1, 3));
    }

    private void printExerciseInfo() {
        printer.printLn("Esercizio: " + currentExercise.getExerciseDescription().getName());
        printer.printLn("Istruzioni: " + currentExercise.getExerciseDescription().getExecution());

        if (currentExercise.getLastWeightLog() != null && currentExercise.getLastWeightLog().getSets() != null && !currentExercise.getLastWeightLog().getSets().isEmpty()) {
            printer.printLn("Ultimi log:");
            for (ExerciseSetBean set : currentExercise.getLastWeightLog().getSets()) {
                printer.printLn("- " + set.getLoad() + "kg x " + set.getReps() + " (RPE: " + set.getRpe() + ")");
            }
        }
    }

    private void processExerciseMenu(int scelta) {
        if (currentPhase != WorkoutExecutionPhase.EXERCISE) {
            printer.printLn("La fase è cambiata. Scelta ignorata.");
            return;
        }

        switch (scelta) {
            case 1 -> logSeries();
            case 2 -> addNotes();
            case 3 -> {
                currentExercise = null;
                waitForTransition(() -> executionManager.skipNext());
            }
            case 4 -> {
                currentExercise = null;
                waitForTransition(() -> executionManager.skipPrevious());
            }
            case 5 -> togglePlayPause();
            case 6 -> finishWorkout();
            default -> printer.printInfo("Scelta non valida.");
        }
    }

    private void logSeries() {
        double weight = reader.readDouble("Peso (kg): ");
        int reps = reader.readInt("Ripetizioni: ", 1, 100);
        int rpe = reader.readInt("RPE (1-10): ", 1, 10);

        if (currentExercise != null) {
            executionManager.logExerciseSet(
                    currentExercise.getExerciseDescription().getExerciseId(),
                    new ExerciseSetBean(reps, weight, rpe)
            );

            printer.printInfo("Serie registrata con successo.");
            currentExercise = null;
            waitForTransition(() -> executionManager.done());
        }
    }

    private void addNotes() {
        String note = reader.readString("Inserisci nota per l'esercizio: ");

        if (currentExercise != null) {
            executionManager.updateExerciseNotes(currentExercise.getExerciseDescription().getExerciseId(), note);
            printer.printInfo("Note aggiornate con successo.");
        }
    }

    private void processRestMenu(int scelta) {
        if (currentPhase != WorkoutExecutionPhase.REST) {
            printer.printInfo("La fase è cambiata. Scelta ignorata.");
            return;
        }

        switch (scelta) {
            case 1 -> waitForTransition(() -> executionManager.done());
            case 2 -> togglePlayPause();
            case 3 -> finishWorkout();
            default -> printer.printInfo("Scelta non valida.");
        }
    }

    private void togglePlayPause() {
        if (engineState == WorkoutExecutionState.PAUSED) {
            executionManager.play();
            printer.printInfo("Allenamento ripreso.");
        } else {
            executionManager.pause();
            printer.printInfo("Allenamento in pausa.");
        }
    }

    private void finishWorkout() {
        String notes = reader.readString("Aggiungi note per l'intera sessione (opzionale): ");

        executionManager.finishAndSaveSession(notes)
                .exceptionally(ex -> {
                    printer.printException("Errore nel salvataggio della sessione:", ex);
                    return null;
                }).join();

        printer.printInfo("Allenamento salvato con successo!");
        isCompleted = true;
    }

    @Override
    public void updateExecutionPhase(WorkoutExecutionPhase phase) {
        this.currentPhase = phase;
        initLatch.countDown();
        if (transitionLatch != null) {
            transitionLatch.countDown();
        }

        if (phase == WorkoutExecutionPhase.COMPLETED) {
            printer.printInfo("L'allenamento è stato completato.");
        } else if (phase == WorkoutExecutionPhase.EXERCISE) {
            printer.printInfo("Passaggio alla fase EXERCISE!");
        }
    }

    @Override
    public void updateCurrentExercise(CurrentExerciseBean currentExercise) {
        this.currentExercise = currentExercise;
    }

    @Override
    public void updateCurrentWorkoutEngineState(WorkoutExecutionState state) {
        this.engineState = state;
    }

    @Override
    public void updateCurrentRestTime(int restTimeSeconds) {
        // Ignorato per evitare spam nella console
    }
}
