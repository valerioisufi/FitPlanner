package com.example.fitplannerclient.ui.fx.view.plan.editor;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.ui.fx.view.plan.editor.components.PlanNodeComponent;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;

public class PlanViewer extends ScrollPane {

    private boolean isEditable = true;

    public PlanViewer() {
        this.setFitToWidth(true);
        this.setStyle("-fx-border-color: transparent;");
        this.setPadding(new Insets(10));
    }

    public void setEditable(boolean editable) {
        this.isEditable = editable;
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
        PlanNodeComponent wrapper = new PlanNodeComponent(bean, startExpanded, parentWrapper, isEditable);

        for (PlanNodeBean childBean : bean.getChildren()) {
            wrapper.addChildNode(buildTree(childBean, true, wrapper));
        }

        return wrapper;
    }


}