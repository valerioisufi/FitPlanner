package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.controller.plan.command.EditorHistoryManager;
import com.example.fitplannerclient.controller.plan.command.WorkoutPlanEditorCommand;
import com.example.fitplannerclient.controller.plan.command.CompositeCommand;
import com.example.fitplannerclient.controller.plan.command.InsertNodeCommand;
import com.example.fitplannerclient.controller.plan.command.RemoveNodeCommand;
import com.example.fitplannerclient.controller.plan.command.RenameBlockCommand;
import com.example.fitplannerclient.controller.plan.command.SetModifierCommand;
import com.example.fitplannerclient.controller.plan.command.AddSessionCommand;
import com.example.fitplannerclient.controller.plan.command.RemoveSessionCommand;
import com.example.fitplannerclient.controller.plan.command.UpdateSessionCommand;
import com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.controller.plan.visitor.NodeFinderVisitor;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EditWorkoutPlanManager {

    private final EditorHistoryManager historyManager = new EditorHistoryManager();

    private final WorkoutPlanSubject workoutPlanSubject = new WorkoutPlanSubject();
    private final WorkoutPlanApi planApi;
    private final ExerciseRepository exerciseRepository;

    private WorkoutPlan plan;
    private final List<ProtocolBlock> protocolBlockLibrary = new ArrayList<>();
    private final List<PlanNodeBean> protocolBlockLibraryCache = new ArrayList<>();

    public EditWorkoutPlanManager(WorkoutPlanApi planApi, ExerciseRepository exerciseRepository) {
        this.planApi = planApi;
        this.exerciseRepository = exerciseRepository;

        exerciseRepository.getExercisesAsync(null).thenAccept(entities -> {});
    }

    public void addObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.attach(observer);
    }
    public void removeObserver(WorkoutPlanObserver observer) {
        workoutPlanSubject.detach(observer);
    }

    public CompletableFuture<WorkoutPlanBean> getPlanAsync() {
        if (plan == null) return CompletableFuture.completedFuture(null);

        if (exerciseRepository == null) {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor();
            plan.accept(visitor);
            return CompletableFuture.completedFuture(visitor.getPlanBean());
        }

        return exerciseRepository.getExercisesAsync(null).thenApply(entities -> {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor(
                    uuid -> {
                        ExerciseDescription entity = exerciseRepository.getCachedExercise(uuid);
                        return entity != null ? entity.getName() : "Esercizio Sconosciuto";
                    }
            );
            plan.accept(visitor);
            return visitor.getPlanBean();
        });
    }

    public CompletableFuture<Void> createNewPlan() {
        plan = new WorkoutPlan("Nuovo piano");
        plan.setCycleLength(7);

        PlanNode rootNode = new Block("Session Giorno 1");
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
                .thenCompose(planDto -> {
                    this.plan = deserializer.toEntity(planDto);

                    if (isCopy) {
                        plan.setPlanId(null);
                        plan.changeName(plan.getName() + " (Copia)");

                        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
                        plan.accept(serializer);

                        return planApi.createPlanAsync(serializer.getPlanDto())
                                .thenAccept(id -> {
                                    plan.setPlanId(id);
                                    workoutPlanSubject.notifyObservers();
                                });
                    }

                    workoutPlanSubject.notifyObservers();
                    return CompletableFuture.completedFuture(null);
                });
    }

    public void changePlanName(String newName) {
        if (plan != null) {
            plan.changeName(newName);
            workoutPlanSubject.notifyObservers();
        }
    }

    public void changeCycleLength(int length) {
        if (plan != null) {
            plan.setCycleLength(length);
            workoutPlanSubject.notifyObservers();
        }
    }

    public void addSession(int day) {
        if (plan != null) {
            // Creo una nuova sessione con un Block vuoto come root
            Block root = new Block("Sessione Giorno " + day);
            WorkoutSession session = new WorkoutSession(String.valueOf(day), day, root);
            AddSessionCommand cmd = new AddSessionCommand(plan, session);
            executeCommand(cmd);
        }
    }

    public void removeSession(int day) {
        if (plan != null) {
            RemoveSessionCommand cmd = new RemoveSessionCommand(plan, day);
            executeCommand(cmd);
        }
    }

    public void updateSessionName(int day, String name) {
        if (plan != null) {
            WorkoutSession session = getSessionByDay(day);
            if (session != null) {
                UpdateSessionCommand cmd = new UpdateSessionCommand(plan, session, name, session.getDay());
                executeCommand(cmd);
            }
        }
    }

    public void updateSessionDay(int oldDay, int newDay) {
        if (plan != null) {
            WorkoutSession session = getSessionByDay(oldDay);
            if (session != null && getSessionByDay(newDay) == null) {
                UpdateSessionCommand cmd = new UpdateSessionCommand(plan, session, session.getName(), newDay);
                executeCommand(cmd);
            }
        }
    }

    private WorkoutSession getSessionByDay(int day) {
        return plan.getSessions().stream().filter(s -> s.getDay() == day).findFirst().orElse(null);
    }


    public void addExercise(String parentBlockId, String exerciseId) {
        NodeFinderVisitor finder = new NodeFinderVisitor(parentBlockId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof Block parent) {
            ExerciseNode newNode = new ExerciseNode(exerciseId);
            InsertNodeCommand cmd = new InsertNodeCommand(newNode, parent, parent.getChildrenCount());
            executeCommand(cmd);
        }
    }

    public void removeNode(String nodeId) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundParent() instanceof Block parent) {
            int index = finder.getFoundPosition();
            RemoveNodeCommand cmd = new RemoveNodeCommand(parent, index);
            executeCommand(cmd);
        }
    }

    public void renameNode(String nodeId, String newName) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof Block block) {
            RenameBlockCommand cmd = new RenameBlockCommand(block, newName);
            executeCommand(cmd);
        }
    }

    public void copyNode(String nodeId, String targetParentId, int targetIndex) {
        NodeFinderVisitor sourceFinder = new NodeFinderVisitor(nodeId);
        NodeFinderVisitor targetFinder = new NodeFinderVisitor(targetParentId);
        plan.accept(sourceFinder);
        plan.accept(targetFinder);

        if (sourceFinder.isFound() && targetFinder.isFound() && targetFinder.getFoundNode() instanceof Block targetParent) {
            PlanNode copy = sourceFinder.getFoundNode().deepCopy();
            InsertNodeCommand cmd = new InsertNodeCommand(copy, targetParent, targetIndex);
            executeCommand(cmd);
        }
    }

    public void moveNode(String nodeId, String targetParentId, int targetIndex) {
        NodeFinderVisitor sourceFinder = new NodeFinderVisitor(nodeId);
        NodeFinderVisitor targetFinder = new NodeFinderVisitor(targetParentId);
        plan.accept(sourceFinder);
        plan.accept(targetFinder);

        if (sourceFinder.isFound() && sourceFinder.getFoundParent() instanceof Block sourceParent 
            && targetFinder.isFound() && targetFinder.getFoundNode() instanceof Block targetParent) {
            
            RemoveNodeCommand removeCmd = new RemoveNodeCommand(sourceParent, sourceFinder.getFoundPosition());
            InsertNodeCommand insertCmd = new InsertNodeCommand(sourceFinder.getFoundNode(), targetParent, targetIndex);
            
            CompositeCommand cmd = new CompositeCommand();
            cmd.addCommand(removeCmd);
            cmd.addCommand(insertCmd);
            executeCommand(cmd);
        }
    }

    public void updateModifier(String nodeId, String badgeId, String newName, String newValue) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof com.example.fitplannerclient.entity.plan.exercise.ExerciseNode node) {
            com.example.fitplannerclient.entity.plan.exercise.ModifierType type = com.example.fitplannerclient.entity.plan.exercise.ModifierType.valueOf(newName);
            com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier modifier = new com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier(type, newValue);
            SetModifierCommand cmd = new SetModifierCommand(node, modifier);
            executeCommand(cmd);
        }
    }

    public void updateDecorator(String nodeId, String badgeId, String newName, String newValue) {
        NodeFinderVisitor finder = new NodeFinderVisitor(badgeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof com.example.fitplannerclient.entity.plan.decorator.FlowDecorator decorator) {
            com.example.fitplannerclient.controller.plan.command.UpdateDecoratorValueCommand cmd = 
                new com.example.fitplannerclient.controller.plan.command.UpdateDecoratorValueCommand(decorator, null, newValue);
            executeCommand(cmd);
        }
    }

    public void copyModifier(String sourceNodeId, String targetNodeId, int sourceIndex, int targetIndex) {}
    public void moveModifier(String sourceNodeId, String targetNodeId, int sourceIndex, int targetIndex) {}
    public void copyDecorator(String sourceNodeId, String targetNodeId, int sourceIndex, int targetIndex) {}
    public void moveDecorator(String sourceNodeId, String targetNodeId, int sourceIndex, int targetIndex) {}

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

    public CompletableFuture<Void> savePlan() {
        if (plan == null) CompletableFuture.completedFuture(null);
        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);

        return planApi.updatePlanAsync(plan.getPlanId(), serializer.getPlanDto())
                .thenRun(workoutPlanSubject::notifyObservers);
    }

    public void addExerciseFromToolbox(String exerciseId, String targetParentId, int targetIndex) {
        com.example.fitplannerclient.entity.plan.exercise.ExerciseNode node = new com.example.fitplannerclient.entity.plan.exercise.ExerciseNode();
        node.setResourceId(exerciseId);
        node.addModifier(new com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier(com.example.fitplannerclient.entity.plan.exercise.ModifierType.REPS, "10"));
        addNodeFromToolbox(node, targetParentId, targetIndex);
    }

    public void addBlockFromToolbox(String blockName, String targetParentId, int targetIndex) {
        addNodeFromToolbox(new com.example.fitplannerclient.entity.plan.block.Block(blockName), targetParentId, targetIndex);
    }

    public java.util.Map<String, String> getDefaultProtocolParameters(String protocolName) {
        com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory factory = new com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory();
        com.example.fitplannerclient.entity.plan.block.ProtocolBlock block = switch (protocolName) {
            case "DROP_SET" -> factory.createDropSet();
            case "SUPER_SET" -> factory.createSuperSet();
            case "GIANT_SET" -> factory.createGiantSet();
            case "CIRCUIT" -> factory.createCircuit();
            case "AMRAP" -> factory.createAMRAP();
            case "EMOM" -> factory.createEMOM();
            default -> factory.createCircuit();
        };
        return block.getParameters();
    }

    public void addProtocolBlockFromToolbox(String protocolName, java.util.Map<String, String> parameters, String targetParentId, int targetIndex) {
        com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory factory = new com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory();
        com.example.fitplannerclient.entity.plan.block.ProtocolBlock block = switch (protocolName) {
            case "DROP_SET" -> factory.createDropSet();
            case "SUPER_SET" -> factory.createSuperSet();
            case "GIANT_SET" -> factory.createGiantSet();
            case "CIRCUIT" -> factory.createCircuit();
            case "AMRAP" -> factory.createAMRAP();
            case "EMOM" -> factory.createEMOM();
            default -> factory.createCircuit();
        };

        for (java.util.Map.Entry<String, String> entry : parameters.entrySet()) {
            block.setParameter(entry.getKey(), entry.getValue());
        }

        addNodeFromToolbox(block, targetParentId, targetIndex);
    }

    private void addNodeFromToolbox(PlanNode newNode, String targetParentId, int targetIndex) {
        if (plan == null) return;

        NodeFinderVisitor finder = new NodeFinderVisitor(targetParentId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof Block block) {
            if (targetIndex >= 0 && targetIndex <= block.getChildrenCount()) {
                block.addNodeAt(targetIndex, newNode);
            } else {
                block.addNode(newNode);
            }
            workoutPlanSubject.notifyObservers();
        }
    }

    public void addDecoratorFromToolbox(String decoratorType, String value, String targetNodeId) {
        if (plan == null) return;
        
        com.example.fitplannerclient.entity.plan.decorator.FlowDecorator newDecorator = null;
        switch (decoratorType.toUpperCase().replace(" ", "_")) {
            case "REST" -> newDecorator = new com.example.fitplannerclient.entity.plan.decorator.RestDecorator(null, value);
            case "LOOP" -> newDecorator = new com.example.fitplannerclient.entity.plan.decorator.LoopDecorator(null, value);
            case "TIME_LIMIT" -> newDecorator = new com.example.fitplannerclient.entity.plan.decorator.TimeLimitDecorator(null, value);
            case "INTERVAL" -> newDecorator = new com.example.fitplannerclient.entity.plan.decorator.IntervalDecorator(null, value);
            case "PROGRESSION" -> newDecorator = new com.example.fitplannerclient.entity.plan.decorator.ProgressionDecorator(null, value);
        }
        if (newDecorator == null) return;

        NodeFinderVisitor finder = new NodeFinderVisitor(targetNodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundParent() instanceof Block block) {
            int index = finder.getFoundPosition();
            if (index != -1) {
                newDecorator.setWrappedNode(finder.getFoundNode());
                block.replaceNode(index, newDecorator);
                 workoutPlanSubject.notifyObservers();
            }
        } else if (finder.isFound() && finder.getFoundParent() instanceof com.example.fitplannerclient.entity.plan.decorator.FlowDecorator parentDecorator) {
             newDecorator.setWrappedNode(finder.getFoundNode());
             parentDecorator.setWrappedNode(newDecorator);
             workoutPlanSubject.notifyObservers();
        }
    }

    public void addModifierFromToolbox(String modifierType, String value, String targetNodeId) {
        if (plan == null) return;

        com.example.fitplannerclient.entity.plan.exercise.ModifierType type = com.example.fitplannerclient.entity.plan.exercise.ModifierType.valueOf(modifierType);
        com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier modifier = new com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier(type, value);

        NodeFinderVisitor finder = new NodeFinderVisitor(targetNodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof ExerciseNode ex) {
            ex.addModifier(modifier);
            workoutPlanSubject.notifyObservers();
        }
    }

    public List<PlanNodeBean> getProtocolBlockLibraryCache() {
        return protocolBlockLibraryCache;
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
