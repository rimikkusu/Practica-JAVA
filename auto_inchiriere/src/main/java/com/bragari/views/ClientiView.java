package com.bragari.views;

import java.util.function.Supplier;

import com.bragari.models.Client;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.FormValidator;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientiView {

    private final AutoInchiriereService service;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;

    private TableView<Client> clientiTable;

    public ClientiView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                       BackgroundRunner backgroundRunner) {
        this.service = service;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showClientiPage() {
        VBox page = new VBox(15);
        page.setPadding(new Insets(20));

        Label title = new Label("Gestionare Clienti");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        clientiTable = new TableView<>();

        TableColumn<Client, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Client, String> numeColumn = new TableColumn<>("Nume");
        numeColumn.setCellValueFactory(new PropertyValueFactory<>("nume"));

        TableColumn<Client, String> telefonColumn = new TableColumn<>("Telefon");
        telefonColumn.setCellValueFactory(new PropertyValueFactory<>("telefon"));

        TableColumn<Client, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        clientiTable.getColumns().addAll(idColumn, numeColumn, telefonColumn, emailColumn);
        clientiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        refreshClientiTable();

        TextField cautareField = new TextField();
        cautareField.setPromptText("Cauta dupa nume");

        Button adaugaButton = new Button("Adauga");
        Button editeazaButton = new Button("Editeaza");
        Button stergeButton = new Button("Sterge");
        Button cautaButton = new Button("Cauta");
        Button refreshButton = new Button("Refresh");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(adaugaButton, editeazaButton, stergeButton, cautareField, cautaButton, refreshButton);

        adaugaButton.setOnAction(e -> showAddClientDialog());

        editeazaButton.setOnAction(e -> {
            Client selectedClient = clientiTable.getSelectionModel().getSelectedItem();

            if (selectedClient == null) {
                DialogHelper.showError(owner(), "Selecteaza un client pentru editare.");
                return;
            }

            showEditClientDialog(selectedClient);
        });

        stergeButton.setOnAction(e -> {
            Client selectedClient = clientiTable.getSelectionModel().getSelectedItem();

            if (selectedClient == null) {
                DialogHelper.showError(owner(), "Selecteaza un client pentru stergere.");
                return;
            }

            if (!DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi acest client?")) {
                return;
            }

            backgroundRunner.run(() -> {
                service.stergeClient(selectedClient.getId());
                return service.obtineClienti();
            }, clienti -> {
                clientiTable.setItems(FXCollections.observableArrayList(clienti));
                DialogHelper.showInfo(owner(), "Client sters cu succes.");
            });
        });

        cautaButton.setOnAction(e -> {
            String text = cautareField.getText();

            backgroundRunner.run(
                    () -> service.cautaClientiDupaNume(text),
                    clienti -> clientiTable.setItems(FXCollections.observableArrayList(clienti))
            );
        });

        refreshButton.setOnAction(e -> {
            refreshClientiTable();
            cautareField.clear();
        });

        page.getChildren().addAll(title, buttons, clientiTable);

        root.setCenter(page);
    }

    private void showAddClientDialog() {
        TextField numeField = new TextField();
        numeField.setPromptText("Nume");

        TextField telefonField = new TextField();
        telefonField.setPromptText("Telefon");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Nume:"), 0, 0);
        form.add(numeField, 1, 0);
        form.add(new Label("Telefon:"), 0, 1);
        form.add(telefonField, 1, 1);
        form.add(new Label("Email:"), 0, 2);
        form.add(emailField, 1, 2);

        Button adaugaButton = new Button("Adauga");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Adauga client"), form, DialogHelper.creeazaButoaneDialog(adaugaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Adauga client", content);

        adaugaButton.setOnAction(e -> {
            try {
                String nume = numeField.getText();
                String telefon = telefonField.getText();
                String email = emailField.getText();

                FormValidator.valideazaClientForm(nume, telefon, email);

                Client client = new Client(0, nume, telefon, email);

                backgroundRunner.run(() -> {
                    service.adaugaClient(client);
                    return service.obtineClienti();
                }, clienti -> {
                    clientiTable.setItems(FXCollections.observableArrayList(clienti));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Client adaugat cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(dialog, ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void showEditClientDialog(Client selectedClient) {
        TextField numeField = new TextField(selectedClient.getNume());
        TextField telefonField = new TextField(selectedClient.getTelefon());
        TextField emailField = new TextField(selectedClient.getEmail());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Nume:"), 0, 0);
        form.add(numeField, 1, 0);
        form.add(new Label("Telefon:"), 0, 1);
        form.add(telefonField, 1, 1);
        form.add(new Label("Email:"), 0, 2);
        form.add(emailField, 1, 2);

        Button salveazaButton = new Button("Salveaza");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Editeaza client"), form, DialogHelper.creeazaButoaneDialog(salveazaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Editeaza client", content);

        salveazaButton.setOnAction(e -> {
            try {
                String nume = numeField.getText();
                String telefon = telefonField.getText();
                String email = emailField.getText();

                FormValidator.valideazaClientForm(nume, telefon, email);

                Client clientActualizat = new Client(selectedClient.getId(), nume, telefon, email);

                backgroundRunner.run(() -> {
                    service.actualizeazaClient(clientActualizat);
                    return service.obtineClienti();
                }, clienti -> {
                    clientiTable.setItems(FXCollections.observableArrayList(clienti));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Client actualizat cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(dialog, ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void refreshClientiTable() {
        backgroundRunner.run(
                () -> service.obtineClienti(),
                clienti -> clientiTable.setItems(FXCollections.observableArrayList(clienti))
        );
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
