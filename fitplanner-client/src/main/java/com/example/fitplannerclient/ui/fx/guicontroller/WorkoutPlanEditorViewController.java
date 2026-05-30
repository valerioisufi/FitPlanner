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
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory;
import java.util.Map;
import java.util.List;

public class WorkoutPlanEditorViewController implements GuiController {

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

        this.view.setOnPlanNameChanged(newName -> {
            this.editWorkoutPlanManager.changePlanName(newName);
        });

        this.view.setOnCycleLengthChanged(length -> {
            this.editWorkoutPlanManager.changeCycleLength(length);
        });

        this.view.setOnSessionNameChanged((day, newName) -> {
            this.editWorkoutPlanManager.updateSessionName(day, newName);
        });

        this.view.setOnSessionDayChanged((oldDay, newDay) -> {
            this.editWorkoutPlanManager.updateSessionDay(oldDay, newDay);
        });

        this.view.setOnSessionAdded(day -> {
            this.editWorkoutPlanManager.addSession(day);
        });

        this.view.setOnSessionRemoved(day -> {
            this.editWorkoutPlanManager.removeSession(day);
        });

        this.view.setOnSavePlanClicked(this::savePlan);
        this.view.setOnCancelClicked(() -> Navigator.getInstance().goToPlanManagement());

        this.view.setOnShowModalRequested(modalContent -> Navigator.getInstance().getGuiManager().showModal(modalContent));
        this.view.setOnHideModalRequested(() -> Navigator.getInstance().getGuiManager().hideModal());

        setupPlanNodeEventHandlers();

        this.view.disableEditing(false);
    }

    private void setupPlanNodeEventHandlers() {
        this.view.getPlanViewer().addEventHandler(PlanNodeEvent.ANY, event -> {
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
                ExerciseNode node = new ExerciseNode();
                node.setResourceId(ex.getExerciseId());
                node.addModifier(new ExerciseModifier(ModifierType.REPS, "10"));
                editWorkoutPlanManager.addNodeFromToolbox(node, targetParentId, targetIndex);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getSelectExerciseModal());

        } else if (payload.equals("BLOCK")) {
            editWorkoutPlanManager.addNodeFromToolbox(new Block("Nuovo Blocco"), targetParentId, targetIndex);

        } else if (payload.startsWith("PROTOCOL:")) {
            String protocolName = payload.substring("PROTOCOL:".length());
            
            ProtocolBlockFactory factory = new ProtocolBlockFactory();
            ProtocolBlock block = switch (protocolName) {
                case "DROP_SET" -> factory.createDropSet();
                case "SUPER_SET" -> factory.createSuperSet();
                case "GIANT_SET" -> factory.createGiantSet();
                case "CIRCUIT" -> factory.createCircuit();
                case "AMRAP" -> factory.createAMRAP();
                case "EMOM" -> factory.createEMOM();
                default -> factory.createCircuit();
            };

            this.view.getEditProtocolModal().setInitialData(protocolName, block.getParameters());
            this.view.getEditProtocolModal().setOnSaveAction(params -> {
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    block.setParameter(entry.getKey(), entry.getValue());
                }
                editWorkoutPlanManager.addNodeFromToolbox(block, targetParentId, targetIndex);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getEditProtocolModal());

        } else if (payload.startsWith("MODIFIER:")) {
            String type = payload.substring("MODIFIER:".length());
            this.view.getEditBadgeModal().setInitialData(BadgeComponent.BadgeType.MODIFIER, type, "");
            this.view.getEditBadgeModal().setOnSaveAction((name, val) -> {
                ExerciseModifier mod = new ExerciseModifier(ModifierType.valueOf(name.toUpperCase()), val);
                editWorkoutPlanManager.addModifierFromToolbox(mod, targetParentId);
                Navigator.getInstance().getGuiManager().hideModal();
            });
            Navigator.getInstance().getGuiManager().showModal(this.view.getEditBadgeModal());

        } else if (payload.startsWith("DECORATOR:")) {
            String type = payload.substring("DECORATOR:".length());
            this.view.getEditBadgeModal().setInitialData(BadgeComponent.BadgeType.DECORATOR, type, "");
            this.view.getEditBadgeModal().setOnSaveAction((name, val) -> {
                FlowDecorator dec = null;
                switch (name.toUpperCase().replace(" ", "_")) {
                    case "REST" -> dec = new RestDecorator(null, val);
                    case "LOOP" -> dec = new LoopDecorator(null, val);
                    case "TIME_LIMIT" -> dec = new TimeLimitDecorator(null, val);
                    case "INTERVAL" -> dec = new IntervalDecorator(null, val);
                    case "PROGRESSION" -> dec = new ProgressionDecorator(null, val);
                }
                if (dec != null) {
                    editWorkoutPlanManager.addDecoratorFromToolbox(dec, targetParentId);
                }
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
