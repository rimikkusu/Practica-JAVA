package com.bragari.views;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.bragari.models.Automobil;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.models.StatusInchiriere;
import com.bragari.services.AutoInchiriereService;
import com.bragari.services.SettingsService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.SkeletonFactory;
import com.bragari.util.ViewFactory;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DashboardView {

    private static final double TARGET_LUNA = 10000;

    private final AutoInchiriereService service;
    private final SettingsService settingsService;
    private final BorderPane root;
    private final BackgroundRunner backgroundRunner;

    private StackPane dashboardContainer;

    public DashboardView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                         BackgroundRunner backgroundRunner) {
        this(service, new SettingsService(), root, ownerSupplier, backgroundRunner);
    }

    public DashboardView(AutoInchiriereService service, SettingsService settingsService, BorderPane root,
                         Supplier<Stage> ownerSupplier, BackgroundRunner backgroundRunner) {
        this.service = service;
        this.settingsService = settingsService;
        this.root = root;
        this.backgroundRunner = backgroundRunner;
    }

    public void showDashboardPage() {
        VBox pageContent = new VBox(0);
        pageContent.getStyleClass().addAll("page-content", "command-dashboard");
        pageContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");

        dashboardContainer = new StackPane();
        dashboardContainer.getStyleClass().add("command-dashboard-container");
        dashboardContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        ScrollPane scrollPane = new ScrollPane(dashboardContainer);
        scrollPane.getStyleClass().add("command-dashboard-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        scrollPane.viewportBoundsProperty().addListener((obs, oldBounds, bounds) -> {
            double viewportWidth = Math.max(320, bounds.getWidth());
            dashboardContainer.setMinWidth(viewportWidth);
            dashboardContainer.setPrefWidth(viewportWidth);
        });
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        pageContent.getChildren().add(scrollPane);
        root.setCenter(ViewFactory.createPage("Auto Inchiriere Command Center", "D", pageContent, refreshButton));

        Runnable refreshAction = () -> {
            arataSkeletonDashboard();
            backgroundRunner.run(this::calculeazaDashboardData, this::arataDashboardReal,
                    error -> arataDashboardReal(new DashboardData()));
        };

        refreshButton.setOnAction(e -> refreshAction.run());
        refreshAction.run();
    }

    private VBox createDashboardContent(DashboardData data) {
        VBox content = new VBox(14);
        content.getStyleClass().add("command-dashboard-content");
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label subtitle = new Label("Date live din baza de date Neon");
        subtitle.getStyleClass().add("command-dashboard-subtitle");

        VBox metricGrid = new VBox(10);
        metricGrid.getStyleClass().add("command-metric-grid");
        metricGrid.setMaxWidth(Double.MAX_VALUE);
        Node[] metricCards = {
                createMetricCard("Total automobile", String.valueOf(data.totalAutomobile), "In flota", "AUTO", "metric-green"),
                createMetricCard("Disponibile", String.valueOf(data.automobileDisponibile), "Gata de inchiriat", "DIS", "metric-amber"),
                createMetricCard("Total clienti", String.valueOf(data.totalClienti), "Clienti inregistrati", "CLI", "metric-blue"),
                createMetricCard("Inchirieri active", String.valueOf(data.inchirieriActive), "In desfasurare", "ACT", "metric-purple"),
                createMetricCard("Total inchirieri", String.valueOf(data.totalInchirieri), "Toate rezervarile", "INC", "metric-blue"),
                createMetricCard("Incasari luna", formatSuma(data.incasariLuna), "Lei luna curenta", "LEI", "metric-orange"),
                createMetricCard("Plati totale", String.valueOf(data.platiTotale), "Tranzactii salvate", "PAY", "metric-amber")
        };
        layoutMetricRows(metricGrid, metricCards, 7);

        GridPane topChartsGrid = new GridPane();
        topChartsGrid.getStyleClass().add("command-chart-grid");
        configureColumns(topChartsGrid, 25, 25, 25, 25);
        Node miniRevenueCard = createChartCard("Trend incasari", createMiniRevenueChart(data), "chart-card-blue");
        Node miniRentalsCard = createChartCard("Trend inchirieri", createMiniRentalsChart(data), "chart-card-green");
        Node availabilityCard = createAvailabilityCard(data);
        Node targetCard = createTargetCard(data);
        Node[] topCards = {miniRevenueCard, miniRentalsCard, availabilityCard, targetCard};
        layoutCardsGrid(topChartsGrid, topCards, 4);

        GridPane mainGrid = new GridPane();
        mainGrid.getStyleClass().add("command-main-grid");
        configureColumns(mainGrid, 66, 34);
        Node revenueCard = createChartCard("Incasari pe ultimele 6 luni", createRevenueAreaChart(data), "chart-card-wide");
        Node statusCard = createChartCard("Status inchirieri", createStatusBarChart(data), "chart-card-amber");
        mainGrid.add(revenueCard, 0, 0);
        mainGrid.add(statusCard, 1, 0);
        GridPane.setHgrow(revenueCard, Priority.ALWAYS);
        GridPane.setHgrow(statusCard, Priority.ALWAYS);

        GridPane bottomGrid = new GridPane();
        bottomGrid.getStyleClass().add("command-main-grid");
        configureColumns(bottomGrid, 36, 64);
        Node categoryCard = createChartCard("Automobile pe categorii", createCategoryPieChart(data), "chart-card-purple");
        Node recentCard = createRecentRentalsCard(data);
        bottomGrid.add(categoryCard, 0, 0);
        bottomGrid.add(recentCard, 1, 0);
        GridPane.setHgrow(categoryCard, Priority.ALWAYS);
        GridPane.setHgrow(recentCard, Priority.ALWAYS);

        content.getChildren().addAll(subtitle, metricGrid, topChartsGrid, mainGrid, bottomGrid);
        configureResponsiveDashboard(
                content,
                metricGrid,
                metricCards,
                topChartsGrid,
                topCards,
                mainGrid,
                revenueCard,
                statusCard,
                bottomGrid,
                categoryCard,
                recentCard
        );
        return content;
    }

    private VBox createMetricCard(String labelText, String valueText, String captionText, String chipText, String styleClass) {
        VBox card = new VBox(8);
        card.getStyleClass().addAll("command-metric-card", styleClass);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(labelText);
        label.getStyleClass().add("command-metric-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label chip = new Label(chipText);
        chip.getStyleClass().add("command-metric-chip");

        header.getChildren().addAll(label, spacer, chip);

        Label value = new Label(valueText);
        value.getStyleClass().add("command-metric-value");
        value.setMaxWidth(Double.MAX_VALUE);

        Label caption = new Label(captionText);
        caption.getStyleClass().add("command-metric-caption");

        card.getChildren().addAll(header, value, caption);
        return card;
    }

    private VBox createChartCard(String title, Node content, String styleClass) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("command-chart-card", styleClass);
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("command-card-title");

        if (content instanceof Region region) {
            region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            VBox.setVgrow(region, Priority.ALWAYS);
        }

        card.getChildren().addAll(titleLabel, content);
        VBox.setVgrow(content, Priority.ALWAYS);
        return card;
    }

    private VBox createAvailabilityCard(DashboardData data) {
        VBox body = new VBox(10);
        body.getStyleClass().add("command-progress-body");

        double total = Math.max(1, data.totalAutomobile);
        double progress = data.automobileDisponibile / total;
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().addAll("command-progress", "command-progress-green");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label value = new Label(data.automobileDisponibile + " / " + data.totalAutomobile);
        value.getStyleClass().add("command-progress-value");

        Label caption = new Label("Disponibile vs indisponibile: " + data.automobileIndisponibile);
        caption.getStyleClass().add("command-progress-caption");

        body.getChildren().addAll(value, progressBar, caption);
        return createChartCard("Disponibilitate flota", body, "chart-card-green");
    }

    private VBox createTargetCard(DashboardData data) {
        VBox body = new VBox(10);
        body.getStyleClass().add("command-progress-body");

        double progress = Math.min(1, data.incasariLuna / TARGET_LUNA);
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.getStyleClass().addAll("command-progress", "command-progress-amber");
        progressBar.setMaxWidth(Double.MAX_VALUE);

        Label value = new Label(formatPercent(progress) + "  " + formatSuma(data.incasariLuna) + " / " + formatSuma(TARGET_LUNA));
        value.getStyleClass().add("command-progress-value");

        Label caption = new Label("Target luna curenta");
        caption.getStyleClass().add("command-progress-caption");

        body.getChildren().addAll(value, progressBar, caption);
        return createChartCard("Target luna", body, "chart-card-amber");
    }

    private AreaChart<String, Number> createRevenueAreaChart(DashboardData data) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(true);

        AreaChart<String, Number> chart = new AreaChart<>(xAxis, yAxis);
        chart.getStyleClass().addAll("command-chart", "command-area-chart");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setMinHeight(140);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<YearMonth, Double> entry : data.incasariPeLuna.entrySet()) {
            series.getData().add(new XYChart.Data<>(formatMonth(entry.getKey()), entry.getValue()));
        }
        chart.getData().add(series);

        return chart;
    }

    private LineChart<String, Number> createMiniRevenueChart(DashboardData data) {
        LineChart<String, Number> chart = createMiniLineChart();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<YearMonth, Double> entry : data.incasariPeLuna.entrySet()) {
            series.getData().add(new XYChart.Data<>(formatMonth(entry.getKey()), entry.getValue()));
        }
        chart.getData().add(series);
        return chart;
    }

    private LineChart<String, Number> createMiniRentalsChart(DashboardData data) {
        LineChart<String, Number> chart = createMiniLineChart();
        chart.getStyleClass().add("command-line-green");
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<YearMonth, Integer> entry : data.inchirieriPeLuna.entrySet()) {
            series.getData().add(new XYChart.Data<>(formatMonth(entry.getKey()), entry.getValue()));
        }
        chart.getData().add(series);
        return chart;
    }

    private LineChart<String, Number> createMiniLineChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        xAxis.setTickLabelsVisible(false);
        xAxis.setTickMarkVisible(false);
        yAxis.setTickLabelsVisible(false);
        yAxis.setTickMarkVisible(false);
        yAxis.setForceZeroInRange(true);

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.getStyleClass().add("command-chart");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(false);
        chart.setHorizontalGridLinesVisible(false);
        chart.setVerticalGridLinesVisible(false);
        chart.setMinHeight(90);
        chart.setMaxHeight(Double.MAX_VALUE);
        return chart;
    }

    private BarChart<String, Number> createStatusBarChart(DashboardData data) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setForceZeroInRange(true);

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.getStyleClass().addAll("command-chart", "command-bar-chart");
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCategoryGap(16);
        chart.setBarGap(4);
        chart.setMinHeight(140);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<StatusInchiriere, Integer> entry : data.inchirieriPeStatus.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey().name(), entry.getValue()));
        }
        chart.getData().add(series);

        return chart;
    }

    private PieChart createCategoryPieChart(DashboardData data) {
        List<PieChart.Data> items = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : data.automobilePeCategorie.entrySet()) {
            items.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        if (items.isEmpty()) {
            items.add(new PieChart.Data("Fara date", 1));
        }

        PieChart chart = new PieChart(FXCollections.observableArrayList(items));
        chart.getStyleClass().addAll("command-pie-chart", "command-chart");
        chart.setLegendVisible(true);
        chart.setLabelsVisible(false);
        chart.setAnimated(false);
        chart.setMinHeight(140);
        return chart;
    }

    private VBox createRecentRentalsCard(DashboardData data) {
        VBox card = new VBox(10);
        card.getStyleClass().addAll("command-chart-card", "command-recent-card");
        card.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Inchirieri recente");
        title.getStyleClass().add("command-card-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label count = new Label(data.inchirieriRecente.size() + " afisate");
        count.getStyleClass().add("command-small-chip");

        header.getChildren().addAll(title, spacer, count);

        VBox list = new VBox(8);
        list.getStyleClass().add("command-recent-list");
        list.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        if (data.inchirieriRecente.isEmpty()) {
            Label emptyLabel = new Label("Nu exista inchirieri recente.");
            emptyLabel.getStyleClass().add("command-muted");
            list.getChildren().add(emptyLabel);
        } else {
            for (Inchiriere inchiriere : data.inchirieriRecente) {
                list.getChildren().add(createRecentRow(inchiriere));
            }
        }

        ScrollPane scrollPane = new ScrollPane(list);
        scrollPane.getStyleClass().add("command-recent-scroll");
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        card.getChildren().addAll(header, scrollPane);
        return card;
    }

    private HBox createRecentRow(Inchiriere inchiriere) {
        HBox row = new HBox(12);
        row.getStyleClass().add("command-recent-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        Label avatar = new Label(initiale(numeClient(inchiriere)));
        avatar.getStyleClass().add("recent-avatar");

        VBox info = new VBox(3);
        info.setMaxWidth(Double.MAX_VALUE);

        Label name = new Label(numeClient(inchiriere));
        name.getStyleClass().add("command-recent-name");

        Label meta = new Label(automobilText(inchiriere) + " | " + perioadaText(inchiriere));
        meta.getStyleClass().add("command-recent-meta");

        info.getChildren().addAll(name, meta);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label amount = new Label(formatSuma(calculeazaTotalSigur(inchiriere)) + " lei");
        amount.getStyleClass().add("command-recent-amount");

        Label statusBadge = new Label(statusText(inchiriere));
        statusBadge.getStyleClass().addAll(
                "status-badge",
                "badge",
                statusClassForInchiriere(inchiriere.getStatus()),
                badgeClassForInchiriere(inchiriere.getStatus())
        );

        row.getChildren().addAll(avatar, info, amount, statusBadge);
        return row;
    }

    private DashboardData calculeazaDashboardData() {
        DashboardData data = new DashboardData();

        List<Automobil> automobile = new ArrayList<>(service.obtineAutomobile());
        List<Inchiriere> inchirieri = new ArrayList<>(service.obtineInchirieri());
        List<Plata> plati = new ArrayList<>(service.obtinePlati());

        data.totalAutomobile = automobile.size();
        data.totalClienti = service.obtineClienti().size();
        data.totalInchirieri = inchirieri.size();
        data.platiTotale = plati.size();

        YearMonth currentMonth = YearMonth.from(LocalDate.now());
        YearMonth startMonth = currentMonth.minusMonths(5);
        for (int i = 0; i < 6; i++) {
            YearMonth month = startMonth.plusMonths(i);
            data.incasariPeLuna.put(month, 0.0);
            data.inchirieriPeLuna.put(month, 0);
        }

        for (StatusInchiriere status : StatusInchiriere.values()) {
            data.inchirieriPeStatus.put(status, 0);
        }

        for (Automobil automobil : automobile) {
            if (automobil.isDisponibil()) {
                data.automobileDisponibile++;
            } else {
                data.automobileIndisponibile++;
            }

            String categorie = automobil.getCategorie() == null ? "Fara categorie" : automobil.getCategorie().getDenumire();
            data.automobilePeCategorie.merge(categorie, 1, Integer::sum);
        }

        for (Inchiriere inchiriere : inchirieri) {
            if (inchiriere.getStatus() == StatusInchiriere.ACTIVA) {
                data.inchirieriActive++;
            }

            if (inchiriere.getStatus() != null) {
                data.inchirieriPeStatus.merge(inchiriere.getStatus(), 1, Integer::sum);
            }

            if (inchiriere.getDataInceput() != null) {
                YearMonth month = YearMonth.from(inchiriere.getDataInceput());
                if (!month.isBefore(startMonth) && !month.isAfter(currentMonth)) {
                    data.inchirieriPeLuna.merge(month, 1, Integer::sum);
                }
            }
        }

        for (Plata plata : plati) {
            data.incasariTotale += plata.getSuma();

            if (plata.getDataPlata() != null) {
                YearMonth month = YearMonth.from(plata.getDataPlata());

                if (month.equals(currentMonth)) {
                    data.incasariLuna += plata.getSuma();
                }

                if (!month.isBefore(startMonth) && !month.isAfter(currentMonth)) {
                    data.incasariPeLuna.merge(month, plata.getSuma(), Double::sum);
                }
            }
        }

        inchirieri.sort(Comparator.comparingInt(Inchiriere::getId).reversed());
        data.inchirieriRecente = inchirieri.stream().limit(10).toList();

        return data;
    }

    private void arataSkeletonDashboard() {
        if (dashboardContainer == null) {
            return;
        }

        VBox skeleton = new VBox(14);
        skeleton.getStyleClass().add("command-dashboard-content");
        skeleton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        skeleton.getChildren().addAll(
                SkeletonFactory.createCardsSkeleton(5),
                SkeletonFactory.createTableSkeleton(4, 4),
                SkeletonFactory.createTableSkeleton(3, 6)
        );

        dashboardContainer.getChildren().setAll(
                settingsService.folosesteSkeletonLoading()
                        ? skeleton
                        : SkeletonFactory.createSimpleLoading("Se incarca dashboard-ul...")
        );
    }

    private void arataDashboardReal(DashboardData data) {
        if (dashboardContainer != null) {
            dashboardContainer.getChildren().setAll(createDashboardContent(data));
        }
    }

    private void configureResponsiveDashboard(VBox content,
                                              VBox metricGrid,
                                              Node[] metricCards,
                                              GridPane topChartsGrid,
                                              Node[] topCards,
                                              GridPane mainGrid,
                                              Node revenueCard,
                                              Node statusCard,
                                              GridPane bottomGrid,
                                              Node categoryCard,
                                              Node recentCard) {
        dashboardContainer.widthProperty().addListener((obs, oldWidth, newWidth) ->
                updateResponsiveDashboard(
                        newWidth.doubleValue(),
                        content,
                        metricGrid,
                        metricCards,
                        topChartsGrid,
                        topCards,
                        mainGrid,
                        revenueCard,
                        statusCard,
                        bottomGrid,
                        categoryCard,
                        recentCard
                )
        );

        content.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                content.applyCss();
                updateResponsiveDashboard(
                        Math.max(dashboardContainer.getWidth(), content.getWidth()),
                        content,
                        metricGrid,
                        metricCards,
                        topChartsGrid,
                        topCards,
                        mainGrid,
                        revenueCard,
                        statusCard,
                        bottomGrid,
                        categoryCard,
                        recentCard
                );
            }
        });
    }

    private void updateResponsiveDashboard(double width,
                                           VBox content,
                                           VBox metricGrid,
                                           Node[] metricCards,
                                           GridPane topChartsGrid,
                                           Node[] topCards,
                                           GridPane mainGrid,
                                           Node revenueCard,
                                           Node statusCard,
                                           GridPane bottomGrid,
                                           Node categoryCard,
                                           Node recentCard) {
        if (width <= 0) {
            return;
        }

        boolean small = width < 760;
        boolean compact = width < 1120;

        setStyleClass(content, "dashboard-compact", compact);
        setStyleClass(content, "dashboard-small", small);

        int metricColumns = columnsForWidth(width, 7, 4, 2, 1);
        int topColumns = columnsForWidth(width, 4, 2, 2, 1);
        double metricHeight = small ? 92 : compact ? 104 : 122;
        double miniChartHeight = small ? 110 : compact ? 130 : 155;
        double mainChartHeight = small ? 190 : compact ? 230 : 300;

        layoutMetricRows(metricGrid, metricCards, metricColumns);

        for (Node node : metricCards) {
            if (node instanceof Region region) {
                region.setMinHeight(metricHeight);
                region.setPrefHeight(metricHeight);
                region.setMaxHeight(metricHeight);
                region.setMaxWidth(Double.MAX_VALUE);
            }
        }

        for (Node node : topCards) {
            setRegionHeight(node, miniChartHeight);
        }

        setRegionHeight(revenueCard, mainChartHeight);
        setRegionHeight(statusCard, mainChartHeight);
        setRegionHeight(categoryCard, mainChartHeight);
        setRegionHeight(recentCard, mainChartHeight);

        layoutCardsGrid(topChartsGrid, topCards, topColumns);

        if (width < 980) {
            layoutTwoCardsGrid(mainGrid, revenueCard, statusCard, true, 100, 100);
        } else {
            layoutTwoCardsGrid(mainGrid, revenueCard, statusCard, false, 66, 34);
        }

        if (width < 1080) {
            layoutTwoCardsGrid(bottomGrid, categoryCard, recentCard, true, 100, 100);
        } else {
            layoutTwoCardsGrid(bottomGrid, categoryCard, recentCard, false, 36, 64);
        }
    }

    private int columnsForWidth(double width, int wide, int desktop, int tablet, int phone) {
        if (width >= 1320) {
            return wide;
        }

        if (width >= 980) {
            return desktop;
        }

        if (width >= 640) {
            return tablet;
        }

        return phone;
    }

    private void layoutCardsGrid(GridPane grid, Node[] cards, int columns) {
        grid.getChildren().clear();
        configureEqualColumns(grid, columns);

        for (int i = 0; i < cards.length; i++) {
            Node card = cards[i];
            int column = i % columns;
            int row = i / columns;

            grid.add(card, column, row);
            GridPane.setHgrow(card, Priority.ALWAYS);
            GridPane.setVgrow(card, Priority.ALWAYS);

            if (card instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
        }
    }

    private void layoutMetricRows(VBox container, Node[] cards, int columns) {
        container.getChildren().clear();

        for (int i = 0; i < cards.length; i += columns) {
            HBox row = new HBox(10);
            row.getStyleClass().add("command-metric-row");
            row.setMaxWidth(Double.MAX_VALUE);

            int end = Math.min(i + columns, cards.length);
            for (int j = i; j < end; j++) {
                Node card = cards[j];

                if (card instanceof Region region) {
                    region.setMaxWidth(Double.MAX_VALUE);
                }

                HBox.setHgrow(card, Priority.ALWAYS);
                row.getChildren().add(card);
            }

            container.getChildren().add(row);
        }
    }

    private void setRegionHeight(Node node, double height) {
        if (node instanceof Region region) {
            region.setMinHeight(height);
            region.setPrefHeight(height);
            region.setMaxHeight(height);
        }
    }

    private void layoutTwoCardsGrid(GridPane grid, Node firstCard, Node secondCard, boolean stacked,
                                    double firstWidth, double secondWidth) {
        grid.getChildren().clear();

        if (stacked) {
            configureColumns(grid, 100);
            grid.add(firstCard, 0, 0);
            grid.add(secondCard, 0, 1);
        } else {
            configureColumns(grid, firstWidth, secondWidth);
            grid.add(firstCard, 0, 0);
            grid.add(secondCard, 1, 0);
        }

        GridPane.setHgrow(firstCard, Priority.ALWAYS);
        GridPane.setHgrow(secondCard, Priority.ALWAYS);
        GridPane.setVgrow(firstCard, Priority.ALWAYS);
        GridPane.setVgrow(secondCard, Priority.ALWAYS);
    }

    private void configureEqualColumns(GridPane grid, int columns) {
        double width = 100.0 / Math.max(1, columns);
        double[] widths = new double[columns];

        for (int i = 0; i < columns; i++) {
            widths[i] = width;
        }

        configureColumns(grid, widths);
    }

    private void setStyleClass(Node node, String styleClass, boolean active) {
        if (active && !node.getStyleClass().contains(styleClass)) {
            node.getStyleClass().add(styleClass);
        }

        if (!active) {
            node.getStyleClass().remove(styleClass);
        }
    }

    private void configureColumns(GridPane grid, double... widths) {
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        grid.setHgap(10);
        grid.setVgap(10);
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();

        for (double width : widths) {
            ColumnConstraints constraints = new ColumnConstraints();
            constraints.setPercentWidth(width);
            constraints.setHgrow(Priority.ALWAYS);
            constraints.setFillWidth(true);
            grid.getColumnConstraints().add(constraints);
        }
    }

    private String formatMonth(YearMonth month) {
        return month.getYear() + "-" + String.format("%02d", month.getMonthValue());
    }

    private String formatSuma(double suma) {
        if (Math.floor(suma) == suma) {
            return String.valueOf((long) suma);
        }

        return String.format("%.2f", suma);
    }

    private String formatPercent(double value) {
        return Math.round(value * 100) + "%";
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

    private String numeClient(Inchiriere inchiriere) {
        if (inchiriere == null || inchiriere.getClient() == null || inchiriere.getClient().getNume() == null) {
            return "Client necunoscut";
        }

        return inchiriere.getClient().getNume();
    }

    private String automobilText(Inchiriere inchiriere) {
        if (inchiriere == null || inchiriere.getAutomobil() == null) {
            return "Automobil necunoscut";
        }

        return inchiriere.getAutomobil().getMarca() + " " + inchiriere.getAutomobil().getModel();
    }

    private String perioadaText(Inchiriere inchiriere) {
        if (inchiriere == null || inchiriere.getDataInceput() == null || inchiriere.getDataSfarsit() == null) {
            return "Perioada necunoscuta";
        }

        return inchiriere.getDataInceput() + " - " + inchiriere.getDataSfarsit();
    }

    private String statusText(Inchiriere inchiriere) {
        return inchiriere == null || inchiriere.getStatus() == null ? "NECUNOSCUT" : inchiriere.getStatus().name();
    }

    private double calculeazaTotalSigur(Inchiriere inchiriere) {
        try {
            return inchiriere.calculeazaTotal();
        } catch (Exception ex) {
            return 0;
        }
    }

    private String statusClassForInchiriere(StatusInchiriere status) {
        return switch (status) {
            case ACTIVA -> "status-success";
            case ANULATA -> "status-danger";
            default -> "status-neutral";
        };
    }

    private String badgeClassForInchiriere(StatusInchiriere status) {
        return switch (status) {
            case ACTIVA -> "badge-activa";
            case ANULATA -> "badge-anulata";
            default -> "badge-finalizata";
        };
    }

    private static class DashboardData {
        private int totalAutomobile;
        private int totalClienti;
        private int totalInchirieri;
        private int inchirieriActive;
        private int automobileDisponibile;
        private int automobileIndisponibile;
        private int platiTotale;
        private double incasariLuna;
        private double incasariTotale;
        private List<Inchiriere> inchirieriRecente = new ArrayList<>();
        private Map<YearMonth, Double> incasariPeLuna = new LinkedHashMap<>();
        private Map<YearMonth, Integer> inchirieriPeLuna = new LinkedHashMap<>();
        private Map<StatusInchiriere, Integer> inchirieriPeStatus = new LinkedHashMap<>();
        private Map<String, Integer> automobilePeCategorie = new LinkedHashMap<>();
    }
}
