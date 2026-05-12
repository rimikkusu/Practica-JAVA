package com.bragari;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bragari.database.DatabaseManager;
import com.bragari.models.Utilizator;
import com.bragari.services.AuthService;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.views.AutomobileView;
import com.bragari.views.ClientiView;
import com.bragari.views.DashboardView;
import com.bragari.views.InchirieriView;
import com.bragari.views.LoginView;
import com.bragari.views.PlatiView;
import com.bragari.views.RapoarteView;
import com.bragari.views.UtilizatoriView;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private BorderPane root;
    private StackPane appRoot;
    private AutoInchiriereService service;
    private AuthService authService;
    private Utilizator utilizatorCurent;
    private boolean afiseazaDoarDisponibile = false;
    private String paginaActiva = "dashboard";

    @Override
    public void start(Stage stage) {
        service = new AutoInchiriereService();
        authService = new AuthService();

        root = new BorderPane();
        root.getStyleClass().add("app-shell");
        appRoot = new StackPane(root);
        appRoot.getStyleClass().add("app-root-stack");

        Scene scene = new Scene(appRoot, 1000, 600);
        aplicaCss(scene);

        stage.setTitle("Auto Inchiriere");
        stage.setScene(scene);
        stage.show();

        showLoginPage();
    }

    @Override
    public void stop() {
        DatabaseManager.closePool();
    }

    private void aplicaCss(Scene scene) {
        DialogHelper.aplicaCss(scene);
    }

    private <T> void runInBackground(Callable<T> action, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };

        task.setOnSucceeded(e -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(e -> {
            Throwable exception = task.getException();
            String message = exception == null || exception.getMessage() == null
                    ? "A aparut o eroare."
                    : exception.getMessage();

            if (onError != null) {
                onError.accept(exception);
            }

            Stage owner = root.getScene() == null ? null : (Stage) root.getScene().getWindow();
            DialogHelper.showError(owner, message);
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void showLoginPage() {
        utilizatorCurent = null;
        paginaActiva = "login";
        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();

        LoginView loginView = new LoginView(
                authService,
                root,
                ownerSupplier,
                this::runInBackground,
                this::showMainPage
        );

        loginView.showLoginPage();
    }

    private void showMainPage(Utilizator utilizator) {
        utilizatorCurent = utilizator;
        deschidePagina("dashboard", this::showDashboardPage);
    }

    private void showDashboardPage() {
        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();
        DashboardView dashboardView = new DashboardView(service, root, ownerSupplier, this::runInBackground);
        dashboardView.showDashboardPage();
    }

    private void deschidePagina(String pagina, Runnable action) {
        paginaActiva = pagina;
        root.setLeft(createMenu());
        action.run();
    }

    private VBox createMenu() {
        VBox menu = new VBox(10);
        menu.getStyleClass().add("sidebar");

        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();
        BackgroundRunner backgroundRunner = this::runInBackground;

        DashboardView dashboardView = new DashboardView(service, root, ownerSupplier, backgroundRunner);
        ClientiView clientiView = new ClientiView(service, root, ownerSupplier, backgroundRunner);
        AutomobileView automobileView = new AutomobileView(service, root, ownerSupplier, backgroundRunner);
        InchirieriView inchirieriView = new InchirieriView(service, root, ownerSupplier, backgroundRunner);
        PlatiView platiView = new PlatiView(service, root, ownerSupplier, backgroundRunner);
        RapoarteView rapoarteView = new RapoarteView(service, root, ownerSupplier, backgroundRunner);
        UtilizatoriView utilizatoriView = new UtilizatoriView(authService, root, ownerSupplier, backgroundRunner, () -> utilizatorCurent);

        VBox brand = new VBox(4);
        brand.getStyleClass().add("sidebar-brand");
        Label brandBadge = new Label("AI");
        brandBadge.getStyleClass().add("brand-badge");
        Label brandTitle = new Label("Auto Inchiriere");
        brandTitle.getStyleClass().add("brand-title");
        Label brandSubtitle = new Label("Amber Noir UI");
        brandSubtitle.getStyleClass().add("brand-subtitle");
        brand.getChildren().addAll(brandBadge, brandTitle, brandSubtitle);

        VBox navBox = new VBox(6);
        navBox.getStyleClass().add("sidebar-nav");

        Button dashboardButton = new Button("Dashboard");
        Button clientiButton = new Button("Clienti");
        Button automobileButton = new Button("Automobile");
        Button inchirieriButton = new Button("Inchirieri");
        Button platiButton = new Button("Plati");
        Button rapoarteButton = new Button("Rapoarte");
        Button utilizatoriButton = new Button("Utilizatori");
        Button logoutButton = new Button("Logout");

        List<Button> navButtons = new ArrayList<>();
        navButtons.add(dashboardButton);
        navButtons.add(clientiButton);
        navButtons.add(automobileButton);
        navButtons.add(inchirieriButton);
        navButtons.add(platiButton);
        navButtons.add(rapoarteButton);
        navButtons.add(utilizatoriButton);

        for (Button navButton : navButtons) {
            navButton.getStyleClass().add("nav-button");
            navButton.setMaxWidth(Double.MAX_VALUE);
        }

        logoutButton.getStyleClass().add("logout-button");
        clientiButton.setMaxWidth(Double.MAX_VALUE);
        automobileButton.setMaxWidth(Double.MAX_VALUE);
        inchirieriButton.setMaxWidth(Double.MAX_VALUE);
        platiButton.setMaxWidth(Double.MAX_VALUE);
        rapoarteButton.setMaxWidth(Double.MAX_VALUE);
        utilizatoriButton.setMaxWidth(Double.MAX_VALUE);
        logoutButton.setMaxWidth(Double.MAX_VALUE);

        actualizeazaStareButon(dashboardButton, "dashboard");
        actualizeazaStareButon(clientiButton, "clienti");
        actualizeazaStareButon(automobileButton, "automobile");
        actualizeazaStareButon(inchirieriButton, "inchirieri");
        actualizeazaStareButon(platiButton, "plati");
        actualizeazaStareButon(rapoarteButton, "rapoarte");
        actualizeazaStareButon(utilizatoriButton, "utilizatori");

        dashboardButton.setOnAction(e -> deschidePagina("dashboard", dashboardView::showDashboardPage));
        clientiButton.setOnAction(e -> deschidePagina("clienti", clientiView::showClientiPage));
        automobileButton.setOnAction(e -> deschidePagina("automobile", automobileView::showAutomobilePage));
        inchirieriButton.setOnAction(e -> deschidePagina("inchirieri", inchirieriView::showInchirieriPage));
        platiButton.setOnAction(e -> deschidePagina("plati", platiView::showPlatiPage));
        rapoarteButton.setOnAction(e -> deschidePagina("rapoarte", rapoarteView::showRapoartePage));
        utilizatoriButton.setOnAction(e -> deschidePagina("utilizatori", utilizatoriView::showUtilizatoriPage));
        logoutButton.setOnAction(e -> showLoginPage());

        navBox.getChildren().addAll(
                dashboardButton,
                clientiButton,
                automobileButton,
                inchirieriButton,
                platiButton,
                rapoarteButton
        );

        if (utilizatorCurent != null && "ADMIN".equalsIgnoreCase(utilizatorCurent.getRol())) {
            navBox.getChildren().add(utilizatoriButton);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footerTop = new HBox(8);
        Label avatarBadge = new Label(initialeUtilizatorCurent());
        avatarBadge.getStyleClass().add("avatar-badge");
        VBox footerText = new VBox(2);
        Label usernameLabel = new Label(utilizatorCurent == null ? "guest" : utilizatorCurent.getUsername());
        usernameLabel.getStyleClass().add("sidebar-user-name");
        Label roleLabel = new Label(utilizatorCurent == null ? "Vizitator" : utilizatorCurent.getRol());
        roleLabel.getStyleClass().add("sidebar-user-role");
        footerText.getChildren().addAll(usernameLabel, roleLabel);
        footerTop.getChildren().addAll(avatarBadge, footerText);

        VBox footer = new VBox(10);
        footer.getStyleClass().add("sidebar-footer");
        footer.getChildren().addAll(logoutButton, footerTop);

        menu.getChildren().addAll(brand, navBox, spacer, footer);

        return menu;
    }

    private void actualizeazaStareButon(Button button, String pagina) {
        if (button.getStyleClass().contains("nav-button-active")) {
            button.getStyleClass().remove("nav-button-active");
        }

        if (pagina.equals(paginaActiva)) {
            button.getStyleClass().add("nav-button-active");
        }
    }

    private String initialeUtilizatorCurent() {
        if (utilizatorCurent == null || utilizatorCurent.getUsername() == null || utilizatorCurent.getUsername().isBlank()) {
            return "NA";
        }

        String username = utilizatorCurent.getUsername().trim().toUpperCase();
        return username.length() >= 2 ? username.substring(0, 2) : username;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
