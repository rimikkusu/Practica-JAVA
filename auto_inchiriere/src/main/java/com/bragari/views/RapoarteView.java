package com.bragari.views;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Supplier;

import com.bragari.models.Automobil;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.models.StatusInchiriere;
import com.bragari.services.AutoInchiriereService;
import com.bragari.services.SettingsService;
import com.bragari.util.BackgroundRunner;
import com.bragari.util.CsvExporter;
import com.bragari.util.DialogHelper;
import com.bragari.util.SkeletonFactory;
import com.bragari.util.ViewFactory;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RapoarteView {

    private final AutoInchiriereService service;
    private final SettingsService settingsService;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final BackgroundRunner backgroundRunner;
    private StackPane reportContainer;
    private TextArea raportTextArea;

    public RapoarteView(AutoInchiriereService service, BorderPane root, Supplier<Stage> ownerSupplier,
                        BackgroundRunner backgroundRunner) {
        this(service, new SettingsService(), root, ownerSupplier, backgroundRunner);
    }

    public RapoarteView(AutoInchiriereService service, SettingsService settingsService, BorderPane root,
                        Supplier<Stage> ownerSupplier, BackgroundRunner backgroundRunner) {
        this.service = service;
        this.settingsService = settingsService;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.backgroundRunner = backgroundRunner;
    }

    public void showRapoartePage() {
        VBox pageContent = new VBox(16);
        pageContent.getStyleClass().add("page-content");

        raportTextArea = new TextArea();
        raportTextArea.setEditable(false);
        raportTextArea.setWrapText(false);
        raportTextArea.setPrefRowCount(25);
        raportTextArea.getStyleClass().add("report-area");

        reportContainer = new StackPane();
        reportContainer.getStyleClass().add("report-content-area");
        reportContainer.getChildren().add(raportTextArea);

        Button raportClientiButton = new Button("Raport clienti");
        raportClientiButton.getStyleClass().add("primary-button");
        Button raportAutomobileButton = new Button("Raport automobile");
        raportAutomobileButton.getStyleClass().add("secondary-button");
        Button raportInchirieriButton = new Button("Raport inchirieri");
        raportInchirieriButton.getStyleClass().add("secondary-button");
        Button exportTxtButton = new Button("Export TXT");
        exportTxtButton.getStyleClass().add("secondary-button");
        Button exportCsvButton = new Button("Export CSV");
        exportCsvButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("page-toolbar");
        buttons.getChildren().addAll(
                raportClientiButton,
                raportAutomobileButton,
                raportInchirieriButton,
                exportTxtButton,
                exportCsvButton
        );

        VBox contentCard = new VBox(14);
        ViewFactory.asCard(contentCard);
        contentCard.getChildren().addAll(buttons, reportContainer);

        final String[] tipRaportCurent = {""};

        raportClientiButton.setOnAction(e -> {
            arataSkeletonRaport();
            backgroundRunner.run(() -> genereazaRaport("clienti"), raport -> {
                raportTextArea.setText(raport);
                arataRaport();
                tipRaportCurent[0] = "clienti";
            }, error -> arataRaport());
        });

        raportAutomobileButton.setOnAction(e -> {
            arataSkeletonRaport();
            backgroundRunner.run(() -> genereazaRaport("automobile"), raport -> {
                raportTextArea.setText(raport);
                arataRaport();
                tipRaportCurent[0] = "automobile";
            }, error -> arataRaport());
        });

        raportInchirieriButton.setOnAction(e -> {
            arataSkeletonRaport();
            backgroundRunner.run(() -> genereazaRaport("inchirieri"), raport -> {
                raportTextArea.setText(raport);
                arataRaport();
                tipRaportCurent[0] = "inchirieri";
            }, error -> arataRaport());
        });

        exportTxtButton.setOnAction(e -> exportRaport(raportTextArea, tipRaportCurent[0], false));
        exportCsvButton.setOnAction(e -> exportRaport(raportTextArea, tipRaportCurent[0], true));

        pageContent.getChildren().add(contentCard);

        root.setCenter(ViewFactory.createPage("Rapoarte", "R", pageContent));
    }

    private String genereazaRaport(String tipRaport) {
        if ("clienti".equals(tipRaport)) {
            return genereazaRaportClienti();
        }

        if ("automobile".equals(tipRaport)) {
            return genereazaRaportAutomobile();
        }

        if ("inchirieri".equals(tipRaport)) {
            return genereazaRaportInchirieri();
        }

        return "";
    }

    private String genereazaRaportClienti() {
        List<Client> clienti = service.obtineClienti();
        StringBuilder raport = new StringBuilder();

        raport.append("RAPORT CLIENTI").append(System.lineSeparator());
        raport.append("====================").append(System.lineSeparator());
        raport.append("Total clienti: ").append(clienti.size()).append(System.lineSeparator());
        raport.append(System.lineSeparator());

        if (clienti.isEmpty()) {
            raport.append("Nu exista clienti in baza de date.").append(System.lineSeparator());
            return raport.toString();
        }

        for (Client client : clienti) {
            raport.append("ID: ").append(client.getId()).append(System.lineSeparator());
            raport.append("Nume: ").append(client.getNume()).append(System.lineSeparator());
            raport.append("Telefon: ").append(client.getTelefon()).append(System.lineSeparator());
            raport.append("Email: ").append(client.getEmail()).append(System.lineSeparator());
            raport.append("--------------------").append(System.lineSeparator());
        }

        return raport.toString();
    }

    private String genereazaRaportAutomobile() {
        List<Automobil> automobile = service.obtineAutomobile();
        StringBuilder raport = new StringBuilder();

        int disponibile = 0;

        for (Automobil automobil : automobile) {
            if (automobil.isDisponibil()) {
                disponibile++;
            }
        }

        raport.append("RAPORT AUTOMOBILE").append(System.lineSeparator());
        raport.append("====================").append(System.lineSeparator());
        raport.append("Total automobile: ").append(automobile.size()).append(System.lineSeparator());
        raport.append("Disponibile: ").append(disponibile).append(System.lineSeparator());
        raport.append("Indisponibile: ").append(automobile.size() - disponibile).append(System.lineSeparator());
        raport.append(System.lineSeparator());

        if (automobile.isEmpty()) {
            raport.append("Nu exista automobile in baza de date.").append(System.lineSeparator());
            return raport.toString();
        }

        for (Automobil automobil : automobile) {
            String categorie = automobil.getCategorie() == null ? "-" : automobil.getCategorie().getDenumire();

            raport.append("ID: ").append(automobil.getId()).append(System.lineSeparator());
            raport.append("Categorie: ").append(categorie).append(System.lineSeparator());
            raport.append("Marca: ").append(automobil.getMarca()).append(System.lineSeparator());
            raport.append("Model: ").append(automobil.getModel()).append(System.lineSeparator());
            raport.append("Numar inmatriculare: ").append(automobil.getNumarInmatriculare()).append(System.lineSeparator());
            raport.append("Pret pe zi: ").append(automobil.getPretPeZi()).append(" lei").append(System.lineSeparator());
            raport.append("Status disponibilitate: ")
                    .append(automobil.isDisponibil() ? "Disponibil" : "Indisponibil")
                    .append(System.lineSeparator());
            raport.append("--------------------").append(System.lineSeparator());
        }

        return raport.toString();
    }

    private String genereazaRaportInchirieri() {
        List<Inchiriere> inchirieri = service.obtineInchirieri();
        List<Plata> plati = service.obtinePlati();
        StringBuilder raport = new StringBuilder();

        double totalIncasari = 0;

        for (Plata plata : plati) {
            totalIncasari += plata.getSuma();
        }

        raport.append("RAPORT INCHIRIERI SI PLATI").append(System.lineSeparator());
        raport.append("====================").append(System.lineSeparator());
        raport.append("Total inchirieri: ").append(inchirieri.size()).append(System.lineSeparator());
        raport.append("Total plati: ").append(plati.size()).append(System.lineSeparator());
        raport.append("Total incasari: ").append(totalIncasari).append(" lei").append(System.lineSeparator());
        raport.append(System.lineSeparator());

        raport.append("Statusuri inchirieri:").append(System.lineSeparator());
        for (StatusInchiriere status : StatusInchiriere.values()) {
            int numar = 0;

            for (Inchiriere inchiriere : inchirieri) {
                if (inchiriere.getStatus() == status) {
                    numar++;
                }
            }

            raport.append(status).append(": ").append(numar).append(System.lineSeparator());
        }

        raport.append(System.lineSeparator());
        raport.append("Inchirieri:").append(System.lineSeparator());

        if (inchirieri.isEmpty()) {
            raport.append("Nu exista inchirieri in baza de date.").append(System.lineSeparator());
        } else {
            for (Inchiriere inchiriere : inchirieri) {
                raport.append("ID: ").append(inchiriere.getId()).append(System.lineSeparator());
                raport.append("Client: ").append(inchiriere.getClient().getNume()).append(System.lineSeparator());
                raport.append("Automobil: ")
                        .append(inchiriere.getAutomobil().getMarca())
                        .append(" ")
                        .append(inchiriere.getAutomobil().getModel())
                        .append(System.lineSeparator());
                raport.append("Perioada: ")
                        .append(inchiriere.getDataInceput())
                        .append(" - ")
                        .append(inchiriere.getDataSfarsit())
                        .append(System.lineSeparator());
                raport.append("Total calculat: ").append(inchiriere.calculeazaTotal()).append(" lei").append(System.lineSeparator());
                raport.append("Status: ").append(inchiriere.getStatus()).append(System.lineSeparator());
                raport.append("--------------------").append(System.lineSeparator());
            }
        }

        raport.append(System.lineSeparator());
        raport.append("Plati:").append(System.lineSeparator());

        if (plati.isEmpty()) {
            raport.append("Nu exista plati in baza de date.").append(System.lineSeparator());
        } else {
            for (Plata plata : plati) {
                raport.append("ID plata: ").append(plata.getId()).append(System.lineSeparator());
                raport.append("ID inchiriere: ").append(plata.getInchiriere().getId()).append(System.lineSeparator());
                raport.append("Client: ").append(plata.getInchiriere().getClient().getNume()).append(System.lineSeparator());
                raport.append("Suma: ").append(plata.getSuma()).append(" lei").append(System.lineSeparator());
                raport.append("Metoda: ").append(plata.getMetodaPlata()).append(System.lineSeparator());
                raport.append("Data plata: ").append(plata.getDataPlata()).append(System.lineSeparator());
                raport.append("--------------------").append(System.lineSeparator());
            }
        }

        return raport.toString();
    }

    private void exportRaport(TextArea raportTextArea, String tipRaport, boolean csv) {
        String raport = raportTextArea.getText();

        if (raport == null || raport.isBlank() || tipRaport == null || tipRaport.isBlank()) {
            DialogHelper.showError(owner(), "Genereaza mai intai un raport.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(csv ? "Export CSV" : "Export TXT");
        fileChooser.setInitialFileName("raport_" + tipRaport + (csv ? ".csv" : ".txt"));

        if (csv) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fisiere CSV", "*.csv"));
        } else {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fisiere TXT", "*.txt"));
        }

        File file = fileChooser.showSaveDialog(owner());

        if (file == null) {
            return;
        }

        String extensie = csv ? ".csv" : ".txt";
        if (!file.getName().toLowerCase().endsWith(extensie)) {
            file = new File(file.getParentFile(), file.getName() + extensie);
        }

        File exportFile = file;

        backgroundRunner.run(() -> {
            try {
                String continut = csv ? CsvExporter.genereazaCsvRaport(service, tipRaport) : raport;
                Files.writeString(exportFile.toPath(), continut, StandardCharsets.UTF_8);
            } catch (Exception ex) {
                throw new RuntimeException("Eroare la export: " + ex.getMessage(), ex);
            }

            return null;
        }, ignored -> DialogHelper.showInfo(owner(), "Raportul a fost exportat cu succes."));
    }

    private Stage owner() {
        return ownerSupplier.get();
    }

    private void arataSkeletonRaport() {
        if (reportContainer != null) {
            reportContainer.getChildren().setAll(
                    settingsService.folosesteSkeletonLoading()
                            ? SkeletonFactory.createReportSkeleton()
                            : SkeletonFactory.createSimpleLoading("Se genereaza raportul...")
            );
        }
    }

    private void arataRaport() {
        if (reportContainer != null) {
            reportContainer.getChildren().setAll(raportTextArea);
        }
    }
}
