package com.bragari;

import java.util.List;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.StatusInchiriere;
import com.bragari.services.AutoInchiriereService;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainApp extends Application {

    private BorderPane root;
    private AutoInchiriereService service;

    private TableView<Client> clientiTable;
    private TableView<Automobil> automobileTable;
    private TableView<Inchiriere> inchirieriTable;

    private boolean afiseazaDoarDisponibile = false;

    @Override
    public void start(Stage stage) {
        service = new AutoInchiriereService();

        root = new BorderPane();

        root.setLeft(createMenu());
        showWelcomePage();

        Scene scene = new Scene(root, 1000, 600);

        stage.setTitle("Auto Inchiriere");
        stage.setScene(scene);
        stage.show();
    }

    private VBox createMenu() {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: #eeeeee;");
        menu.setPrefWidth(200);

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

        clientiButton.setOnAction(e -> showClientiPage());
        automobileButton.setOnAction(e -> showAutomobilePage());
        inchirieriButton.setOnAction(e -> showInchirieriPage());
        platiButton.setOnAction(e -> showSimplePage("Pagina Plati"));
        rapoarteButton.setOnAction(e -> showSimplePage("Pagina Rapoarte"));

        menu.getChildren().addAll(
                clientiButton,
                automobileButton,
                inchirieriButton,
                platiButton,
                rapoarteButton
        );

        return menu;
    }

    private void showWelcomePage() {
        Label label = new Label("Bine ai venit in aplicatia de inchiriere automobile");
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        BorderPane.setMargin(label, new Insets(30));
        root.setCenter(label);
    }

    private void showSimplePage(String title) {
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");
        BorderPane.setMargin(label, new Insets(30));
        root.setCenter(label);
    }

    private void showClientiPage() {
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

        TextField numeField = new TextField();
        numeField.setPromptText("Nume");

        TextField telefonField = new TextField();
        telefonField.setPromptText("Telefon");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");

        TextField cautareField = new TextField();
        cautareField.setPromptText("Cauta dupa nume");

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
        Button editeazaButton = new Button("Editeaza");
        Button stergeButton = new Button("Sterge");
        Button cautaButton = new Button("Cauta");
        Button refreshButton = new Button("Refresh");

        HBox buttons = new HBox(10);
        buttons.getChildren().addAll(adaugaButton, editeazaButton, stergeButton, cautareField, cautaButton, refreshButton);

        clientiTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedClient) -> {
            if (selectedClient != null) {
                numeField.setText(selectedClient.getNume());
                telefonField.setText(selectedClient.getTelefon());
                emailField.setText(selectedClient.getEmail());
            }
        });

        adaugaButton.setOnAction(e -> {
            try {
                Client client = new Client(
                        0,
                        numeField.getText(),
                        telefonField.getText(),
                        emailField.getText()
                );

                service.adaugaClient(client);
                refreshClientiTable();

                numeField.clear();
                telefonField.clear();
                emailField.clear();

                showInfo("Client adaugat cu succes.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        editeazaButton.setOnAction(e -> {
            try {
                Client selectedClient = clientiTable.getSelectionModel().getSelectedItem();

                if (selectedClient == null) {
                    showError("Selecteaza un client pentru editare.");
                    return;
                }

                selectedClient.setNume(numeField.getText());
                selectedClient.setTelefon(telefonField.getText());
                selectedClient.setEmail(emailField.getText());

                service.actualizeazaClient(selectedClient);
                refreshClientiTable();

                showInfo("Client actualizat cu succes.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        stergeButton.setOnAction(e -> {
            try {
                Client selectedClient = clientiTable.getSelectionModel().getSelectedItem();

                if (selectedClient == null) {
                    showError("Selecteaza un client pentru stergere.");
                    return;
                }

                service.stergeClient(selectedClient.getId());
                refreshClientiTable();

                numeField.clear();
                telefonField.clear();
                emailField.clear();

                showInfo("Client sters cu succes.");
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        cautaButton.setOnAction(e -> {
            String text = cautareField.getText();

            clientiTable.setItems(
                    FXCollections.observableArrayList(
                            service.cautaClientiDupaNume(text)
                    )
            );
        });

        refreshButton.setOnAction(e -> {
            refreshClientiTable();
            cautareField.clear();
        });

        page.getChildren().addAll(title, form, buttons, clientiTable);

        root.setCenter(page);
    }

    private void refreshClientiTable() {
        clientiTable.setItems(
                FXCollections.observableArrayList(
                        service.obtineClienti()
                )
        );
    }

    private void showAutomobilePage() {
        asiguraCategoriiImplicite();
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

    ComboBox<CategorieAuto> categorieComboBox = new ComboBox<>();
    categorieComboBox.setPromptText("Alege categoria");
    categorieComboBox.setItems(
            FXCollections.observableArrayList(service.obtineCategorii())
    );

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

    automobileTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, selectedAutomobil) -> {
        if (selectedAutomobil != null) {
            categorieComboBox.setValue(selectedAutomobil.getCategorie());
            marcaField.setText(selectedAutomobil.getMarca());
            modelField.setText(selectedAutomobil.getModel());
            numarField.setText(selectedAutomobil.getNumarInmatriculare());
            pretField.setText(String.valueOf(selectedAutomobil.getPretPeZi()));
            disponibilCheckBox.setSelected(selectedAutomobil.isDisponibil());
        }
    });

    adaugaButton.setOnAction(e -> {
        try {
            CategorieAuto categorie = categorieComboBox.getValue();

            if (categorie == null) {
                showError("Selecteaza o categorie.");
                return;
            }

            double pret = Double.parseDouble(pretField.getText());

            Automobil automobil = new Automobil(
                    0,
                    categorie,
                    marcaField.getText(),
                    modelField.getText(),
                    numarField.getText(),
                    pret,
                    disponibilCheckBox.isSelected()
            );

            service.adaugaAutomobil(automobil);
            refreshAutomobileTable();

            marcaField.clear();
            modelField.clear();
            numarField.clear();
            pretField.clear();
            disponibilCheckBox.setSelected(true);

            showInfo("Automobil adaugat cu succes.");
        } catch (NumberFormatException ex) {
            showError("Pretul trebuie sa fie un numar.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    });

    editeazaButton.setOnAction(e -> {
        try {
            Automobil selectedAutomobil = automobileTable.getSelectionModel().getSelectedItem();

            if (selectedAutomobil == null) {
                showError("Selecteaza un automobil pentru editare.");
                return;
            }

            CategorieAuto categorie = categorieComboBox.getValue();

            if (categorie == null) {
                showError("Selecteaza o categorie.");
                return;
            }

            double pret = Double.parseDouble(pretField.getText());

            selectedAutomobil.setCategorie(categorie);
            selectedAutomobil.setMarca(marcaField.getText());
            selectedAutomobil.setModel(modelField.getText());
            selectedAutomobil.setNumarInmatriculare(numarField.getText());
            selectedAutomobil.setPretPeZi(pret);
            selectedAutomobil.setDisponibil(disponibilCheckBox.isSelected());

            service.actualizeazaAutomobil(selectedAutomobil);
            refreshAutomobileTable();

            showInfo("Automobil actualizat cu succes.");
        } catch (NumberFormatException ex) {
            showError("Pretul trebuie sa fie un numar.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    });

    stergeButton.setOnAction(e -> {
        try {
            Automobil selectedAutomobil = automobileTable.getSelectionModel().getSelectedItem();

            if (selectedAutomobil == null) {
                showError("Selecteaza un automobil pentru stergere.");
                return;
            }

            service.stergeAutomobil(selectedAutomobil.getId());
            refreshAutomobileTable();

            marcaField.clear();
            modelField.clear();
            numarField.clear();
            pretField.clear();
            disponibilCheckBox.setSelected(true);

            showInfo("Automobil sters cu succes.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    });

    refreshButton.setOnAction(e -> {
    afiseazaDoarDisponibile = false;
    disponibileButton.setText("Doar disponibile");

    refreshAutomobileTable();

    categorieComboBox.setItems(
            FXCollections.observableArrayList(service.obtineCategorii())
    );

    });

    disponibileButton.setOnAction(e -> {
    afiseazaDoarDisponibile = !afiseazaDoarDisponibile;

    if (afiseazaDoarDisponibile) {
        automobileTable.setItems(
                FXCollections.observableArrayList(
                        service.obtineAutomobileDisponibile()
                )
        );

        disponibileButton.setText("Afiseaza toate");
    } else {
        refreshAutomobileTable();
        disponibileButton.setText("Doar disponibile");
    }

});

    page.getChildren().addAll(title, form, buttons, automobileTable);

    root.setCenter(page);
}

private void refreshAutomobileTable() {
    automobileTable.setItems(
            FXCollections.observableArrayList(
                    service.obtineAutomobile()
            )
    );
}

private void refreshInchirieriTable() {
    inchirieriTable.setItems(
            FXCollections.observableArrayList(
                    service.obtineInchirieri()
            )
    );
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

    private void showInchirieriPage() {
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

    ComboBox<Client> clientComboBox = new ComboBox<>();
    clientComboBox.setPromptText("Alege client");
    clientComboBox.setItems(FXCollections.observableArrayList(service.obtineClienti()));

    ComboBox<Automobil> automobilComboBox = new ComboBox<>();
    automobilComboBox.setPromptText("Alege automobil disponibil");
    automobilComboBox.setItems(FXCollections.observableArrayList(service.obtineAutomobileDisponibile()));

    DatePicker dataInceputPicker = new DatePicker();
    dataInceputPicker.setPromptText("Data inceput");

    DatePicker dataSfarsitPicker = new DatePicker();
    dataSfarsitPicker.setPromptText("Data sfarsit");

    ComboBox<StatusInchiriere> statusComboBox = new ComboBox<>();
    statusComboBox.setPromptText("Status");
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
    Button editeazaStatusButton = new Button("Editeaza status");
    Button refreshButton = new Button("Refresh");

    HBox buttons = new HBox(10);
    buttons.getChildren().addAll(adaugaButton, editeazaStatusButton, refreshButton);

    adaugaButton.setOnAction(e -> {
        try {
            Client client = clientComboBox.getValue();
            Automobil automobil = automobilComboBox.getValue();
            StatusInchiriere status = statusComboBox.getValue();

            if (client == null) {
                showError("Selecteaza un client.");
                return;
            }

            if (automobil == null) {
                showError("Selecteaza un automobil disponibil.");
                return;
            }

            Inchiriere inchiriere = new Inchiriere(
                    0,
                    client,
                    automobil,
                    dataInceputPicker.getValue(),
                    dataSfarsitPicker.getValue(),
                    status
            );

            service.adaugaInchiriere(inchiriere);

            refreshInchirieriTable();

            automobilComboBox.setItems(
                    FXCollections.observableArrayList(service.obtineAutomobileDisponibile())
            );

            automobilComboBox.setValue(null);
            dataInceputPicker.setValue(null);
            dataSfarsitPicker.setValue(null);
            statusComboBox.setValue(StatusInchiriere.ACTIVA);

            showInfo("Inchiriere adaugata cu succes.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    });

    refreshButton.setOnAction(e -> {
        refreshInchirieriTable();

        clientComboBox.setItems(FXCollections.observableArrayList(service.obtineClienti()));
        automobilComboBox.setItems(FXCollections.observableArrayList(service.obtineAutomobileDisponibile()));
    });

    page.getChildren().addAll(title, form, buttons, inchirieriTable);

    root.setCenter(page);
}

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}