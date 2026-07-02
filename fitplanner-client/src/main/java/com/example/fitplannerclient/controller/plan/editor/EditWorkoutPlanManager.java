package com.example.fitplannerclient.controller.plan.editor;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.repository.WorkoutPlanRepository;
import com.example.fitplannerclient.controller.plan.ProtocolLibraryManager;
import com.example.fitplannerclient.entity.plan.visitor.NodeFinderVisitor;
import com.example.fitplannerclient.controller.plan.editor.command.EditorHistoryManager;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.controller.plan.mapper.PlanToBeanVisitor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EditWorkoutPlanManager {

    private final EditorHistoryManager historyManager = new EditorHistoryManager();
    private final WorkoutPlanSubject workoutPlanSubject = new WorkoutPlanSubject();

    private WorkoutPlan plan;

    private final WorkoutPlanRepository repository;
    private final ExerciseRepository exerciseRepository;
    private final WorkoutPlanStructureEditor structureEditor;
    private final WorkoutPlanBadgeEditor badgeEditor;
    private final ProtocolLibraryManager protocolLibraryManager;

    public EditWorkoutPlanManager(WorkoutPlanRepository repository, ExerciseRepository exerciseRepository) {
        this.repository = repository;
        this.exerciseRepository = exerciseRepository;
        this.protocolLibraryManager = new ProtocolLibraryManager();
        this.structureEditor = new WorkoutPlanStructureEditor(historyManager, workoutPlanSubject, protocolLibraryManager);
        this.badgeEditor = new WorkoutPlanBadgeEditor(historyManager, workoutPlanSubject);
    }

    public void addObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.attach(observer);
    }
    public void removeObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.detach(observer);
    }

    public CompletableFuture<WorkoutPlanBean> getPlanAsync() {
        if (this.plan == null) return CompletableFuture.completedFuture(null);
        
        PlanToBeanVisitor visitor = new PlanToBeanVisitor(
                uuid -> {
                    if (exerciseRepository != null) {
                        var exercise = exerciseRepository.getCachedExercise(uuid);
                        if (exercise != null) return exercise.getName();
                    }
                    return "Esercizio Sconosciuto";
                }
        );
        this.plan.accept(visitor);
        return CompletableFuture.completedFuture(visitor.getPlanBean());
    }

    public CompletableFuture<Void> createNewPlan() {
        return repository.createNewPlan().thenAccept(newPlan -> {
            this.plan = newPlan;
            workoutPlanSubject.notifyObservers();
        });
    }

    public CompletableFuture<Void> editExistingPlan(String planId, boolean isCopy) {
        return repository.editExistingPlan(planId, isCopy).thenAccept(loadedPlan -> {
            this.plan = loadedPlan;
            workoutPlanSubject.notifyObservers();
        });
    }

    public void changePlanName(String newName) {
        structureEditor.changePlanName(this.plan, newName);
    }

    public void changeCycleLength(int length) {
        structureEditor.changeCycleLength(this.plan, length);
    }

    // Command methods for sessions and nodes

    public void addSession(int day) {
        structureEditor.addSession(this.plan, day);
    }

    public void removeSession(int day) {
        structureEditor.removeSession(this.plan, day);
    }

    public void updateSessionName(int day, String name) {
        structureEditor.updateSessionName(this.plan, day, name);
    }

    public void updateSessionDay(int oldDay, int newDay) {
        structureEditor.updateSessionDay(this.plan, oldDay, newDay);
    }

    public void removeNode(String nodeId) {
        structureEditor.removeNode(this.plan, nodeId);
    }

    public void renameNode(String nodeId, String newName) {
        structureEditor.renameNode(this.plan, nodeId, newName);
    }

    public void changeExerciseResource(String nodeId, String newResourceId) {
        structureEditor.changeExerciseResource(this.plan, nodeId, newResourceId);
    }

    public void emptyNode(String nodeId) {
        structureEditor.emptyNode(this.plan, nodeId);
    }

    public void updateProtocolParameters(String nodeId, Map<String, String> params) {
        structureEditor.updateProtocolParameters(this.plan, nodeId, params);
    }

    public void copyNode(String nodeId, String targetParentId, int targetIndex) {
        structureEditor.copyNode(this.plan, nodeId, targetParentId, targetIndex);
    }

    public void duplicateNode(String nodeId) {
        structureEditor.duplicateNode(this.plan, nodeId);
    }

    public void moveNode(String nodeId, String targetParentId, int targetIndex) {
        structureEditor.moveNode(this.plan, nodeId, targetParentId, targetIndex);
    }

    public void updateModifier(String targetNodeId, String modifierType, String newValue) {
        badgeEditor.updateModifier(this.plan, targetNodeId, modifierType, newValue);
    }

    public void updateDecorator(String decoratorId, String newValue) {
        badgeEditor.updateDecorator(this.plan, decoratorId, newValue);
    }

    public void copyModifier(String sourceNodeId, String targetNodeId, int sourceIndex) {
        badgeEditor.copyModifier(this.plan, sourceNodeId, targetNodeId, sourceIndex);
    }

    public void moveModifier(String sourceNodeId, String targetNodeId, int sourceIndex) {
        badgeEditor.moveModifier(this.plan, sourceNodeId, targetNodeId, sourceIndex);
    }

    public void copyDecorator(String sourceDecoratorId, String targetNodeId, int targetIndex) {
        badgeEditor.copyDecorator(this.plan, sourceDecoratorId, targetNodeId, targetIndex);
    }

    public void moveDecorator(String sourceDecoratorId, String targetNodeId, int targetIndex) {
        badgeEditor.moveDecorator(this.plan, sourceDecoratorId, targetNodeId, targetIndex);
    }

    public CompletableFuture<Void> saveChanges() {
        return repository.saveChanges(this.plan);
    }

    public void undo() {
        historyManager.undo();
        workoutPlanSubject.notifyObservers();
    }

    public void redo() {
        historyManager.redo();
        workoutPlanSubject.notifyObservers();
    }

    public CompletableFuture<Void> savePlan() {
        return repository.savePlan(this.plan).thenRun(workoutPlanSubject::notifyObservers);
    }

    public void addExerciseFromToolbox(String exerciseId, String targetParentId, int targetIndex) {
        structureEditor.addExerciseFromToolbox(this.plan, exerciseId, targetParentId, targetIndex);
    }

    public void addBlockFromToolbox(String blockName, String targetParentId, int targetIndex) {
        structureEditor.addBlockFromToolbox(this.plan, blockName, targetParentId, targetIndex);
    }

    public Map<String, String> getDefaultProtocolParameters(String protocolName) {
        return protocolLibraryManager.getDefaultProtocolParameters(protocolName);
    }

    public void addProtocolBlockFromToolbox(String protocolName, Map<String, String> parameters, String targetParentId, int targetIndex) {
        structureEditor.addProtocolBlockFromToolbox(this.plan, protocolName, parameters, targetParentId, targetIndex);
    }

    public void addDecoratorFromToolbox(String decoratorType, String value, String targetNodeId) {
        badgeEditor.addDecoratorFromToolbox(this.plan, decoratorType, value, targetNodeId);
    }

    public void addModifierFromToolbox(String modifierType, String value, String targetNodeId) {
        badgeEditor.addModifierFromToolbox(this.plan, modifierType, value, targetNodeId);
    }

    public List<PlanNodeBean> getProtocolBlockLibraryCache() {
        return protocolLibraryManager.getProtocolBlockLibraryCache();
    }

    public List<String> getAvailableVariablesForNode(String nodeId) {
        return badgeEditor.getAvailableVariablesForNode(this.plan, nodeId);
    }


    // Query sui nodi

    public boolean isExerciseNode(String nodeId) {
        return findNode(nodeId) instanceof ExerciseNode;
    }

    public String getNodeName(String nodeId) {
        PlanNode node = findNode(nodeId);
        if (node instanceof Block block) return block.getTitle();
        if (node instanceof ProtocolBlock protocolBlock) return protocolBlock.getSemanticType();
        return null;
    }

    public Map<String, String> getProtocolParameters(String nodeId) {
        PlanNode node = findNode(nodeId);
        if (node instanceof ProtocolBlock protocolBlock) {
            return new HashMap<>(protocolBlock.getParameters());
        }
        return Map.of();
    }

    private PlanNode findNode(String nodeId) {
        if (this.plan == null) return null;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        this.plan.accept(finder);
        return finder.isFound() ? finder.getFoundNode() : null;
    }

    public void buildProtocolBlockLibrary() {
        protocolLibraryManager.buildProtocolBlockLibrary();
    }

}
