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

    private static String currentDraggedNodeId = null;

    public static void setup(PlanNodeComponent component, VBox dragHandle) {

        dragHandle.setOnDragDetected(event -> {
            Dragboard db = dragHandle.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(DragConstants.FITPLANNER_FORMAT, "NODE_" + component.getPlanNodeId());
            db.setContent(content);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            db.setDragView(dragHandle.snapshot(snapParams, null));

            currentDraggedNodeId = component.getPlanNodeId();
            component.setOpacity(0.5);
            event.consume();
        });

        component.setOnDragOver(event -> {
            boolean hasFitData = event.getDragboard().hasContent(DragConstants.FITPLANNER_FORMAT);
            String payload = hasFitData ? (String) event.getDragboard().getContent(DragConstants.FITPLANNER_FORMAT) : "";
            
            boolean isToolbox = payload.startsWith("TOOLBOX:");
            boolean isNode = payload.startsWith("NODE_");

            if (isToolbox) {
                event.acceptTransferModes(TransferMode.COPY);
            } else if (isNode && currentDraggedNodeId != null && !currentDraggedNodeId.equals(component.getPlanNodeId())) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            if (isToolbox || isNode) {
                boolean dropAbove = event.getY() < (component.getHeight() / 2);
                component.getStyleClass().removeAll("drop-above", "drop-below");
                component.getStyleClass().add(dropAbove ? "drop-above" : "drop-below");
            }
            event.consume();
        });

        component.setOnDragExited(event -> {
            component.getStyleClass().removeAll("drop-above", "drop-below");
            event.consume();
        });

        component.setOnDragDropped(event -> {
            boolean success = false;
            Dragboard db = event.getDragboard();
            boolean dropAbove = event.getY() < (component.getHeight() / 2);
            
            boolean hasFitData = db.hasContent(DragConstants.FITPLANNER_FORMAT);
            String payload = hasFitData ? (String) db.getContent(DragConstants.FITPLANNER_FORMAT) : "";

            if (payload.startsWith("NODE_") && currentDraggedNodeId != null) {
                boolean isCopy = event.getTransferMode() != TransferMode.COPY;

                PlanNodeComponent newParent = component.getParentWrapper();
                if (newParent != null) {
                    int targetIndex = newParent.getChildrenContainer().getChildren().indexOf(component);
                    if (!dropAbove) targetIndex++;

                    component.fireEvent(
                        new PlanNodeEvent(PlanNodeEvent.NODE_REORDERED, currentDraggedNodeId)
                            .setTargetParentId(newParent.getPlanNodeId())
                            .setTargetIndex(targetIndex)
                            .setIsCopy(isCopy)
                    );
                    success = true;
                }

            } else if (payload.startsWith("TOOLBOX:")) {
                String toolboxPayload = payload.substring("TOOLBOX:".length());

                String targetParentId;
                int targetIndex;

                if (component.getOriginalBean().getType() == NodeType.BLOCK) {
                    targetParentId = component.getPlanNodeId();
                    targetIndex = component.getChildrenContainer().getChildren().size();
                } else if (component.getParentWrapper() != null) {
                    targetParentId = component.getParentWrapper().getPlanNodeId();
                    targetIndex = component.getParentWrapper().getChildrenContainer().getChildren().indexOf(component);
                    if (!dropAbove) targetIndex++;
                } else {
                    targetParentId = null;
                    targetIndex = -1;
                }

                if (targetParentId != null) {
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
            component.getStyleClass().removeAll("drop-above", "drop-below");
            event.consume();
        });
    }
}
