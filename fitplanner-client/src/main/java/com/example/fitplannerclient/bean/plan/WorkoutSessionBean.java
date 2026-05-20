package com.example.fitplannerclient.bean.plan;

public class WorkoutSessionBean {
    private  String id;
    private String name;

    private PlanNodeBean planRoot;

    public WorkoutSessionBean() {}

    public WorkoutSessionBean(String id, String name, PlanNodeBean planRoot) {
        this.id = id;
        this.name = name;
        this.planRoot = planRoot;
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

    public PlanNodeBean getPlanRoot(){
        return planRoot;
    }

    public void setPlanRoot(PlanNodeBean planRoot) {
        this.planRoot = planRoot;
    }
}
