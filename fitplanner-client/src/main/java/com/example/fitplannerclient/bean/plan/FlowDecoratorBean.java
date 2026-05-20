package com.example.fitplannerclient.bean.plan;

public class FlowDecoratorBean {
    private String id;

    private FlowDecoratorType type;
    private String value;

    public FlowDecoratorBean() {}

    public FlowDecoratorBean(String id, FlowDecoratorType type, String value) {
        this.id = id;
        this.type = type;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public FlowDecoratorType getType() {
        return type;
    }

    public void setType(FlowDecoratorType type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
