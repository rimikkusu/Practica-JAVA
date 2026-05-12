package com.bragari.util;

import javafx.geometry.Insets;
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

        skeleton.getChildren().add(createSkeletonRow(columns, 18, "skeleton-header-box"));

        for (int i = 0; i < rows; i++) {
            skeleton.getChildren().add(createSkeletonRow(columns, 28, "skeleton-box"));
        }

        return skeleton;
    }

    public static VBox createReportSkeleton() {
        VBox skeleton = new VBox(10);
        skeleton.getStyleClass().add("skeleton-report");
        skeleton.setPadding(new Insets(8));

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

        for (int i = 0; i < count; i++) {
            VBox card = new VBox(10);
            card.getStyleClass().add("skeleton-card");

            Region icon = new Region();
            icon.getStyleClass().add("skeleton-box");
            icon.setPrefSize(42, 28);

            Region title = new Region();
            title.getStyleClass().add("skeleton-box");
            title.setPrefHeight(12);
            title.setPrefWidth(100);

            Region value = new Region();
            value.getStyleClass().add("skeleton-box");
            value.setPrefHeight(24);
            value.setPrefWidth(88);

            Region caption = new Region();
            caption.getStyleClass().add("skeleton-box");
            caption.setPrefHeight(10);
            caption.setPrefWidth(120);

            card.getChildren().addAll(icon, title, value, caption);
            pane.getChildren().add(card);
        }

        return pane;
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
