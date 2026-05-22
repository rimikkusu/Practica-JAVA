package com.bragari;

// MainApp este clasa principala a interfetei. Aici se porneste fereastra,
// se incarca paginile si se tine minte ce utilizator este logat.

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bragari.database.DatabaseManager;
import com.bragari.models.Utilizator;
import com.bragari.services.AuthService;
import com.bragari.services.AutoInchiriereService;
import com.bragari.services.SettingsService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.views.AutomobileView;
import com.bragari.views.ClientiView;
import com.bragari.views.DashboardView;
import com.bragari.views.InchirieriView;
import com.bragari.views.LoginView;
import com.bragari.views.PlatiView;
import com.bragari.views.RapoarteView;
import com.bragari.views.SetariView;
import com.bragari.views.UtilizatoriView;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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
    private SettingsService settingsService;
    private Utilizator utilizatorCurent;
    private boolean afiseazaDoarDisponibile = false;
    private String paginaActiva = "dashboard";

    @Override
    public void start(Stage stage) {
        service = new AutoInchiriereService();
        authService = new AuthService();
        settingsService = new SettingsService();

        root = new BorderPane();
        root.getStyleClass().add("app-shell");
        appRoot = new StackPane(root);
        appRoot.getStyleClass().add("app-root-stack");
        aplicaSetariVizuale();

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
        String paginaImplicita = settingsService.getPaginaImplicitaKey();
        deschidePagina(paginaImplicita, () -> afiseazaPagina(paginaImplicita));
    }

    private void showDashboardPage() {
        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();
        DashboardView dashboardView = new DashboardView(service, settingsService, root, ownerSupplier, this::runInBackground);
        dashboardView.showDashboardPage();
    }

    private void afiseazaPagina(String pagina) {
        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();
        BackgroundRunner backgroundRunner = this::runInBackground;

        switch (pagina) {
            case "clienti" -> new ClientiView(service, settingsService, root, ownerSupplier, backgroundRunner).showClientiPage();
            case "automobile" -> new AutomobileView(service, settingsService, root, ownerSupplier, backgroundRunner).showAutomobilePage();
            case "inchirieri" -> new InchirieriView(service, settingsService, root, ownerSupplier, backgroundRunner).showInchirieriPage();
            case "plati" -> new PlatiView(service, settingsService, root, ownerSupplier, backgroundRunner).showPlatiPage();
            case "rapoarte" -> new RapoarteView(service, settingsService, root, ownerSupplier, backgroundRunner).showRapoartePage();
            case "setari" -> new SetariView(settingsService, root, ownerSupplier, this::aplicaSetariVizuale).showSetariPage();
            case "utilizatori" -> new UtilizatoriView(authService, settingsService, root, ownerSupplier, backgroundRunner, () -> utilizatorCurent).showUtilizatoriPage();
            default -> showDashboardPage();
        }
    }

    private void deschidePagina(String pagina, Runnable action) {
        paginaActiva = pagina;
        root.setLeft(createMenu());
        action.run();
    }

    private VBox createMenu() {
        VBox menu = new VBox();
        menu.getStyleClass().add("sidebar");

        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();
        BackgroundRunner backgroundRunner = this::runInBackground;

        VBox header = new VBox(6);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER_LEFT);

        Label appName = new Label("AutoFleet");
        appName.getStyleClass().addAll("sidebar-app-name", "sidebar-brand-title");
        header.getChildren().add(appName);

        VBox navBox = new VBox();
        navBox.getStyleClass().add("sidebar-nav");

        navBox.getChildren().addAll(
                createNavItem("Dashboard", "D", "dashboard",
                        () -> new DashboardView(service, settingsService, root, ownerSupplier, backgroundRunner).showDashboardPage()),
                createNavItem("Clienti", "C", "clienti",
                        () -> new ClientiView(service, settingsService, root, ownerSupplier, backgroundRunner).showClientiPage()),
                createNavItem("Automobile", "A", "automobile",
                        () -> new AutomobileView(service, settingsService, root, ownerSupplier, backgroundRunner).showAutomobilePage()),
                createNavItem("Inchirieri", "I", "inchirieri",
                        () -> new InchirieriView(service, settingsService, root, ownerSupplier, backgroundRunner).showInchirieriPage()),
                createNavItem("Plati", "P", "plati",
                        () -> new PlatiView(service, settingsService, root, ownerSupplier, backgroundRunner).showPlatiPage()),
                createNavItem("Rapoarte", "R", "rapoarte",
                        () -> new RapoarteView(service, settingsService, root, ownerSupplier, backgroundRunner).showRapoartePage())
        );

        if (utilizatorCurent != null && "ADMIN".equalsIgnoreCase(utilizatorCurent.getRol())) {
            Region separator = new Region();
            separator.getStyleClass().add("nav-separator");
            navBox.getChildren().addAll(
                    separator,
                    createNavItem("Utilizatori", "U", "utilizatori",
                            () -> new UtilizatoriView(authService, settingsService, root, ownerSupplier, backgroundRunner, () -> utilizatorCurent).showUtilizatoriPage())
            );
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox footerTop = new HBox(8);
        footerTop.setAlignment(Pos.CENTER_LEFT);
        Label avatarBadge = new Label(initialeUtilizatorCurent());
        avatarBadge.getStyleClass().add("sidebar-avatar");
        VBox footerText = new VBox(2);
        Label usernameLabel = new Label(utilizatorCurent == null ? "guest" : utilizatorCurent.getUsername());
        usernameLabel.getStyleClass().add("sidebar-username");
        Label roleLabel = new Label(utilizatorCurent == null ? "Vizitator" : utilizatorCurent.getRol());
        roleLabel.getStyleClass().add("sidebar-role");
        footerText.getChildren().addAll(usernameLabel, roleLabel);
        footerTop.getChildren().addAll(avatarBadge, footerText);

        VBox footer = new VBox(10);
        footer.getStyleClass().add("sidebar-footer");
        HBox setariItem = createNavItem("Setari", "S", "setari",
                () -> new SetariView(settingsService, root, ownerSupplier, this::aplicaSetariVizuale).showSetariPage());
        setariItem.getStyleClass().add("sidebar-footer-action");

        HBox logoutItem = createSidebarActionItem("Logout", "X", this::showLoginPage);
        logoutItem.getStyleClass().add("sidebar-footer-action");

        footer.getChildren().addAll(setariItem, logoutItem, footerTop);

        menu.getChildren().addAll(header, navBox, spacer, footer);

        return menu;
    }

    private HBox createNavItem(String labelText, String iconText, String pagina, Runnable action) {
        HBox item = createSidebarActionItem(labelText, iconText, () -> deschidePagina(pagina, action));
        actualizeazaStareItem(item, pagina);
        return item;
    }

    private HBox createSidebarActionItem(String labelText, String iconText, Runnable action) {
        HBox item = new HBox(8);
        item.getStyleClass().add("nav-item");
        item.setAlignment(Pos.CENTER_LEFT);

        Label icon = new Label(iconText);
        icon.getStyleClass().add("nav-icon");
        Label label = new Label(labelText);
        label.getStyleClass().add("nav-label");

        item.getChildren().addAll(icon, label);
        item.setOnMouseClicked(e -> action.run());
        return item;
    }

    private void actualizeazaStareItem(HBox item, String pagina) {
        if (item.getStyleClass().contains("active")) {
            item.getStyleClass().remove("active");
        }

        if (pagina.equals(paginaActiva)) {
            item.getStyleClass().add("active");
        }
    }

    private String initialeUtilizatorCurent() {
        if (utilizatorCurent == null || utilizatorCurent.getUsername() == null || utilizatorCurent.getUsername().isBlank()) {
            return "NA";
        }

        String username = utilizatorCurent.getUsername().trim().toUpperCase();
        return username.length() >= 2 ? username.substring(0, 2) : username;
    }

    private void aplicaSetariVizuale() {
        if (appRoot == null || settingsService == null) {
            return;
        }

        appRoot.getStyleClass().removeAll(
                "theme-amber",
                "theme-light",
                "theme-dark",
                "text-small",
                "text-normal",
                "text-large",
                "table-compact",
                "table-normal"
        );

        appRoot.getStyleClass().addAll(
                settingsService.getTemaCssClass(),
                settingsService.getTextCssClass(),
                settingsService.getTableCssClass()
        );
    }

    public static void main(String[] args) {
        launch(args);
    }
}
