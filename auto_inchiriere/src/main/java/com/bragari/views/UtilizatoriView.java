package com.bragari.views;

import java.util.function.Supplier;

import com.bragari.models.Utilizator;
import com.bragari.services.AuthService;
import com.bragari.services.SettingsService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.ViewFactory;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UtilizatoriView {

    private final AuthService authService;
    private final SettingsService settingsService;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;
    private final Supplier<Utilizator> utilizatorCurentSupplier;

    private TableView<Utilizator> utilizatoriTable;

    public UtilizatoriView(AuthService authService, BorderPane root, Supplier<Stage> ownerSupplier,
                           BackgroundRunner backgroundRunner, Supplier<Utilizator> utilizatorCurentSupplier) {
        this(authService, new SettingsService(), root, ownerSupplier, backgroundRunner, utilizatorCurentSupplier);
    }

    public UtilizatoriView(AuthService authService, SettingsService settingsService, BorderPane root,
                           Supplier<Stage> ownerSupplier, BackgroundRunner backgroundRunner,
                           Supplier<Utilizator> utilizatorCurentSupplier) {
        this.authService = authService;
        this.settingsService = settingsService;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
        this.utilizatorCurentSupplier = utilizatorCurentSupplier;
    }

    public void showUtilizatoriPage() {
        VBox pageContent = new VBox(16);
        pageContent.getStyleClass().addAll("page-content", "table-page-background");
        pageContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        utilizatoriTable = new TableView<>();
        utilizatoriTable.getStyleClass().add("app-table");

        TableColumn<Utilizator, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Utilizator, String> usernameColumn = new TableColumn<>("Username");
        usernameColumn.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<Utilizator, String> rolColumn = new TableColumn<>("Rol");
        rolColumn.setCellValueFactory(new PropertyValueFactory<>("rol"));
        rolColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label(item);
                badge.getStyleClass().addAll(
                        "status-badge",
                        "badge",
                        "ADMIN".equalsIgnoreCase(item) ? "status-admin" : "status-user"
                );
                setGraphic(badge);
                setText(null);
            }
        });

        utilizatoriTable.getColumns().addAll(idColumn, usernameColumn, rolColumn);
        ViewFactory.styleTable(utilizatoriTable);

        refreshUtilizatoriTable();

        Button adaugaButton = new Button("Adauga utilizator");
        adaugaButton.getStyleClass().add("primary-button");
        Button schimbaParolaButton = new Button("Schimba parola");
        schimbaParolaButton.getStyleClass().add("secondary-button");
        Button stergeButton = new Button("Sterge utilizator");
        stergeButton.getStyleClass().add("danger-button");
        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("page-toolbar");
        buttons.getChildren().addAll(adaugaButton, schimbaParolaButton, stergeButton, refreshButton);

        VBox contentCard = new VBox(14);
        ViewFactory.asCard(contentCard);
        VBox.setVgrow(contentCard, Priority.ALWAYS);
        VBox.setVgrow(utilizatoriTable, Priority.ALWAYS);
        contentCard.getChildren().addAll(buttons, utilizatoriTable);

        adaugaButton.setOnAction(e -> showAddUtilizatorDialog());

        schimbaParolaButton.setOnAction(e -> {
            Utilizator selectedUtilizator = utilizatoriTable.getSelectionModel().getSelectedItem();

            if (selectedUtilizator == null) {
                DialogHelper.showError(owner(), "Selecteaza un utilizator.");
                return;
            }

            showSchimbaParolaDialog(selectedUtilizator);
        });

        stergeButton.setOnAction(e -> {
            Utilizator selectedUtilizator = utilizatoriTable.getSelectionModel().getSelectedItem();

            if (selectedUtilizator == null) {
                DialogHelper.showError(owner(), "Selecteaza un utilizator pentru stergere.");
                return;
            }

            Utilizator utilizatorCurent = utilizatorCurentSupplier.get();

            if (utilizatorCurent != null && utilizatorCurent.getId() == selectedUtilizator.getId()) {
                DialogHelper.showError(owner(), "Nu poti sterge utilizatorul curent logat.");
                return;
            }

            if (settingsService.isConfirmareStergereActiva()
                    && !DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi acest utilizator?")) {
                return;
            }

            backgroundRunner.run(() -> {
                authService.stergeUtilizator(selectedUtilizator.getId());
                return authService.obtineUtilizatori();
            }, utilizatori -> {
                utilizatoriTable.setItems(FXCollections.observableArrayList(utilizatori));
                DialogHelper.showInfo(owner(), "Utilizator sters cu succes.");
            });
        });

        refreshButton.setOnAction(e -> refreshUtilizatoriTable());

        pageContent.getChildren().add(contentCard);

        root.setCenter(ViewFactory.createPage("Utilizatori", "U", pageContent));
    }

    private void showAddUtilizatorDialog() {
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");

        PasswordField parolaField = new PasswordField();
        parolaField.setPromptText("Parola");

        ComboBox<String> rolComboBox = new ComboBox<>();
        rolComboBox.setItems(FXCollections.observableArrayList("ADMIN", "USER"));
        rolComboBox.setValue("USER");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Username:"), 0, 0);
        form.add(usernameField, 1, 0);
        form.add(new Label("Parola:"), 0, 1);
        form.add(parolaField, 1, 1);
        form.add(new Label("Rol:"), 0, 2);
        form.add(rolComboBox, 1, 2);

        Button adaugaButton = new Button("Adauga");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Adauga utilizator"), form, DialogHelper.creeazaButoaneDialog(adaugaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Adauga utilizator", content);

        adaugaButton.setOnAction(e -> {
            try {
                String username = usernameField.getText();
                String parola = parolaField.getText();
                String rol = rolComboBox.getValue();

                if (username == null || username.isBlank()) {
                    DialogHelper.showError(dialog, "Username este obligatoriu.");
                    return;
                }

                if (parola == null || parola.isBlank()) {
                    DialogHelper.showError(dialog, "Parola este obligatorie.");
                    return;
                }

                backgroundRunner.run(() -> {
                    authService.creeazaUtilizator(username, parola, rol);
                    return authService.obtineUtilizatori();
                }, utilizatori -> {
                    utilizatoriTable.setItems(FXCollections.observableArrayList(utilizatori));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Utilizator adaugat cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(dialog, ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void showSchimbaParolaDialog(Utilizator utilizator) {
        PasswordField parolaField = new PasswordField();
        parolaField.setPromptText("Parola noua");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Utilizator:"), 0, 0);
        form.add(new Label(utilizator.getUsername()), 1, 0);
        form.add(new Label("Parola noua:"), 0, 1);
        form.add(parolaField, 1, 1);

        Button salveazaButton = new Button("Salveaza");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Schimba parola"), form, DialogHelper.creeazaButoaneDialog(salveazaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Schimba parola", content);

        salveazaButton.setOnAction(e -> {
            try {
                String parolaNoua = parolaField.getText();

                if (parolaNoua == null || parolaNoua.isBlank()) {
                    DialogHelper.showError(dialog, "Parola noua este obligatorie.");
                    return;
                }

                backgroundRunner.run(() -> {
                    authService.schimbaParola(utilizator.getId(), parolaNoua);
                    return authService.obtineUtilizatori();
                }, utilizatori -> {
                    utilizatoriTable.setItems(FXCollections.observableArrayList(utilizatori));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Parola a fost schimbata cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(dialog, ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void refreshUtilizatoriTable() {
        backgroundRunner.run(
                () -> authService.obtineUtilizatori(),
                utilizatori -> utilizatoriTable.setItems(FXCollections.observableArrayList(utilizatori))
        );
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
