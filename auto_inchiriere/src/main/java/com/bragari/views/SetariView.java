package com.bragari.views;

// SetariView este pagina unde se schimba tema, marimea textului si alte optiuni.
// Dupa salvare, aplicatia aplica setarile pe interfata.

import java.util.function.Supplier;

import com.bragari.services.SettingsService;
import com.bragari.util.DialogHelper;
import com.bragari.util.ViewFactory;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SetariView {

    private final SettingsService settingsService;
    private final BorderPane root;
    private final Supplier<Stage> ownerSupplier;
    private final Runnable onSettingsChanged;

    public SetariView(SettingsService settingsService, BorderPane root, Supplier<Stage> ownerSupplier,
                      Runnable onSettingsChanged) {
        this.settingsService = settingsService;
        this.root = root;
        this.ownerSupplier = ownerSupplier;
        this.onSettingsChanged = onSettingsChanged;
    }

    public void showSetariPage() {
        VBox pageContent = new VBox(16);
        pageContent.getStyleClass().add("page-content");

        ComboBox<String> temaComboBox = new ComboBox<>();
        temaComboBox.setItems(FXCollections.observableArrayList(
                SettingsService.TEMA_LIGHT,
                SettingsService.TEMA_DARK,
                SettingsService.TEMA_AMBER
        ));
        temaComboBox.setValue(settingsService.getTema());

        ComboBox<String> textComboBox = new ComboBox<>();
        textComboBox.setItems(FXCollections.observableArrayList(
                SettingsService.TEXT_MIC,
                SettingsService.TEXT_NORMAL,
                SettingsService.TEXT_MARE
        ));
        textComboBox.setValue(settingsService.getDimensiuneText());

        ComboBox<String> densitateComboBox = new ComboBox<>();
        densitateComboBox.setItems(FXCollections.observableArrayList(
                SettingsService.DENSITATE_COMPACT,
                SettingsService.DENSITATE_NORMAL
        ));
        densitateComboBox.setValue(settingsService.getDensitateTabele());

        CheckBox confirmareStergereCheckBox = new CheckBox("Cere confirmare inainte de stergere");
        confirmareStergereCheckBox.setSelected(settingsService.isConfirmareStergereActiva());

        ComboBox<String> loadingComboBox = new ComboBox<>();
        loadingComboBox.setItems(FXCollections.observableArrayList(
                SettingsService.LOADING_SKELETON,
                SettingsService.LOADING_SIMPLU
        ));
        loadingComboBox.setValue(settingsService.getTipLoading());

        ComboBox<String> paginaImplicitaComboBox = new ComboBox<>();
        paginaImplicitaComboBox.setItems(FXCollections.observableArrayList(
                SettingsService.PAGINA_DASHBOARD,
                SettingsService.PAGINA_CLIENTI,
                SettingsService.PAGINA_AUTOMOBILE,
                SettingsService.PAGINA_INCHIRIERI,
                SettingsService.PAGINA_PLATI,
                SettingsService.PAGINA_RAPOARTE
        ));
        paginaImplicitaComboBox.setValue(settingsService.getPaginaImplicita());

        GridPane form = new GridPane();
        form.setHgap(14);
        form.setVgap(12);
        form.setPadding(new Insets(4));

        adaugaRand(form, 0, "Tema aplicatiei", temaComboBox);
        adaugaRand(form, 1, "Dimensiune text", textComboBox);
        adaugaRand(form, 2, "Densitate tabele", densitateComboBox);
        adaugaRand(form, 3, "Confirmare la stergere", confirmareStergereCheckBox);
        adaugaRand(form, 4, "Tip loading", loadingComboBox);
        adaugaRand(form, 5, "Pagina implicita dupa login", paginaImplicitaComboBox);

        Button salveazaButton = new Button("Salveaza setarile");
        salveazaButton.getStyleClass().add("primary-button");

        Button reseteazaButton = new Button("Reseteaza setarile");
        reseteazaButton.getStyleClass().add("secondary-button");

        HBox buttons = new HBox(10, salveazaButton, reseteazaButton);
        buttons.getStyleClass().add("page-toolbar");

        VBox contentCard = new VBox(14);
        ViewFactory.asCard(contentCard);
        Label cardTitle = new Label("Preferinte aplicatie");
        cardTitle.getStyleClass().add("card-title");
        contentCard.getChildren().addAll(cardTitle, form, buttons);

        salveazaButton.setOnAction(e -> {
            settingsService.salveazaSetari(
                    temaComboBox.getValue(),
                    textComboBox.getValue(),
                    densitateComboBox.getValue(),
                    confirmareStergereCheckBox.isSelected(),
                    loadingComboBox.getValue(),
                    paginaImplicitaComboBox.getValue()
            );
            onSettingsChanged.run();
            DialogHelper.showInfo(owner(), "Setarile au fost salvate.");
        });

        reseteazaButton.setOnAction(e -> {
            settingsService.reseteazaSetari();
            temaComboBox.setValue(settingsService.getTema());
            textComboBox.setValue(settingsService.getDimensiuneText());
            densitateComboBox.setValue(settingsService.getDensitateTabele());
            confirmareStergereCheckBox.setSelected(settingsService.isConfirmareStergereActiva());
            loadingComboBox.setValue(settingsService.getTipLoading());
            paginaImplicitaComboBox.setValue(settingsService.getPaginaImplicita());
            onSettingsChanged.run();
            DialogHelper.showInfo(owner(), "Setarile au fost resetate.");
        });

        pageContent.getChildren().add(contentCard);
        root.setCenter(ViewFactory.createPage("Setari", "S", pageContent));
    }

    private void adaugaRand(GridPane form, int row, String labelText, javafx.scene.Node control) {
        Label label = new Label(labelText.toUpperCase());
        label.getStyleClass().add("form-label");
        form.add(label, 0, row);
        form.add(control, 1, row);
    }

    private Stage owner() {
        return ownerSupplier.get();
    }
}
