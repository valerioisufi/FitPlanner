package com.example.fitplannerclient.controller.plan.editor;

import com.example.fitplannerclient.entity.plan.visitor.AvailableVariablesVisitor;
import com.example.fitplannerclient.entity.plan.visitor.NodeFinderVisitor;
import com.example.fitplannerclient.controller.plan.editor.command.*;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.decorator.*;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;

import java.util.ArrayList;
import java.util.List;

public class WorkoutPlanBadgeEditor {

    private final EditorHistoryManager historyManager;
    private final WorkoutPlanSubject workoutPlanSubject;

    public WorkoutPlanBadgeEditor(EditorHistoryManager historyManager, WorkoutPlanSubject workoutPlanSubject) {
        this.historyManager = historyManager;
        this.workoutPlanSubject = workoutPlanSubject;
    }

    private void executeCommand(WorkoutPlanEditorCommand command) {
        historyManager.executeCommand(command);
        workoutPlanSubject.notifyObservers();
    }

    public void updateModifier(WorkoutPlan plan, String targetNodeId, String modifierType, String newValue) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(targetNodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof ExerciseNode node) {
            ModifierType type = ModifierType.valueOf(modifierType);
            ExerciseModifier modifier = new ExerciseModifier(type, newValue);

            SetModifierCommand cmd = new SetModifierCommand(node, modifier);
            executeCommand(cmd);
        }
    }

    public void updateDecorator(WorkoutPlan plan, String decoratorId, String newValue) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(decoratorId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof FlowDecorator decorator) {
            UpdateDecoratorValueCommand cmd =
                    new UpdateDecoratorValueCommand(decorator, newValue);
            executeCommand(cmd);
        }
    }

    public void copyModifier(WorkoutPlan plan, String sourceNodeId, String targetNodeId, int sourceIndex) {
        if (plan == null) return;
        if (sourceNodeId.equals(targetNodeId)) return;

        NodeFinderVisitor srcFinder = new NodeFinderVisitor(sourceNodeId);
        NodeFinderVisitor tgtFinder = new NodeFinderVisitor(targetNodeId);
        plan.accept(srcFinder);
        plan.accept(tgtFinder);

        if (srcFinder.isFound() && tgtFinder.isFound() && 
            srcFinder.getFoundNode() instanceof ExerciseNode srcNode &&
            tgtFinder.getFoundNode() instanceof ExerciseNode tgtNode
        ) {
            var mods = new ArrayList<>(srcNode.getModifiers());

            if (sourceIndex >= 0 && sourceIndex < mods.size()) {
                ExerciseModifier mod = mods.get(sourceIndex);
                ExerciseModifier copy = new ExerciseModifier(mod.getType(), mod.getValue());

                executeCommand(new SetModifierCommand(tgtNode, copy));
            }
        }
    }

    public void moveModifier(WorkoutPlan plan, String sourceNodeId, String targetNodeId, int sourceIndex) {
        if (plan == null) return;
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

    public void removeDecoratorFromChain(WorkoutPlan plan, String decoratorId) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(decoratorId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof FlowDecorator flowDecorator) {
            ReplaceNodeCommand cmd = new ReplaceNodeCommand(flowDecorator.getWrappedNode(), finder.getFoundParent(), finder.getFoundPosition());
            executeCommand(cmd);
        }
    }

    public void copyDecorator(WorkoutPlan plan, String sourceDecoratorId, String targetNodeId, int targetIndex) {
        if (plan == null) return;
        NodeFinderVisitor srcFinder = new NodeFinderVisitor(sourceDecoratorId);
        NodeFinderVisitor tgtFinder = new NodeFinderVisitor(targetNodeId);
        plan.accept(srcFinder);
        plan.accept(tgtFinder);

        if (srcFinder.isFound() && tgtFinder.isFound() && srcFinder.getFoundNode() instanceof FlowDecorator srcDecorator) {

            List<PlanNode> path = tgtFinder.getFoundPath();
            int groupIndex = tgtFinder.getFoundGroupNodeIndex();

            // Calcoliamo quanti decoratori ci sono già nella catena (il max index consentito)
            // path.size() - 1 è l'ultimo nodo (es. l'esercizio)
            // groupIndex + 1 è il primo nodo avvolto
            int maxTargetIndex = (path.size() - 1) - (groupIndex + 1);

            // Limitiamo il targetIndex per sicurezza
            int safeTargetIndex = Math.clamp(targetIndex, 0, maxTargetIndex);

            PlanNode nodeToWrap = path.get(groupIndex + 1 + safeTargetIndex);
            FlowDecorator copy = srcDecorator.cloneWithNode(nodeToWrap);

            PlanNode wrapperNode = path.get(groupIndex + safeTargetIndex);

            ReplaceNodeCommand cmd = new ReplaceNodeCommand(copy, wrapperNode, tgtFinder.getFoundGroupNodePosition());
            executeCommand(cmd);
        }
    }

    public void moveDecorator(WorkoutPlan plan, String sourceDecoratorId, String targetNodeId, int targetIndex) {
        if (plan == null) return;
        NodeFinderVisitor srcFinder = new NodeFinderVisitor(sourceDecoratorId);
        NodeFinderVisitor tgtFinder = new NodeFinderVisitor(targetNodeId);
        plan.accept(srcFinder);
        plan.accept(tgtFinder);

        if (srcFinder.isFound() && tgtFinder.isFound() && srcFinder.getFoundNode() instanceof FlowDecorator srcDecorator) {

            List<PlanNode> tgtPath = tgtFinder.getFoundPath();
            int tgtGroupIndex = tgtFinder.getFoundGroupNodeIndex();

            int maxTargetIndex = (tgtPath.size() - 1) - (tgtGroupIndex + 1);
            int safeTargetIndex = Math.clamp(targetIndex, 0, maxTargetIndex);

            PlanNode nodeToWrap = tgtPath.get(tgtGroupIndex + 1 + safeTargetIndex);
            PlanNode wrapperNode = tgtPath.get(tgtGroupIndex + safeTargetIndex);

            GroupNode srcGroup = srcFinder.getFoundGroupNodeParent();
            GroupNode tgtGroup = tgtFinder.getFoundGroupNodeParent();
            // Se condividono lo stesso Blocco genitore e lo stesso indice nel blocco, sono nella stessa catena di decoratori
            if (srcGroup != null && srcGroup == tgtGroup &&
                    srcFinder.getFoundGroupNodePosition() == tgtFinder.getFoundGroupNodePosition()) {
                List<PlanNode> srcPath = srcFinder.getFoundPath();
                int srcGroupIndex = srcFinder.getFoundGroupNodeIndex();
                int sourceIndex = (srcPath.size() - 1) - (srcGroupIndex + 1);

                // Usiamo il safeTargetIndex per garantire la sicurezza matematica
                if (safeTargetIndex == sourceIndex || safeTargetIndex == sourceIndex + 1) {
                    return;
                }
            }

            CompositeCommand cmd = new CompositeCommand();
            // Estraiamo il decoratore dal suo vecchio padre
            cmd.addCommand(new ReplaceNodeCommand(srcDecorator.getWrappedNode(), srcFinder.getFoundParent(), srcFinder.getFoundPosition()));
            // Diciamo al decoratore appena estratto di avvolgere il nuovo nodo di destinazione
            cmd.addCommand(new ReplaceNodeCommand(nodeToWrap, srcDecorator, -1));
            // Diciamo al nuovo padre di avvolgere il decoratore
            cmd.addCommand(new ReplaceNodeCommand(srcDecorator, wrapperNode, tgtFinder.getFoundGroupNodePosition()));

            executeCommand(cmd);
        }
    }

    public void addDecoratorFromToolbox(WorkoutPlan plan, String decoratorType, String value, String targetNodeId) {
        if (plan == null) return;

        FlowDecorator newDecorator = getFlowDecorator(decoratorType, value);
        if (newDecorator == null) return;

        NodeFinderVisitor finder = new NodeFinderVisitor(targetNodeId);
        plan.accept(finder);

        if (finder.isFound()) {

            newDecorator.setWrappedNode(finder.getFoundNode());

            ReplaceNodeCommand cmd = new ReplaceNodeCommand(newDecorator, finder.getFoundParent(), finder.getFoundPosition());
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
            default -> throw new IllegalArgumentException("Unknown decorator type: " + decoratorType);
        }
        return newDecorator;
    }

    public void addModifierFromToolbox(WorkoutPlan plan, String modifierType, String value, String targetNodeId) {
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

    public List<String> getAvailableVariablesForNode(WorkoutPlan plan, String nodeId) {
        if (plan == null) return List.of();
        AvailableVariablesVisitor visitor = new AvailableVariablesVisitor(nodeId);
        plan.accept(visitor);
        return visitor.getAvailableVariables();
    }
}
