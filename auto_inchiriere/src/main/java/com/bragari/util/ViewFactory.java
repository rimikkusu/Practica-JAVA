package com.bragari.util;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class ViewFactory {

    private ViewFactory() {
    }

    public static VBox createPage(String pageTitle, String pageIcon, Node content, Node... actions) {
        VBox page = new VBox();
        page.getStyleClass().add("page-container");

        if (!content.getStyleClass().contains("page-content")) {
            content.getStyleClass().add("page-content");
        }
        VBox.setVgrow(content, Priority.ALWAYS);

        page.getChildren().addAll(createTopBar(pageTitle, pageIcon, actions), content);
        return page;
    }

    public static HBox createTopBar(String pageTitle, String pageIcon, Node... actions) {
        HBox topBar = new HBox();
        topBar.getStyleClass().add("top-bar");
        topBar.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(pageIcon);
        icon.getStyleClass().add("page-title-icon");

        Label title = new Label(pageTitle);
        title.getStyleClass().add("page-title");

        HBox titleBox = new HBox(7, icon, title);
        titleBox.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topBar.getChildren().addAll(titleBox, spacer);

        if (actions != null && actions.length > 0) {
            HBox actionsBox = new HBox(7);
            actionsBox.getStyleClass().add("top-bar-actions");
            actionsBox.setAlignment(Pos.CENTER_RIGHT);
            actionsBox.getChildren().addAll(actions);
            topBar.getChildren().add(actionsBox);
        }

        return topBar;
    }

    public static void styleTable(TableView<?> table) {
        if (!table.getStyleClass().contains("app-table")) {
            table.getStyleClass().add("app-table");
        }

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        for (TableColumn<?, ?> column : table.getColumns()) {
            column.setText(column.getText().toUpperCase());
        }
    }

    public static <T extends Node> T asCard(T node) {
        if (!node.getStyleClass().contains("card")) {
            node.getStyleClass().add("card");
        }

        if (!node.getStyleClass().contains("content-card")) {
            node.getStyleClass().add("content-card");
        }

        return node;
    }

    public static Label createBadge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", styleClass);
        return badge;
    }
}
