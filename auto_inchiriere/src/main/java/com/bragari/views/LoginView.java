package com.bragari.views;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.bragari.models.Utilizator;
import com.bragari.services.AuthService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginView {

    private final AuthService authService;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;
    private final Consumer<Utilizator> onLoginSuccess;

    public LoginView(AuthService authService, BorderPane root, Supplier<Stage> ownerSupplier,
                     BackgroundRunner backgroundRunner, Consumer<Utilizator> onLoginSuccess) {
        this.authService = authService;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
        this.onLoginSuccess = onLoginSuccess;
    }

    public void showLoginPage() {
        root.setLeft(null);

        StackPane page = new StackPane();
        page.getStyleClass().add("login-page");
        page.setPadding(new Insets(30));

        VBox card = new VBox(14);
        card.getStyleClass().add("login-card");
        card.setAlignment(Pos.CENTER);

        Label badge = new Label("AI");
        badge.getStyleClass().add("login-badge");

        Label title = new Label("Auto Inchiriere");
        title.getStyleClass().add("login-title");

        Label subtitle = new Label("Sistem de management flota auto");
        subtitle.getStyleClass().add("login-subtitle");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField parolaField = new PasswordField();
        parolaField.setPromptText("Parola");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setAlignment(Pos.CENTER);

        Label usernameLabel = new Label("Username");
        usernameLabel.getStyleClass().add("login-field-label");
        Label parolaLabel = new Label("Parola");
        parolaLabel.getStyleClass().add("login-field-label");

        form.add(usernameLabel, 0, 0);
        form.add(usernameField, 1, 0);
        form.add(parolaLabel, 0, 1);
        form.add(parolaField, 1, 1);

        Button loginButton = new Button("Autentificare");
        loginButton.getStyleClass().addAll("primary-button", "login-button");
        loginButton.setDisable(true);

        Runnable loginAction = () -> {
            String username = usernameField.getText();
            String parola = parolaField.getText();

            if (username == null || username.isBlank() || parola == null || parola.isBlank()) {
                DialogHelper.showError(owner(), "Completeaza username si parola.");
                return;
            }

            backgroundRunner.run(
                    () -> authService.login(username, parola),
                    utilizator -> {
                        if (utilizator == null) {
                            DialogHelper.showError(owner(), "Username sau parola gresita.");
                            return;
                        }

                        usernameField.clear();
                        parolaField.clear();
                        onLoginSuccess.accept(utilizator);
                    }
            );
        };

        loginButton.setOnAction(e -> loginAction.run());
        usernameField.setOnAction(e -> parolaField.requestFocus());
        parolaField.setOnAction(e -> loginAction.run());

        card.getChildren().addAll(badge, title, subtitle, form, loginButton);
        page.getChildren().add(card);
        root.setCenter(page);

        backgroundRunner.run(() -> {
            authService.creeazaAdminImplicitDacaNuExista();
            return null;
        }, ignored -> loginButton.setDisable(false));
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
