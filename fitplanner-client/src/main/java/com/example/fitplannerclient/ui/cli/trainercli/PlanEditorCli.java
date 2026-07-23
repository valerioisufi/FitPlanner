package com.example.fitplannerclient.ui.cli.trainercli;

import com.example.fitplannerclient.bean.exercise.ExerciseDescriptionBean;
import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.editor.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.ui.cli.AbstractCliView;
import com.example.fitplannerclient.ui.cli.CliView;

import java.util.*;

public class PlanEditorCli extends AbstractCliView {

    private static final String SCELTA= "Scelta: ";
    private static final String BACK_LABEL = "Indietro";
    private static final String NO_NAME = "Senza Nome";
    private static final String NODE_RENAME = "Rinomina Nodo";

    private EditWorkoutPlanManager planManager;
    private ExerciseLibraryManager exerciseManager;

    private WorkoutPlanObserver observer;

    private final String planId;
    private final boolean isClone;
    private WorkoutPlanBean activePlan;

    private final Map<Integer, PlanNodeBean> nodeIndexMap = new HashMap<>();

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

        if (!createPlan()) {
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
                        boolean success = planManager.savePlan()
                                .handle((res, ex) -> {
                                    if (ex != null) {
                                        printer.printException("Errore durante il salvataggio del piano: ", ex);
                                        reader.waitForEnter();
                                        return false;
                                    }
                                    return true;
                                }).join();

                        if (success) {
                            printer.printSuccess("Piano salvato!");
                            running = false;
                        }
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

    private boolean createPlan() {
        boolean created;
        if (planId == null) {
            created = planManager.createNewPlan()
                    .handle((res, ex) -> {
                        if (ex != null) {
                            printer.printException("Errore nella creazione del piano: ", ex);
                            return false;
                        }
                        return true;
                    }).join();
        } else {
            created = planManager.editExistingPlan(planId, isClone)
                    .handle((res, ex) -> {
                        if (ex != null) {
                            printer.printException("Errore nell'apertura del piano da modificare: ", ex);
                            return false;
                        }
                        return true;
                    }).join();
        }

        return created;
    }

    private Optional<WorkoutSessionBean> chooseSession() {
        if (activePlan.getSessions() == null || activePlan.getSessions().isEmpty()) {
            printer.printInfo("Nessuna sessione presente. Aggiungi prima una sessione.");
            reader.waitForEnter();
            return Optional.empty();
        }

        return reader.selectFrom("Seleziona la sessione da modificare: ",
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
                case 2 -> addFromToolBox(session);
                case 3 -> {
                    if (nodeIndexMap.isEmpty()) {
                        printer.printInfo("Albero vuoto.");
                        reader.waitForEnter();
                    } else {
                        int nodeIdx = reader.readInt("Inserisci l'indice del nodo [1-" + nodeIndexMap.size() + "]: ", 1, nodeIndexMap.size());
                        PlanNodeBean nodeBean = nodeIndexMap.get(nodeIdx);
                        if (nodeBean != null) {
                            manageNode(nodeBean.getId(), nodeBean.getName(), nodeBean.getType(), nodeBean.getParameters());
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

    private PlanNodeBean findNodeBean(String nodeId) {
        if (activePlan == null || activePlan.getSessions() == null) return null;

        for (WorkoutSessionBean session : activePlan.getSessions()) {
            if (session.getPlanRoot() != null) {
                PlanNodeBean found = searchNodeBean(session.getPlanRoot(), nodeId);
                if (found != null) return found;
            }
        }

        return null;
    }

    private PlanNodeBean searchNodeBean(PlanNodeBean root, String nodeId) {
        if (root.getId().equals(nodeId)) return root;

        if (root.getChildren() != null) {
            for (PlanNodeBean child : root.getChildren()) {
                PlanNodeBean found = searchNodeBean(child, nodeId);
                if (found != null) return found;
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
            printer.printLn("  (Vuota)");
        }
    }

    private int printNode(PlanNodeBean node, int counter, int depth) {
        int currentDepth = depth;

        if (node.getFlowDecorators() != null) {
            for (FlowDecoratorBean dec : node.getFlowDecorators()) {
                String decIndent = "   ".repeat(currentDepth);
                printer.printLn(decIndent + "└── " + dec.getType().name() + " = " + dec.getValue() + " [DECORATORE]");
                currentDepth++;
            }
        }

        String nodeIndent = "   ".repeat(currentDepth);
        String nodeType = node.getType() != null ? node.getType().toString() : "Sconosciuto";

        printer.printLn(nodeIndent + "└── [" + counter + "] " + node.getName() + " (" + nodeType + ")");
        nodeIndexMap.put(counter, node);
        counter++;

        if (node.getModifiers() != null) {
            for (ExerciseModifierBean mod : node.getModifiers()) {
                printer.printLn(nodeIndent + "      * " + mod.getName() + " = " + mod.getValue() + " [MODIFICATORE]");
            }
        }

        if (node.getChildren() != null) {
            for (PlanNodeBean child : node.getChildren()) {
                counter = printNode(child, counter, currentDepth + 1);
            }
        }
        return counter;
    }

    private void addFromToolBox(WorkoutSessionBean session) {
        String targetParentId = null;
        if (!nodeIndexMap.isEmpty()) {
            String q = reader.readString("Vuoi aggiungere un elemento dentro un blocco esistente? (s/n): ");
            if (q.equalsIgnoreCase("s")) {
                int nodeIdx = reader.readInt("Indice del blocco padre: ", 1, nodeIndexMap.size());
                targetParentId = nodeIndexMap.get(nodeIdx).getId();
            }
        }
        if (targetParentId == null) {
            targetParentId = session.getPlanRoot().getId();
        }

        int targetIndex = reader.readInt("Posizione dell'esercizio nel blocco scelto: ", 0, 999);

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

    private void manageNode(String nodeId, String nodeName, NodeType nodeType, Map<String, String> nodeParams) {
        boolean nodeRunning = true;
        while (nodeRunning) {
            printer.printHeader("GESTIONE NODO: " + nodeName);



            boolean isBlock = nodeType == NodeType.BLOCK;
            boolean isProtocol = nodeType == NodeType.PROTOCOL;
            boolean isExercise = nodeType == NodeType.EXERCISE;

            List<String> menuOptions = getMenuOptions(isBlock, isProtocol, isExercise);

            printer.printMenu(null, menuOptions);
            int choice = reader.readInt( SCELTA, 1, menuOptions.size());
            String option = menuOptions.get(choice - 1);

            switch (option) {
                case BACK_LABEL -> nodeRunning = false;
                case NODE_RENAME -> planManager.renameNode(nodeId, reader.readString("Nuovo nome: "));
                case "Svuota Nodo" -> planManager.emptyNode(nodeId);
                case "Elimina Nodo" -> {
                    planManager.removeNode(nodeId);
                    nodeRunning = false;
                }
                case "Duplica Nodo" -> {
                    planManager.duplicateNode(nodeId);
                    nodeRunning = false;
                }
                case "Cambia Esercizio Associato" -> {
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
                case "Modifica Parametri Protocollo" -> editProtocolParameters(nodeId);
                case "Aggiungi Modificatore" -> addModifier(nodeId);
                case "Rimuovi Modificatore" -> removeModifier(nodeId);
                case "Aggiungi Decoratore" -> addDecorator(nodeId);
                case "Rimuovi Decoratore" -> removeDecorator(nodeId);
                default -> printer.printInfo("Scelta non valida.");
            }
        }
    }

    private static List<String> getMenuOptions(boolean isBlock, boolean isProtocol, boolean isExercise) {
        List<String> menuOptions = new ArrayList<>();
        menuOptions.add(BACK_LABEL);

        if (isBlock || isProtocol) {
            menuOptions.add(NODE_RENAME);
            menuOptions.add("Svuota Nodo");
        }
        if (isExercise) {
            menuOptions.add("Cambia Esercizio Associato");
            menuOptions.add("Aggiungi Modificatore");
            menuOptions.add("Rimuovi Modificatore");
        }
        if (isProtocol) {
            menuOptions.add("Modifica Parametri Protocollo");
        }

        menuOptions.add("Elimina Nodo");
        menuOptions.add("Duplica Nodo");
        menuOptions.add("Aggiungi Decoratore");
        menuOptions.add("Rimuovi Decoratore");
        return menuOptions;
    }

    private void editProtocolParameters(String nodeId) {
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

    private void addModifier(String nodeId) {
        List<String> opzioniMod = List.of("REPS", "WEIGHT", "TUT", "RPE");
        Optional<String> sceltoMod = reader.selectFrom("Seleziona Tipo Modificatore:", opzioniMod, s -> s);

        if (sceltoMod.isEmpty()) return;
        String modifierType = sceltoMod.get();

        String value = reader.readString("Valore: ");
        planManager.addModifierFromToolbox(modifierType.toUpperCase(), value, nodeId);
    }

    private void addDecorator(String nodeId) {
        PlanNodeBean nodeBean = findNodeBean(nodeId);
        String targetId = nodeId;

        if (nodeBean != null && nodeBean.getFlowDecorators() != null && !nodeBean.getFlowDecorators().isEmpty()) {
            String q = reader.readString("Vuoi avvolgere un decoratore esistente anziché il nodo? (s/n): ");

            if (q.equalsIgnoreCase("s")) {
                Optional<FlowDecoratorBean> selected = reader.selectFrom("Seleziona decoratore da avvolgere: ",
                        nodeBean.getFlowDecorators(),
                        d -> d.getType().name() + " = " + d.getValue());
                if (selected.isPresent()) {
                    targetId = selected.get().getId();
                }
            }
        }

        List<String> opzioniDec = List.of("Rest", "Loop", "Interval", "Progression", "Time Limit");
        Optional<String> sceltoDec = reader.selectFrom("Seleziona Tipo Decoratore:", opzioniDec, s -> s);

        if (sceltoDec.isEmpty()) return;
        String decoratorType = sceltoDec.get();

        String value2 = reader.readString("Valore: ");
        planManager.addDecoratorFromToolbox(decoratorType, value2, targetId);
    }

    private void removeModifier(String nodeId) {
        List<String> opzioniMod = List.of("REPS", "WEIGHT", "TUT", "RPE");
        Optional<String> sceltoMod = reader.selectFrom("Seleziona Tipo Modificatore da rimuovere:", opzioniMod, s -> s);

        if (sceltoMod.isEmpty()) return;
        String modifierType = sceltoMod.get().toUpperCase();

        planManager.removeModifier(nodeId, modifierType);
        printer.printInfo("Modificatore rimosso.");
    }

    private void removeDecorator(String nodeId) {
        PlanNodeBean nodeBean = findNodeBean(nodeId);
        if (nodeBean == null || nodeBean.getFlowDecorators() == null || nodeBean.getFlowDecorators().isEmpty()) {
            printer.printInfo("Nessun decoratore presente su questo nodo.");
            reader.waitForEnter();
            return;
        }

        Optional<FlowDecoratorBean> selected = reader.selectFrom("Seleziona decoratore da rimuovere:",
                nodeBean.getFlowDecorators(),
                d -> d.getType().name() + " = " + d.getValue());

        if (selected.isPresent()) {
            planManager.removeDecorator(selected.get().getId());
            printer.printInfo("Decoratore rimosso.");
        }
    }

}
