package com.example.fitplannerclient.ui.fx.view.plan.editor.dnd;

import com.example.fitplannerclient.ui.fx.event.PlanNodeEvent;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.PlanNodeComponent;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class BadgeDragHandler {

    private static final String BADGE_DROP_LEFT = "badge-drop-left";
    private static final String BADGE_DROP_RIGHT = "badge-drop-right";

    private static BadgeDragContext activeBadgeDrag;

    public record BadgeDragContext(
            String sourceNodeId,
            Object badgeData,
            BadgeComponent.BadgeType badgeType,
            int sourceIndex
    ) {}

    public static void setup(PlanNodeComponent component, Region badge, Object data, BadgeComponent.BadgeType type, int currentIndex) {
        badge.setOnDragDetected(e -> {
            Dragboard db = badge.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.put(DragConstants.FITPLANNER_FORMAT, "BADGE_" + type.name());
            db.setContent(content);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            db.setDragView(badge.snapshot(snapParams, null));

            activeBadgeDrag = new BadgeDragContext(component.getPlanNodeId(), data, type, currentIndex);

            badge.setOpacity(0.4);
            e.consume();
        });

        badge.setOnDragOver(e -> {
            if (e.getGestureSource() != badge && activeBadgeDrag != null && type.equals(activeBadgeDrag.badgeType())) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                boolean dropBefore = e.getX() < (badge.getWidth() / 2);
                badge.getStyleClass().removeAll(BADGE_DROP_LEFT, BADGE_DROP_RIGHT);
                badge.getStyleClass().add(dropBefore ? BADGE_DROP_LEFT : BADGE_DROP_RIGHT);
            }
            e.consume();
        });

        badge.setOnDragExited(e -> {
            badge.getStyleClass().removeAll(BADGE_DROP_LEFT, BADGE_DROP_RIGHT);
            e.consume();
        });

        badge.setOnDragDropped(e -> {
            boolean success = false;
            if (activeBadgeDrag != null && activeBadgeDrag.badgeData() != null && type.equals(activeBadgeDrag.badgeType())) {

                boolean dropBefore = e.getX() < (badge.getWidth() / 2);
                boolean isCopy = e.getTransferMode() != TransferMode.COPY || activeBadgeDrag.sourceNodeId() == null;

                int targetIndex = currentIndex;
                if (!dropBefore) targetIndex++;

                component.fireEvent(
                    new PlanNodeEvent(PlanNodeEvent.BADGE_REORDERED, component.getPlanNodeId())
                        .setBadgeType(type.name())
                        .setBadgeData(activeBadgeDrag.badgeData())
                        .setSourceNodeId(activeBadgeDrag.sourceNodeId())
                        .setSourceIndex(activeBadgeDrag.sourceIndex())
                        .setTargetIndex(targetIndex)
                        .setIsCopy(isCopy)
                );

                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });

        badge.setOnDragDone(e -> {
            badge.setOpacity(1.0);
            activeBadgeDrag = null;
            e.consume();
        });
    }

    public static void setupFallbackDropZone(PlanNodeComponent component, VBox dropArea) {
        dropArea.addEventHandler(DragEvent.DRAG_OVER, e -> {
            if (activeBadgeDrag != null) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                e.consume();
            }
        });

        dropArea.addEventHandler(DragEvent.DRAG_DROPPED, e -> {
            if (activeBadgeDrag != null) {
                boolean isCopy = e.getTransferMode() != TransferMode.COPY || activeBadgeDrag.sourceNodeId() == null;

                int targetIndex = Integer.MAX_VALUE; // will be clamped by the manager

                component.fireEvent(
                    new PlanNodeEvent(PlanNodeEvent.BADGE_REORDERED, component.getPlanNodeId())
                        .setBadgeType(activeBadgeDrag.badgeType().name())
                        .setBadgeData(activeBadgeDrag.badgeData())
                        .setSourceNodeId(activeBadgeDrag.sourceNodeId())
                        .setSourceIndex(activeBadgeDrag.sourceIndex())
                        .setTargetIndex(targetIndex)
                        .setIsCopy(isCopy)
                );

                e.setDropCompleted(true);
                e.consume();
            }
        });
    }

    public static void initiateExternalDrag(Object data, BadgeComponent.BadgeType type) {
        activeBadgeDrag = new BadgeDragContext(null, data, type, -1);
    }
}
