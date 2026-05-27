package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.controller.profile.ProfileManager;
import com.example.fitplannerclient.ui.fx.view.WorkoutExecutionView;
import com.example.fitplannerclient.service.facade.SessionLogFacade;
import com.example.fitplannercommon.ExerciseLogDTO;
import com.example.fitplannercommon.ExerciseSetDTO;
import com.example.fitplannercommon.SessionLogDTO;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class WorkoutExecutionViewController implements GuiController {

    private final BorderPane mainPane;
    private final WorkoutExecutionView view;
    private final HeaderViewController headerViewController;

    private final WorkoutSessionBean sessionBean;
    private final List<PlanNodeBean> exerciseNodes = new ArrayList<>();
    private final List<ExerciseLogDTO> exerciseLogs = new ArrayList<>();
    private int currentExerciseIndex = 0;
    private final SessionLogFacade sessionLogFacade;
    private final ProfileManager profileManager;

    public WorkoutExecutionViewController(WorkoutSessionBean session, SessionLogFacade sessionLogFacade, ProfileManager profileManager) {
        this.sessionBean = session;
        this.sessionLogFacade = sessionLogFacade;
        this.profileManager = profileManager;
        this.headerViewController = new HeaderViewController(1, profileManager); // "Piano" highlight
        this.view = new WorkoutExecutionView(headerViewController.getView());

        this.mainPane = new BorderPane();
        this.mainPane.setCenter(this.view);

        this.view.setOnFinishAction(this::handleNextOrFinish);
    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void start() {
        collectExerciseNodes(sessionBean.getPlanRoot());

        if (exerciseNodes.isEmpty()) {
            Platform.runLater(() -> Navigator.getInstance().goHome());
        } else {
            loadExercise(0);
        }
    }

    @Override
    public void stop() {}

    private void collectExerciseNodes(PlanNodeBean node) {
        if (node == null) return;
        if (node.getType() == NodeType.EXERCISE) {
            exerciseNodes.add(node);
        }
        if (node.getChildren() != null) {
            for (PlanNodeBean child : node.getChildren()) {
                collectExerciseNodes(child);
            }
        }
    }

    private void loadExercise(int index) {
        this.currentExerciseIndex = index;
        PlanNodeBean exNode = exerciseNodes.get(index);

        view.clearSets();

        int sets = 3;
        int reps = 10;
        if (exNode.getModifiers() != null) {
            for (ExerciseModifierBean mod : exNode.getModifiers()) {
                if ("Sets".equalsIgnoreCase(mod.getName())) {
                    try { sets = Integer.parseInt(mod.getValue()); } catch (Exception ignored) {}
                }
                if ("Reps".equalsIgnoreCase(mod.getName())) {
                    try { reps = Integer.parseInt(mod.getValue()); } catch (Exception ignored) {}
                }
            }
        }

        for (int i = 1; i <= sets; i++) {
            view.addSetRow(i, "0.0", String.valueOf(reps));
        }

        String name = exNode.getName();
        String history = "Ultima sessione: 3 set x " + reps + " reps con peso target.";

        String instructions = "1. Preparazione:\n" +
                "   • Posizionati correttamente per iniziare l'esercizio " + name + ".\n" +
                "   • Assicurati di avere i piedi ben saldi e la schiena in posizione neutra.\n\n" +
                "2. Esecuzione:\n" +
                "   • Svolgi il movimento controllando la fase eccentrica ed esplodendo nella fase concentrica.\n" +
                "   • Concentrati sulla contrazione muscolare target.\n\n" +
                "3. Conclusione:\n" +
                "   • Riponi i pesi in sicurezza e riposa per il tempo stabilito.";

        view.setExerciseDetails(name, history);
        view.setInstructions(name, instructions);
        view.setVideoUrl("https://www.youtube.com/embed/5n4MBR6yW4o");

        if (index == exerciseNodes.size() - 1) {
            view.setFinishButtonText("Termina Allenamento");
        } else {
            view.setFinishButtonText("Esercizio Successivo");
        }
    }

    private void handleNextOrFinish() {
        List<WorkoutExecutionView.SetData> loggedSets = view.getLoggedSets();
        List<ExerciseSetDTO> setDtos = new ArrayList<>();
        for (WorkoutExecutionView.SetData set : loggedSets) {
            if (set.done()) {
                setDtos.add(new ExerciseSetDTO(set.reps(), set.weight()));
            }
        }

        PlanNodeBean exNode = exerciseNodes.get(currentExerciseIndex);
        ExerciseLogDTO exLog = new ExerciseLogDTO(exNode.getName(), exNode.getId(), setDtos, 8, "Log");
        exerciseLogs.add(exLog);

        if (currentExerciseIndex < exerciseNodes.size() - 1) {
            loadExercise(currentExerciseIndex + 1);
        } else {
            finishWorkoutSession();
        }
    }

    private void finishWorkoutSession() {
        SessionLogDTO logDto = new SessionLogDTO();
        logDto.setDate(System.currentTimeMillis());
        logDto.setStatus(SessionLogDTO.SessionStatus.COMPLETED);
        
        int dayNum = 1;
        try {
//            dayNum = Integer.parseInt(sessionBean.getDay());
        } catch (Exception ignored) {}
        logDto.setWorkoutSessionDay(dayNum);
        logDto.setNotes("Allenamento completato con successo!");
        logDto.setExerciseLogs(exerciseLogs);

        sessionLogFacade.saveSessionLogAsync(logDto)
                .thenRun(() -> Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Allenamento Salvato");
                    alert.setHeaderText(null);
                    alert.setContentText("Complimenti! Il tuo allenamento è stato registrato con successo sul server.");
                    alert.showAndWait();

                    Navigator.getInstance().goHome();
                }))
                .exceptionally(ex -> {
                    Platform.runLater(() -> Navigator.getInstance().getGuiManager().showNotification(
                            GuiManager.NotificationType.ERROR, 
                            "Errore durante l'azione del timer: " + ex.getMessage()));
                    return null;
                });
    }
}
