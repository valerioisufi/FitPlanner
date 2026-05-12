package com.example.fitplannerclient.ui.fx.view.plan;

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

import static com.example.fitplannerclient.ui.fx.view.plan.BadgeComponent.resolveColorFromName;

public class PlanNodeComponent extends VBox {

    private final String planNodeId;
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
    private static BadgeDragContext activeBadgeDrag;

    private record BadgeDragContext(
            PlanNodeComponent sourceNode,
            Object badgeData,
            BadgeComponent.BadgeType badgeType
    ) {}

    public PlanNodeComponent(PlanNodeBean bean, Boolean startExpanded, PlanNodeComponent parentWrapper) {
        this.originalBean = bean;
        this.planNodeId = bean.getId();
        this.parentWrapper = parentWrapper;

        this.exerciseModifierBeans = new ArrayList<>(bean.getModifiers() != null ? bean.getModifiers() : List.of());
        this.flowDecoratorBeans = new ArrayList<>(bean.getFlowDecorators() != null ? bean.getFlowDecorators() : List.of());

        this.getStyleClass().add("plan-node-drop-area");

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

        childrenContainer = new VBox();

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

        VBox nodeContent = new VBox(8, headerBox, childrenContainer);
        nodeContent.getStyleClass().add("plan-node");
        this.getChildren().addAll(nodeContent);

        // Render Initial Data
        renderModifiers();
        renderDecorators();

        // Initialize Drag and Drop logic
        setupNodeDragAndDrop(headerBox);
        setupFallbackBadgeDropZone(headerBox);
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
                setupBadgeDragAndDrop(badge, modifier, BadgeComponent.BadgeType.MODIFIER);
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

            setupBadgeDragAndDrop(badge, decorator, BadgeComponent.BadgeType.DECORATOR);
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
                boolean isCopy = event.getTransferMode() != TransferMode.COPY;

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

    private void setupBadgeDragAndDrop(Region badge, Object data, BadgeComponent.BadgeType type) {
        badge.setOnDragDetected(e -> {
            Dragboard db = badge.startDragAndDrop(TransferMode.COPY_OR_MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(type.name());
            db.setContent(content);

            SnapshotParameters snapParams = new SnapshotParameters();
            snapParams.setFill(Color.TRANSPARENT);
            db.setDragView(badge.snapshot(snapParams, null));

            activeBadgeDrag = new BadgeDragContext(this, data, type);

            badge.setOpacity(0.4);
            e.consume();
        });

        badge.setOnDragOver(e -> {
            if (e.getGestureSource() != badge && activeBadgeDrag != null && type.equals(activeBadgeDrag.badgeType())) {
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
            if (activeBadgeDrag != null && activeBadgeDrag.badgeData() != null && type.equals(activeBadgeDrag.badgeType())) {

                boolean dropBefore = e.getX() < (badge.getWidth() / 2);
                boolean isCopy = e.getTransferMode() != TransferMode.COPY || activeBadgeDrag.sourceNode() == null;

                Object draggedData = activeBadgeDrag.badgeData();
                PlanNodeComponent sourceComponent = activeBadgeDrag.sourceNode();

                int targetIndex = (type == BadgeComponent.BadgeType.MODIFIER)
                        ? this.exerciseModifierBeans.indexOf((ExerciseModifierBean) data)
                        : this.flowDecoratorBeans.indexOf((FlowDecoratorBean) data);

                if (!dropBefore) targetIndex++;

                if (!isCopy && sourceComponent == this) {
                    int currentSourceIndex = (type == BadgeComponent.BadgeType.MODIFIER)
                            ? this.exerciseModifierBeans.indexOf((ExerciseModifierBean) draggedData)
                            : this.flowDecoratorBeans.indexOf((FlowDecoratorBean) draggedData);

                    if (currentSourceIndex != -1 && currentSourceIndex < targetIndex) {
                        targetIndex--;
                    }
                }

                processBadgeDrop(type.name(), draggedData, targetIndex, isCopy, sourceComponent);

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

    private void setupFallbackBadgeDropZone(VBox dropArea) {
        dropArea.addEventHandler(DragEvent.DRAG_OVER, e -> {
            if (activeBadgeDrag != null) {
                e.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                e.consume();
            }
        });

        dropArea.addEventHandler(DragEvent.DRAG_DROPPED, e -> {
            if (activeBadgeDrag != null) {

                boolean isCopy = e.getTransferMode() != TransferMode.COPY || activeBadgeDrag.sourceNode() == null;

                BadgeComponent.BadgeType type = activeBadgeDrag.badgeType();
                Object draggedData = activeBadgeDrag.badgeData();
                PlanNodeComponent sourceComponent = activeBadgeDrag.sourceNode();

                int targetIndex = (type == BadgeComponent.BadgeType.MODIFIER)
                        ? this.exerciseModifierBeans.size()
                        : this.flowDecoratorBeans.size();

                processBadgeDrop(type.name(), draggedData, targetIndex, isCopy, sourceComponent);

                e.setDropCompleted(true);
                e.consume();
            }
        });
    }

    // --- BADGE DROP LOGIC ---

    private void processBadgeDrop(String badgeType, Object badgeData, int targetIndex, boolean isCopy, PlanNodeComponent sourceComponent) {
        int sourceIndex = -1;

        // Find original index if it's coming from an existing component
        if (sourceComponent != null) {
            sourceIndex = "MODIFIER".equals(badgeType)
                    ? sourceComponent.exerciseModifierBeans.indexOf((ExerciseModifierBean) badgeData)
                    : sourceComponent.flowDecoratorBeans.indexOf((FlowDecoratorBean) badgeData);
        }

        if ("MODIFIER".equals(badgeType)) {
            if (!isCopy && sourceComponent != null) {
                sourceComponent.exerciseModifierBeans.remove((ExerciseModifierBean) badgeData);
            }

            if (targetIndex > this.exerciseModifierBeans.size()) targetIndex = this.exerciseModifierBeans.size();
            if (targetIndex < 0) targetIndex = 0;

            this.exerciseModifierBeans.add(targetIndex, (ExerciseModifierBean) badgeData);
        } else {
            if (!isCopy && sourceComponent != null) {
                sourceComponent.flowDecoratorBeans.remove((FlowDecoratorBean) badgeData);
            }

            if (targetIndex > this.flowDecoratorBeans.size()) targetIndex = this.flowDecoratorBeans.size();
            if (targetIndex < 0) targetIndex = 0;

            this.flowDecoratorBeans.add(targetIndex, (FlowDecoratorBean) badgeData);
        }

        finalizeBadgeDropAndFireEvent(badgeType, sourceIndex, targetIndex, isCopy, sourceComponent, badgeData);
    }

    private void finalizeBadgeDropAndFireEvent(String badgeType, int sourceIndex, int targetIndex, boolean isCopy, PlanNodeComponent sourceComponent, Object draggedData) {
        if (sourceComponent != null && sourceComponent != this) {
            sourceComponent.renderModifiers();
            sourceComponent.renderDecorators();
        }

        this.renderModifiers();
        this.renderDecorators();

        if (onBadgeTransformationCallback != null) {
            onBadgeTransformationCallback.accept(new BadgeTransformationEvent(
                    badgeType,
                    sourceComponent != null ? sourceComponent.getPlanNodeId() : null,
                    this.planNodeId,
                    sourceIndex,
                    targetIndex,
                    draggedData,
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

    public static void initiateExternalDrag(Object data, BadgeComponent.BadgeType type) {
        activeBadgeDrag = new BadgeDragContext(null, data, type);
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