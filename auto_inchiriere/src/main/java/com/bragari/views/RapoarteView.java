package com.bragari.views;

// RapoarteView afiseaza rapoarte si permite exportul datelor.
// Practic, transforma datele din aplicatie in informatii mai usor de citit.

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
import javafx.scene.layout.Priority;
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
        pageContent.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        raportTextArea = new TextArea();
        raportTextArea.setEditable(false);
        raportTextArea.setWrapText(false);
        raportTextArea.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        raportTextArea.getStyleClass().add("report-area");

        reportContainer = new StackPane();
        reportContainer.getStyleClass().add("report-content-area");
        reportContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        VBox.setVgrow(reportContainer, Priority.ALWAYS);
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
        VBox.setVgrow(contentCard, Priority.ALWAYS);
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

    private static String linie(String label, Object valoare) {
        return label + ": " + valoare + System.lineSeparator();
    }

    private String genereazaRaportClienti() {
        List<Client> clienti = service.obtineClienti();
        StringBuilder raport = new StringBuilder();
        raport.append("RAPORT CLIENTI").append(System.lineSeparator());
        raport.append("====================").append(System.lineSeparator());
        raport.append(linie("Total clienti", clienti.size()));
        raport.append(System.lineSeparator());

        if (clienti.isEmpty()) {
            raport.append("Nu exista clienti in baza de date.").append(System.lineSeparator());
            return raport.toString();
        }

        for (Client client : clienti) {
            raport.append(linie("ID", client.getId()));
            raport.append(linie("Nume", client.getNume()));
            raport.append(linie("Telefon", client.getTelefon()));
            raport.append(linie("Email", client.getEmail()));
            raport.append("--------------------").append(System.lineSeparator());
        }
        return raport.toString();
    }

    private String genereazaRaportAutomobile() {
        List<Automobil> automobile = service.obtineAutomobile();
        long disponibile = automobile.stream().filter(Automobil::isDisponibil).count();
        StringBuilder raport = new StringBuilder();
        raport.append("RAPORT AUTOMOBILE").append(System.lineSeparator());
        raport.append("====================").append(System.lineSeparator());
        raport.append(linie("Total automobile", automobile.size()));
        raport.append(linie("Disponibile", disponibile));
        raport.append(linie("Indisponibile", automobile.size() - disponibile));
        raport.append(System.lineSeparator());

        if (automobile.isEmpty()) {
            raport.append("Nu exista automobile in baza de date.").append(System.lineSeparator());
            return raport.toString();
        }

        for (Automobil automobil : automobile) {
            String categorie = automobil.getCategorie() == null ? "-" : automobil.getCategorie().getDenumire();
            raport.append(linie("ID", automobil.getId()));
            raport.append(linie("Categorie", categorie));
            raport.append(linie("Marca", automobil.getMarca()));
            raport.append(linie("Model", automobil.getModel()));
            raport.append(linie("Numar inmatriculare", automobil.getNumarInmatriculare()));
            raport.append(linie("Pret pe zi", automobil.getPretPeZi() + " lei"));
            raport.append(linie("Status disponibilitate", automobil.isDisponibil() ? "Disponibil" : "Indisponibil"));
            raport.append("--------------------").append(System.lineSeparator());
        }
        return raport.toString();
    }

    private String genereazaRaportInchirieri() {
        List<Inchiriere> inchirieri = service.obtineInchirieri();
        List<Plata> plati = service.obtinePlati();
        double totalIncasari = plati.stream().mapToDouble(Plata::getSuma).sum();
        StringBuilder raport = new StringBuilder();

        raport.append("RAPORT INCHIRIERI SI PLATI").append(System.lineSeparator());
        raport.append("====================").append(System.lineSeparator());
        raport.append(linie("Total inchirieri", inchirieri.size()));
        raport.append(linie("Total plati", plati.size()));
        raport.append(linie("Total incasari", totalIncasari + " lei"));
        raport.append(System.lineSeparator());

        raport.append("Statusuri inchirieri:").append(System.lineSeparator());
        for (StatusInchiriere status : StatusInchiriere.values()) {
            long numar = inchirieri.stream().filter(i -> i.getStatus() == status).count();
            raport.append(status).append(": ").append(numar).append(System.lineSeparator());
        }
        raport.append(System.lineSeparator());

        raport.append("Inchirieri:").append(System.lineSeparator());
        if (inchirieri.isEmpty()) {
            raport.append("Nu exista inchirieri in baza de date.").append(System.lineSeparator());
        } else {
            for (Inchiriere i : inchirieri) {
                raport.append(linie("ID", i.getId()));
                raport.append(linie("Client", i.getClient().getNume()));
                raport.append(linie("Automobil", i.getAutomobil().getMarca() + " " + i.getAutomobil().getModel()));
                raport.append(linie("Perioada", i.getDataInceput() + " - " + i.getDataSfarsit()));
                raport.append(linie("Total calculat", i.calculeazaTotal() + " lei"));
                raport.append(linie("Status", i.getStatus()));
                raport.append("--------------------").append(System.lineSeparator());
            }
        }

        raport.append(System.lineSeparator());
        raport.append("Plati:").append(System.lineSeparator());
        if (plati.isEmpty()) {
            raport.append("Nu exista plati in baza de date.").append(System.lineSeparator());
        } else {
            for (Plata p : plati) {
                raport.append(linie("ID plata", p.getId()));
                raport.append(linie("ID inchiriere", p.getInchiriere().getId()));
                raport.append(linie("Client", p.getInchiriere().getClient().getNume()));
                raport.append(linie("Suma", p.getSuma() + " lei"));
                raport.append(linie("Metoda", p.getMetodaPlata()));
                raport.append(linie("Data plata", p.getDataPlata()));
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
