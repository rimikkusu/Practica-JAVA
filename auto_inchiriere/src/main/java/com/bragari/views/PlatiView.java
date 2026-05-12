package com.bragari.views;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.FormValidator;
import com.bragari.util.SkeletonFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PlatiView {

    private final AutoInchiriereService service;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;

    private TableView<Plata> platiTable;
    private StackPane tableContainer;

    public PlatiView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                     BackgroundRunner backgroundRunner) {
        this.service = service;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showPlatiPage() {
        VBox page = new VBox(18);
        page.getStyleClass().add("page-container");

        Label title = new Label("Gestionare Plati");
        title.getStyleClass().add("page-title");

        platiTable = new TableView<>();
        platiTable.getStyleClass().add("app-table");

        TableColumn<Plata, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Plata, String> inchiriereColumn = new TableColumn<>("Inchiriere");
        inchiriereColumn.setCellValueFactory(data ->
                new SimpleStringProperty("Inchiriere ID: " + data.getValue().getInchiriere().getId())
        );

        TableColumn<Plata, String> clientColumn = new TableColumn<>("Client");
        clientColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getInchiriere().getClient().getNume())
        );

        TableColumn<Plata, String> automobilColumn = new TableColumn<>("Automobil");
        automobilColumn.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getInchiriere().getAutomobil().getMarca() + " " +
                                data.getValue().getInchiriere().getAutomobil().getModel()
                )
        );

        TableColumn<Plata, Double> sumaColumn = new TableColumn<>("Suma");
        sumaColumn.setCellValueFactory(new PropertyValueFactory<>("suma"));

        TableColumn<Plata, String> metodaColumn = new TableColumn<>("Metoda");
        metodaColumn.setCellValueFactory(new PropertyValueFactory<>("metodaPlata"));

        TableColumn<Plata, String> dataColumn = new TableColumn<>("Data plata");
        dataColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getDataPlata().toString())
        );

        platiTable.getColumns().addAll(
                idColumn,
                inchiriereColumn,
                clientColumn,
                automobilColumn,
                sumaColumn,
                metodaColumn,
                dataColumn
        );

        platiTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        Button adaugaButton = new Button("Adauga");
        adaugaButton.getStyleClass().add("primary-button");
        Button editeazaButton = new Button("Editeaza");
        editeazaButton.getStyleClass().add("secondary-button");
        Button stergeButton = new Button("Sterge");
        stergeButton.getStyleClass().add("danger-button");
        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("page-toolbar");
        buttons.getChildren().addAll(adaugaButton, editeazaButton, stergeButton, refreshButton);

        tableContainer = new StackPane();
        tableContainer.getStyleClass().add("table-content-area");

        VBox contentCard = new VBox(14);
        contentCard.getStyleClass().add("content-card");
        contentCard.getChildren().addAll(buttons, tableContainer);

        adaugaButton.setOnAction(e -> showAddPlataDialog());

        editeazaButton.setOnAction(e -> {
            try {
                Plata selectedPlata = platiTable.getSelectionModel().getSelectedItem();

                if (selectedPlata == null) {
                    DialogHelper.showError(owner(), "Selecteaza o plata pentru editare.");
                    return;
                }

                showEditPlataDialog(selectedPlata);
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        stergeButton.setOnAction(e -> {
            try {
                Plata selectedPlata = platiTable.getSelectionModel().getSelectedItem();

                if (selectedPlata == null) {
                    DialogHelper.showError(owner(), "Selecteaza o plata pentru stergere.");
                    return;
                }

                if (!DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi aceasta plata?")) {
                    return;
                }

                incarcaPlati(() -> {
                    service.stergePlata(selectedPlata.getId());
                    return service.obtinePlati();
                }, "Plata stearsa cu succes.");
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        refreshButton.setOnAction(e -> refreshPlatiTable());

        page.getChildren().addAll(title, contentCard);

        root.setCenter(page);
        refreshPlatiTable();
    }

    private void showAddPlataDialog() {
        ComboBox<Inchiriere> inchiriereComboBox = new ComboBox<>();
        inchiriereComboBox.setPromptText("Alege inchirierea");
        refreshInchirieriComboBox(inchiriereComboBox);

        TextField sumaField = new TextField();
        sumaField.setPromptText("Suma");

        ComboBox<String> metodaComboBox = new ComboBox<>();
        metodaComboBox.setItems(FXCollections.observableArrayList("CARD", "CASH", "TRANSFER"));
        metodaComboBox.setValue("CARD");

        DatePicker dataPlataPicker = new DatePicker();
        dataPlataPicker.setPromptText("Data plata");

        inchiriereComboBox.setOnAction(e -> {
            Inchiriere inchiriere = inchiriereComboBox.getValue();

            if (inchiriere != null) {
                sumaField.setText(String.valueOf(inchiriere.calculeazaTotal()));
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Inchiriere:"), 0, 0);
        form.add(inchiriereComboBox, 1, 0);
        form.add(new Label("Suma:"), 0, 1);
        form.add(sumaField, 1, 1);
        form.add(new Label("Metoda plata:"), 0, 2);
        form.add(metodaComboBox, 1, 2);
        form.add(new Label("Data plata:"), 0, 3);
        form.add(dataPlataPicker, 1, 3);

        Button adaugaButton = new Button("Adauga");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Adauga plata"), form, DialogHelper.creeazaButoaneDialog(adaugaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Adauga plata", content);

        adaugaButton.setOnAction(e -> {
            try {
                Inchiriere inchiriere = inchiriereComboBox.getValue();
                String sumaText = sumaField.getText();
                String metoda = metodaComboBox.getValue();
                LocalDate dataPlata = dataPlataPicker.getValue();

                FormValidator.valideazaPlataForm(inchiriere, sumaText, metoda, dataPlata);

                double suma = FormValidator.parseazaSuma(sumaText);
                Plata plata = new Plata(0, inchiriere, suma, metoda, dataPlata);

                backgroundRunner.run(() -> {
                    service.adaugaPlata(plata);
                    return service.obtinePlati();
                }, plati -> {
                    platiTable.setItems(FXCollections.observableArrayList(plati));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Plata adaugata cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void showEditPlataDialog(Plata selectedPlata) {
        ComboBox<Inchiriere> inchiriereComboBox = new ComboBox<>();
        inchiriereComboBox.setPromptText("Alege inchirierea");
        inchiriereComboBox.setValue(selectedPlata.getInchiriere());
        refreshInchirieriComboBox(inchiriereComboBox);

        TextField sumaField = new TextField(String.valueOf(selectedPlata.getSuma()));

        ComboBox<String> metodaComboBox = new ComboBox<>();
        metodaComboBox.setItems(FXCollections.observableArrayList("CARD", "CASH", "TRANSFER"));
        metodaComboBox.setValue(selectedPlata.getMetodaPlata());

        DatePicker dataPlataPicker = new DatePicker(selectedPlata.getDataPlata());

        inchiriereComboBox.setOnAction(e -> {
            Inchiriere inchiriere = inchiriereComboBox.getValue();

            if (inchiriere != null) {
                sumaField.setText(String.valueOf(inchiriere.calculeazaTotal()));
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Inchiriere:"), 0, 0);
        form.add(inchiriereComboBox, 1, 0);
        form.add(new Label("Suma:"), 0, 1);
        form.add(sumaField, 1, 1);
        form.add(new Label("Metoda plata:"), 0, 2);
        form.add(metodaComboBox, 1, 2);
        form.add(new Label("Data plata:"), 0, 3);
        form.add(dataPlataPicker, 1, 3);

        Button salveazaButton = new Button("Salveaza");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Editeaza plata"), form, DialogHelper.creeazaButoaneDialog(salveazaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Editeaza plata", content);

        salveazaButton.setOnAction(e -> {
            try {
                Inchiriere inchiriere = inchiriereComboBox.getValue();
                String sumaText = sumaField.getText();
                String metoda = metodaComboBox.getValue();
                LocalDate dataPlata = dataPlataPicker.getValue();

                FormValidator.valideazaPlataForm(inchiriere, sumaText, metoda, dataPlata);

                double suma = FormValidator.parseazaSuma(sumaText);
                Plata plataActualizata = new Plata(selectedPlata.getId(), inchiriere, suma, metoda, dataPlata);

                backgroundRunner.run(() -> {
                    service.actualizeazaPlata(plataActualizata);
                    return service.obtinePlati();
                }, plati -> {
                    platiTable.setItems(FXCollections.observableArrayList(plati));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Plata actualizata cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void refreshPlatiTable() {
        incarcaPlati(() -> service.obtinePlati(), null);
    }

    private void refreshInchirieriComboBox(ComboBox<Inchiriere> inchiriereComboBox) {
        backgroundRunner.run(
                () -> service.obtineInchirieri(),
                inchirieri -> inchiriereComboBox.setItems(FXCollections.observableArrayList(inchirieri))
        );
    }

    private Stage owner() {
        return ownerSupplier.get();
    }

    private void incarcaPlati(Callable<List<Plata>> action, String successMessage) {
        arataSkeleton();

        backgroundRunner.run(action, plati -> {
            platiTable.setItems(FXCollections.observableArrayList(plati));
            arataTabel();

            if (successMessage != null && !successMessage.isBlank()) {
                DialogHelper.showInfo(owner(), successMessage);
            }
        }, error -> arataTabel());
    }

    private void arataSkeleton() {
        if (tableContainer != null) {
            tableContainer.getChildren().setAll(SkeletonFactory.createTableSkeleton(7, 6));
        }
    }

    private void arataTabel() {
        if (tableContainer != null) {
            tableContainer.getChildren().setAll(platiTable);
        }
    }
}
