package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.PlanNode;
import com.example.fitplannerclient.entity.plan.block.GroupNode;
import com.example.fitplannerclient.entity.plan.decorator.FlowDecorator;

import java.util.function.Consumer;

public class ReplaceNodeCommand implements WorkoutPlanEditorCommand {

    private PlanNode oldNode; // il nodo precedente da sostituire
    private final PlanNode newNode; // il nuovo nodo sostitutivo
    private PlanNode parent;
    private int index = -1; // indice all'interno del GroupNode (se applicabile)

    public ReplaceNodeCommand(PlanNode newNode, PlanNode parent, int index) {
        this.newNode = newNode;
        this.parent = parent;
        this.index = index;
    }

    @Override
    public void execute() {
        executeIfGroupNode(parent, groupNode ->
            oldNode = groupNode.replaceNode(index, newNode)
        );

        executeIfFlowDecorator(parent, flowDecorator -> {
            oldNode = flowDecorator.getWrappedNode();
            flowDecorator.setWrappedNode(newNode);
        });
    }

    @Override
    public void undo() {
        executeIfGroupNode(parent, groupNode ->
            groupNode.replaceNode(index, oldNode)
        );

        executeIfFlowDecorator(parent, flowDecorator ->
            flowDecorator.setWrappedNode(oldNode)
        );
    }

    private void executeIfGroupNode(PlanNode node, Consumer<GroupNode> action) {
        node.accept(new GroupingWorkoutPlanVisitor() {

            @Override
            public void visitGroupNode(GroupNode groupNode) {
                action.accept(groupNode);
            }

        });
    }

    private void executeIfFlowDecorator(PlanNode node, Consumer<FlowDecorator> action) {
        node.accept(new GroupingWorkoutPlanVisitor() {

            @Override
            public void visitFlowDecorator(FlowDecorator flowDecorator) {
                action.accept(flowDecorator);
            }

        });
    }

}