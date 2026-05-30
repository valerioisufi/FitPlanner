package com.example.fitplannerclient.entity.plan.exercise;

import com.example.fitplannerclient.controller.plan.visitor.WorkoutPlanVisitor;
import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.context.ExecutionResult;
import com.example.fitplannerclient.entity.plan.context.PlanNodeState;

import java.util.*;

public class ExerciseNode extends PlanNode {
    private String resourceId;
    private Map<ModifierType, ExerciseModifier> modifiers = new EnumMap<>(ModifierType.class);

    public ExerciseNode(String resourceId) {
        this.resourceId = resourceId;
    }

    public ExerciseNode() {}

    @Override
    public PlanNode deepCopy() {
        ExerciseNode copy = new ExerciseNode(this.resourceId);
        for (Map.Entry<ModifierType, ExerciseModifier> entry : this.modifiers.entrySet()) {
            copy.modifiers.put(entry.getKey(), new ExerciseModifier(entry.getValue()));
        }
        return copy;
    }

    @Override
    public void accept(WorkoutPlanVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public ExecutionResult execute(ExecutionContext context) {
        if (this.state == PlanNodeState.COMPLETED) {
            return new ExecutionResult(PlanNodeState.COMPLETED);
        }

        if (context.consumeSignal(ControlSignal.SKIP_NEXT)) {
            context.consumeTickDelta(context.getTickDelta());

            this.state = PlanNodeState.SKIPPED;
            return new ExecutionResult(PlanNodeState.SKIPPED);
        }
        else if (context.consumeSignal(ControlSignal.SKIP_PREVIOUS)) {
            context.consumeTickDelta(context.getTickDelta());

            this.state = PlanNodeState.IDLE;
            return new ExecutionResult(PlanNodeState.REVERT);
        }

        if (this.state == PlanNodeState.IDLE) {
            // l'esercizio è iniziato
            context.setActiveNode(this);
            this.state = PlanNodeState.RUNNING;
            return new ExecutionResult(PlanNodeState.RUNNING);
        } else if (this.state == PlanNodeState.RUNNING) {

            if (context.consumeSignal(ControlSignal.DONE)) {
                // l'esercizio è stato contrassegnato come completato
                context.consumeTickDelta(context.getTickDelta());

                this.state = PlanNodeState.COMPLETED;
                return new ExecutionResult(PlanNodeState.COMPLETED);
            }

            return new ExecutionResult(PlanNodeState.RUNNING);
        } else {
            return new ExecutionResult(this.state);
        }
    }

    @Override
    public void reset() {
        this.state = PlanNodeState.IDLE;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public Collection<ExerciseModifier> getModifiers() {
        return Collections.unmodifiableCollection(modifiers.values());
    }

    public void addModifier(ExerciseModifier modifier) {
        modifiers.put(modifier.getType(), modifier);
    }

    public ExerciseModifier removeModifier(ModifierType type) {
        return modifiers.remove(type);
    }

    public ExerciseModifier getModifier(ModifierType type) {
        return modifiers.get(type);
    }

    public boolean hasModifier(ModifierType type) {
        return modifiers.containsKey(type);
    }

    public List<ExerciseModifier> getResolvedModifiers(ExecutionContext context) {
        if (context == null) {
            return new ArrayList<>(modifiers.values());
        }
        
        List<ExerciseModifier> resolved = new ArrayList<>();

        for (ExerciseModifier mod : modifiers.values()) {
            String resolvedValue = context.resolveVariables(mod.getValue());
            resolved.add(new ExerciseModifier(mod.getType(), resolvedValue));
        }

        return resolved;
    }
}


