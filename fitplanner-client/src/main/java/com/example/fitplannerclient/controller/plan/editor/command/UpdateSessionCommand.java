package com.example.fitplannerclient.controller.plan.editor.command;

import com.example.fitplannerclient.entity.plan.WorkoutPlan;
import com.example.fitplannerclient.entity.plan.WorkoutSession;

public class UpdateSessionCommand implements WorkoutPlanEditorCommand {

    private WorkoutPlan plan;
    private WorkoutSession session;

    private String oldName;
    private String newName;
    private int oldDay;
    private int newDay;

    public UpdateSessionCommand(WorkoutPlan plan, WorkoutSession session, String newName, int newDay) {
        this.plan = plan;
        this.session = session;

        this.newName = newName;
        this.newDay = newDay;
    }

    @Override
    public void execute() {
        oldName = session.getName();
        oldDay = session.getDay();

        if (oldDay != newDay) {
            plan.removeSession(oldDay);
            session.setDay(newDay);
            plan.addSession(session);
        }
        session.setName(newName);
    }

    @Override
    public void undo() {
        if (oldDay != newDay) {
            plan.removeSession(newDay);
            session.setDay(oldDay);
            plan.addSession(session);
        }
        session.setName(oldName);
    }
}
