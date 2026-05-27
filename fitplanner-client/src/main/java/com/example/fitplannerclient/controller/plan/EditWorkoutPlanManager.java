package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.controller.plan.command.EditorHistoryManager;
import com.example.fitplannerclient.controller.plan.command.WorkoutPlanEditorCommand;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.service.facade.WorkoutPlanFacade;

import java.util.concurrent.CompletableFuture;

public class EditWorkoutPlanManager {

    private final EditorHistoryManager historyManager = new EditorHistoryManager();

    private final WorkoutPlanSubject workoutPlanSubject = new WorkoutPlanSubject();
    private final WorkoutPlanFacade planFacade;

    private WorkoutPlan plan;

    public EditWorkoutPlanManager(WorkoutPlanFacade planFacade) {
        this.planFacade = planFacade;
    }

    public void addObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.attach(observer);
    }
    public void removeObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.detach(observer);
    }

    public WorkoutPlanBean getPlan() {
        return null;
    }

    public CompletableFuture<Void> createNewPlan(String planName) {
        plan = new WorkoutPlan(planName);

        PlanNode rootNode = new Block("Blocco 1");
        WorkoutSession firstSession = new WorkoutSession("Sessione 1", 0, rootNode);

        plan.addSession(firstSession);

        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);

        return planFacade.createPlanAsync(serializer.getPlanDto())
                .thenAccept(plan::setPlanId);
    }

    public CompletableFuture<Void> editExistingPlan(String planId) {
        PlanDeserializer deserializer = new PlanDeserializer();

        return planFacade.getPlanDetailsByIdAsync(planId)
                .thenAccept(planDto -> {
                    this.plan = deserializer.toEntity(planDto);
                });

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


}
