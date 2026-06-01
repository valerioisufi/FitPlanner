package com.example.fitplannerclient.ui.fx.view.plan.editor.dnd;

import com.example.fitplannerclient.bean.plan.NodeType;
import com.example.fitplannerclient.ui.fx.event.PlanNodeEvent;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.PlanNodeComponent;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class NodeDragHandler {

    private static final String NODE_PREFIX = "NODE_";
    private static final String TOOLBOX_PREFIX = "TOOLBOX:";
    private static final String DROP_ABOVE = "drop-above";
    private static final String DROP_BELOW = "drop-below";
    private static final String DROP_INSIDE = "drop-inside";

    private static String currentDraggedNodeId = null;
    private static PlanNodeComponent currentDropTarget = null;

    public static void setup(PlanNodeComponent component, VBox dragHandle) {
        if (component.getParentWrapper() != null) {
            dragHandle.setOnDragDetected(event -> {
                Dragboard db = dragHandle.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(DragConstants.FITPLANNER_FORMAT, NODE_PREFIX + component.getPlanNodeId());
                db.setContent(content);

                SnapshotParameters snapParams = new SnapshotParameters();
                snapParams.setFill(Color.TRANSPARENT);
                db.setDragView(dragHandle.snapshot(snapParams, null));

                currentDraggedNodeId = component.getPlanNodeId();
                component.setOpacity(0.5);
                event.consume();
            });
        }
        component.setOnDragOver(event -> {
            boolean hasFitData = event.getDragboard().hasContent(DragConstants.FITPLANNER_FORMAT);
            String payload = hasFitData ? (String) event.getDragboard().getContent(DragConstants.FITPLANNER_FORMAT) : "";
            
            boolean isToolboxBadge = payload.startsWith(TOOLBOX_PREFIX + "MODIFIER") || payload.startsWith(TOOLBOX_PREFIX + "DECORATOR");
            boolean isToolboxNode = payload.startsWith(TOOLBOX_PREFIX) && !isToolboxBadge;
            boolean isNode = payload.startsWith(NODE_PREFIX);

            if (isToolboxBadge && component.getParentWrapper() == null) {
                return;
            }

            if (isToolboxNode || isToolboxBadge) {
                event.acceptTransferModes(TransferMode.COPY);
            } else if (isNode && currentDraggedNodeId != null && !currentDraggedNodeId.equals(component.getPlanNodeId())) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            if (isToolboxNode || isToolboxBadge || isNode) {
                if (currentDropTarget != null && currentDropTarget != component) {
                    currentDropTarget.getStyleClass().removeAll(DROP_ABOVE, DROP_BELOW, DROP_INSIDE);
                }
                currentDropTarget = component;

                component.getStyleClass().removeAll(DROP_ABOVE, DROP_BELOW, DROP_INSIDE);
                DropPosition pos;
                if (isToolboxBadge) {
                    pos = DropPosition.INSIDE;
                } else {
                    pos = getDropPosition(component, event.getY());
                }
                
                if (pos == DropPosition.ABOVE) component.getStyleClass().add(DROP_ABOVE);
                else if (pos == DropPosition.BELOW) component.getStyleClass().add(DROP_BELOW);
                else component.getStyleClass().add(DROP_INSIDE);
            }
            event.consume();
        });

        component.setOnDragExited(event -> {
            component.getStyleClass().removeAll(DROP_ABOVE, DROP_BELOW, DROP_INSIDE);
            if (currentDropTarget == component) {
                currentDropTarget = null;
            }
            event.consume();
        });

        component.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard db = event.getDragboard();
            
            boolean hasFitData = db.hasContent(DragConstants.FITPLANNER_FORMAT);
            String payload = hasFitData ? (String) db.getContent(DragConstants.FITPLANNER_FORMAT) : "";
            boolean isToolboxBadge = payload.startsWith(TOOLBOX_PREFIX + "MODIFIER") || payload.startsWith(TOOLBOX_PREFIX + "DECORATOR");

            if (isToolboxBadge && component.getParentWrapper() == null) {
                return;
            }

            DropPosition pos = isToolboxBadge ? DropPosition.INSIDE : getDropPosition(component, event.getY());

            String targetParentId = null;
            int targetIndex = -1;

            if (isToolboxBadge) {
                targetParentId = component.getPlanNodeId();
                // targetIndex remains -1 since order for badges might not matter when dropping, or they are just appended
            } else if (pos == DropPosition.INSIDE) {
                targetParentId = component.getPlanNodeId();
                targetIndex = component.getChildrenContainer().getChildren().size();

            } else if (component.getParentWrapper() != null) {
                targetParentId = component.getParentWrapper().getPlanNodeId();
                targetIndex = component.getParentWrapper().getChildrenContainer().getChildren().indexOf(component);
                if (pos == DropPosition.BELOW) targetIndex++;
            }

            if (targetParentId != null) {
                if (payload.startsWith(NODE_PREFIX) && currentDraggedNodeId != null) {
                    boolean isCopy = event.getTransferMode() != TransferMode.COPY;
                    component.fireEvent(
                        new PlanNodeEvent(PlanNodeEvent.NODE_REORDERED, currentDraggedNodeId)
                            .setTargetParentId(targetParentId)
                            .setTargetIndex(targetIndex)
                            .setIsCopy(isCopy)
                    );
                    success = true;

                } else if (payload.startsWith(TOOLBOX_PREFIX)) {
                    String toolboxPayload = payload.substring(TOOLBOX_PREFIX.length());
                    component.fireEvent(
                        new PlanNodeEvent(PlanNodeEvent.TOOLBOX_ITEM_DROPPED, component.getPlanNodeId())
                            .setPayload(toolboxPayload)
                            .setTargetParentId(targetParentId)
                            .setTargetIndex(targetIndex)
                    );
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        dragHandle.setOnDragDone(event -> {
            component.setOpacity(1.0);
            currentDraggedNodeId = null;

            if (currentDropTarget != null) {
                currentDropTarget.getStyleClass().removeAll(DROP_ABOVE, DROP_BELOW, DROP_INSIDE);
                currentDropTarget = null;
            }

            component.getStyleClass().removeAll(DROP_ABOVE, DROP_BELOW, DROP_INSIDE);
            event.consume();
        });
    }

    private enum DropPosition {
        ABOVE, BELOW, INSIDE
    }

    private static DropPosition getDropPosition(PlanNodeComponent component, double eventY) {
        NodeType type = component.getOriginalBean().getType();
        if (type == NodeType.BLOCK || type == NodeType.PROTOCOL_BLOCK) {
            double threshold = 20.0;

            if (eventY < threshold && component.getParentWrapper() != null) {
                return DropPosition.ABOVE;
            } else if (eventY > component.getHeight() - threshold && component.getParentWrapper() != null) {
                return DropPosition.BELOW;
            } else {
                return DropPosition.INSIDE;
            }

        } else {
            return eventY < (component.getHeight() / 2) ? DropPosition.ABOVE : DropPosition.BELOW;
        }
    }
}
