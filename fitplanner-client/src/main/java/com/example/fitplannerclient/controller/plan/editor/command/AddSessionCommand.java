package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;

public class AddSessionCommand implements WorkoutPlanEditorCommand {

    private WorkoutPlan plan;
    private WorkoutSession session;

    public AddSessionCommand(WorkoutPlan plan, WorkoutSession session) {
        this.plan = plan;
        this.session = session;
    }

    @Override
    public void execute() {
        plan.addSession(session);
    }

    @Override
    public void undo() {
        plan.removeSession(session.getDay());
    }

}
