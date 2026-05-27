package com.example.fitplannerclient.controller.plan.command;

import com.example.fitplannerclient.entity.plan.WorkoutSession;

public class UpdateSessionCommand implements WorkoutPlanEditorCommand {

    private WorkoutSession session;

    private String oldName;
    private String newName;
    private int oldDay;
    private int newDay;

    public UpdateSessionCommand(WorkoutSession session, String newName, int newDay) {
        this.session = session;

        this.newName = newName;
        this.newDay = newDay;
    }

    @Override
    public void execute() {
        oldName = session.getName();
        oldDay = session.getDay();

        session.setName(newName);
        session.setDay(newDay);
    }

    @Override
    public void undo() {
        session.setName(oldName);
        session.setDay(oldDay);
    }
}
