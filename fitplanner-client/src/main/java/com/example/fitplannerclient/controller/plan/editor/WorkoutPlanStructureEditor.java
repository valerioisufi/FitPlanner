package com.example.fitplannerclient.controller.plan.editor;

import com.example.fitplannerclient.controller.plan.core.library.ProtocolLibraryManager;
import com.example.fitplannerclient.entity.plan.visitor.NodeFinderVisitor;
import com.example.fitplannerclient.controller.plan.editor.command.*;
import com.example.fitplannerclient.controller.plan.editor.observer.WorkoutPlanSubject;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;
import com.example.fitplannerclient.entity.plan.block.Block;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseModifier;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import com.example.fitplannerclient.entity.plan.exercise.ModifierType;

import java.util.Map;

public class WorkoutPlanStructureEditor {

    private final EditorHistoryManager historyManager;
    private final WorkoutPlanSubject workoutPlanSubject;
    private final ProtocolLibraryManager protocolLibraryManager;

    public WorkoutPlanStructureEditor(EditorHistoryManager historyManager, 
                                      WorkoutPlanSubject workoutPlanSubject,
                                      ProtocolLibraryManager protocolLibraryManager) {
        this.historyManager = historyManager;
        this.workoutPlanSubject = workoutPlanSubject;
        this.protocolLibraryManager = protocolLibraryManager;
    }

    private void executeCommand(WorkoutPlanEditorCommand command) {
        historyManager.executeCommand(command);
        workoutPlanSubject.notifyObservers();
    }

    public void changePlanName(WorkoutPlan plan, String newName) {
        if (plan != null) {
            plan.changeName(newName);
            workoutPlanSubject.notifyObservers();
        }
    }

    public void changeCycleLength(WorkoutPlan plan, int length) {
        if (plan != null) {
            plan.setCycleLength(length);
            workoutPlanSubject.notifyObservers();
        }
    }

    // Command methods for sessions and nodes

    public void addSession(WorkoutPlan plan, int day) {
        if (plan != null) {
            // Creo una nuova sessione con un Block vuoto come root
            Block root = new Block("Sessione Giorno " + day);
            WorkoutSession session = new WorkoutSession("Sessione " + day, day, root);

            AddSessionCommand cmd = new AddSessionCommand(plan, session);
            executeCommand(cmd);
        }
    }

    public void removeSession(WorkoutPlan plan, int day) {
        if (plan != null) {
            RemoveSessionCommand cmd = new RemoveSessionCommand(plan, day);
            executeCommand(cmd);
        }
    }

    public void updateSessionName(WorkoutPlan plan, int day, String name) {
        if (plan != null) {
            WorkoutSession session = plan.getSessionByDay(day);

            if (session != null) {
                UpdateSessionCommand cmd = new UpdateSessionCommand(plan, session, name, session.getDay());
                executeCommand(cmd);
            }
        }
    }

    public void updateSessionDay(WorkoutPlan plan, int oldDay, int newDay) {
        if (plan != null) {
            WorkoutSession session = plan.getSessionByDay(oldDay);

            if (session != null && plan.getSessionByDay(newDay) == null) {
                UpdateSessionCommand cmd = new UpdateSessionCommand(plan, session, session.getName(), newDay);
                executeCommand(cmd);
            }
        }
    }

    public void removeNode(WorkoutPlan plan, String nodeId) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        finder.visit(plan);

        if (finder.isFound()) {
            RemoveNodeCommand cmd = new RemoveNodeCommand(finder.getFoundGroupNodeParent(), finder.getFoundGroupNodePosition());
            executeCommand(cmd);
        }
    }

    public void renameNode(WorkoutPlan plan, String nodeId, String newName) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof Block block) {
            RenameBlockCommand cmd = new RenameBlockCommand(block, newName);
            executeCommand(cmd);
        }
    }

    public void changeExerciseResource(WorkoutPlan plan, String nodeId, String newResourceId) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof ExerciseNode node) {
            ChangeExerciseResourceCommand cmd = new ChangeExerciseResourceCommand(node, newResourceId);
            executeCommand(cmd);
        }
    }

    public void emptyNode(WorkoutPlan plan, String nodeId) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof GroupNode groupNode) {
            CompositeCommand cmd = new CompositeCommand();

            // Remove nodes from last to first
            for (int i = groupNode.getChildrenCount() - 1; i >= 0; i--) {
                cmd.addCommand(new RemoveNodeCommand(groupNode, i));
            }
            executeCommand(cmd);
        }
    }

    public void updateProtocolParameters(WorkoutPlan plan, String nodeId, Map<String, String> params) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        if (finder.isFound() && finder.getFoundNode() instanceof ProtocolBlock block) {
            UpdateProtocolParametersCommand cmd = new UpdateProtocolParametersCommand(block, params);
            executeCommand(cmd);
        }
    }

    public void copyNode(WorkoutPlan plan, String nodeId, String targetParentId, int targetIndex) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        NodeFinderVisitor targetFinder = new NodeFinderVisitor(targetParentId);
        plan.accept(targetFinder);

        if (finder.isFound() && targetFinder.isFound() && targetFinder.getFoundNode() instanceof GroupNode targetParent) {
            PlanNode copy = finder.getFoundOutmostNode().deepCopy();

            InsertNodeCommand cmd = new InsertNodeCommand(copy, targetParent, targetIndex);
            executeCommand(cmd);
        }
    }

    public void duplicateNode(WorkoutPlan plan, String nodeId) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        if (finder.isFound()) {
            PlanNode copy = finder.getFoundOutmostNode().deepCopy();

            InsertNodeCommand cmd = new InsertNodeCommand(copy, finder.getFoundGroupNodeParent(), finder.getFoundGroupNodePosition() + 1);
            executeCommand(cmd);
        }
    }

    public void moveNode(WorkoutPlan plan, String nodeId, String targetParentId, int targetIndex) {
        if (plan == null) return;
        NodeFinderVisitor finder = new NodeFinderVisitor(nodeId);
        plan.accept(finder);

        NodeFinderVisitor targetFinder = new NodeFinderVisitor(targetParentId);
        plan.accept(targetFinder);

        if (finder.isFound() && targetFinder.isFound() && targetFinder.getFoundNode() instanceof GroupNode targetParent) {
            
            int sourceIndex = finder.getFoundGroupNodePosition();
            GroupNode sourceParent = finder.getFoundGroupNodeParent();

            if (sourceIndex == targetIndex && sourceParent == targetParent) return;

            if (sourceParent == targetParent && sourceIndex < targetIndex) {
                targetIndex--;
            }

            RemoveNodeCommand removeCmd = new RemoveNodeCommand(sourceParent, sourceIndex);
            InsertNodeCommand insertCmd = new InsertNodeCommand(finder.getFoundOutmostNode(), targetParent, targetIndex);
            
            CompositeCommand cmd = new CompositeCommand();
            cmd.addCommand(removeCmd);
            cmd.addCommand(insertCmd);
            executeCommand(cmd);
        }
    }

    public void addExerciseFromToolbox(WorkoutPlan plan, String exerciseId, String targetParentId, int targetIndex) {
        ExerciseNode node = new ExerciseNode();
        node.setResourceId(exerciseId);
        node.addModifier(new ExerciseModifier(ModifierType.REPS, "10"));
        addNodeFromToolbox(plan, node, targetParentId, targetIndex);
    }

    public void addBlockFromToolbox(WorkoutPlan plan, String blockName, String targetParentId, int targetIndex) {
        addNodeFromToolbox(plan, new Block(blockName), targetParentId, targetIndex);
    }

    public void addProtocolBlockFromToolbox(WorkoutPlan plan, String protocolName, Map<String, String> parameters, String targetParentId, int targetIndex) {
        ProtocolBlock block = protocolLibraryManager.getProtocolBlock(protocolName);

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            block.setParameter(entry.getKey(), entry.getValue());
        }

        addNodeFromToolbox(plan, block, targetParentId, targetIndex);
    }

    private void addNodeFromToolbox(WorkoutPlan plan, PlanNode newNode, String targetParentId, int targetIndex) {
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
}
