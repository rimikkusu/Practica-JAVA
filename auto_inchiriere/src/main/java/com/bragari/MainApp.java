package com.bragari;

import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bragari.database.DatabaseManager;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.views.AutomobileView;
import com.bragari.views.ClientiView;
import com.bragari.views.InchirieriView;
import com.bragari.views.PlatiView;
import com.bragari.views.RapoarteView;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private BorderPane root;
    private StackPane appRoot;
    private StackPane loadingOverlay;
    private AutoInchiriereService service;
    private int operatiiIncarcare = 0;
    private boolean afiseazaDoarDisponibile = false;

    @Override
    public void start(Stage stage) {
        service = new AutoInchiriereService();

        root = new BorderPane();
        loadingOverlay = createLoadingOverlay();
        appRoot = new StackPane(root, loadingOverlay);

        root.setLeft(createMenu());

        Label label = new Label("Bine ai venit in aplicatia de inchiriere automobile");
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        BorderPane.setMargin(label, new Insets(30));
        root.setCenter(label);

        Scene scene = new Scene(appRoot, 1000, 600);
        aplicaCss(scene);

        stage.setTitle("Auto Inchiriere");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        DatabaseManager.closePool();
    }

    private StackPane createLoadingOverlay() {
        ProgressIndicator progressIndicator = new ProgressIndicator();
        Label loadingLabel = new Label("Se incarca...");

        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        content.getChildren().addAll(progressIndicator, loadingLabel);

        StackPane overlay = new StackPane(content);
        overlay.setStyle("-fx-background-color: rgba(255, 255, 255, 0.65);");
        overlay.setVisible(false);
        overlay.setMouseTransparent(false);

        return overlay;
    }

    private void aplicaCss(Scene scene) {
        DialogHelper.aplicaCss(scene);
    }

    private <T> void runInBackground(Callable<T> action, Consumer<T> onSuccess) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return action.call();
            }
        };

        task.setOnSucceeded(e -> {
            setLoading(false);

            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(e -> {
            setLoading(false);

            Throwable exception = task.getException();
            String message = exception == null || exception.getMessage() == null
                    ? "A aparut o eroare."
                    : exception.getMessage();

            Stage owner = root.getScene() == null ? null : (Stage) root.getScene().getWindow();
            DialogHelper.showError(owner, message);
        });

        setLoading(true);

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private void setLoading(boolean loading) {
        if (loading) {
            operatiiIncarcare++;
        } else if (operatiiIncarcare > 0) {
            operatiiIncarcare--;
        }

        loadingOverlay.setVisible(operatiiIncarcare > 0);
    }

    private VBox createMenu() {
        VBox menu = new VBox(10);
        menu.getStyleClass().add("sidebar");
        menu.setPadding(new Insets(20));
        menu.setPrefWidth(200);

        Supplier<Stage> ownerSupplier = () -> root.getScene() == null ? null : (Stage) root.getScene().getWindow();
        BackgroundRunner backgroundRunner = this::runInBackground;

        ClientiView clientiView = new ClientiView(service, root, ownerSupplier, backgroundRunner);
        AutomobileView automobileView = new AutomobileView(service, root, ownerSupplier, backgroundRunner);
        InchirieriView inchirieriView = new InchirieriView(service, root, ownerSupplier, backgroundRunner);
        PlatiView platiView = new PlatiView(service, root, ownerSupplier, backgroundRunner);
        RapoarteView rapoarteView = new RapoarteView(service, root, ownerSupplier, backgroundRunner);

        Button clientiButton = new Button("Clienti");
        Button automobileButton = new Button("Automobile");
        Button inchirieriButton = new Button("Inchirieri");
        Button platiButton = new Button("Plati");
        Button rapoarteButton = new Button("Rapoarte");

        clientiButton.setMaxWidth(Double.MAX_VALUE);
        automobileButton.setMaxWidth(Double.MAX_VALUE);
        inchirieriButton.setMaxWidth(Double.MAX_VALUE);
        platiButton.setMaxWidth(Double.MAX_VALUE);
        rapoarteButton.setMaxWidth(Double.MAX_VALUE);

        clientiButton.setOnAction(e -> clientiView.showClientiPage());
        automobileButton.setOnAction(e -> automobileView.showAutomobilePage());
        inchirieriButton.setOnAction(e -> inchirieriView.showInchirieriPage());
        platiButton.setOnAction(e -> platiView.showPlatiPage());
        rapoarteButton.setOnAction(e -> rapoarteView.showRapoartePage());

        menu.getChildren().addAll(
                clientiButton,
                automobileButton,
                inchirieriButton,
                platiButton,
                rapoarteButton
        );

        return menu;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
