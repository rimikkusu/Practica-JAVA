package com.bragari.views;

import java.util.List;
import java.util.function.Supplier;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.services.AutoInchiriereService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.FormValidator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

public class AutomobileView {

    private final AutoInchiriereService service;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;

    private TableView<Automobil> automobileTable;
    private boolean afiseazaDoarDisponibile = false;

    public AutomobileView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                          BackgroundRunner backgroundRunner) {
        this.service = service;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showAutomobilePage() {
        afiseazaDoarDisponibile = false;

        VBox page = new VBox(15);
        page.setPadding(new Insets(20));

        Label title = new Label("Gestionare Automobile");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

        automobileTable = new TableView<>();

        TableColumn<Automobil, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<Automobil, String> categorieColumn = new TableColumn<>("Categorie");
        categorieColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getCategorie().getDenumire())
        );

        TableColumn<Automobil, String> marcaColumn = new TableColumn<>("Marca");
        marcaColumn.setCellValueFactory(new PropertyValueFactory<>("marca"));

        TableColumn<Automobil, String> modelColumn = new TableColumn<>("Model");
        modelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));

        TableColumn<Automobil, String> numarColumn = new TableColumn<>("Nr. inmatriculare");
        numarColumn.setCellValueFactory(new PropertyValueFactory<>("numarInmatriculare"));

        TableColumn<Automobil, Double> pretColumn = new TableColumn<>("Pret/zi");
        pretColumn.setCellValueFactory(new PropertyValueFactory<>("pretPeZi"));

        TableColumn<Automobil, String> disponibilColumn = new TableColumn<>("Disponibil");
        disponibilColumn.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().isDisponibil() ? "Da" : "Nu")
        );

        automobileTable.getColumns().addAll(
                idColumn,
                categorieColumn,
                marcaColumn,
                modelColumn,
                numarColumn,
                pretColumn,
                disponibilColumn
        );

        automobileTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        refreshAutomobileTable();

        Button adaugaButton = new Button("Adauga");
        Button editeazaButton = new Button("Editeaza");
        Button stergeButton = new Button("Sterge");
        Button refreshButton = new Button("Refresh");
        Button disponibileButton = new Button("Doar disponibile");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(
                adaugaButton,
                editeazaButton,
                stergeButton,
                refreshButton,
                disponibileButton
        );

        adaugaButton.setOnAction(e -> showAddAutomobilDialog());

        editeazaButton.setOnAction(e -> {
            try {
                Automobil selectedAutomobil = automobileTable.getSelectionModel().getSelectedItem();

                if (selectedAutomobil == null) {
                    DialogHelper.showError(owner(), "Selecteaza un automobil pentru editare.");
                    return;
                }

                showEditAutomobilDialog(selectedAutomobil);
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        stergeButton.setOnAction(e -> {
            try {
                Automobil selectedAutomobil = automobileTable.getSelectionModel().getSelectedItem();

                if (selectedAutomobil == null) {
                    DialogHelper.showError(owner(), "Selecteaza un automobil pentru stergere.");
                    return;
                }

                if (!DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi acest automobil?")) {
                    return;
                }

                backgroundRunner.run(() -> {
                    service.stergeAutomobil(selectedAutomobil.getId());
                    return service.obtineAutomobile();
                }, automobile -> {
                    automobileTable.setItems(FXCollections.observableArrayList(automobile));
                    DialogHelper.showInfo(owner(), "Automobil sters cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        refreshButton.setOnAction(e -> {
            afiseazaDoarDisponibile = false;
            disponibileButton.setText("Doar disponibile");
            refreshAutomobileTable();
        });

        disponibileButton.setOnAction(e -> {
            afiseazaDoarDisponibile = !afiseazaDoarDisponibile;

            if (afiseazaDoarDisponibile) {
                backgroundRunner.run(
                        () -> service.obtineAutomobileDisponibile(),
                        automobile -> {
                            automobileTable.setItems(FXCollections.observableArrayList(automobile));
                            disponibileButton.setText("Afiseaza toate");
                        }
                );
            } else {
                refreshAutomobileTable();
                disponibileButton.setText("Doar disponibile");
            }
        });

        page.getChildren().addAll(title, buttons, automobileTable);

        root.setCenter(page);
    }

    private void showAddAutomobilDialog() {
        ComboBox<CategorieAuto> categorieComboBox = new ComboBox<>();
        categorieComboBox.setPromptText("Alege categoria");
        refreshCategoriiComboBox(categorieComboBox);

        TextField marcaField = new TextField();
        marcaField.setPromptText("Marca");

        TextField modelField = new TextField();
        modelField.setPromptText("Model");

        TextField numarField = new TextField();
        numarField.setPromptText("Numar inmatriculare");

        TextField pretField = new TextField();
        pretField.setPromptText("Pret pe zi");

        CheckBox disponibilCheckBox = new CheckBox("Disponibil");
        disponibilCheckBox.setSelected(true);

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Categorie:"), 0, 0);
        form.add(categorieComboBox, 1, 0);
        form.add(new Label("Marca:"), 0, 1);
        form.add(marcaField, 1, 1);
        form.add(new Label("Model:"), 0, 2);
        form.add(modelField, 1, 2);
        form.add(new Label("Nr. inmatriculare:"), 0, 3);
        form.add(numarField, 1, 3);
        form.add(new Label("Pret pe zi:"), 0, 4);
        form.add(pretField, 1, 4);
        form.add(disponibilCheckBox, 1, 5);

        Button adaugaButton = new Button("Adauga");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Adauga automobil"), form, DialogHelper.creeazaButoaneDialog(adaugaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Adauga automobil", content);

        adaugaButton.setOnAction(e -> {
            try {
                CategorieAuto categorie = categorieComboBox.getValue();
                String marca = marcaField.getText();
                String model = modelField.getText();
                String numar = numarField.getText();
                String pretText = pretField.getText();

                FormValidator.valideazaAutomobilForm(categorie, marca, model, numar, pretText);

                double pret = FormValidator.parseazaPret(pretText);
                Automobil automobil = new Automobil(0, categorie, marca, model, numar, pret, disponibilCheckBox.isSelected());

                backgroundRunner.run(() -> {
                    service.adaugaAutomobil(automobil);
                    return service.obtineAutomobile();
                }, automobile -> {
                    automobileTable.setItems(FXCollections.observableArrayList(automobile));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Automobil adaugat cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void showEditAutomobilDialog(Automobil selectedAutomobil) {
        ComboBox<CategorieAuto> categorieComboBox = new ComboBox<>();
        categorieComboBox.setPromptText("Alege categoria");
        categorieComboBox.setValue(selectedAutomobil.getCategorie());
        refreshCategoriiComboBox(categorieComboBox);

        TextField marcaField = new TextField(selectedAutomobil.getMarca());
        TextField modelField = new TextField(selectedAutomobil.getModel());
        TextField numarField = new TextField(selectedAutomobil.getNumarInmatriculare());
        TextField pretField = new TextField(String.valueOf(selectedAutomobil.getPretPeZi()));

        CheckBox disponibilCheckBox = new CheckBox("Disponibil");
        disponibilCheckBox.setSelected(selectedAutomobil.isDisponibil());

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.add(new Label("Categorie:"), 0, 0);
        form.add(categorieComboBox, 1, 0);
        form.add(new Label("Marca:"), 0, 1);
        form.add(marcaField, 1, 1);
        form.add(new Label("Model:"), 0, 2);
        form.add(modelField, 1, 2);
        form.add(new Label("Nr. inmatriculare:"), 0, 3);
        form.add(numarField, 1, 3);
        form.add(new Label("Pret pe zi:"), 0, 4);
        form.add(pretField, 1, 4);
        form.add(disponibilCheckBox, 1, 5);

        Button salveazaButton = new Button("Salveaza");
        Button anuleazaButton = new Button("Anuleaza");
        VBox content = new VBox(new Label("Editeaza automobil"), form, DialogHelper.creeazaButoaneDialog(salveazaButton, anuleazaButton));
        Stage dialog = DialogHelper.creeazaDialog(owner(), "Editeaza automobil", content);

        salveazaButton.setOnAction(e -> {
            try {
                CategorieAuto categorie = categorieComboBox.getValue();
                String marca = marcaField.getText();
                String model = modelField.getText();
                String numar = numarField.getText();
                String pretText = pretField.getText();

                FormValidator.valideazaAutomobilForm(categorie, marca, model, numar, pretText);

                double pret = FormValidator.parseazaPret(pretText);
                Automobil automobilActualizat = new Automobil(
                        selectedAutomobil.getId(),
                        categorie,
                        marca,
                        model,
                        numar,
                        pret,
                        disponibilCheckBox.isSelected()
                );

                backgroundRunner.run(() -> {
                    service.actualizeazaAutomobil(automobilActualizat);
                    return service.obtineAutomobile();
                }, automobile -> {
                    automobileTable.setItems(FXCollections.observableArrayList(automobile));
                    dialog.close();
                    DialogHelper.showInfo(owner(), "Automobil actualizat cu succes.");
                });
            } catch (Exception ex) {
                DialogHelper.showError(owner(), ex.getMessage());
            }
        });

        anuleazaButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private void refreshAutomobileTable() {
        backgroundRunner.run(
                () -> service.obtineAutomobile(),
                automobile -> automobileTable.setItems(FXCollections.observableArrayList(automobile))
        );
    }

    private void refreshCategoriiComboBox(ComboBox<CategorieAuto> categorieComboBox) {
        backgroundRunner.run(() -> {
            asiguraCategoriiImplicite();
            return service.obtineCategorii();
        }, categorii -> categorieComboBox.setItems(FXCollections.observableArrayList(categorii)));
    }

    private void asiguraCategoriiImplicite() {
        List<CategorieAuto> categorii = service.obtineCategorii();

        String[][] categoriiDefault = {
                {"SUV", "Automobile mari si confortabile"},
                {"Sedan", "Automobile potrivite pentru oras si drum lung"},
                {"Hatchback", "Automobile compacte si economice"},
                {"Electric", "Automobile electrice"},
                {"Luxury", "Automobile premium"}
        };

        for (String[] categorieData : categoriiDefault) {
            String denumire = categorieData[0];
            String descriere = categorieData[1];

            boolean exista = false;

            for (CategorieAuto categorie : categorii) {
                if (categorie.getDenumire().equalsIgnoreCase(denumire)) {
                    exista = true;
                    break;
                }
            }

            if (!exista) {
                try {
                    service.adaugaCategorie(new CategorieAuto(0, denumire, descriere));
                } catch (Exception e) {
                    // ignoram daca exista deja
                }
            }
        }
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
