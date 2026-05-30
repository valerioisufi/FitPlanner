package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.controller.plan.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.event.PlanNodeEvent;
import com.example.fitplannerclient.ui.fx.view.plan.editor.WorkoutPlanEditorView;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import java.util.Map;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkoutPlanEditorViewController implements GuiController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutPlanEditorViewController.class);

    private final BorderPane mainPane;
    private final WorkoutPlanEditorView view;

    private WorkoutPlanBean activePlan;
    private WorkoutSessionBean activeSession;
    private final EditWorkoutPlanManager editWorkoutPlanManager;
    private final ExerciseLibraryManager exerciseManager;

    private final WorkoutPlanObserver observer;

    public WorkoutPlanEditorViewController(String planIdToEdit, boolean copyOfExisting, EditWorkoutPlanManager editWorkoutPlanManager, ExerciseLibraryManager exerciseManager) {
        this.editWorkoutPlanManager = editWorkoutPlanManager;
        this.exerciseManager = exerciseManager;

        this.view = new WorkoutPlanEditorView();
        this.mainPane = new BorderPane();
        this.mainPane.setCenter(this.view);

        observer = () -> editWorkoutPlanManager.getPlanAsync().thenAccept(planBean -> {
            Platform.runLater(() -> {
                activePlan = planBean;
                view.setPlan(activePlan);
            });
        });
        editWorkoutPlanManager.addObserver(observer);

        if (planIdToEdit == null) {
            this.editWorkoutPlanManager.createNewPlan()
                    .exceptionally(ex -> {
                    Navigator.getInstance().getGuiManager().showExceptionError(
                                "Errore nella creazione del piano:", ex);
                        return null;
                    });
        } else {
            this.editWorkoutPlanManager.editExistingPlan(planIdToEdit, copyOfExisting)
                    .exceptionally(ex -> {
                    Navigator.getInstance().getGuiManager().showExceptionError(
                                "Errore nel caricamento del piano:", ex);
                        return null;
                    });
        }

        // Bind callbacks
        this.view.setOnSessionSelected(session -> this.activeSession = session);
        this.view.setOnManageSessionsRequested(() -> {
            if (activePlan != null) {
                this.view.showManageSessionsModal(
                    activePlan.getCycleLength(),
                    activePlan.getSessions()
                );
            }
        });

        this.view.setOnPlanNameChanged(this.editWorkoutPlanManager::changePlanName);
        this.view.setOnCycleLengthChanged(this.editWorkoutPlanManager::changeCycleLength);
        this.view.setOnSessionNameChanged(this.editWorkoutPlanManager::updateSessionName);
        this.view.setOnSessionDayChanged(this.editWorkoutPlanManager::updateSessionDay);
        this.view.setOnSessionAdded(this.editWorkoutPlanManager::addSession);
        this.view.setOnSessionRemoved(this.editWorkoutPlanManager::removeSession);

        this.view.setOnSavePlanClicked(this::savePlan);
        this.view.setOnCancelClicked(() -> Navigator.getInstance().goToPlanManagement());

        this.view.setOnShowModalRequested(modalContent -> Navigator.getInstance().getGuiManager().showModal(modalContent));
        this.view.setOnHideModalRequested(() -> Navigator.getInstance().getGuiManager().hideModal());

        setupPlanNodeEventHandlers();

        this.view.disableEditing(false);
    }

    private void setupPlanNodeEventHandlers() {
        this.view.getPlanViewer().addEventHandler(PlanNodeEvent.ANY, event -> {
            logger.info("Ricevuto PlanNodeEvent - Type: {}, NodeId: {}, SourceId: {}, SourceIndex: {}, TargetParentId: {}, TargetIndex: {}, isCopy: {}, BadgeType: {}, Payload: {}",
                    event.getEventType().getName(), event.getNodeId(), event.getSourceNodeId(), event.getSourceIndex(),
                    event.getTargetParentId(), event.getTargetIndex(), event.isCopy(), event.getBadgeType(), event.getPayload());

            if (event.getEventType() == PlanNodeEvent.TOOLBOX_ITEM_DROPPED) {
                handleToolboxDrop(event);
            } else if (event.getEventType() == PlanNodeEvent.DELETE_NODE_REQUESTED) {
                editWorkoutPlanManager.removeNode(event.getNodeId());
            } else if (event.getEventType() == PlanNodeEvent.NODE_REORDERED) {
                if (event.isCopy()) {
                    editWorkoutPlanManager.copyNode(event.getNodeId(), event.getTargetParentId(), event.getTargetIndex());
                } else {
                    editWorkoutPlanManager.moveNode(event.getNodeId(), event.getTargetParentId(), event.getTargetIndex());
                }
            } else if (event.getEventType() == PlanNodeEvent.BADGE_REORDERED) {
                if ("MODIFIER".equals(event.getBadgeType())) {
                    if (event.isCopy()) {
                        editWorkoutPlanManager.copyModifier(event.getSourceNodeId(), event.getNodeId(), event.getSourceIndex(), event.getTargetIndex());
                    } else {
                        editWorkoutPlanManager.moveModifier(event.getSourceNodeId(), event.getNodeId(), event.getSourceIndex(), event.getTargetIndex());
                    }
                } else {
                    if (event.isCopy()) {
                        editWorkoutPlanManager.copyDecorator(event.getSourceNodeId(), event.getNodeId(), event.getSourceIndex(), event.getTargetIndex());
                    } else {
                        editWorkoutPlanManager.moveDecorator(event.getSourceNodeId(), event.getNodeId(), event.getSourceIndex(), event.getTargetIndex());
                    }
                }
            } else if (event.getEventType() == PlanNodeEvent.EDIT_NAME_CLICKED) {
                // Find node name
                PlanNodeBean node = activePlan.findNodeById(event.getNodeId());
                if (node != null) {
                    this.view.getEditNodeModal().setInitialName(node.getName());
                    this.view.getEditNodeModal().setOnSaveAction(newName -> {
                        editWorkoutPlanManager.renameNode(event.getNodeId(), newName);
                        Navigator.getInstance().getGuiManager().hideModal();
                    });
                    Navigator.getInstance().getGuiManager().showModal(this.view.getEditNodeModal());
                }
            } else if (event.getEventType() == PlanNodeEvent.EDIT_BADGE_CLICKED) {
                System.out.println("WorkoutPlanEditorViewController received EDIT_BADGE_CLICKED!");
                BadgeComponent badge = (BadgeComponent) event.getBadgeData();
                this.view.getEditBadgeModal().setInitialData(badge.getBadgeType(), badge.getName(), badge.getValue());
                this.view.getEditBadgeModal().setOnSaveAction((newName, newValue) -> {
                    if ("MODIFIER".equals(event.getBadgeType())) {
                        editWorkoutPlanManager.updateModifier(event.getNodeId(), badge.getBadgeId(), newName, newValue);
                    } else {
                        editWorkoutPlanManager.updateDecorator(event.getNodeId(), badge.getBadgeId(), newName, newValue);
                    }
                    Navigator.getInstance().getGuiManager().hideModal();
                });
                Navigator.getInstance().getGuiManager().showModal(this.view.getEditBadgeModal());
            }
            event.consume();
        });
    }

    private void handleToolboxDrop(PlanNodeEvent event) {
        String payload = event.getPayload();
        String targetParentId = event.getTargetParentId();
        int targetIndex = event.getTargetIndex();

        if (payload.equals("EXERCISE")) {
            this.view.getSelectExerciseModal().setOnSaveAction(ex -> {
                editWorkoutPlanManager.addExerciseFromToolbox(ex.getExerciseId(), targetParentId, targetIndex);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getSelectExerciseModal());

        } else if (payload.equals("BLOCK")) {
            editWorkoutPlanManager.addBlockFromToolbox("Nuovo Blocco", targetParentId, targetIndex);

        } else if (payload.startsWith("PROTOCOL:")) {
            String protocolName = payload.substring("PROTOCOL:".length());
            
            Map<String, String> initialParams = editWorkoutPlanManager.getDefaultProtocolParameters(protocolName);
            this.view.getEditProtocolModal().setInitialData(protocolName, initialParams);
            this.view.getEditProtocolModal().setOnSaveAction(params -> {
                editWorkoutPlanManager.addProtocolBlockFromToolbox(protocolName, params, targetParentId, targetIndex);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getEditProtocolModal());

        } else if (payload.startsWith("MODIFIER:")) {
            PlanNodeBean targetNode = activePlan.findNodeById(targetParentId);
            if (targetNode == null || targetNode.getType() != NodeType.EXERCISE) {
                Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.ERROR, "I Modifier possono essere aggiunti solo agli Esercizi.");
                return;
            }

            String type = payload.substring("MODIFIER:".length());
            this.view.getEditBadgeModal().setInitialData(BadgeComponent.BadgeType.MODIFIER, type, "");
            this.view.getEditBadgeModal().setOnSaveAction((name, val) -> {
                editWorkoutPlanManager.addModifierFromToolbox(name.toUpperCase(), val, targetParentId);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getEditBadgeModal());

        } else if (payload.startsWith("DECORATOR:")) {
            String type = payload.substring("DECORATOR:".length());
            this.view.getEditBadgeModal().setInitialData(BadgeComponent.BadgeType.DECORATOR, type, "");
            this.view.getEditBadgeModal().setOnSaveAction((name, val) -> {
                editWorkoutPlanManager.addDecoratorFromToolbox(name, val, targetParentId);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getEditBadgeModal());
        }
    }

    private void loadLibrary() {
        exerciseManager.getExercisesAsync(null)
                .thenAccept(exercises -> Platform.runLater(() -> view.setExercises(exercises)))
                .exceptionally(ex -> {
                    System.err.println("Errore caricamento esercizi: " + ex.getMessage());
                    return null;
                });

        Platform.runLater(() -> {
            editWorkoutPlanManager.buildProtocolBlockLibrary();
            view.setProtocolBlocks(editWorkoutPlanManager.getProtocolBlockLibraryCache());
            
            view.setDecorators(List.of("Rest", "Loop", "Time Limit", "Interval", "Progression"));
            view.setModifiers(List.of("Sets", "Reps", "RPE", "Tempo", "Weight"));
        });
    }


    private void savePlan() {
        if (activePlan == null) return;

        editWorkoutPlanManager.savePlan()
                .thenRun(() -> {
                    Navigator.getInstance().getGuiManager().showNotification(GuiManager.NotificationType.SUCCESS, "Piano salvato con successo.");
                    Navigator.getInstance().goToPlanManagement();
                })
                .exceptionally(ex -> {
                    Navigator.getInstance().getGuiManager().showExceptionError("Errore nel salvataggio del piano:", ex);
                    return null;
                });

    }

    @Override
    public Pane getView() {
        return this.mainPane;
    }

    @Override
    public void start() {
        this.view.setPlan(this.activePlan);

        if (activePlan != null && activePlan.getSessions() != null && !activePlan.getSessions().isEmpty()) {
            this.activeSession = activePlan.getSessions().getFirst();
        }

        loadLibrary();
    }

    @Override
    public void stop() {
        editWorkoutPlanManager.removeObserver(observer);
        Navigator.getInstance().getGuiManager().hideModal();
    }

}
