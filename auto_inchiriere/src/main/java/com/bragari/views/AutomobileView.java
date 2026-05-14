package com.bragari.views;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Supplier;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.services.AutoInchiriereService;
import com.bragari.services.SettingsService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.DialogHelper;
import com.bragari.util.FormValidator;
import com.bragari.util.SkeletonFactory;
import com.bragari.util.ViewFactory;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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

public class AutomobileView {

    private final AutoInchiriereService service;
    private final SettingsService settingsService;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;

    private TableView<Automobil> automobileTable;
    private StackPane tableContainer;
    private boolean afiseazaDoarDisponibile = false;

    public AutomobileView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                          BackgroundRunner backgroundRunner) {
        this(service, new SettingsService(), root, ownerSupplier, backgroundRunner);
    }

    public AutomobileView(AutoInchiriereService service, SettingsService settingsService, BorderPane root,
                          Supplier<Stage> ownerSupplier, BackgroundRunner backgroundRunner) {
        this.service = service;
        this.settingsService = settingsService;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showAutomobilePage() {
        afiseazaDoarDisponibile = false;

        VBox pageContent = new VBox(16);
        pageContent.getStyleClass().addAll("page-content", "table-page-background");
        pageContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        automobileTable = new TableView<>();
        automobileTable.getStyleClass().add("app-table");

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
        disponibilColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    return;
                }

                Label badge = new Label("Da".equals(item) ? "Disponibil" : "Indisponibil");
                badge.getStyleClass().addAll(
                        "status-badge",
                        "badge",
                        "Da".equals(item) ? "status-success" : "status-danger",
                        "Da".equals(item) ? "badge-disponibil" : "badge-indisponibil"
                );
                setGraphic(badge);
                setText(null);
            }
        });

        automobileTable.getColumns().addAll(
                idColumn,
                categorieColumn,
                marcaColumn,
                modelColumn,
                numarColumn,
                pretColumn,
                disponibilColumn
        );

        ViewFactory.styleTable(automobileTable);

        Button adaugaButton = new Button("Adauga");
        adaugaButton.getStyleClass().add("primary-button");
        Button editeazaButton = new Button("Editeaza");
        editeazaButton.getStyleClass().add("secondary-button");
        Button stergeButton = new Button("Sterge");
        stergeButton.getStyleClass().add("danger-button");
        Button refreshButton = new Button("Refresh");
        refreshButton.getStyleClass().add("secondary-button");
        Button disponibileButton = new Button("Doar disponibile");
        disponibileButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("page-toolbar");
        buttons.getChildren().addAll(
                adaugaButton,
                editeazaButton,
                stergeButton,
                refreshButton,
                disponibileButton
        );

        tableContainer = new StackPane();
        tableContainer.getStyleClass().add("table-content-area");
        tableContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(tableContainer, Priority.ALWAYS);

        VBox contentCard = new VBox(14);
        ViewFactory.asCard(contentCard);
        VBox.setVgrow(contentCard, Priority.ALWAYS);
        contentCard.getChildren().addAll(buttons, tableContainer);

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

                if (settingsService.isConfirmareStergereActiva()
                        && !DialogHelper.confirmaActiune(owner(), "Sigur vrei sa stergi acest automobil?")) {
                    return;
                }

                incarcaAutomobile(() -> {
                    service.stergeAutomobil(selectedAutomobil.getId());
                    return service.obtineAutomobile();
                }, "Automobil sters cu succes.");
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
                incarcaAutomobile(() -> service.obtineAutomobileDisponibile(), null, () -> {
                    disponibileButton.setText("Afiseaza toate");
                });
            } else {
                refreshAutomobileTable();
                disponibileButton.setText("Doar disponibile");
            }
        });

        pageContent.getChildren().add(contentCard);

        root.setCenter(ViewFactory.createPage("Automobile", "A", pageContent));
        refreshAutomobileTable();
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
        incarcaAutomobile(() -> service.obtineAutomobile(), null);
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

    private void incarcaAutomobile(Callable<List<Automobil>> action, String successMessage) {
        incarcaAutomobile(action, successMessage, null);
    }

    private void incarcaAutomobile(Callable<List<Automobil>> action, String successMessage, Runnable successUiAction) {
        arataSkeleton();

        backgroundRunner.run(action, automobile -> {
            automobileTable.setItems(FXCollections.observableArrayList(automobile));
            arataTabel();

            if (successUiAction != null) {
                successUiAction.run();
            }

            if (successMessage != null && !successMessage.isBlank()) {
                DialogHelper.showInfo(owner(), successMessage);
            }
        }, error -> arataTabel());
    }

    private void arataSkeleton() {
        if (tableContainer != null) {
            tableContainer.getChildren().setAll(
                    settingsService.folosesteSkeletonLoading()
                            ? SkeletonFactory.createTableSkeleton(7, 6)
                            : SkeletonFactory.createSimpleLoading("Se incarca automobilele...")
            );
        }
    }

    private void arataTabel() {
        if (tableContainer != null) {
            tableContainer.getChildren().setAll(automobileTable);
        }
    }
}
