package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.dto.plan.*;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PlanViewer extends ScrollPane {

    public PlanViewer() {
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        WorkoutPlanBean plan = createComplexPlanBean();
        PlanNodeBean rootBean = plan.getSessions().getFirst().getPlanRoot();

        PlanNodeComponent rootWrapper = buildTree(rootBean, true, null);

        this.setContent(rootWrapper);
    }

    private PlanNodeComponent buildTree(PlanNodeBean bean, boolean startExpanded, PlanNodeComponent parentWrapper) {
        PlanNodeComponent wrapper = new PlanNodeComponent(bean, startExpanded, parentWrapper);

        wrapper.setOnNodeTransformationCallback(event -> {
            System.out.println("CONTROLLER: Sposta Nodo " + event.draggedNodeId() + " in " + event.targetParentId());
            // TODO: controller.handleNodeMove(event);
        });

        wrapper.setOnBadgeTransformationCallback(event -> {
            System.out.println("CONTROLLER: Sposta " + event.type() + " dal nodo " + event.sourceNodeId() + " al nodo " + event.targetNodeId());
            // TODO: controller.handleBadgeMove(event);
        });

        for (PlanNodeBean childBean : bean.getChildren()) {
            wrapper.addChildNode(buildTree(childBean, false, wrapper));
        }

        return wrapper;
    }

    public WorkoutPlanBean createComplexPlanBean() {
        List<WorkoutSessionBean> sessions = new ArrayList<>();
        WorkoutPlanBean plan = new WorkoutPlanBean("plan-stress-test", "Performance Stress Test Plan", sessions);

        PlanNodeBean session1Root = new PlanNodeBean("s1-root", "Massive Stress Test Container", NodeType.BLOCK);
        session1Root.addFlowDecorator(new FlowDecoratorBean("fd-root-time", FlowDecoratorType.TIME_LIMIT, "999 Mins"));

        int numBlocks = 100;
        int exercisesPerBlock = 50;

        for (int i = 0; i < numBlocks; i++) {
            PlanNodeBean blockNode = new PlanNodeBean("block-" + i, "Stress Block " + i, NodeType.BLOCK);
            blockNode.addFlowDecorator(new FlowDecoratorBean("fd-loop-" + i, FlowDecoratorType.LOOP, (i % 4 + 1) + " Rounds"));
            blockNode.addFlowDecorator(new FlowDecoratorBean("fd-rest-" + i, FlowDecoratorType.REST, "60s"));

            for (int j = 0; j < exercisesPerBlock; j++) {
                PlanNodeBean exerciseNode = new PlanNodeBean("ex-" + i + "-" + j, "Generated Exercise " + i + " - " + j, NodeType.EXERCISE);
                exerciseNode.addModifier(new ExerciseModifierBean("mod-set-" + i + "-" + j, "Sets", "4"));
                exerciseNode.addModifier(new ExerciseModifierBean("mod-rep-" + i + "-" + j, "Reps", String.valueOf(8 + (j % 10))));

                if (j % 2 == 0) exerciseNode.addModifier(new ExerciseModifierBean("mod-tut-" + i + "-" + j, "TUT", "3-0-1-0"));
                if (j % 3 == 0) exerciseNode.addModifier(new ExerciseModifierBean("mod-rpe-" + i + "-" + j, "RPE", "8.5"));

                exerciseNode.addFlowDecorator(new FlowDecoratorBean("fd-ex-rest-" + i + "-" + j, FlowDecoratorType.REST, "30s"));
                blockNode.addChild(exerciseNode);
            }
            session1Root.addChild(blockNode);
        }

        WorkoutSessionBean session1 = new WorkoutSessionBean("session-1", "Stress Test Day", session1Root);
        plan.addSession(session1);
        return plan;
    }
}