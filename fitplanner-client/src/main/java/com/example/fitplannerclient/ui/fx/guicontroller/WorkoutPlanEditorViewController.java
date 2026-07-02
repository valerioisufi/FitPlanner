package com.example.fitplannerclient.ui.fx.guicontroller;

import com.example.fitplannerclient.bean.plan.*;
import com.example.fitplannerclient.controller.exercise.ExerciseLibraryManager;
import com.example.fitplannerclient.controller.plan.editor.EditWorkoutPlanManager;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.ui.fx.GuiController;
import com.example.fitplannerclient.ui.fx.GuiManager;
import com.example.fitplannerclient.ui.fx.Navigator;
import com.example.fitplannerclient.ui.fx.event.PlanNodeEvent;
import com.example.fitplannerclient.ui.fx.view.plan.editor.WorkoutPlanEditorView;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
import javafx.application.Platform;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class WorkoutPlanEditorViewController implements GuiController {

    private static final Logger logger = LoggerFactory.getLogger(WorkoutPlanEditorViewController.class);
    private static final String MODIFIER_BADGE_TYPE = "MODIFIER";

    private final BorderPane mainPane;
    private final WorkoutPlanEditorView view;

    private WorkoutPlanBean activePlan;
    private WorkoutSessionBean activeSession;
    private final EditWorkoutPlanManager editWorkoutPlanManager;
    private final ExerciseLibraryManager exerciseManager;
    private final GuiManager guiManager;

    private final WorkoutPlanObserver observer;

    public WorkoutPlanEditorViewController(String planIdToEdit, boolean copyOfExisting, EditWorkoutPlanManager editWorkoutPlanManager, ExerciseLibraryManager exerciseManager, GuiManager guiManager) {
        this.editWorkoutPlanManager = editWorkoutPlanManager;
        this.exerciseManager = exerciseManager;
        this.guiManager = guiManager;

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
                    guiManager.showExceptionError(
                                "Errore nella creazione del piano:", ex);
                        return null;
                    });
        } else {
            this.editWorkoutPlanManager.editExistingPlan(planIdToEdit, copyOfExisting)
                    .exceptionally(ex -> {
                    guiManager.showExceptionError(
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

        this.view.setOnShowModalRequested(modalContent -> guiManager.showModal(modalContent));
        this.view.setOnHideModalRequested(() -> guiManager.hideModal());

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
                handleNodeReordered(event);
            } else if (event.getEventType() == PlanNodeEvent.BADGE_REORDERED) {
                handleBadgeReordered(event);
            } else if (event.getEventType() == PlanNodeEvent.EDIT_NAME_CLICKED) {
                handleEditNameClicked(event);
            } else if (event.getEventType() == PlanNodeEvent.EDIT_BADGE_CLICKED) {
                handleEditBadgeClicked(event);
            } else if (event.getEventType() == PlanNodeEvent.CHANGE_EXERCISE_REQUESTED) {
                handleChangeExerciseRequested(event);
            } else if (event.getEventType() == PlanNodeEvent.DUPLICATE_NODE_REQUESTED) {
                editWorkoutPlanManager.duplicateNode(event.getNodeId());
            } else if (event.getEventType() == PlanNodeEvent.EMPTY_NODE_REQUESTED) {
                editWorkoutPlanManager.emptyNode(event.getNodeId());
            } else if (event.getEventType() == PlanNodeEvent.EDIT_PROTOCOL_PARAMETERS_REQUESTED) {
                handleEditProtocolParameters(event);
            }
            event.consume();
        });

        this.view.setOnUndoClicked(editWorkoutPlanManager::undo);
        this.view.setOnRedoClicked(editWorkoutPlanManager::redo);
    }

    private void handleNodeReordered(PlanNodeEvent event) {
        if (event.isCopy()) {
            editWorkoutPlanManager.copyNode(event.getNodeId(), event.getTargetParentId(), event.getTargetIndex());
        } else {
            editWorkoutPlanManager.moveNode(event.getNodeId(), event.getTargetParentId(), event.getTargetIndex());
        }
    }

    private void handleBadgeReordered(PlanNodeEvent event) {
        if (MODIFIER_BADGE_TYPE.equals(event.getBadgeType())) {
            if (event.isCopy()) {
                editWorkoutPlanManager.copyModifier(event.getSourceNodeId(), event.getNodeId(), event.getSourceIndex());
            } else {
                editWorkoutPlanManager.moveModifier(event.getSourceNodeId(), event.getNodeId(), event.getSourceIndex());
            }

        } else {
            FlowDecoratorBean bean = (FlowDecoratorBean) event.getBadgeData();
            if (event.isCopy()) {
                editWorkoutPlanManager.copyDecorator(bean.getId(), event.getNodeId(), event.getTargetIndex());
            } else {
                editWorkoutPlanManager.moveDecorator(bean.getId(), event.getNodeId(), event.getTargetIndex());
            }
        }

    }

    private void handleEditNameClicked(PlanNodeEvent event) {
        String name = editWorkoutPlanManager.getNodeName(event.getNodeId());

        if (name != null) {
            this.view.getEditNodeModal().setInitialName(name);
            this.view.getEditNodeModal().setOnSaveAction(newName -> {
                editWorkoutPlanManager.renameNode(event.getNodeId(), newName);
                guiManager.hideModal();
            });
            guiManager.showModal(this.view.getEditNodeModal());
        }
    }

    private void handleEditBadgeClicked(PlanNodeEvent event) {
        BadgeComponent badge = (BadgeComponent) event.getBadgeData();
        String targetScopeId = MODIFIER_BADGE_TYPE.equals(event.getBadgeType()) ? event.getNodeId() : badge.getBadgeId();
        List<String> vars = editWorkoutPlanManager.getAvailableVariablesForNode(targetScopeId);

        this.view.getEditBadgeModal().setInitialData(badge.getBadgeType(), badge.getName(), badge.getValue(), vars);
        this.view.getEditBadgeModal().setOnSaveAction((newValue) -> {
            if (MODIFIER_BADGE_TYPE.equals(event.getBadgeType())) {
                editWorkoutPlanManager.updateModifier(event.getNodeId(), badge.getName(), newValue);
            } else {
                editWorkoutPlanManager.updateDecorator(badge.getBadgeId(), newValue);
            }
            guiManager.hideModal();
        });
        guiManager.showModal(this.view.getEditBadgeModal());
    }

    private void handleChangeExerciseRequested(PlanNodeEvent event) {
        this.view.getSelectExerciseModal().setOnSaveAction(ex -> {
            editWorkoutPlanManager.changeExerciseResource(event.getNodeId(), ex.getExerciseId());
            guiManager.hideModal();
        });
        guiManager.showModal(this.view.getSelectExerciseModal());
    }

    private void handleEditProtocolParameters(PlanNodeEvent event) {
        Map<String, String> params = editWorkoutPlanManager.getProtocolParameters(event.getNodeId());

        if (params != null) {
            String name = editWorkoutPlanManager.getNodeName(event.getNodeId());
            this.view.getEditProtocolModal().setInitialData(name, params);
            this.view.getEditProtocolModal().setOnSaveAction(newParams -> {
                editWorkoutPlanManager.updateProtocolParameters(event.getNodeId(), newParams);
                guiManager.hideModal();
            });
            guiManager.showModal(this.view.getEditProtocolModal());
        }
    }

    private void handleToolboxDrop(PlanNodeEvent event) {
        String payload = event.getPayload();
        String targetParentId = event.getTargetParentId();
        int targetIndex = event.getTargetIndex();

        if (payload.equals("EXERCISE")) {
            this.view.getSelectExerciseModal().setOnSaveAction(ex -> {
                editWorkoutPlanManager.addExerciseFromToolbox(ex.getExerciseId(), targetParentId, targetIndex);
                guiManager.hideModal();
            });
            guiManager.showModal(this.view.getSelectExerciseModal());

        } else if (payload.equals("BLOCK")) {
            editWorkoutPlanManager.addBlockFromToolbox("Nuovo Blocco", targetParentId, targetIndex);

        } else if (payload.startsWith("PROTOCOL:")) {
            String protocolName = payload.substring("PROTOCOL:".length());

            Map<String, String> initialParams = editWorkoutPlanManager.getDefaultProtocolParameters(protocolName);
            this.view.getEditProtocolModal().setInitialData(protocolName, initialParams);
            this.view.getEditProtocolModal().setOnSaveAction(params -> {
                editWorkoutPlanManager.addProtocolBlockFromToolbox(protocolName, params, targetParentId, targetIndex);
                guiManager.hideModal();
            });
            guiManager.showModal(this.view.getEditProtocolModal());

        } else if (payload.startsWith("MODIFIER:")) {
            if (!editWorkoutPlanManager.isExerciseNode(targetParentId)) {
                guiManager.showNotification(GuiManager.NotificationType.ERROR, "I Modifier possono essere aggiunti solo agli Esercizi.");
                return;
            }

            String type = payload.substring("MODIFIER:".length());
            List<String> vars = editWorkoutPlanManager.getAvailableVariablesForNode(targetParentId);
            this.view.getEditBadgeModal().setInitialData(BadgeComponent.BadgeType.MODIFIER, type, "", vars);
            this.view.getEditBadgeModal().setOnSaveAction(val -> {
                editWorkoutPlanManager.addModifierFromToolbox(type.toUpperCase(), val, targetParentId);
                guiManager.hideModal();
            });
            guiManager.showModal(this.view.getEditBadgeModal());

        } else if (payload.startsWith("DECORATOR:")) {
            String type = payload.substring("DECORATOR:".length());
            List<String> vars = editWorkoutPlanManager.getAvailableVariablesForNode(targetParentId);
            this.view.getEditBadgeModal().setInitialData(BadgeComponent.BadgeType.DECORATOR, type, "", vars);
            this.view.getEditBadgeModal().setOnSaveAction(val -> {
                editWorkoutPlanManager.addDecoratorFromToolbox(type, val, targetParentId);
                guiManager.hideModal();
            });
            guiManager.showModal(this.view.getEditBadgeModal());
        }
    }

    private void loadLibrary() {
        exerciseManager.getExercisesAsync(null)
                .thenAccept(exercises -> Platform.runLater(() -> view.setExercises(exercises)))
                .exceptionally(ex -> {
                    logger.error("Errore caricamento esercizi", ex);
                    return null;
                });

        Platform.runLater(() -> {
            editWorkoutPlanManager.buildProtocolBlockLibrary();
            view.setProtocolBlocks(editWorkoutPlanManager.getProtocolBlockLibraryCache());

            view.setDecorators(List.of("Rest", "Loop", "Time Limit", "Interval", "Progression"));
            view.setModifiers(List.of("REPS", "WEIGHT", "TUT", "RPE"));
        });
    }


    private void savePlan() {
        if (activePlan == null) return;

        editWorkoutPlanManager.savePlan()
                .thenRun(() -> {
                    guiManager.showNotification(GuiManager.NotificationType.SUCCESS, "Piano salvato con successo.");
                    Navigator.getInstance().goToPlanManagement();
                })
                .exceptionally(ex -> {
                    guiManager.showExceptionError("Errore nel salvataggio del piano:", ex);
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
        guiManager.hideModal();
    }

}
