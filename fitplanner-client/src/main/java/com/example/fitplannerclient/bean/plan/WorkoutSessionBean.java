package com.example.fitplannerclient.bean.plan;

public class WorkoutSessionBean {
    private String name;
    private int day;

    private PlanNodeBean planRoot;

    public WorkoutSessionBean() {}

    public WorkoutSessionBean(String name, int day, PlanNodeBean planRoot) {
        this.name = name;
        this.day = day;
        this.planRoot = planRoot;
    }

    public String getName(){
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDay(){
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public PlanNodeBean getPlanRoot(){
        return planRoot;
    }

    public void setPlanRoot(PlanNodeBean planRoot) {
        this.planRoot = planRoot;
    }
}
