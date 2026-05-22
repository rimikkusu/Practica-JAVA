package com.bragari.views;

// ClientiView construieste pagina de clienti.
// In aceasta pagina utilizatorul poate vedea, cauta, adauga si modifica clienti.

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.bragari.models.Client;
import com.bragari.services.AutoInchiriereService;
import com.bragari.services.SettingsService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.FormValidator;
import com.bragari.util.SkeletonFactory;
import com.bragari.util.ViewFactory;

import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ClientiView {

    private final AutoInchiriereService service;
    private final SettingsService settingsService;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;

    private TableView<Client> clientiTable;
    private StackPane tableContainer;

    public ClientiView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                       BackgroundRunner backgroundRunner) {
        this(service, new SettingsService(), root, ownerSupplier, backgroundRunner);
    }

    public ClientiView(AutoInchiriereService service, SettingsService settingsService, BorderPane root,
                       Supplier<Stage> ownerSupplier, BackgroundRunner backgroundRunner) {
        this.service = service;
        this.settingsService = settingsService;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showClientiPage() {
        VBox pageContent = new VBox(16);
        pageContent.getStyleClass().addAll("page-content", "table-page-background");
        pageContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        clientiTable = new TableView<>();
        clientiTable.getStyleClass().add("app-table");

        TableColumn<Client, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Client, String> numeColumn = new TableColumn<>("Nume");
        numeColumn.setCellValueFactory(new PropertyValueFactory<>("nume"));

        TableColumn<Client, String> telefonColumn = new TableColumn<>("Telefon");
        telefonColumn.setCellValueFactory(new PropertyValueFactory<>("telefon"));

        TableColumn<Client, String> emailColumn = new TableColumn<>("Email");
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));

        clientiTable.getColumns().addAll(idColumn, numeColumn, telefonColumn, emailColumn);
        ViewFactory.styleTable(clientiTable);

        TextField cautareField = new TextField();
        cautareField.setPromptText("Cauta dupa nume");
        cautareField.getStyleClass().add("search-field");
        cautareField.setPrefWidth(220);

        Button adaugaButton = new Button("Adauga");
        adaugaButton.getStyleClass().add("primary-button");
        Button editeazaButton = new Button("Editeaza");
        editeazaButton.getStyleClass().add("secondary-button");
        Button stergeButton = new Button("Sterge");
        stergeButton.getStyleClass().add("danger-button");
        Button cautaButton = new Button("Cauta");
        cautaButton.getStyleClass().add("secondary-button");
        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("page-toolbar");
        buttons.getChildren().addAll(adaugaButton, editeazaButton, stergeButton, cautareField, cautaButton, refreshButton);

        tableContainer = new StackPane();
        tableContainer.getStyleClass().add("table-content-area");
        tableContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        VBox contentCard = new VBox(14);
        ViewFactory.asCard(contentCard);
        VBox.setVgrow(contentCard, Priority.ALWAYS);
        contentCard.getChildren().addAll(buttons, tableContainer);

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

            if (settingsService.isConfirmareStergereActiva()
                    && !DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi acest client?")) {
                return;
            }

            incarcaClienti(() -> {
                service.stergeClient(selectedClient.getId());
                return service.obtineClienti();
            }, "Client sters cu succes.");
        });

        cautaButton.setOnAction(e -> {
            String text = cautareField.getText();

            incarcaClienti(() -> service.cautaClientiDupaNume(text), null);
        });

        refreshButton.setOnAction(e -> {
            refreshClientiTable();
            cautareField.clear();
        });

        pageContent.getChildren().add(contentCard);

        root.setCenter(ViewFactory.createPage("Clienti", "C", pageContent));
        refreshClientiTable();
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
        incarcaClienti(() -> service.obtineClienti(), null);
    }

    private void incarcaClienti(Callable<List<Client>> action, String successMessage) {
        arataSkeleton();

        backgroundRunner.run(action, clienti -> {
            clientiTable.setItems(FXCollections.observableArrayList(clienti));
            arataTabel();

            if (successMessage != null && !successMessage.isBlank()) {
                DialogHelper.showInfo(owner(), successMessage);
            }
        }, error -> arataTabel());
    }

    private void arataSkeleton() {
        if (tableContainer != null) {
            tableContainer.getChildren().setAll(
                    settingsService.folosesteSkeletonLoading()
                            ? SkeletonFactory.createTableSkeleton(4, 6)
                            : SkeletonFactory.createSimpleLoading("Se incarca clientii...")
            );
        }
    }

    private void arataTabel() {
        if (tableContainer != null) {
            tableContainer.getChildren().setAll(clientiTable);
        }
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
