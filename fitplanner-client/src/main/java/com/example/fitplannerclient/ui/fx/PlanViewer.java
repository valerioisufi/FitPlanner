package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.dto.plan.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PlanViewer extends VBox {
    public PlanViewer(){
        this.getStyleClass().add("card");

        WorkoutPlanBean plan = createComplexPlanBean();
        PlanNodeBean rootBean = plan.getSessions().getFirst().getPlanRoot();

        buildPlan(rootBean, null);

    }

    public void buildPlan(PlanNodeBean nodeBean, PlanNodeComponent parent) {
        PlanNodeComponent node = new PlanNodeComponent(nodeBean.getName());
        node.setModifiers(nodeBean.getModifiers());
        node.setFlowDecorators(nodeBean.getFlowDecorators());

        for(PlanNodeBean childBean : nodeBean.getChildren()) {
            buildPlan(childBean, node);
        }

        Objects.requireNonNullElse(parent, this).getChildren().add(node);

    }

    public WorkoutPlanBean createComplexPlanBean() {
        List<WorkoutSessionBean> sessions = new ArrayList<>();
        WorkoutPlanBean plan = new WorkoutPlanBean("plan-monster-1", "Elite Powerbuilding Mesocycle", sessions);

        // ==========================================
        // SESSION 1: MONSTER LEG DAY
        // ==========================================
        PlanNodeBean session1Root = new PlanNodeBean("s1-root", "Lower Body: Maximum Hypertrophy", NodeType.BLOCK);

        // --- 1. MOBILITY & ACTIVATION (PROTOCOL BLOCK) ---
        PlanNodeBean mobilityBlock = new PlanNodeBean("s1-mob", "Mobility & Glute Activation", NodeType.PROTOCOL_BLOCK);
        mobilityBlock.addFlowDecorator(new FlowDecoratorBean("fd-mob-time", FlowDecoratorType.TIME_LIMIT, "10 Mins"));

        PlanNodeBean legSwings = new PlanNodeBean("ex-ls", "Dynamic Leg Swings", NodeType.EXERCISE);
        legSwings.addModifier(new ExerciseModifierBean("mod-ls-reps", "Reps", "15/leg"));

        PlanNodeBean bandedWalks = new PlanNodeBean("ex-bw", "Banded Lateral Walks", NodeType.EXERCISE);
        bandedWalks.addModifier(new ExerciseModifierBean("mod-bw-dist", "Distance", "20m"));
        bandedWalks.addModifier(new ExerciseModifierBean("mod-bw-loop", "Loops", "2"));

        mobilityBlock.addChild(legSwings);
        mobilityBlock.addChild(bandedWalks);


        // --- 2. HEAVY COMPOUND BLOCK ---
        PlanNodeBean strengthBlock = new PlanNodeBean("s1-str", "Heavy Primary Compound", NodeType.BLOCK);
        strengthBlock.addFlowDecorator(new FlowDecoratorBean("fd-str-loop", FlowDecoratorType.LOOP, "5 Working Sets"));
        strengthBlock.addFlowDecorator(new FlowDecoratorBean("fd-str-rest", FlowDecoratorType.REST, "180s"));

        PlanNodeBean barbellSquat = new PlanNodeBean("ex-sq", "Barbell Back Squat", NodeType.EXERCISE);
        barbellSquat.addModifier(new ExerciseModifierBean("mod-sq-reps", "Reps", "5"));
        barbellSquat.addModifier(new ExerciseModifierBean("mod-sq-rpe", "RPE", "8.5"));
        barbellSquat.addModifier(new ExerciseModifierBean("mod-sq-tut", "TUT", "3-1-X-1"));
        barbellSquat.addModifier(new ExerciseModifierBean("mod-sq-load", "Load", "80% 1RM"));

        strengthBlock.addChild(barbellSquat);


        // --- 3. VOLUME SUPERSET BLOCK (NESTED BLOCKS) ---
        PlanNodeBean supersetContainer = new PlanNodeBean("s1-ss-cont", "Quad & Hamstring Superset", NodeType.BLOCK);
        supersetContainer.addFlowDecorator(new FlowDecoratorBean("fd-ss-loop", FlowDecoratorType.LOOP, "4 Rounds"));
        supersetContainer.addFlowDecorator(new FlowDecoratorBean("fd-ss-rest", FlowDecoratorType.REST, "90s between rounds"));

        // First exercise of superset
        PlanNodeBean legPress = new PlanNodeBean("ex-lp", "Hack Squat / Leg Press", NodeType.EXERCISE);
        legPress.addModifier(new ExerciseModifierBean("mod-lp-reps", "Reps", "12-15"));
        legPress.addModifier(new ExerciseModifierBean("mod-lp-rpe", "RPE", "9"));
        legPress.addFlowDecorator(new FlowDecoratorBean("fd-lp-rest", FlowDecoratorType.REST, "10s (Transition)"));

        // Second exercise of superset
        PlanNodeBean rdl = new PlanNodeBean("ex-rdl", "Romanian Deadlift (Dumbbells)", NodeType.EXERCISE);
        rdl.addModifier(new ExerciseModifierBean("mod-rdl-reps", "Reps", "10"));
        rdl.addModifier(new ExerciseModifierBean("mod-rdl-tut", "TUT", "4-0-1-0"));
        rdl.addFlowDecorator(new FlowDecoratorBean("fd-rdl-prog", FlowDecoratorType.PROGRESSION, "Increase weight if RPE < 8"));

        supersetContainer.addChild(legPress);
        supersetContainer.addChild(rdl);


        // --- 4. ISOLATION GIANT SET ---
        PlanNodeBean giantSet = new PlanNodeBean("s1-gs", "Isolation Giant Set (Burnout)", NodeType.PROTOCOL_BLOCK);
        giantSet.addFlowDecorator(new FlowDecoratorBean("fd-gs-loop", FlowDecoratorType.LOOP, "3 Rounds"));
        giantSet.addFlowDecorator(new FlowDecoratorBean("fd-gs-rest", FlowDecoratorType.REST, "NO REST"));

        PlanNodeBean legExt = new PlanNodeBean("ex-le", "Leg Extensions", NodeType.EXERCISE);
        legExt.addModifier(new ExerciseModifierBean("mod-le-reps", "Reps", "20"));
        legExt.addModifier(new ExerciseModifierBean("mod-le-tut", "TUT", "1-0-1-2 (Hold at top)"));

        PlanNodeBean legCurl = new PlanNodeBean("ex-lc", "Seated Leg Curls", NodeType.EXERCISE);
        legCurl.addModifier(new ExerciseModifierBean("mod-lc-reps", "Reps", "15"));

        PlanNodeBean calfRaise = new PlanNodeBean("ex-cr", "Standing Calf Raises", NodeType.EXERCISE);
        calfRaise.addModifier(new ExerciseModifierBean("mod-cr-reps", "Reps", "Failure"));

        giantSet.addChild(legExt);
        giantSet.addChild(legCurl);
        giantSet.addChild(calfRaise);


        // --- ASSEMBLE THE SESSION ---
        session1Root.addChild(mobilityBlock);
        session1Root.addChild(strengthBlock);
        session1Root.addChild(supersetContainer);
        session1Root.addChild(giantSet);

        WorkoutSessionBean session1 = new WorkoutSessionBean("session-1", "Day 1: Monster Leg Day", session1Root);
        plan.addSession(session1);

        return plan;
    }

}
