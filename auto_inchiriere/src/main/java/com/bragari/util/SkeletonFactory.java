package com.bragari.util;

// SkeletonFactory creeaza elemente de incarcare, adica forme simple afisate
// pana cand vin datele reale din baza de date.

import java.util.stream.IntStream;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class SkeletonFactory {

    private SkeletonFactory() {
    }

    public static VBox createTableSkeleton(int columns, int rows) {
        VBox skeleton = new VBox(10);
        skeleton.getStyleClass().add("skeleton-table");
        skeleton.setPadding(new Insets(4, 0, 4, 0));
        skeleton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        skeleton.getChildren().add(createSkeletonRow(columns, 18, "skeleton-header-box"));
        IntStream.range(0, rows)
                .mapToObj(i -> createSkeletonRow(columns, 28, "skeleton-box"))
                .forEach(skeleton.getChildren()::add);
        return skeleton;
    }

    public static VBox createReportSkeleton() {
        VBox skeleton = new VBox(10);
        skeleton.getStyleClass().add("skeleton-report");
        skeleton.setPadding(new Insets(8));
        skeleton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int i = 0; i < 9; i++) {
            Region line = new Region();
            line.getStyleClass().add("skeleton-box");
            line.setPrefHeight(i == 0 ? 18 : 14);
            line.setMaxWidth(Double.MAX_VALUE);
            line.setPrefWidth(i % 3 == 0 ? 420 : i % 3 == 1 ? 360 : 300);
            skeleton.getChildren().add(line);
        }

        return skeleton;
    }

    public static TilePane createCardsSkeleton(int count) {
        TilePane pane = new TilePane();
        pane.getStyleClass().add("skeleton-cards");
        pane.setPrefColumns(count);
        pane.setHgap(12);
        pane.setVgap(12);

        IntStream.range(0, count).forEach(i -> {
            Region icon = createSkeletonRegion(42, 28);
            Region title = createSkeletonRegion(100, 12);
            Region value = createSkeletonRegion(88, 24);
            Region caption = createSkeletonRegion(120, 10);

            VBox card = new VBox(10, icon, title, value, caption);
            card.getStyleClass().add("skeleton-card");
            pane.getChildren().add(card);
        });

        return pane;
    }

    private static Region createSkeletonRegion(double width, double height) {
        Region r = new Region();
        r.getStyleClass().add("skeleton-box");
        r.setPrefSize(width, height);
        return r;
    }

    public static VBox createSimpleLoading(String message) {
        VBox loading = new VBox(10);
        loading.getStyleClass().add("simple-loading");
        loading.setAlignment(Pos.CENTER);
        loading.setPadding(new Insets(24));
        loading.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setMaxSize(34, 34);

        Label label = new Label(message == null || message.isBlank() ? "Se incarca..." : message);
        label.getStyleClass().add("loading-label");

        loading.getChildren().addAll(indicator, label);
        return loading;
    }

    private static HBox createSkeletonRow(int columns, double height, String boxClass) {
        HBox row = new HBox(10);
        row.getStyleClass().add("skeleton-row");

        for (int i = 0; i < columns; i++) {
            Region cell = new Region();
            cell.getStyleClass().add(boxClass);
            cell.setPrefHeight(height);
            cell.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(cell, Priority.ALWAYS);
            row.getChildren().add(cell);
        }

        return row;
    }
}
