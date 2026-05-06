package com.example.fitplannerclient.entity.plan;

public abstract class PlanNode {
    private String id;

    public abstract void accept();
    public abstract void execute();
    public abstract void reset();
}
