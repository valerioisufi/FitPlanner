package com.example.fitplannerclient.ui.fx.view.plan;

import com.example.fitplannerclient.bean.plan.*;
import javafx.scene.control.ScrollPane;

import java.util.function.Consumer;

public class PlanViewer extends ScrollPane {

    private Consumer<PlanNodeComponent> onNodeEditRequest;
    private Consumer<BadgeComponent> onBadgeEditRequest;

    public PlanViewer() {
        this.setFitToWidth(true);
        this.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
    }

    public void setRootNode(PlanNodeBean rootBean) {
        if (rootBean == null) {
            this.setContent(null);
            return;
        }
        PlanNodeComponent rootWrapper = buildTree(rootBean, true, null);
        this.setContent(rootWrapper);
    }

    private PlanNodeComponent buildTree(PlanNodeBean bean, boolean startExpanded, PlanNodeComponent parentWrapper) {
        PlanNodeComponent wrapper = new PlanNodeComponent(bean, startExpanded, parentWrapper);

        wrapper.setOnNodeTransformationCallback(event -> {
            System.out.println("CONTROLLER: Sposta Nodo " + event.draggedNodeId() + " in " + event.targetParentId());
            // TODO: controller.handleNodeMove(event);
        });

        wrapper.setOnBadgeTransformationCallback(event -> {
            System.out.println("CONTROLLER: Sposta " + event.type() + " dal nodo " + event.sourceNodeId() + " al nodo " + event.targetNodeId());
            // TODO: controller.handleBadgeMove(event);
        });

        wrapper.setOnEditNameClicked(nodeComponent -> {
            if (onNodeEditRequest != null) onNodeEditRequest.accept(nodeComponent);
        });

        wrapper.setOnEditBadgeClicked(badgeComponent -> {
            if (onBadgeEditRequest != null) onBadgeEditRequest.accept(badgeComponent);
        });

        for (PlanNodeBean childBean : bean.getChildren()) {
            wrapper.addChildNode(buildTree(childBean, false, wrapper));
        }

        return wrapper;
    }

    public void setOnNodeEditRequest(Consumer<PlanNodeComponent> onNodeEditRequest) {
        this.onNodeEditRequest = onNodeEditRequest;
    }

    public void setOnBadgeEditRequest(Consumer<BadgeComponent> onBadgeEditRequest) {
        this.onBadgeEditRequest = onBadgeEditRequest;
    }
}