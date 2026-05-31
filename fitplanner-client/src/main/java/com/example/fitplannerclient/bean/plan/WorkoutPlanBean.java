package com.example.fitplannerclient.bean.plan;

import java.util.List;

public class WorkoutPlanBean {
    private String id;
    private String name;

    private int cycleLength;

    private List<WorkoutSessionBean> sessions;

    public WorkoutPlanBean() {}

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

    public int getCycleLength() {
        return cycleLength;
    }

    public void setCycleLength(int cycleLength) {
        this.cycleLength = cycleLength;
    }

    public List<WorkoutSessionBean> getSessions(){
        return sessions;
    }

    public void setSessions(List<WorkoutSessionBean> sessions) {
        this.sessions = sessions;
    }

    public WorkoutSessionBean getSession(int day) {
        if (sessions == null) return null;
        for (WorkoutSessionBean session : sessions) {
            if (session.getDay() == day) {
                return session;
            }
        }
        return null;
    }

    public void addSession(WorkoutSessionBean session) {
        sessions.add(session);
    }

    public PlanNodeBean findNodeById(String nodeId) {
        if (sessions == null) return null;
        for (WorkoutSessionBean session : sessions) {
            PlanNodeBean found = findInNode(session.getPlanRoot(), nodeId);
            if (found != null) return found;
        }
        return null;
    }

    private PlanNodeBean findInNode(PlanNodeBean current, String nodeId) {
        if (current == null) return null;
        if (current.getId().equals(nodeId)) return current;
        if (current.getChildren() != null) {
            for (PlanNodeBean child : current.getChildren()) {
                PlanNodeBean found = findInNode(child, nodeId);
                if (found != null) return found;
            }
        }
        return null;
    }
}
