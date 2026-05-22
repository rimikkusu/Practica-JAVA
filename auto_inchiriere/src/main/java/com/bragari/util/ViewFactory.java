package com.bragari.util;

// ViewFactory contine metode pentru construirea componentelor comune din pagini.
// Este mai usor asa, pentru ca butoanele, cardurile si layout-urile arata la fel.

import java.util.function.UnaryOperator;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class ViewFactory {

    private ViewFactory() {
    }

    public static VBox createPage(String pageTitle, String pageIcon, Node content, Node... actions) {
        VBox page = new VBox();
        page.getStyleClass().add("page-container");
        page.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Node pageContent = content;
        boolean needsTableBackground = content.getStyleClass().contains("table-page-background");

        if (!content.getStyleClass().contains("page-content")) {
            content.getStyleClass().add("page-content");
        }
        if (content instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }
        VBox.setVgrow(content, Priority.ALWAYS);

        if (needsTableBackground) {
            content.getStyleClass().remove("table-page-background");
            if (!content.getStyleClass().contains("table-page-content")) {
                content.getStyleClass().add("table-page-content");
            }

            StackPane backgroundPane = new StackPane();
            backgroundPane.getStyleClass().add("table-page-background");
            backgroundPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            Region overlay = new Region();
            overlay.getStyleClass().add("table-page-overlay");
            overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

            backgroundPane.getChildren().addAll(overlay, content);
            VBox.setVgrow(backgroundPane, Priority.ALWAYS);
            pageContent = backgroundPane;
        }

        page.getChildren().addAll(createTopBar(pageTitle, pageIcon, actions), pageContent);
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

        table.setMaxWidth(Double.MAX_VALUE);
        table.setMaxHeight(Double.MAX_VALUE);
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

        if (node instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        }

        return node;
    }

    public static Label createBadge(String text, String styleClass) {
        Label badge = new Label(text);
        badge.getStyleClass().addAll("badge", styleClass);
        return badge;
    }

    public static void acceptaDoarNume(TextField field) {
        seteazaFiltruText(field, "[\\p{L} .'-]{0,80}");
    }

    public static void acceptaDoarTextAuto(TextField field) {
        seteazaFiltruText(field, "[\\p{L}0-9 .'-]{0,50}");
    }

    public static void acceptaDoarTelefon(TextField field) {
        seteazaFiltruText(field, "\\+?[0-9 -]{0,17}");
    }

    public static void acceptaDoarNumarDecimal(TextField field) {
        seteazaFiltruText(field, "\\d{0,8}([.,]\\d{0,2})?");
    }

    public static void acceptaDoarNumarInmatriculare(TextField field) {
        seteazaFiltruText(field, "[A-Za-z0-9 -]{0,12}");
    }

    public static void acceptaDoarUsername(TextField field) {
        seteazaFiltruText(field, "[A-Za-z0-9._-]{0,30}");
    }

    private static void seteazaFiltruText(TextField field, String regex) {
        UnaryOperator<TextFormatter.Change> filtru = change ->
                change.getControlNewText().matches(regex) ? change : null;
        field.setTextFormatter(new TextFormatter<>(filtru));
    }
}
