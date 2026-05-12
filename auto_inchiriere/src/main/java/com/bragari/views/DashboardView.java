package com.bragari.views;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.models.StatusInchiriere;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.SkeletonFactory;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardView {

    private final AutoInchiriereService service;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;
    private StackPane statsContainer;
    private StackPane recentContainer;

    public DashboardView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                         BackgroundRunner backgroundRunner) {
        this.service = service;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showDashboardPage() {
        VBox page = new VBox(18);
        page.getStyleClass().add("page-container");

        HBox header = new HBox(10);
        header.setFillHeight(false);

        VBox titleBox = new VBox(4);
        Label title = new Label("Dashboard");
        title.getStyleClass().add("page-title");
        Label subtitle = new Label("Privire rapida asupra activitatii curente.");
        subtitle.getStyleClass().add("page-subtitle");
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");

        header.getChildren().addAll(titleBox, spacer, refreshButton);

        TilePane statGrid = new TilePane();
        statGrid.getStyleClass().add("stat-grid");
        statGrid.setPrefColumns(4);
        statGrid.setHgap(12);
        statGrid.setVgap(12);

        VBox autoCard = createStatCard("AUTO", "stat-icon status-warning", "Total automobile", "0", "In flota");
        VBox clientiCard = createStatCard("CLI", "stat-icon status-success", "Total clienti", "0", "Inregistrati");
        VBox activeCard = createStatCard("ACT", "stat-icon status-info", "Inchirieri active", "0", "In desfasurare");
        VBox venitCard = createStatCard("LEI", "stat-icon status-neutral", "Incasari luna", "0", "Plati luna curenta");

        statGrid.getChildren().addAll(autoCard, clientiCard, activeCard, venitCard);
        statsContainer = new StackPane(statGrid);
        statsContainer.getStyleClass().add("dashboard-section");

        VBox recentCard = new VBox(12);
        recentCard.getStyleClass().add("content-card");
        Label recentTitle = new Label("Inchirieri recente");
        recentTitle.getStyleClass().add("page-subtitle");
        VBox recentList = new VBox();
        recentList.getStyleClass().add("recent-list");
        recentCard.getChildren().addAll(recentTitle, recentList);
        recentContainer = new StackPane(recentCard);
        recentContainer.getStyleClass().add("dashboard-section");

        Runnable refreshAction = () -> {
            arataSkeletonDashboard();
            backgroundRunner.run(this::calculeazaDashboardData, data -> {
            setStatCardValue(autoCard, String.valueOf(data.totalAutomobile), "In flota");
            setStatCardValue(clientiCard, String.valueOf(data.totalClienti), "Inregistrati");
            setStatCardValue(activeCard, String.valueOf(data.inchirieriActive), "In desfasurare");
            setStatCardValue(venitCard, formatSuma(data.incasariLuna), "Lei in luna curenta");
            populeazaInchirieriRecente(recentList, data.inchirieriRecente);
            arataDashboardReal(statGrid, recentCard);
        }, error -> arataDashboardReal(statGrid, recentCard));
        };

        refreshButton.setOnAction(e -> refreshAction.run());

        page.getChildren().addAll(header, statsContainer, recentContainer);
        root.setCenter(page);

        refreshAction.run();
    }

    private VBox createStatCard(String iconText, String iconClass, String labelText, String valueText, String captionText) {
        VBox card = new VBox(6);
        card.getStyleClass().add("stat-card");
        card.setPadding(new Insets(14));

        Label icon = new Label(iconText);
        icon.getStyleClass().addAll(iconClass.split(" "));

        Label label = new Label(labelText);
        label.getStyleClass().add("stat-label");

        Label value = new Label(valueText);
        value.getStyleClass().add("stat-value");

        Label caption = new Label(captionText);
        caption.getStyleClass().add("stat-caption");

        card.getChildren().addAll(icon, label, value, caption);
        return card;
    }

    private void setStatCardValue(VBox card, String value, String caption) {
        Label valueLabel = (Label) card.getChildren().get(2);
        Label captionLabel = (Label) card.getChildren().get(3);
        valueLabel.setText(value);
        captionLabel.setText(caption);
    }

    private DashboardData calculeazaDashboardData() {
        DashboardData data = new DashboardData();
        data.totalAutomobile = service.obtineAutomobile().size();
        data.totalClienti = service.obtineClienti().size();

        List<Inchiriere> inchirieri = new ArrayList<>(service.obtineInchirieri());
        List<Plata> plati = service.obtinePlati();

        for (Inchiriere inchiriere : inchirieri) {
            if (inchiriere.getStatus() == StatusInchiriere.ACTIVA) {
                data.inchirieriActive++;
            }
        }

        LocalDate now = LocalDate.now();
        for (Plata plata : plati) {
            if (plata.getDataPlata() != null
                    && plata.getDataPlata().getMonth() == now.getMonth()
                    && plata.getDataPlata().getYear() == now.getYear()) {
                data.incasariLuna += plata.getSuma();
            }
        }

        inchirieri.sort(Comparator.comparingInt(Inchiriere::getId).reversed());
        data.inchirieriRecente = inchirieri.stream().limit(4).toList();

        return data;
    }

    private void populeazaInchirieriRecente(VBox recentList, List<Inchiriere> inchirieri) {
        recentList.getChildren().clear();

        if (inchirieri.isEmpty()) {
            Label emptyLabel = new Label("Nu exista inchirieri recente.");
            emptyLabel.getStyleClass().add("muted-label");
            recentList.getChildren().add(emptyLabel);
            return;
        }

        for (Inchiriere inchiriere : inchirieri) {
            HBox row = new HBox(12);
            row.getStyleClass().add("recent-row");

            Label avatar = new Label(initiale(inchiriere.getClient().getNume()));
            avatar.getStyleClass().add("recent-avatar");

            VBox info = new VBox(3);
            Label name = new Label(inchiriere.getClient().getNume());
            name.getStyleClass().add("recent-name");
            Label meta = new Label(
                    inchiriere.getAutomobil().getMarca() + " " + inchiriere.getAutomobil().getModel()
                            + " | " + inchiriere.getDataInceput() + " - " + inchiriere.getDataSfarsit()
            );
            meta.getStyleClass().add("recent-meta");
            info.getChildren().addAll(name, meta);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label amount = new Label(formatSuma(inchiriere.calculeazaTotal()) + " lei");
            amount.getStyleClass().add("recent-amount");

            Label statusBadge = new Label(inchiriere.getStatus().name());
            statusBadge.getStyleClass().addAll("status-badge", statusClassForInchiriere(inchiriere.getStatus()));

            row.getChildren().addAll(avatar, info, spacer, amount, statusBadge);
            recentList.getChildren().add(row);
        }
    }

    private String formatSuma(double suma) {
        if (Math.floor(suma) == suma) {
            return String.valueOf((long) suma);
        }

        return String.format("%.2f", suma);
    }

    private String initiale(String nume) {
        if (nume == null || nume.isBlank()) {
            return "NA";
        }

        String[] parts = nume.trim().split("\\s+");
        StringBuilder builder = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                builder.append(Character.toUpperCase(part.charAt(0)));
            }

            if (builder.length() == 2) {
                break;
            }
        }

        return builder.length() == 0 ? "NA" : builder.toString();
    }

    private String statusClassForInchiriere(StatusInchiriere status) {
        if (status == StatusInchiriere.ACTIVA) {
            return "status-success";
        }

        if (status == StatusInchiriere.ANULATA) {
            return "status-danger";
        }

        return "status-neutral";
    }

    private void arataSkeletonDashboard() {
        if (statsContainer != null) {
            statsContainer.getChildren().setAll(SkeletonFactory.createCardsSkeleton(4));
        }

        if (recentContainer != null) {
            recentContainer.getChildren().setAll(SkeletonFactory.createTableSkeleton(2, 4));
        }
    }

    private void arataDashboardReal(TilePane statGrid, VBox recentCard) {
        if (statsContainer != null) {
            statsContainer.getChildren().setAll(statGrid);
        }

        if (recentContainer != null) {
            recentContainer.getChildren().setAll(recentCard);
        }
    }

    private static class DashboardData {
        private int totalAutomobile;
        private int totalClienti;
        private int inchirieriActive;
        private double incasariLuna;
        private List<Inchiriere> inchirieriRecente = new ArrayList<>();
    }
}
