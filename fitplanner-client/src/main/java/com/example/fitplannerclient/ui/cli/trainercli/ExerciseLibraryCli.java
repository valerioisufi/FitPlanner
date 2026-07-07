package com.example.fitplannerclient.ui.cli.trainercli;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;
import com.example.fitplannerclient.ui.cli.DashboardCli;
import com.example.fitplannerclient.util.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ExerciseLibraryCli extends AbstractCliView {

    private static final String EXERCISE_NOT_FOUND = "Nessun esercizio nella libreria.";
    private static final String LIBRARY_NOT_FOUND = "Errore durante il caricamento della libreria: ";

    private ExerciseLibraryManager manager;

    @Override
    protected CliView render() {
        this.manager = engine.getSessionContext().createExerciseLibraryManager();

        printer.printHeader("LIBRERIA ESERCIZI");
        printer.printMenu(null, List.of(
                "Indietro",
                "Visualizza Libreria",
                "Aggiungi Esercizio",
                "Modifica Esercizio",
                "Elimina Esercizio"
        ));
        int scelta = reader.readInt("Scegli un'opzione: ", 1, 5);

        switch (scelta) {
            case 1 -> { return new DashboardCli(); }
            case 2 -> visualizzaLibreria();
            case 3 -> aggiungiEsercizio();
            case 4 -> modificaEsercizio();
            case 5 -> eliminaEsercizio();
            default -> {
                return this;
            }
        }

        return this;
    }

    private void visualizzaLibreria() {
        List<ExerciseDescriptionBean> exercises = getExercises();
        if (exercises == null || exercises.isEmpty()) {
            printer.printInfo(EXERCISE_NOT_FOUND);
            reader.waitForEnter();
            return;
        }
        List<String> headers = List.of("Nome", "Gruppi Muscolari", "Descrizione");
        List<List<String>> data = new ArrayList<>();

        for (ExerciseDescriptionBean ex : exercises) {
            String muscleGroups = ex.getMuscleGroups() != null ? String.join(", ", ex.getMuscleGroups()) : "";
            String execution = ex.getExecution() != null ? ex.getExecution() : "";

            data.add(List.of(ex.getName() != null ? ex.getName() : "", muscleGroups, execution));
        }

        printer.printTable(headers, data);
        reader.waitForEnter();
    }

    private void aggiungiEsercizio() {
        printer.printHeader("NUOVO ESERCIZIO");
        String name = reader.readStringAndValidate("Nome Esercizio: ", input -> ValidationUtils.validateRequired(input, "Nome", 50));
        String execution = reader.readString("Descrizione esecuzione: ");
        String groupsStr = reader.readString("Gruppi muscolari (separati da virgola): ");

        ExerciseDescriptionBean bean = new ExerciseDescriptionBean();
        bean.setName(name);
        bean.setExecution(execution);
        bean.setMuscleGroups(parseMuscleGroups(groupsStr));

        manager.addExerciseAsync(bean)
                .exceptionally(e -> {
                    printer.printException("Errore durante l'aggiunta: ", e);
                    return null;
                }).join();

        printer.printSuccess("Esercizio aggiunto con successo!");
        reader.waitForEnter();
    }

    private void modificaEsercizio() {
        List<ExerciseDescriptionBean> exercises = getExercises();
        if (exercises == null || exercises.isEmpty()) {
            printer.printInfo(EXERCISE_NOT_FOUND);
            reader.waitForEnter();
            return;
        }

        Optional<ExerciseDescriptionBean> selected = reader.selectFrom("Seleziona l'esercizio da modificare:",
                exercises, ex -> ex.getName() != null ? ex.getName() : "Esercizio Sconosciuto");
        if (selected.isEmpty()) return;
        ExerciseDescriptionBean beanToEdit = selected.get();

        String name = reader.readStringAndValidate("Nome Esercizio", input -> ValidationUtils.validateRequired(input, "Nome", 50), beanToEdit.getName() != null ? beanToEdit.getName() : "");
        String execDefault = beanToEdit.getExecution() != null ? beanToEdit.getExecution() : "";
        String execution = reader.readStringAndValidate("Descrizione esecuzione", input -> null, execDefault);
        String groupsDefault = beanToEdit.getMuscleGroups() != null ? String.join(", ", beanToEdit.getMuscleGroups()) : "";
        String groupsStr = reader.readStringAndValidate("Gruppi muscolari (separati da virgola)", input -> null, groupsDefault);

        beanToEdit.setName(name);
        beanToEdit.setExecution(execution);
        beanToEdit.setMuscleGroups(parseMuscleGroups(groupsStr));

        manager.updateExerciseAsync(beanToEdit)
                .exceptionally(e -> {
                    printer.printException("Errore durante l'aggiornamento: ", e);
                    return null;
                }).join();

        printer.printSuccess("Esercizio modificato con successo!");


        reader.waitForEnter();
    }

    private void eliminaEsercizio() {
        List<ExerciseDescriptionBean> exercises = getExercises();
        if (exercises == null || exercises.isEmpty()) {
            printer.printInfo(EXERCISE_NOT_FOUND);
            reader.waitForEnter();
            return;
        }

        Optional<ExerciseDescriptionBean> selected = reader.selectFrom("Seleziona l'esercizio da eliminare:",
                exercises, ex -> ex.getName() != null ? ex.getName() : "Esercizio Sconosciuto");
        if (selected.isEmpty()) return;

        manager.removeExerciseAsync(selected.get().getExerciseId()).join();
        printer.printSuccess("Esercizio eliminato con successo!");


    reader.waitForEnter();
    }

    private List<ExerciseDescriptionBean> getExercises() {
        return manager.getExercisesAsync(null)
                .exceptionally(e-> {
                    printer.printException( LIBRARY_NOT_FOUND, e);
                    return new ArrayList<>();
                }).join();

    }

    /** Divide una stringa di gruppi muscolari separati da virgola, ignorando i vuoti. */
    private List<String> parseMuscleGroups(String csv) {
        List<String> groups = new ArrayList<>();
        for (String group : csv.split(",")) {
            String trimmed = group.trim();
            if (!trimmed.isEmpty()) {
                groups.add(trimmed);
            }
        }
        return groups;
    }
}
