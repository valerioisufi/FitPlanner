package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.WorkoutPlanBean;
import com.example.fitplannerclient.controller.plan.command.*;
import com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanObserver;
import com.example.fitplannerclient.controller.plan.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.controller.plan.visitor.AvailableVariablesVisitor;
import com.example.fitplannerclient.controller.plan.visitor.NodeFinderVisitor;
import com.example.fitplannerclient.controller.plan.visitor.NodePathVisitor;
import com.example.fitplannerclient.entity.ExerciseDescription;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;
import com.example.fitplannerclient.repository.ExerciseRepository;
import com.example.fitplannerclient.serializer.PlanDeserializer;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;
import com.example.fitplannerclient.serializer.PlanToDtoVisitor;
import com.example.fitplannerclient.service.api.WorkoutPlanApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

        PlanNode rootNode = new Block("Session Giorno 0");
        WorkoutSession firstSession = new WorkoutSession("Sessione 0", 0, rootNode);

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

    // Command methods for sessions and nodes

    public void addSession(int day) {
        if (plan != null) {
            // Creo una nuova sessione con un Block vuoto come root
            Block root = new Block("Sessione Giorno " + day);
            WorkoutSession session = new WorkoutSession("Sessione " + day, day, root);

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
            WorkoutSession session = plan.getSessionByDay(day);

            if (session != null) {
                UpdateSessionCommand cmd = new UpdateSessionCommand(plan, session, name, session.getDay());
                executeCommand(cmd);
            }
        }
    }

    public void updateSessionDay(int oldDay, int newDay) {
        if (plan != null) {
            WorkoutSession session = plan.getSessionByDay(oldDay);

            if (session != null && plan.getSessionByDay(newDay) == null) {
                UpdateSessionCommand cmd = new UpdateSessionCommand(plan, session, session.getName(), newDay);
                executeCommand(cmd);
            }
        }
    }

    public void addExercise(String parentBlockId, String exerciseId) {
        NodeFinderVisitor finder = new NodeFinderVisitor(parentBlockId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof GroupNode parent) {
            ExerciseNode newNode = new ExerciseNode(exerciseId);
            InsertNodeCommand cmd = new InsertNodeCommand(newNode, parent, parent.getChildrenCount());
            executeCommand(cmd);
        }
    }

    public void removeNode(String nodeId) {
        NodeContext ctx = getOutmostNodeContext(nodeId);

        if (ctx != null && ctx.groupParent != null) {
            RemoveNodeCommand cmd = new RemoveNodeCommand(ctx.groupParent, ctx.indexInParent);
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

    private static class NodeContext {
        public PlanNode outmostNode;
        public GroupNode groupParent;
        public int indexInParent;
    }

    private NodeContext getOutmostNodeContext(String targetInnermostId) {
        NodePathVisitor pathVisitor = new NodePathVisitor(targetInnermostId);
        plan.accept(pathVisitor);
        List<PlanNode> path = pathVisitor.getPath();
        if (path == null || path.isEmpty()) return null;

        NodeContext ctx = new NodeContext();
        for (int i = path.size() - 2; i >= 0; i--) {
            PlanNode node = path.get(i);
            if (node instanceof GroupNode gn) {
                ctx.groupParent = gn;
                ctx.outmostNode = path.get(i + 1);
                for (int j = 0; j < gn.getChildrenCount(); j++) {
                    if (gn.getNodeAt(j) == ctx.outmostNode) {
                        ctx.indexInParent = j;
                        break;
                    }
                }
                return ctx;
            }
        }
        return null;
    }

    public void changeExerciseResource(String nodeId, String newResourceId) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof ExerciseNode node) {
            ChangeExerciseResourceCommand cmd = 
                new ChangeExerciseResourceCommand(node, newResourceId);
            executeCommand(cmd);
        }
    }

    public void emptyNode(String nodeId) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof GroupNode groupNode) {
            CompositeCommand cmd = new CompositeCommand();
            // Remove nodes from last to first to avoid index shifting issues
            for (int i = groupNode.getChildrenCount() - 1; i >= 0; i--) {
                cmd.addCommand(new RemoveNodeCommand(groupNode, i));
            }
            executeCommand(cmd);
        }
    }

    public void updateProtocolParameters(String nodeId, Map<String, String> params) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof ProtocolBlock block) {
            UpdateProtocolParametersCommand cmd = 
                new UpdateProtocolParametersCommand(block, params);
            executeCommand(cmd);
        }
    }

    public void copyNode(String nodeId, String targetParentId, int targetIndex) {
        NodeContext ctx = getOutmostNodeContext(nodeId);
        NodeFinderVisitor targetFinder = new NodeFinderVisitor(targetParentId);
        plan.accept(targetFinder);

        if (ctx != null && targetFinder.isFound() && targetFinder.getFoundNode() instanceof GroupNode targetParent) {
            PlanNode copy = ctx.outmostNode.deepCopy();
            InsertNodeCommand cmd = new InsertNodeCommand(copy, targetParent, targetIndex);
            executeCommand(cmd);
        }
    }

    public void duplicateNode(String nodeId) {
        NodeContext ctx = getOutmostNodeContext(nodeId);
        if (ctx != null && ctx.groupParent != null) {
            PlanNode copy = ctx.outmostNode.deepCopy();
            InsertNodeCommand cmd = new InsertNodeCommand(copy, ctx.groupParent, ctx.indexInParent + 1);
            executeCommand(cmd);
        }
    }

    public void moveNode(String nodeId, String targetParentId, int targetIndex) {
        NodeContext ctx = getOutmostNodeContext(nodeId);
        NodeFinderVisitor targetFinder = new NodeFinderVisitor(targetParentId);
        plan.accept(targetFinder);

        if (ctx != null && ctx.groupParent != null 
            && targetFinder.isFound() && targetFinder.getFoundNode() instanceof GroupNode targetParent) {
            
            int sourceIndex = ctx.indexInParent;
            GroupNode sourceParent = ctx.groupParent;

            if (sourceParent == targetParent && sourceIndex < targetIndex) {
                targetIndex--;
            }

            RemoveNodeCommand removeCmd = new RemoveNodeCommand(sourceParent, sourceIndex);
            InsertNodeCommand insertCmd = new InsertNodeCommand(ctx.outmostNode, targetParent, targetIndex);
            
            CompositeCommand cmd = new CompositeCommand();
            cmd.addCommand(removeCmd);
            cmd.addCommand(insertCmd);
            executeCommand(cmd);
        }
    }

    public void updateModifier(String nodeId, String badgeId, String newName, String newValue) {
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof ExerciseNode node) {
            ModifierType type = ModifierType.valueOf(newName);
            ExerciseModifier modifier = new ExerciseModifier(type, newValue);
            SetModifierCommand cmd = new SetModifierCommand(node, modifier);
            executeCommand(cmd);
        }
    }

    public void updateDecorator(String nodeId, String badgeId, String newName, String newValue) {
        NodeFinderVisitor finder = new NodeFinderVisitor(badgeId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof FlowDecorator decorator) {
            UpdateDecoratorValueCommand cmd = 
                new UpdateDecoratorValueCommand(decorator, null, newValue);
            executeCommand(cmd);
        }
    }

    public void copyModifier(String sourceNodeId, String targetNodeId, int sourceIndex, int targetIndex) {
        if (sourceNodeId.equals(targetNodeId)) return;
        NodeFinderVisitor srcFinder = new NodeFinderVisitor(sourceNodeId);
        NodeFinderVisitor tgtFinder = new NodeFinderVisitor(targetNodeId);
        plan.accept(srcFinder);
        plan.accept(tgtFinder);
        if (srcFinder.isFound() && tgtFinder.isFound() && 
            srcFinder.getFoundNode() instanceof ExerciseNode srcNode &&
            tgtFinder.getFoundNode() instanceof ExerciseNode tgtNode) {
            
            var mods = new ArrayList<>(srcNode.getModifiers());
            if (sourceIndex >= 0 && sourceIndex < mods.size()) {
                ExerciseModifier mod = mods.get(sourceIndex);
                ExerciseModifier copy = new ExerciseModifier(mod.getType(), mod.getValue());
                executeCommand(new SetModifierCommand(tgtNode, copy));
            }
        }
    }

    public void moveModifier(String sourceNodeId, String targetNodeId, int sourceIndex, int targetIndex) {
        if (sourceNodeId.equals(targetNodeId)) return;
        NodeFinderVisitor srcFinder = new NodeFinderVisitor(sourceNodeId);
        NodeFinderVisitor tgtFinder = new NodeFinderVisitor(targetNodeId);
        plan.accept(srcFinder);
        plan.accept(tgtFinder);
        if (srcFinder.isFound() && tgtFinder.isFound() && 
            srcFinder.getFoundNode() instanceof ExerciseNode srcNode &&
            tgtFinder.getFoundNode() instanceof ExerciseNode tgtNode) {
            
            var mods = new ArrayList<>(srcNode.getModifiers());
            if (sourceIndex >= 0 && sourceIndex < mods.size()) {
                ExerciseModifier mod = mods.get(sourceIndex);
                ExerciseModifier copy = new ExerciseModifier(mod.getType(), mod.getValue());
                
                CompositeCommand cmd = new CompositeCommand();
                cmd.addCommand(new RemoveModifierCommand(srcNode, mod.getType()));
                cmd.addCommand(new SetModifierCommand(tgtNode, copy));
                executeCommand(cmd);
            }
        }
    }

    private void removeDecoratorFromChain(String decoratorId) {
        NodeFinderVisitor finder = new NodeFinderVisitor(decoratorId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof FlowDecorator decorator) {
            PlanNode parent = finder.getFoundParent();
            if (parent instanceof GroupNode group) {
                group.replaceNode(finder.getFoundPosition(), decorator.getWrappedNode());
            } else if (parent instanceof FlowDecorator parentDecorator) {
                parentDecorator.setWrappedNode(decorator.getWrappedNode());
            }
        }
    }

    private void insertDecoratorToChain(FlowDecorator decorator, String targetInnermostNodeId, int targetIndex) {
        NodePathVisitor pathVisitor = new NodePathVisitor(targetInnermostNodeId);
        plan.accept(pathVisitor);
        List<PlanNode> path = pathVisitor.getPath();
        if (path == null || path.size() < 2) return;

        GroupNode groupNode = null;
        int groupIndex = -1;
        int firstDecoratorIndex = -1;

        for (int i = path.size() - 2; i >= 0; i--) {
            PlanNode node = path.get(i);
            if (node instanceof FlowDecorator) {
                firstDecoratorIndex = i;
            } else if (node instanceof GroupNode gn) {
                groupNode = gn;
                PlanNode nextNode = path.get(i + 1);
                for (int j = 0; j < gn.getChildrenCount(); j++) {
                    if (gn.getNodeAt(j) == nextNode) {
                        groupIndex = j;
                        break;
                    }
                }
                break;
            }
        }

        if (groupNode != null && groupIndex != -1) {
            List<FlowDecorator> currentChain = new ArrayList<>();
            if (firstDecoratorIndex != -1) {
                for (int i = firstDecoratorIndex; i < path.size() - 1; i++) {
                    currentChain.add((FlowDecorator) path.get(i));
                }
            }

            if (targetIndex >= 0 && targetIndex <= currentChain.size()) {
                currentChain.add(targetIndex, decorator);
            } else {
                currentChain.add(decorator);
            }

            PlanNode chainRoot = path.get(path.size() - 1);
            for (int i = currentChain.size() - 1; i >= 0; i--) {
                FlowDecorator d = currentChain.get(i);
                d.setWrappedNode(chainRoot);
                chainRoot = d;
            }

            groupNode.replaceNode(groupIndex, chainRoot);
        }
    }

    public void copyDecorator(String sourceDecoratorId, String targetNodeId, int targetIndex) {
        NodeFinderVisitor finder = new NodeFinderVisitor(sourceDecoratorId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof FlowDecorator srcDecorator) {
            FlowDecorator copy = (FlowDecorator) srcDecorator.deepCopy();
            insertDecoratorToChain(copy, targetNodeId, targetIndex);
            workoutPlanSubject.notifyObservers();
        }
    }

    public void moveDecorator(String sourceDecoratorId, String targetNodeId, int targetIndex) {
        NodeFinderVisitor finder = new NodeFinderVisitor(sourceDecoratorId);
        plan.accept(finder);
        if (finder.isFound() && finder.getFoundNode() instanceof FlowDecorator srcDecorator) {
            
            PlanNode innermost = srcDecorator.getWrappedNode();
            while(innermost instanceof FlowDecorator) {
                innermost = ((FlowDecorator) innermost).getWrappedNode();
            }
            String sourceInnermostNodeId = innermost.getId();

            if (sourceInnermostNodeId.equals(targetNodeId)) {
                NodePathVisitor pathVisitor = new NodePathVisitor(targetNodeId);
                plan.accept(pathVisitor);
                List<PlanNode> path = pathVisitor.getPath();
                if (path != null) {
                    int sourceIndex = -1;
                    int firstDecoratorIndex = -1;
                    for (int i = path.size() - 2; i >= 0; i--) {
                        if (path.get(i) instanceof FlowDecorator) {
                            firstDecoratorIndex = i;
                        } else {
                            break;
                        }
                    }
                    if (firstDecoratorIndex != -1) {
                        for (int i = firstDecoratorIndex; i < path.size() - 1; i++) {
                            if (path.get(i).getId().equals(sourceDecoratorId)) {
                                sourceIndex = i - firstDecoratorIndex;
                                break;
                            }
                        }
                    }
                    if (sourceIndex != -1 && sourceIndex < targetIndex) {
                        targetIndex--;
                    }
                }
            }

            removeDecoratorFromChain(sourceDecoratorId);
            insertDecoratorToChain(srcDecorator, targetNodeId, targetIndex);
            workoutPlanSubject.notifyObservers();
        }
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

    public CompletableFuture<Void> savePlan() {
        if (plan == null) CompletableFuture.completedFuture(null);
        PlanToDtoVisitor serializer = new PlanToDtoVisitor();
        plan.accept(serializer);

        return planApi.updatePlanAsync(plan.getPlanId(), serializer.getPlanDto())
                .thenRun(workoutPlanSubject::notifyObservers);
    }

    public void addExerciseFromToolbox(String exerciseId, String targetParentId, int targetIndex) {
        ExerciseNode node = new ExerciseNode();
        node.setResourceId(exerciseId);
        node.addModifier(new ExerciseModifier(ModifierType.REPS, "10"));
        addNodeFromToolbox(node, targetParentId, targetIndex);
    }

    public void addBlockFromToolbox(String blockName, String targetParentId, int targetIndex) {
        addNodeFromToolbox(new Block(blockName), targetParentId, targetIndex);
    }

    public Map<String, String> getDefaultProtocolParameters(String protocolName) {
        return getProtocolBlock(protocolName).getParameters();
    }

    public void addProtocolBlockFromToolbox(String protocolName, Map<String, String> parameters, String targetParentId, int targetIndex) {
        ProtocolBlock block = getProtocolBlock(protocolName);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            block.setParameter(entry.getKey(), entry.getValue());
        }

        addNodeFromToolbox(block, targetParentId, targetIndex);
    }

    private ProtocolBlock getProtocolBlock(String protocolName) {
        ProtocolBlockFactory factory = new ProtocolBlockFactory();
        return switch (protocolName) {
            case "DROP_SET" -> factory.createDropSet();
            case "SUPER_SET" -> factory.createSuperSet();
            case "GIANT_SET" -> factory.createGiantSet();
            case "CIRCUIT" -> factory.createCircuit();
            case "AMRAP" -> factory.createAMRAP();
            case "EMOM" -> factory.createEMOM();
            default -> factory.createCircuit();
        };
    }

    private void addNodeFromToolbox(PlanNode newNode, String targetParentId, int targetIndex) {
        if (plan == null) return;

        NodeFinderVisitor finder = new NodeFinderVisitor(targetParentId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof GroupNode block) {
            int idx = targetIndex;
            if (idx < 0 || idx > block.getChildrenCount()) {
                idx = block.getChildrenCount();
            }
            InsertNodeCommand cmd = new InsertNodeCommand(newNode, block, idx);
            executeCommand(cmd);
        }
    }

    public void addDecoratorFromToolbox(String decoratorType, String value, String targetNodeId) {
        if (plan == null) return;

        FlowDecorator newDecorator = getFlowDecorator(decoratorType, value);
        if (newDecorator == null) return;

        NodeFinderVisitor finder = new NodeFinderVisitor(targetNodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundParent() instanceof GroupNode block) {
            int index = finder.getFoundPosition();
            if (index != -1) {
                newDecorator.setWrappedNode(finder.getFoundNode());
                ReplaceNodeCommand cmd =
                    new ReplaceNodeCommand(finder.getFoundNode(), newDecorator, (PlanNode) block, index);
                executeCommand(cmd);
            }
        } else if (finder.isFound() && finder.getFoundParent() instanceof FlowDecorator parentDecorator) {
             newDecorator.setWrappedNode(finder.getFoundNode());
             ReplaceNodeCommand cmd =
                 new ReplaceNodeCommand(finder.getFoundNode(), newDecorator, parentDecorator, -1);
             executeCommand(cmd);
        }
    }

    private static FlowDecorator getFlowDecorator(String decoratorType, String value) {
        FlowDecorator newDecorator = null;
        switch (decoratorType.toUpperCase().replace(" ", "_")) {
            case "REST" -> newDecorator = new RestDecorator(null, value);
            case "LOOP" -> newDecorator = new LoopDecorator(null, value);
            case "TIME_LIMIT" -> newDecorator = new TimeLimitDecorator(null, value);
            case "INTERVAL" -> newDecorator = new IntervalDecorator(null, value);
            case "PROGRESSION" -> newDecorator = new ProgressionDecorator(null, value);
        }
        return newDecorator;
    }

    public void addModifierFromToolbox(String modifierType, String value, String targetNodeId) {
        if (plan == null) return;

        ModifierType type = ModifierType.valueOf(modifierType);
        ExerciseModifier modifier = new ExerciseModifier(type, value);

        NodeFinderVisitor finder = new NodeFinderVisitor(targetNodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof ExerciseNode ex) {
            SetModifierCommand cmd =
                new SetModifierCommand(ex, modifier);
            executeCommand(cmd);
        }
    }

    public List<PlanNodeBean> getProtocolBlockLibraryCache() {
        return protocolBlockLibraryCache;
    }

    public List<String> getAvailableVariablesForNode(String nodeId) {
        if (plan == null) return List.of();
        AvailableVariablesVisitor visitor = new AvailableVariablesVisitor(nodeId);
        plan.accept(visitor);
        return visitor.getAvailableVariables();
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
