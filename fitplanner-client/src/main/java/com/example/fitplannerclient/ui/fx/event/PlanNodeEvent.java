package com.example.fitplannerclient.ui.fx.event;

import javafx.event.Event;
import javafx.event.EventType;

public class PlanNodeEvent extends Event {

    public static final EventType<PlanNodeEvent> ANY = new EventType<>(Event.ANY, "PLAN_NODE_ANY");
    public static final EventType<PlanNodeEvent> EDIT_NAME_CLICKED = new EventType<>(ANY, "EDIT_NAME_CLICKED");
    public static final EventType<PlanNodeEvent> EDIT_BADGE_CLICKED = new EventType<>(ANY, "EDIT_BADGE_CLICKED");
    public static final EventType<PlanNodeEvent> DELETE_NODE_REQUESTED = new EventType<>(ANY, "DELETE_NODE_REQUESTED");
    public static final EventType<PlanNodeEvent> TOOLBOX_ITEM_DROPPED = new EventType<>(ANY, "TOOLBOX_ITEM_DROPPED");
    public static final EventType<PlanNodeEvent> NODE_REORDERED = new EventType<>(ANY, "NODE_REORDERED");
    public static final EventType<PlanNodeEvent> BADGE_REORDERED = new EventType<>(ANY, "BADGE_REORDERED");

    // Event properties
    private String nodeId;
    private String targetParentId;
    private int targetIndex;
    
    // For Badge specific events
    private Object badgeData;
    private String badgeType; // e.g. "MODIFIER", "DECORATOR"
    private int sourceIndex;
    private String sourceNodeId;
    
    // For Toolbox drop
    private String payload;
    private boolean isCopy; // Used for drag/drop (copy vs move)

    public PlanNodeEvent(EventType<? extends Event> eventType) {
        super(eventType);
    }

    public PlanNodeEvent(EventType<? extends Event> eventType, String nodeId) {
        super(eventType);
        this.nodeId = nodeId;
    }

    public PlanNodeEvent setNodeId(String nodeId) {
        this.nodeId = nodeId;
        return this;
    }

    public PlanNodeEvent setTargetParentId(String targetParentId) {
        this.targetParentId = targetParentId;
        return this;
    }

    public PlanNodeEvent setTargetIndex(int targetIndex) {
        this.targetIndex = targetIndex;
        return this;
    }

    public PlanNodeEvent setBadgeData(Object badgeData) {
        this.badgeData = badgeData;
        return this;
    }

    public PlanNodeEvent setBadgeType(String badgeType) {
        this.badgeType = badgeType;
        return this;
    }

    public PlanNodeEvent setSourceIndex(int sourceIndex) {
        this.sourceIndex = sourceIndex;
        return this;
    }

    public PlanNodeEvent setSourceNodeId(String sourceNodeId) {
        this.sourceNodeId = sourceNodeId;
        return this;
    }

    public PlanNodeEvent setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public PlanNodeEvent setIsCopy(boolean isCopy) {
        this.isCopy = isCopy;
        return this;
    }

    // Getters
    public String getNodeId() { return nodeId; }
    public String getTargetParentId() { return targetParentId; }
    public int getTargetIndex() { return targetIndex; }
    public Object getBadgeData() { return badgeData; }
    public String getBadgeType() { return badgeType; }
    public int getSourceIndex() { return sourceIndex; }
    public String getSourceNodeId() { return sourceNodeId; }
    public String getPayload() { return payload; }
    public boolean isCopy() { return isCopy; }
}
