package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.NodeType;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.controller.plan.command.EditorHistoryManager;
import com.example.fitplannerclient.controller.plan.command.WorkoutPlanEditorCommand;
import com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditWorkoutPlanManager {

    private final EditorHistoryManager historyManager = new EditorHistoryManager();

    private final WorkoutPlanSubject workoutPlanSubject = new WorkoutPlanSubject();
    private final WorkoutPlanApi planApi;

    private WorkoutPlan plan;
    private final List<ProtocolBlock> protocolBlockLibrary = new ArrayList<>();
    private final List<PlanNodeBean> protocolBlockLibraryCache = new ArrayList<>();

    public EditWorkoutPlanManager(WorkoutPlanApi planApi) {
        this.planApi = planApi;
    }

    public void addObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.attach(observer);
    }
    public void removeObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.detach(observer);
    }

    public WorkoutPlanBean getPlan() {
        if (plan == null) return null;

        PlanToBeanVisitor visitor = new PlanToBeanVisitor();
        plan.accept(visitor);

        return visitor.getPlanBean();
    }

    public CompletableFuture<Void> createNewPlan() {
        plan = new WorkoutPlan("Nuovo piano");
        plan.setCycleLength(7);

        PlanNode rootNode = new Block("Blocco 1");
        WorkoutSession firstSession = new WorkoutSession("Sessione 1", 0, rootNode);

        plan.addSession(firstSession);

        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);

        return planApi.createPlanAsync(serializer.getPlanDto())
                .thenAccept(id -> {
                    plan.setPlanId(id);
                    workoutPlanSubject.notifyObservers();
                });
    }

    public CompletableFuture<Void> editExistingPlan(String planId, boolean isCopy) {
        PlanDeserializer deserializer = new PlanDeserializer();

        return planApi.getPlanDetailsByIdAsync(planId)
                .thenAccept(planDto -> {
                    this.plan = deserializer.toEntity(planDto);
                    if (isCopy) {
                        plan.setPlanId(null);
                    }

                    workoutPlanSubject.notifyObservers();
                });
    }


    public void addExercise(String parentBlockId, String exerciseId) {
        // TODO: Create and execute AddExerciseCommand
        // WorkoutPlanEditorCommand cmd = new AddExerciseCommand(plan, parentBlockId, exerciseId);
        // executeCommand(cmd);
        
        System.out.println("Esercizio aggiunto al blocco: " + parentBlockId);
    }

    public void removeNode(String nodeId) {
        // TODO: Create and execute RemoveNodeCommand
        // executeCommand(new RemoveNodeCommand(plan, nodeId));
    }

    public void setRestTime(String nodeId, int timeMillis) {
        // TODO: Create and execute SetRestTimeCommand
    }

    public CompletableFuture<Void> saveChanges() {
        if (plan == null || plan.getPlanId() == null) {
            return CompletableFuture.failedFuture(new IllegalStateException("Nessun piano in modifica"));
        }
        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);
        return planApi.updatePlanAsync(plan.getPlanId(), serializer.getPlanDto());
    }

    private void executeCommand(WorkoutPlanEditorCommand command) {
        historyManager.executeCommand(command);
        workoutPlanSubject.notifyObservers();
    }

    public void undo() {
        historyManager.undo();
        workoutPlanSubject.notifyObservers();
    }

    public void redo() {
        historyManager.redo();
        workoutPlanSubject.notifyObservers();
    }

    public void getProtocolBlockLibrary() {

    }

    public void buildProtocolBlockLibrary() {
        protocolBlockLibrary.clear();
        protocolBlockLibraryCache.clear();

        ProtocolBlockFactory factory = new ProtocolBlockFactory();

        protocolBlockLibrary.add(factory.createCircuit());
        protocolBlockLibrary.add(factory.createSuperSet());
        protocolBlockLibrary.add(factory.createDropSet());
        protocolBlockLibrary.add(factory.createGiantSet());
        protocolBlockLibrary.add(factory.createAMRAP());
        protocolBlockLibrary.add(factory.createEMOM());

        for (ProtocolBlock block : protocolBlockLibrary) {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor();
            block.accept(visitor);

            protocolBlockLibraryCache.add(visitor.getCurrentPlanNodeBean());
        }

    }


}
