package com.example.fitplannerclient.dto.plan;

import java.util.List;

public class WorkoutPlanBean {
    private String id;
    private String name;

    private List<WorkoutSessionBean> sessions;

    public WorkoutPlanBean(String id, String name, List<WorkoutSessionBean> sessions) {
        this.id = id;
        this.name = name;
        this.sessions = sessions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<WorkoutSessionBean> getSessions(){
        return sessions;
    }

    public void setSessions(List<WorkoutSessionBean> sessions) {
        this.sessions = sessions;
    }

    public void addSession(WorkoutSessionBean session) {
        if (session != null) this.sessions.add(session);
    }
}
