package com.example.fitplannerclient.ui.fx.view.plan.editor.components;

import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.FlowDecoratorBean;
import com.example.fitplannerclient.bean.plan.NodeType;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.ui.fx.view.plan.editor.dnd.BadgeDragHandler;
import com.example.fitplannerclient.ui.fx.view.plan.editor.dnd.NodeDragHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import com.example.fitplannerclient.ui.fx.event.PlanNodeEvent;
import com.example.fitplannerclient.ui.fx.components.Icon;
import com.example.fitplannerclient.ui.fx.components.utils.MenuUtils;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;

import java.util.ArrayList;
import java.util.List;

import static com.example.fitplannerclient.ui.fx.view.plan.editor.components.BadgeComponent.resolveColorFromName;

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
        nameLabel.setStyle("-fx-cursor: text;");

        if(bean.getType() == NodeType.BLOCK) {
            nameLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    this.fireEvent(new PlanNodeEvent(PlanNodeEvent.EDIT_NAME_CLICKED, this.planNodeId));
                    e.consume();
                }
            });
        } else if(bean.getType() == NodeType.EXERCISE) {
            nameLabel.setOnMouseClicked(e -> {
                if (e.getClickCount() == 1) {
                    this.fireEvent(new PlanNodeEvent(PlanNodeEvent.CHANGE_EXERCISE_REQUESTED, this.planNodeId));
                    e.consume();
                }
            });
        }

        inlineDecoratorsBox = new HBox(8);
        inlineDecoratorsBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button optionsBtn = new Button();
        optionsBtn.getStyleClass().add("button-header");
        optionsBtn.setGraphic(new Icon("dots-vertical-icon", List.of("button-header-icon")));
        
        optionsBtn.setOnMousePressed(e -> {
            ContextMenu existingMenu = (ContextMenu) optionsBtn.getProperties().get("activeMenu");
            if (existingMenu != null && existingMenu.isShowing()) {
                existingMenu.hide();
                return;
            }
            
            ContextMenu menu = new ContextMenu();
            menu.setAutoHide(true);
            optionsBtn.getProperties().put("activeMenu", menu);
            
            switch (bean.getType()) {
                case EXERCISE -> {
                    menu.getItems().addAll(
                        MenuUtils.createCustomMenuItem("Cambia Esercizio...", "swap-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.CHANGE_EXERCISE_REQUESTED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Duplica", "copy-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.DUPLICATE_NODE_REQUESTED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Elimina", "delete-icon", "button-header-danger-icon", "-fx-text-fill: #ef4444;", () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.DELETE_NODE_REQUESTED, this.planNodeId));
                        })
                    );
                }
                case BLOCK -> {
                    menu.getItems().addAll(
                        MenuUtils.createCustomMenuItem("Modifica Nome", "edit-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.EDIT_NAME_CLICKED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Duplica Blocco", "copy-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.DUPLICATE_NODE_REQUESTED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Svuota Blocco", "eraser-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.EMPTY_NODE_REQUESTED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Elimina Blocco", "delete-icon", "button-header-danger-icon", "-fx-text-fill: #ef4444;", () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.DELETE_NODE_REQUESTED, this.planNodeId));
                        })
                    );
                }
                case PROTOCOL_BLOCK -> {
                    menu.getItems().addAll(
                        MenuUtils.createCustomMenuItem("Modifica Parametri Protocollo", "sliders-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.EDIT_PROTOCOL_PARAMETERS_REQUESTED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Svuota Protocollo", "eraser-icon", "button-header-icon", null, () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.EMPTY_NODE_REQUESTED, this.planNodeId));
                        }),
                        MenuUtils.createCustomMenuItem("Elimina Protocollo", "delete-icon", "button-header-danger-icon", "-fx-text-fill: #ef4444;", () -> {
                            this.fireEvent(new PlanNodeEvent(PlanNodeEvent.DELETE_NODE_REQUESTED, this.planNodeId));
                        })
                    );
                }
            }
            
            menu.show(optionsBtn, Side.BOTTOM, 0, 5);
            e.consume();
        });

        childrenContainer = new VBox();

        HBox titleBox = new HBox(12);
        titleBox.setAlignment(Pos.CENTER_LEFT);
        titleBox.getChildren().addAll(nameLabel, inlineDecoratorsBox, spacer, optionsBtn);

        badgesBox = new FlowPane(8, 8);
        badgesBox.setAlignment(Pos.CENTER_LEFT);

        VBox headerBox = new VBox(8, titleBox, badgesBox);
        headerBox.setStyle("-fx-cursor: hand;");

        Rectangle clipRect = new Rectangle();
        clipRect.widthProperty().bind(childrenContainer.widthProperty());
        clipRect.heightProperty().bind(childrenContainer.heightProperty());
        childrenContainer.setClip(clipRect);

        isExpanded = startExpanded;
        updateExpansionState();

        VBox nodeContent = new VBox(8, headerBox);
        nodeContent.getChildren().add(childrenContainer);
        
        nodeContent.getStyleClass().add("plan-node");
        switch(bean.getType()) {
            case EXERCISE -> nodeContent.getStyleClass().add("node-exercise");
            case BLOCK -> nodeContent.getStyleClass().add("node-block");
            case PROTOCOL_BLOCK -> nodeContent.getStyleClass().add("node-protocol");
        }
        this.getChildren().addAll(nodeContent);

        // Render Initial Data
        renderModifiers();
        renderDecorators();

        // Initialize Drag and Drop logic via external handlers
        NodeDragHandler.setup(this, headerBox);
        if (parentWrapper != null) {
            BadgeDragHandler.setupFallbackDropZone(this, headerBox);
        }
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

    public PlanNodeComponent getParentWrapper() { return parentWrapper; }
    public VBox getChildrenContainer() { return childrenContainer; }
    public PlanNodeBean getOriginalBean() { return originalBean; }

    private void fireEditBadgeClicked(BadgeComponent badge) {
        this.fireEvent(new PlanNodeEvent(PlanNodeEvent.EDIT_BADGE_CLICKED, this.planNodeId)
            .setBadgeType(badge.getBadgeType().name())
            .setBadgeData(badge)
        );
    }

    // --- RENDERING METHODS ---

    private void renderModifiers() {
        badgesBox.getChildren().clear();

        boolean hasModifiers = !exerciseModifierBeans.isEmpty();
        boolean hasParameters = originalBean.getType() == NodeType.PROTOCOL_BLOCK && originalBean.getParameters() != null && !originalBean.getParameters().isEmpty();

        if (!hasModifiers && !hasParameters) {
            badgesBox.setVisible(false);
            badgesBox.setManaged(false);
        } else {
            badgesBox.setVisible(true);
            badgesBox.setManaged(true);

            if (hasModifiers) {
                for (int i = 0; i < exerciseModifierBeans.size(); i++) {
                    ExerciseModifierBean modifier = exerciseModifierBeans.get(i);
                    BadgeComponent.BadgeColor color = resolveColorFromName(modifier.getName(), BadgeComponent.BadgeType.MODIFIER);
                    BadgeComponent badge = new BadgeComponent(
                            modifier.getId(),
                            BadgeComponent.BadgeType.MODIFIER,
                            modifier.getName(),
                            formatValueWithUnit(modifier.getName(), modifier.getValue()),
                            color
                    );
                    badge.setOnEditClicked(this::fireEditBadgeClicked);
                    BadgeDragHandler.setup(this, badge, modifier, BadgeComponent.BadgeType.MODIFIER, i);
                    badgesBox.getChildren().add(badge);
                }
            }

            if (hasParameters) {
                originalBean.getParameters().forEach((key, value) -> {
                    Label paramLabel = new Label(key + ": " + value);
                    paramLabel.getStyleClass().add("protocol-param-label");
                    badgesBox.getChildren().add(paramLabel);
                });
            }
        }
    }

    private void renderDecorators() {
        inlineDecoratorsBox.getChildren().clear();

        if (flowDecoratorBeans.isEmpty()) return;

        for (int i = 0; i < flowDecoratorBeans.size(); i++) {
            FlowDecoratorBean decorator = flowDecoratorBeans.get(i);

            String typeName = decorator.getType().name().replace("_", " ");
            BadgeComponent.BadgeColor color = resolveColorFromName(typeName, BadgeComponent.BadgeType.DECORATOR);

            BadgeComponent badge = new BadgeComponent(decorator.getId(), BadgeComponent.BadgeType.DECORATOR, typeName, formatValueWithUnit(typeName, decorator.getValue()), color);
            badge.setOnEditClicked(this::fireEditBadgeClicked);

            BadgeDragHandler.setup(this, badge, decorator, BadgeComponent.BadgeType.DECORATOR, i);
            inlineDecoratorsBox.getChildren().add(badge);

            if (i < flowDecoratorBeans.size() - 1) {
                Region chevron = new Region();
                chevron.getStyleClass().add("chevron-separator");
                inlineDecoratorsBox.getChildren().add(chevron);
            }
        }
    }



    public void updateName(String newName) {
        this.originalBean.setName(newName);
        this.nameLabel.setText(newName);
    }

    private String formatValueWithUnit(String type, String value) {
        if (value == null || value.trim().isEmpty()) return value;
        // Se è già una variabile formattata come ${nome}, non aggiungiamo unità
        if (value.startsWith("${") && value.endsWith("}")) return value;
        // Non aggiungere l'unità alla progressione che è una stringa complessa
        if (type.equalsIgnoreCase("PROGRESSION")) return value;

        String unit = switch (type.toUpperCase().replace(" ", "_")) {
            case "REST", "TIME_LIMIT", "INTERVAL", "TEMPO" -> "s";
            case "LOOP", "SETS" -> "x";
            case "WEIGHT" -> " kg";
            case "DISTANCE" -> " km";
            default -> "";
        };

        return value + unit;
    }
}