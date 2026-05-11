package com.bragari.views;

import java.time.LocalDate;
import java.util.function.Supplier;

import com.bragari.models.Automobil;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.StatusInchiriere;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.FormValidator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class InchirieriView {

    private final AutoInchiriereService service;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;

    private TableView<Inchiriere> inchirieriTable;

    public InchirieriView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                          BackgroundRunner backgroundRunner) {
        this.service = service;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showInchirieriPage() {
        VBox page = new VBox(15);
        page.setPadding(new Insets(20));

        Label title = new Label("Gestionare Inchirieri");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        inchirieriTable = new TableView<>();

        TableColumn<Inchiriere, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Inchiriere, String> clientColumn = new TableColumn<>("Client");
        clientColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClient().getNume())
        );

        TableColumn<Inchiriere, String> automobilColumn = new TableColumn<>("Automobil");
        automobilColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getAutomobil().getMarca() + " " +
                                data.getValue().getAutomobil().getModel()
                )
        );

        TableColumn<Inchiriere, String> dataInceputColumn = new TableColumn<>("Data inceput");
        dataInceputColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataInceput().toString())
        );

        TableColumn<Inchiriere, String> dataSfarsitColumn = new TableColumn<>("Data sfarsit");
        dataSfarsitColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataSfarsit().toString())
        );

        TableColumn<Inchiriere, String> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().calculeazaTotal() + " lei")
        );

        TableColumn<Inchiriere, String> statusColumn = new TableColumn<>("Status");
        statusColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getStatus().toString())
        );

        inchirieriTable.getColumns().addAll(
                idColumn,
                clientColumn,
                automobilColumn,
                dataInceputColumn,
                dataSfarsitColumn,
                totalColumn,
                statusColumn
        );

        inchirieriTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        refreshInchirieriTable();

        Button adaugaButton = new Button("Adauga");
        Button editeazaStatusButton = new Button("Editeaza");
        Button stergeButton = new Button("Sterge");
        Button refreshButton = new Button("Refresh");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(adaugaButton, editeazaStatusButton, stergeButton, refreshButton);

        adaugaButton.setOnAction(e -> showAddInchiriereDialog());

        editeazaStatusButton.setOnAction(e -> {
            try {
                Inchiriere selectedInchiriere = inchirieriTable.getSelectionModel().getSelectedItem();

                if (selectedInchiriere == null) {
                    DialogHelper.showError(owner(), "Selecteaza o inchiriere pentru editare.");
                    return;
                }

                showEditInchiriereDialog(selectedInchiriere);
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        stergeButton.setOnAction(e -> {
            try {
                Inchiriere selectedInchiriere = inchirieriTable.getSelectionModel().getSelectedItem();

                if (selectedInchiriere == null) {
                    DialogHelper.showError(owner(), "Selecteaza o inchiriere pentru stergere.");
                    return;
                }

                if (!DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi aceasta inchiriere?")) {
                    return;
                }

                backgroundRunner.run(() -> {
                    service.stergeInchiriere(selectedInchiriere.getId());
                    return service.obtineInchirieri();
                }, inchirieri -> {
                    inchirieriTable.setItems(FXCollections.observableArrayList(inchirieri));
                    DialogHelper.showInfo(owner(), "Inchiriere stearsa cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        refreshButton.setOnAction(e -> refreshInchirieriTable());

        page.getChildren().addAll(title, buttons, inchirieriTable);

        root.setCenter(page);
    }

    private void showAddInchiriereDialog() {
        ComboBox<Client> clientComboBox = new ComboBox<>();
        clientComboBox.setPromptText("Alege client");
        refreshClientiComboBox(clientComboBox);

        ComboBox<Automobil> automobilComboBox = new ComboBox<>();
        automobilComboBox.setPromptText("Alege automobil disponibil");
        refreshAutomobileDisponibileComboBox(automobilComboBox);

        DatePicker dataInceputPicker = new DatePicker();
        dataInceputPicker.setPromptText("Data inceput");

        DatePicker dataSfarsitPicker = new DatePicker();
        dataSfarsitPicker.setPromptText("Data sfarsit");

        ComboBox<StatusInchiriere> statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList(StatusInchiriere.values()));
        statusComboBox.setValue(StatusInchiriere.ACTIVA);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Client:"), 0, 0);
        form.add(clientComboBox, 1, 0);
        form.add(new Label("Automobil:"), 0, 1);
        form.add(automobilComboBox, 1, 1);
        form.add(new Label("Data inceput:"), 0, 2);
        form.add(dataInceputPicker, 1, 2);
        form.add(new Label("Data sfarsit:"), 0, 3);
        form.add(dataSfarsitPicker, 1, 3);
        form.add(new Label("Status:"), 0, 4);
        form.add(statusComboBox, 1, 4);

        Button adaugaButton = new Button("Adauga");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Adauga inchiriere"), form, DialogHelper.creeazaButoaneDialog(adaugaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Adauga inchiriere", content);

        adaugaButton.setOnAction(e -> {
            try {
                Client client = clientComboBox.getValue();
                Automobil automobil = automobilComboBox.getValue();
                LocalDate dataInceput = dataInceputPicker.getValue();
                LocalDate dataSfarsit = dataSfarsitPicker.getValue();
                StatusInchiriere status = statusComboBox.getValue();

                FormValidator.valideazaInchiriereForm(client, automobil, dataInceput, dataSfarsit, status);

                Inchiriere inchiriere = new Inchiriere(0, client, automobil, dataInceput, dataSfarsit, status);

                backgroundRunner.run(() -> {
                    service.adaugaInchiriere(inchiriere);
                    return service.obtineInchirieri();
                }, inchirieri -> {
                    inchirieriTable.setItems(FXCollections.observableArrayList(inchirieri));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Inchiriere adaugata cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void showEditInchiriereDialog(Inchiriere selectedInchiriere) {
        Label clientLabel = new Label(selectedInchiriere.getClient().getNume());
        Label automobilLabel = new Label(selectedInchiriere.getAutomobil().getMarca() + " " + selectedInchiriere.getAutomobil().getModel());
        Label perioadaLabel = new Label(selectedInchiriere.getDataInceput() + " - " + selectedInchiriere.getDataSfarsit());

        ComboBox<StatusInchiriere> statusComboBox = new ComboBox<>();
        statusComboBox.setItems(FXCollections.observableArrayList(StatusInchiriere.values()));
        statusComboBox.setValue(selectedInchiriere.getStatus());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Client:"), 0, 0);
        form.add(clientLabel, 1, 0);
        form.add(new Label("Automobil:"), 0, 1);
        form.add(automobilLabel, 1, 1);
        form.add(new Label("Perioada:"), 0, 2);
        form.add(perioadaLabel, 1, 2);
        form.add(new Label("Status:"), 0, 3);
        form.add(statusComboBox, 1, 3);

        Button salveazaButton = new Button("Salveaza");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Editeaza inchiriere"), form, DialogHelper.creeazaButoaneDialog(salveazaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Editeaza inchiriere", content);

        salveazaButton.setOnAction(e -> {
            try {
                StatusInchiriere statusNou = statusComboBox.getValue();

                if (statusNou == null) {
                    DialogHelper.showError(dialog, "Selecteaza statusul nou.");
                    return;
                }

                Inchiriere inchiriereActualizata = new Inchiriere(
                        selectedInchiriere.getId(),
                        selectedInchiriere.getClient(),
                        selectedInchiriere.getAutomobil(),
                        selectedInchiriere.getDataInceput(),
                        selectedInchiriere.getDataSfarsit(),
                        statusNou
                );

                backgroundRunner.run(() -> {
                    service.actualizeazaInchiriere(inchiriereActualizata);
                    return service.obtineInchirieri();
                }, inchirieri -> {
                    inchirieriTable.setItems(FXCollections.observableArrayList(inchirieri));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Inchiriere actualizata cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(dialog, ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void refreshInchirieriTable() {
        backgroundRunner.run(
                () -> service.obtineInchirieri(),
                inchirieri -> inchirieriTable.setItems(FXCollections.observableArrayList(inchirieri))
        );
    }

    private void refreshClientiComboBox(ComboBox<Client> clientComboBox) {
        backgroundRunner.run(
                () -> service.obtineClienti(),
                clienti -> clientComboBox.setItems(FXCollections.observableArrayList(clienti))
        );
    }

    private void refreshAutomobileDisponibileComboBox(ComboBox<Automobil> automobilComboBox) {
        backgroundRunner.run(
                () -> service.obtineAutomobileDisponibile(),
                automobile -> automobilComboBox.setItems(FXCollections.observableArrayList(automobile))
        );
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
