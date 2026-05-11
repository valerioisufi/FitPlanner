package com.example.fitplannerclient.ui.fx;

import com.example.fitplannerclient.dto.plan.ExerciseModifierBean;
import com.example.fitplannerclient.dto.plan.FlowDecoratorBean;
import com.example.fitplannerclient.dto.plan.PlanNodeBean;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.example.fitplannerclient.ui.fx.BadgeComponent.resolveColorFromName;

public class PlanNodeComponent extends VBox {

    private final String planNodeId;
    // Keep a reference to the original bean for Optimistic Copying
    private final PlanNodeBean originalBean;

    private final List<ExerciseModifierBean> exerciseModifierBeans;
    private final List<FlowDecoratorBean> flowDecoratorBeans;

    private final Label nameLabel;
    private final FlowPane badgesBox;
    private final HBox inlineDecoratorsBox;
    private final VBox childrenContainer;

    private PlanNodeComponent parentWrapper;
    private boolean isExpanded;

    // Callbacks for the Controller
    private Consumer<BadgeTransformationEvent> onBadgeTransformationCallback;
    private Consumer<NodeTransformationEvent> onNodeTransformationCallback;

    // Static fields to track the currently dragged elements across the UI
    private static PlanNodeComponent draggedNode;
    private static PlanNodeComponent sourceBadgeComponent;
    private static Object draggedBadgeData;
    private static String draggedBadgeType; // "MODIFIER" or "DECORATOR"

    public PlanNodeComponent(PlanNodeBean bean, Boolean startExpanded, PlanNodeComponent parentWrapper) {
        this.originalBean = bean;
        this.planNodeId = bean.getId();
        this.parentWrapper = parentWrapper;

        this.exerciseModifierBeans = new ArrayList<>(bean.getModifiers() != null ? bean.getModifiers() : List.of());
        this.flowDecoratorBeans = new ArrayList<>(bean.getFlowDecorators() != null ? bean.getFlowDecorators() : List.of());

        this.getStyleClass().add("plan-node");

        this.nameLabel = new Label(bean.getName());
        nameLabel.getStyleClass().add("heading-h3");

        inlineDecoratorsBox = new HBox(8);
        inlineDecoratorsBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox titleBox = new HBox(16, nameLabel, inlineDecoratorsBox, spacer);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        badgesBox = new FlowPane(8, 8);
        badgesBox.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(8, titleBox, badgesBox);
        headerBox.setStyle("-fx-cursor: hand;");

        childrenContainer = new VBox(8);

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(childrenContainer.widthProperty());
        clipRect.heightProperty().bind(childrenContainer.heightProperty());
        childrenContainer.setClip(clipRect);

        isExpanded = startExpanded;
        updateExpansionState();

        // Expand/Collapse logic on click
        headerBox.setOnMouseClicked(e -> {
            if (e.isStillSincePress()) {
                isExpanded = !isExpanded;
                updateExpansionState();
            }
        });

        this.getChildren().addAll(headerBox, childrenContainer);

        // Render Initial Data
        renderModifiers();
        renderDecorators();

        // Initialize Drag and Drop logic
        setupNodeDragAndDrop(headerBox);
        setupFallbackBadgeDropZone();
    }

    private void updateExpansionState() {
        if (!isExpanded) {
            childrenContainer.setVisible(false);
            childrenContainer.setManaged(false);
            childrenContainer.setOpacity(0);
            childrenContainer.setMaxHeight(0);
        } else {
            childrenContainer.setVisible(true);
            childrenContainer.setManaged(true);
            childrenContainer.setOpacity(1);
            childrenContainer.setMaxHeight(Region.USE_COMPUTED_SIZE);
        }
    }

    public String getPlanNodeId() {
        return this.planNodeId;
    }

    public void addChildNode(PlanNodeComponent child) {
        childrenContainer.getChildren().add(child);
    }

    // --- CALLBACK SETTERS ---

    public void setOnBadgeTransformationCallback(Consumer<BadgeTransformationEvent> callback) {
        this.onBadgeTransformationCallback = callback;
    }

    public void setOnNodeTransformationCallback(Consumer<NodeTransformationEvent> callback) {
        this.onNodeTransformationCallback = callback;
    }

    // --- RENDERING METHODS ---

    private void renderModifiers() {
        badgesBox.getChildren().clear();

        if (exerciseModifierBeans.isEmpty()) {
            badgesBox.setVisible(false);
            badgesBox.setManaged(false);
        } else {
            badgesBox.setVisible(true);
            badgesBox.setManaged(true);

            for (ExerciseModifierBean modifier : exerciseModifierBeans) {
                BadgeComponent.BadgeColor color = resolveColorFromName(modifier.getName());
                Region badge = new BadgeComponent(
                        modifier.getId(),
                        BadgeComponent.BadgeType.MODIFIER,
                        modifier.getName(),
                        modifier.getValue(),
                        color
                );
                setupBadgeDragAndDrop(badge, modifier, "MODIFIER");
                badgesBox.getChildren().add(badge);
            }
        }
    }

    private void renderDecorators() {
        inlineDecoratorsBox.getChildren().clear();

        if (flowDecoratorBeans.isEmpty()) return;

        for (int i = 0; i < flowDecoratorBeans.size(); i++) {
            FlowDecoratorBean decorator = flowDecoratorBeans.get(i);

            String typeName = decorator.getType().name().replace("_", " ");
            BadgeComponent.BadgeColor color = resolveColorFromName(typeName);

            Region badge = new BadgeComponent(decorator.getId(), BadgeComponent.BadgeType.DECORATOR, typeName, decorator.getValue(), color);

            setupBadgeDragAndDrop(badge, decorator, "DECORATOR");
            inlineDecoratorsBox.getChildren().add(badge);

            if (i < flowDecoratorBeans.size() - 1) {
                Region chevron = new Region();
                chevron.getStyleClass().add("chevron-separator");
                inlineDecoratorsBox.getChildren().add(chevron);
            }
        }
    }

    // --- DRAG AND DROP LOGIC ---

    private void setupNodeDragAndDrop(VBox dragHandle) {

        dragHandle.setOnDragDetected(event -> {
            Dragboard db = dragHandle.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("NODE_" + planNodeId);
            db.setContent(content);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            db.setDragView(dragHandle.snapshot(snapParams, null));

            draggedNode = this;
            this.setOpacity(0.5);
            event.consume();
        });

        this.setOnDragOver(event -> {
            if (draggedNode != null && draggedNode != this && isNotAncestorOf(draggedNode, this)) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                boolean dropAbove = event.getY() < (this.getHeight() / 2);

                this.getStyleClass().removeAll("drop-above", "drop-below");
                this.getStyleClass().add(dropAbove ? "drop-above" : "drop-below");
            }
            event.consume();
        });

        this.setOnDragExited(event -> {
            this.getStyleClass().removeAll("drop-above", "drop-below");
            event.consume();
        });

        this.setOnDragDropped(event -> {
            boolean success = false;
            if (draggedNode != null && draggedNode != this && isNotAncestorOf(draggedNode, this)) {

                boolean dropAbove = event.getY() < (this.getHeight() / 2);
                boolean isCopy = event.getTransferMode() == TransferMode.COPY;

                PlanNodeComponent newParent = this.parentWrapper;
                if (newParent != null) {
                    int targetIndex = newParent.childrenContainer.getChildren().indexOf(this);
                    if (!dropAbove) targetIndex++;

                    String sourceParentId = draggedNode.parentWrapper != null ? draggedNode.parentWrapper.getPlanNodeId() : null;

                    if (isCopy) {
                        PlanNodeComponent optimisticClone = new PlanNodeComponent(draggedNode.originalBean, false, newParent);
                        newParent.childrenContainer.getChildren().add(targetIndex, optimisticClone);
                    } else {
                        PlanNodeComponent oldParent = draggedNode.parentWrapper;
                        if (oldParent != null) {
                            oldParent.childrenContainer.getChildren().remove(draggedNode);
                        }
                        newParent.childrenContainer.getChildren().add(targetIndex, draggedNode);
                        draggedNode.parentWrapper = newParent;
                    }

                    if (onNodeTransformationCallback != null) {
                        onNodeTransformationCallback.accept(new NodeTransformationEvent(
                                draggedNode.getPlanNodeId(),
                                sourceParentId,
                                newParent.getPlanNodeId(),
                                targetIndex,
                                isCopy
                        ));
                    }
                    success = true;
                }
            }

            event.setDropCompleted(success);
            event.consume();
        });

        dragHandle.setOnDragDone(event -> {
            if (draggedNode != null) draggedNode.setOpacity(1.0);
            draggedNode = null;
            this.getStyleClass().removeAll("drop-above", "drop-below");
            event.consume();
        });
    }

    private void setupBadgeDragAndDrop(Region badge, Object data, String type) {
        badge.setOnDragDetected(e -> {
            Dragboard db = badge.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(type);
            db.setContent(content);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            db.setDragView(badge.snapshot(snapParams, null));

            sourceBadgeComponent = this;
            draggedBadgeData = data;
            draggedBadgeType = type;

            badge.setOpacity(0.4);
            e.consume();
        });

        badge.setOnDragOver(e -> {
            if (e.getGestureSource() != badge && type.equals(draggedBadgeType)) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                boolean dropBefore = e.getX() < (badge.getWidth() / 2);
                badge.getStyleClass().removeAll("badge-drop-left", "badge-drop-right");
                badge.getStyleClass().add(dropBefore ? "badge-drop-left" : "badge-drop-right");
            }
            e.consume();
        });

        badge.setOnDragExited(e -> {
            badge.getStyleClass().removeAll("badge-drop-left", "badge-drop-right");
            e.consume();
        });

        badge.setOnDragDropped(e -> {
            boolean success = false;
            if (draggedBadgeData != null && type.equals(draggedBadgeType)) {
                boolean dropBefore = e.getX() < (badge.getWidth() / 2);
                boolean isCopy = e.getTransferMode() == TransferMode.COPY || sourceBadgeComponent == null;

                // Explicit casting removes the "Suspicious call to List.indexOf()" warning
                int targetIndex = "MODIFIER".equals(type)
                        ? this.exerciseModifierBeans.indexOf((ExerciseModifierBean) data)
                        : this.flowDecoratorBeans.indexOf((FlowDecoratorBean) data);

                if (!dropBefore) targetIndex++;

                // Adjust index if dragging downwards within the same list
                if (!isCopy && sourceBadgeComponent == this) {
                    int currentSourceIndex = "MODIFIER".equals(type)
                            ? this.exerciseModifierBeans.indexOf((ExerciseModifierBean) draggedBadgeData)
                            : this.flowDecoratorBeans.indexOf((FlowDecoratorBean) draggedBadgeData);

                    if (currentSourceIndex != -1 && currentSourceIndex < targetIndex) {
                        targetIndex--;
                    }
                }

                // Process the drop via centralized helper
                processBadgeDrop(type, draggedBadgeData, targetIndex, isCopy);

                success = true;
            }
            e.setDropCompleted(success);
            e.consume();
        });

        badge.setOnDragDone(e -> {
            badge.setOpacity(1.0);
            draggedBadgeData = null;
            sourceBadgeComponent = null;
            draggedBadgeType = null;
            e.consume();
        });
    }

    private void setupFallbackBadgeDropZone() {
        this.addEventHandler(DragEvent.DRAG_OVER, e -> {
            if ("MODIFIER".equals(draggedBadgeType) || "DECORATOR".equals(draggedBadgeType)) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                e.consume();
            }
        });

        this.addEventHandler(DragEvent.DRAG_DROPPED, e -> {
            if (draggedBadgeData != null && ("MODIFIER".equals(draggedBadgeType) || "DECORATOR".equals(draggedBadgeType))) {

                boolean isCopy = e.getTransferMode() == TransferMode.COPY || sourceBadgeComponent == null;

                // Append to the end of the list
                int targetIndex = "MODIFIER".equals(draggedBadgeType)
                        ? this.exerciseModifierBeans.size()
                        : this.flowDecoratorBeans.size();

                // Process the drop via centralized helper
                processBadgeDrop(draggedBadgeType, draggedBadgeData, targetIndex, isCopy);

                e.setDropCompleted(true);
                e.consume();
            }
        });
    }

    // --- BADGE DROP LOGIC ---

    // Handles the list manipulation and triggers the final event
    private void processBadgeDrop(String badgeType, Object badgeData, int targetIndex, boolean isCopy) {
        int sourceIndex = -1;

        // Find original index if it's coming from an existing component with explicit casting
        if (sourceBadgeComponent != null) {
            sourceIndex = "MODIFIER".equals(badgeType)
                    ? sourceBadgeComponent.exerciseModifierBeans.indexOf((ExerciseModifierBean) badgeData)
                    : sourceBadgeComponent.flowDecoratorBeans.indexOf((FlowDecoratorBean) badgeData);
        }

        if ("MODIFIER".equals(badgeType)) {
            if (!isCopy && sourceBadgeComponent != null) {
                sourceBadgeComponent.exerciseModifierBeans.remove((ExerciseModifierBean) badgeData);
            }
            // Safely clamp the index to prevent out-of-bounds exceptions
            if (targetIndex > this.exerciseModifierBeans.size()) targetIndex = this.exerciseModifierBeans.size();
            if (targetIndex < 0) targetIndex = 0;

            this.exerciseModifierBeans.add(targetIndex, (ExerciseModifierBean) badgeData);
        } else {
            if (!isCopy && sourceBadgeComponent != null) {
                sourceBadgeComponent.flowDecoratorBeans.remove((FlowDecoratorBean) badgeData);
            }
            // Safely clamp the index to prevent out-of-bounds exceptions
            if (targetIndex > this.flowDecoratorBeans.size()) targetIndex = this.flowDecoratorBeans.size();
            if (targetIndex < 0) targetIndex = 0;

            this.flowDecoratorBeans.add(targetIndex, (FlowDecoratorBean) badgeData);
        }

        finalizeBadgeDropAndFireEvent(badgeType, sourceIndex, targetIndex, isCopy);
    }

    // Helper method to DRY up the badge drop finalization and event firing
    private void finalizeBadgeDropAndFireEvent(String badgeType, int sourceIndex, int targetIndex, boolean isCopy) {
        if (sourceBadgeComponent != null && sourceBadgeComponent != this) {
            sourceBadgeComponent.renderModifiers();
            sourceBadgeComponent.renderDecorators();
        }
        this.renderModifiers();
        this.renderDecorators();

        if (onBadgeTransformationCallback != null) {
            onBadgeTransformationCallback.accept(new BadgeTransformationEvent(
                    badgeType,
                    sourceBadgeComponent != null ? sourceBadgeComponent.getPlanNodeId() : null,
                    this.planNodeId,
                    sourceIndex,
                    targetIndex,
                    draggedBadgeData,
                    isCopy
            ));
        }
    }

    private boolean isNotAncestorOf(PlanNodeComponent potentialAncestor, PlanNodeComponent node) {
        PlanNodeComponent current = node;
        while (current != null) {
            if (current == potentialAncestor) return false;
            current = current.parentWrapper;
        }
        return true;
    }

    public static void initiateExternalDrag(Object data, String type) {
        sourceBadgeComponent = null;
        draggedBadgeData = data;
        draggedBadgeType = type;
    }

    // --- EVENT RECORDS ---

    public record BadgeTransformationEvent(
            String type,
            String sourceNodeId,
            String targetNodeId,
            int sourceIndex,
            int targetIndex,
            Object item,
            boolean isCopy
    ) { }

    public record NodeTransformationEvent(
            String draggedNodeId,
            String sourceParentId,
            String targetParentId,
            int targetIndex,
            boolean isCopy
    ) { }
}