package com.example.fitplannerclient.ui.cli.trainercli;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.FlowDecoratorBean;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.bean.plan.WorkoutSessionBean;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.editor.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlanEditorCli extends AbstractCliView {

    private static final String SCELTA= "Scelta: ";
    private static final String BACK_LABEL = "Indietro";
    private static final String NO_NAME = "Senza Nome";

    private EditWorkoutPlanManager planManager;
    private ExerciseLibraryManager exerciseManager;

    private WorkoutPlanObserver observer;

    private final String planId;
    private final boolean isClone;
    private WorkoutPlanBean activePlan;

    private final Map<Integer, String> nodeIndexMap = new HashMap<>();

    public PlanEditorCli(String planId, boolean isClone) {
        this.planId = planId;
        this.isClone = isClone;
    }

    @Override
    protected CliView render() {
        this.planManager = engine.getSessionContext().createEditWorkoutPlanManager();
        this.exerciseManager = engine.getSessionContext().createExerciseLibraryManager();

        this.observer = () ->
            this.activePlan = planManager.getPlanAsync()
                    .exceptionally(ex -> activePlan).join(); // Mantieni il piano attivo corrente in caso di errore

        planManager.addObserver(observer);

        AtomicBoolean created = new AtomicBoolean(true);
        if (planId == null) {
            planManager.createNewPlan()
                    .exceptionally(ex -> {
                        printer.printException("Errore nella creazione del piano:", ex);
                        created.set(false);
                        return null;
                    }).join();
        } else {
            planManager.editExistingPlan(planId, isClone).join();
        }

        if (!created.get()) {
            return new WorkoutPlanLibraryCli();
        }
        planManager.buildProtocolBlockLibrary();

        boolean running = true;
        while (running) {
            printer.printHeader("EDITOR PIANI DI ALLENAMENTO");
            printer.printInfo("Piano attuale: " + (activePlan.getName() != null ? activePlan.getName() : "Senza Titolo") + " (Ciclo: " + activePlan.getCycleLength() + " giorni)");

            printSessions();

            printer.printMenu("Menu Principale", List.of("Modifica Nome Piano", "Modifica Lunghezza Ciclo", "Aggiungi Sessione", "Elimina Sessione", "Rinomina Sessione", "Modifica Contenuto Sessione (Albero)", "Undo", "Redo", "Salva e Chiudi", "Esci senza salvare"));
            int choice = reader.readInt(SCELTA, 1, 10);

            switch (choice) {
                case 1 -> planManager.changePlanName(reader.readString("Nuovo nome: "));
                case 2 -> planManager.changeCycleLength(reader.readInt("Nuovo ciclo (1-30): ", 1, 30));
                case 3 -> planManager.addSession(reader.readInt("Giorno della sessione (0-" + (activePlan.getCycleLength() - 1) + "): ", 0, activePlan.getCycleLength() - 1));
                case 4 -> chooseSession().ifPresent(s -> planManager.removeSession(s.getDay()));
                case 5 ->
                    chooseSession().ifPresent(s ->
                        planManager.updateSessionName(s.getDay(), reader.readString("Nuovo nome sessione: ")));
                case 6 -> chooseSession().ifPresent(s -> manageSessionTree(s.getDay()));
                case 7 -> planManager.undo();
                case 8 -> planManager.redo();
                case 9 -> {
                        planManager.savePlan()
                                .exceptionally(e -> {
                                    printer.printException("Errore durante il salvataggio del piano:", e);
                                    reader.waitForEnter();
                                    return null;
                                }).join();

                        printer.printSuccess("Piano salvato!");
                        running = false;
                }
                case 10 -> running = false;
                default -> {
                    return new WorkoutPlanLibraryCli();
                }
            }
        }

        return new WorkoutPlanLibraryCli();
    }

    @Override
    public void stop() {
        planManager.removeObserver(observer);
    }

    private Optional<WorkoutSessionBean> chooseSession() {
        if (activePlan.getSessions() == null || activePlan.getSessions().isEmpty()) {
            printer.printInfo("Nessuna sessione presente. Aggiungi prima una sessione.");
            reader.waitForEnter();
            return Optional.empty();
        }

        return reader.selectFrom("Seleziona la sessione da modificare:",
                activePlan.getSessions(),
                s -> "Giorno " + s.getDay() + " - " + (s.getName() != null ? s.getName() : NO_NAME),
                BACK_LABEL);
    }

    private void printSessions(){
        if (activePlan.getSessions() == null || activePlan.getSessions().isEmpty()) {
            printer.printInfo("Nessuna sessione presente. Aggiungi prima una sessione.");
            reader.waitForEnter();
            return;
        }

        List<String> header = List.of("Nome Sessione", "Giorno");
        List<List<String>> data = new ArrayList<>();
        for (WorkoutSessionBean s : activePlan.getSessions()) {
            data.add(List.of(s.getName() != null ? s.getName() : NO_NAME, String.valueOf(s.getDay())));
        }
        printer.printTable(header, data);
    }

    private void manageSessionTree(int sessionDay) {
        boolean sessionRunning = true;
        while (sessionRunning) {
            WorkoutSessionBean session = findSession(sessionDay);
            if (session == null) return;

            printSessionTree(session);

            printer.printMenu("Menu Albero della Sessione", List.of(
                    BACK_LABEL,
                    "Aggiungi Nodo",
                    "Seleziona Nodo per Modifica/Eliminazione"
            ));

            int choice = reader.readInt(SCELTA, 1, 3);

            switch (choice) {
                case 1 -> sessionRunning = false;
                case 2 -> addFromToolBox(sessionDay);
                case 3 -> {
                    if (nodeIndexMap.isEmpty()) {
                        printer.printInfo("Albero vuoto.");
                        reader.waitForEnter();
                    } else {
                        int nodeIdx = reader.readInt("Inserisci l'indice del nodo [1-" + nodeIndexMap.size() + "]: ", 1, nodeIndexMap.size());
                        String nodeId = nodeIndexMap.get(nodeIdx);
                        if (nodeId != null) {
                            manageNode(nodeId);
                        }
                    }
                }
                default -> printer.printInfo("Scelta non valida.");
            }
        }
    }

    private WorkoutSessionBean findSession(int sessionDay) {
        for (WorkoutSessionBean s : activePlan.getSessions()) {
            if (s.getDay() == sessionDay) {
                return s;
            }
        }
        return null;
    }

    private void printSessionTree(WorkoutSessionBean session) {
        nodeIndexMap.clear();
        printer.printHeader("ALBERO SESSIONE - Giorno " + session.getDay());

        int counter = 1;
        if (session.getPlanRoot() != null && session.getPlanRoot().getChildren() != null && !session.getPlanRoot().getChildren().isEmpty()) {
            for (PlanNodeBean node : session.getPlanRoot().getChildren()) {
                counter = printNode(node, counter, 0);
            }
        } else {
            printer.printInfo("  (Vuota)");
        }
    }

    private int printNode(PlanNodeBean node, int counter, int depth) {
        String indent = "   ".repeat(depth);
        String nodeType = node.getType() != null ? node.getType().toString() : "Sconosciuto";
        printer.printInfo(indent + "[" + counter + "] " + node.getName() + " (" + nodeType + ")");
        nodeIndexMap.put(counter, node.getId());
        counter++;

        if (node.getModifiers() != null) {
            for (ExerciseModifierBean mod : node.getModifiers()) {
                printer.printInfo(indent + "  * Modificatore: " + mod.getName() + " = " + mod.getValue());
            }
        }

        if (node.getFlowDecorators() != null) {
            for (FlowDecoratorBean dec : node.getFlowDecorators()) {
                printer.printInfo(indent + "  * Decoratore: " + dec.getType().name() + " = " + dec.getValue());
            }
        }

        if (node.getChildren() != null) {
            for (PlanNodeBean child : node.getChildren()) {
                counter = printNode(child, counter, depth + 1);
            }
        }
        return counter;
    }

    private void addFromToolBox(int sessionDay) {
        String targetParentId = null;
        if (!nodeIndexMap.isEmpty()) {
            String q = reader.readString("Vuoi aggiungere l'elemento sotto un nodo esistente? (s/n): ");
            if (q.equalsIgnoreCase("s")) {
                int nodeIdx = reader.readInt("Indice del nodo padre: ", 1, nodeIndexMap.size());
                targetParentId = nodeIndexMap.get(nodeIdx);
            }
        }
        if (targetParentId == null) {
            targetParentId = "SESSION_" + sessionDay;
        }

        int targetIndex = reader.readInt("Posizione dell'esercizio nella scheda (0 = primo; 100 = ultimo): ", 0, 100);

        printer.printMenu("Toolbox", List.of(
                "Aggiungi Blocco Generico",
                "Aggiungi Protocollo",
                "Aggiungi Esercizio dalla Libreria"
        ));
        int choice = reader.readInt( SCELTA, 1, 3);

        switch (choice) {
            case 1 -> planManager.addBlockFromToolbox("Nuovo Blocco", targetParentId, targetIndex);
            case 2 -> {
                List<PlanNodeBean> protocols = planManager.getProtocolBlockLibraryCache();
                if (protocols == null || protocols.isEmpty()) {
                    printer.printInfo("Nessun protocollo disponibile.");
                    return;
                }

                Optional<PlanNodeBean> selected = reader.selectFrom("Seleziona Protocollo:", protocols, PlanNodeBean::getName);
                if (selected.isPresent()) {
                    String pName = selected.get().getName();
                    Map<String, String> defaultParams = planManager.getDefaultProtocolParameters(pName);
                    planManager.addProtocolBlockFromToolbox(pName, defaultParams, targetParentId, targetIndex);
                }
            }
            case 3 -> {
                List<ExerciseDescriptionBean> exercises = exerciseManager.getExercisesAsync(null)
                        .exceptionally(e -> {
                            printer.printException("Errore durante il recupero della libreria di esercizi:", e);
                            return List.of();
                        }).join();

                if (exercises == null || exercises.isEmpty()) {
                    printer.printInfo("Libreria esercizi vuota.");
                    return;
                }

                Optional<ExerciseDescriptionBean> selected = reader.selectFrom("Seleziona Esercizio:", exercises, ExerciseDescriptionBean::getName);
                if (selected.isPresent()) {
                    planManager.addExerciseFromToolbox(selected.get().getExerciseId(), targetParentId, targetIndex);
                }
            }
            default -> {
                // niente
            }
        }

    }

    private void manageNode(String nodeId) {
        boolean nodeRunning = true;
        while (nodeRunning) {
            printer.printHeader("GESTIONE NODO: " + planManager.getNodeName(nodeId));

            printer.printMenu(null, List.of(
                    BACK_LABEL,
                    "Rinomina Nodo",
                    "Elimina Nodo",
                    "Duplica Nodo",
                    "Svuota Nodo",
                    "Cambia Esercizio Associato",
                    "Modifica Parametri Protocollo",
                    "Aggiungi Modificatore/Decoratore (Badge)"
            ));
            int choice = reader.readInt( SCELTA, 1, 8);

            switch (choice) {
                case 1 -> nodeRunning = false;
                case 2 -> planManager.renameNode(nodeId, reader.readString("Nuovo nome: "));
                case 3 -> {
                    planManager.removeNode(nodeId);
                    nodeRunning = false;
                }
                case 4 -> {
                    planManager.duplicateNode(nodeId);
                    nodeRunning = false;
                }
                case 5 -> planManager.emptyNode(nodeId);
                case 6 -> {
                    List<ExerciseDescriptionBean> exercises = exerciseManager.getExercisesAsync(null)
                            .exceptionally(e -> {
                                printer.printException("Errore durante il recupero della libreria di esercizi:", e);
                                return List.of();
                            }).join();

                    if (exercises != null && !exercises.isEmpty()) {
                        reader.selectFrom("Seleziona nuovo esercizio:", exercises, ExerciseDescriptionBean::getName)
                                .ifPresent(e -> planManager.changeExerciseResource(nodeId, e.getExerciseId()));
                    }
                }
                case 7 -> modificaParametriProtocollo(nodeId);
                case 8 -> aggiungiBadge(nodeId);
                default -> {
                    //niente pt.2
                }
            }
        }
    }

    private void modificaParametriProtocollo(String nodeId) {
        Map<String, String> params = planManager.getProtocolParameters(nodeId);
        if (params == null || params.isEmpty()) {
            printer.printInfo("Il nodo non supporta parametri o non ne ha.");
            return;
        }
        Map<String, String> newParams = new HashMap<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String pVal = reader.readString("Parametro " + entry.getKey() + " [" + entry.getValue() + "]: ");
            if (pVal.isEmpty()) pVal = entry.getValue();
            newParams.put(entry.getKey(), pVal);
        }
        planManager.updateProtocolParameters(nodeId, newParams);
    }

    private void aggiungiBadge(String nodeId) {
        printer.printMenu("Aggiungi Badge", List.of(
                "Aggiungi Modificatore (es. REPS, WEIGHT, TUT)",
                "Aggiungi Decoratore (es. Rest, Loop, Progression)",
                "Annulla"
        ));
        int choice = reader.readInt(SCELTA, 1, 3);

        try {
            if (choice == 1) {
                if (!planManager.isExerciseNode(nodeId)) {
                    printer.printInfo("I modificatori possono essere aggiunti solo ai nodi Esercizio.");
                    reader.waitForEnter();
                    return;
                }
                String modifierType = reader.readString("Tipo (REPS, WEIGHT, TUT, RPE...): ");
                String value = reader.readString("Valore: ");
                planManager.addModifierFromToolbox(modifierType.toUpperCase(), value, nodeId);
            } else if (choice == 2) {
                String decoratorType = reader.readString("Tipo Decoratore (Rest, Loop, Interval, Progression, Time Limit): ");
                String value = reader.readString("Valore: ");
                planManager.addDecoratorFromToolbox(decoratorType, value, nodeId);
            }
        } catch (Exception e) {
            printer.printException("Errore nell'aggiunta del badge:", e);
            reader.waitForEnter();
        }
    }

}
