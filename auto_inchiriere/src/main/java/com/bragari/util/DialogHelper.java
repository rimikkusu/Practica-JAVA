package com.bragari.util;

// DialogHelper strange metodele folosite la dialoguri si la incarcarea CSS-ului.
// Asa nu repetam acelasi cod in fiecare pagina a aplicatiei.

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogHelper {

    // Toate fisierele CSS separate sunt tinute in acelasi folder de resurse.
    // Constanta evita repetarea caii complete la fiecare fisier din lista.
    private static final String STYLES_PATH = "/com/bragari/styles/";

    // Ordinea este importanta: JavaFX aplica regulile CSS in ordinea in care
    // sunt incarcate. Fisierele de la final pot suprascrie reguli din fisierele
    // anterioare, exact cum se intampla in vechiul application.css monolitic.
    private static final String[] APP_STYLESHEETS = {
            "/styles.css",
            STYLES_PATH + "application.css",
            STYLES_PATH + "01-base-layout.css",
            STYLES_PATH + "02-sidebar.css",
            STYLES_PATH + "03-top-bar.css",
            STYLES_PATH + "04-buttons.css",
            STYLES_PATH + "05-cards-stats.css",
            STYLES_PATH + "06-tables.css",
            STYLES_PATH + "07-badges.css",
            STYLES_PATH + "08-forms-dialogs.css",
            STYLES_PATH + "09-login.css",
            STYLES_PATH + "10-reports-loading.css",
            STYLES_PATH + "11-themes.css",
            STYLES_PATH + "12-dashboard-widgets.css",
            STYLES_PATH + "13-app-dialogs.css",
            STYLES_PATH + "14-command-dashboard.css",
            STYLES_PATH + "15-login-overrides.css",
            STYLES_PATH + "16-table-pages.css"
    };

    private DialogHelper() {
    }

    // Se foloseste pentru ferestrele principale, unde stilurile se pun pe Scene.
    public static void aplicaCss(Scene scene) {
        aplicaCss(scene.getStylesheets());
    }

    // Se foloseste pentru componente sau dialoguri, unde stilurile se pun direct
    // pe radacina vizuala a ferestrei.
    public static void aplicaCss(Parent parent) {
        aplicaCss(parent.getStylesheets());
    }

    // Parcurge lista centralizata si incarca toate fisierele CSS ale aplicatiei.
    // Astfel, daca mai adaugam un fisier CSS, il inregistram intr-un singur loc.
    private static void aplicaCss(ObservableList<String> stylesheets) {
        for (String path : APP_STYLESHEETS) {
            adaugaCss(stylesheets, path);
        }
    }

    // Cauta fisierul CSS in resursele aplicatiei si il adauga doar daca exista.
    // Verificarea contains previne dublarea aceluiasi stylesheet pe aceeasi scena.
    private static void adaugaCss(ObservableList<String> stylesheets, String path) {
        URL resource = DialogHelper.class.getResource(path);

        if (resource == null) {
            return;
        }

        String stylesheet = resource.toExternalForm();

        if (!stylesheets.contains(stylesheet)) {
            stylesheets.add(stylesheet);
        }
    }

    public static Stage creeazaDialog(Stage owner, String title, VBox content) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        if (owner != null) {
            dialog.initOwner(owner);
        }

        dialog.setResizable(false);

        content.getStyleClass().addAll("dialog-content", "content-card", "dialog-pane", "card");
        copiaClaseSetari(owner, content);
        content.setPadding(new Insets(20));
        content.setSpacing(14);

        Scene scene = new Scene(content);
        aplicaCss(scene);
        dialog.setScene(scene);

        return dialog;
    }

    public static HBox creeazaButoaneDialog(Button primaryButton, Button cancelButton) {
        if (!primaryButton.getStyleClass().contains("primary-button")) {
            primaryButton.getStyleClass().add("primary-button");
        }
        if (!primaryButton.getStyleClass().contains("btn-primary")) {
            primaryButton.getStyleClass().add("btn-primary");
        }

        if (!cancelButton.getStyleClass().contains("secondary-button")) {
            cancelButton.getStyleClass().add("secondary-button");
        }
        if (!cancelButton.getStyleClass().contains("btn-secondary")) {
            cancelButton.getStyleClass().add("btn-secondary");
        }

        HBox buttons = new HBox(10);
        buttons.getStyleClass().add("dialog-actions");
        buttons.setAlignment(Pos.CENTER_RIGHT);
        buttons.getChildren().addAll(primaryButton, cancelButton);
        return buttons;
    }

    public static boolean confirmaActiune(Stage owner, String mesaj) {
        AtomicBoolean confirmat = new AtomicBoolean(false);

        Button stergeButton = new Button("Da, sterge");
        stergeButton.getStyleClass().add("dialog-danger-button");
        stergeButton.setDefaultButton(true);

        Button anuleazaButton = new Button("Anuleaza");
        anuleazaButton.getStyleClass().add("dialog-secondary-button");
        anuleazaButton.setCancelButton(true);

        VBox content = creeazaMesajDialogContent("Confirmare", mesaj, "?", "dialog-confirm-icon");
        content.getChildren().add(creeazaActiuniDialog(anuleazaButton, stergeButton));

        Stage dialog = creeazaAppDialog(owner, "Confirmare", content);

        stergeButton.setOnAction(e -> {
            confirmat.set(true);
            dialog.close();
        });
        anuleazaButton.setOnAction(e -> dialog.close());

        dialog.showAndWait();
        return confirmat.get();
    }

    public static void showError(Stage owner, String message) {
        afiseazaMesaj(owner, "Eroare", curataMesajEroare(message), "!", "dialog-error-icon");
    }

    public static String curataMesajEroare(String message) {
        if (message == null || message.isBlank()) {
            return "A aparut o eroare.";
        }

        String text = message.toLowerCase();

        if (text.contains("clienti") && text.contains("inchirieri")) {
            return "Clientul nu poate fi sters deoarece are inchirieri.";
        }

        if (text.contains("automobile") && text.contains("inchirieri")) {
            return "Automobilul nu poate fi sters deoarece are inchirieri.";
        }

        if (text.contains("inchirieri") && text.contains("plati")) {
            return "Inchirierea nu poate fi stearsa deoarece are plati.";
        }

        if (text.contains("foreign key") || text.contains("violates") || text.contains("constraint")) {
            return "Inregistrarea nu poate fi stearsa deoarece este folosita in alta parte.";
        }

        return message;
    }

    public static void showInfo(Stage owner, String message) {
        afiseazaMesaj(owner, "Succes", message, "OK", "dialog-success-icon");
    }

    private static void afiseazaMesaj(Stage owner, String title, String message, String iconText, String iconClass) {
        Button okButton = new Button("OK");
        okButton.getStyleClass().add("dialog-primary-button");
        okButton.setDefaultButton(true);
        okButton.setCancelButton(true);

        VBox content = creeazaMesajDialogContent(title, message, iconText, iconClass);
        content.getChildren().add(creeazaActiuniDialog(okButton));

        Stage dialog = creeazaAppDialog(owner, title, content);
        okButton.setOnAction(e -> dialog.close());
        dialog.showAndWait();
    }

    private static VBox creeazaMesajDialogContent(String title, String message, String iconText, String iconClass) {
        Label icon = new Label(iconText);
        icon.getStyleClass().addAll("dialog-icon", iconClass);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("dialog-title");

        Label messageLabel = new Label(message == null || message.isBlank() ? "A aparut o problema." : message);
        messageLabel.getStyleClass().add("dialog-message");
        messageLabel.setWrapText(true);
        messageLabel.setMaxWidth(360);

        VBox textBox = new VBox(4, titleLabel, messageLabel);

        HBox header = new HBox(12, icon, textBox);
        header.getStyleClass().add("dialog-header");
        header.setAlignment(Pos.CENTER_LEFT);

        VBox content = new VBox(18, header);
        content.getStyleClass().add("app-dialog");
        content.setPadding(new Insets(22, 24, 20, 24));
        content.setMinWidth(410);
        return content;
    }

    private static HBox creeazaActiuniDialog(Button... buttons) {
        HBox actions = new HBox(10);
        actions.getStyleClass().add("dialog-actions");
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(buttons);
        return actions;
    }

    private static Stage creeazaAppDialog(Stage owner, String title, VBox content) {
        Stage dialog = new Stage();
        dialog.setTitle(title);
        dialog.initModality(Modality.APPLICATION_MODAL);

        if (owner != null) {
            dialog.initOwner(owner);
        }

        dialog.setResizable(false);
        copiaClaseSetari(owner, content);

        Scene scene = new Scene(content);
        aplicaCss(scene);
        dialog.setScene(scene);

        return dialog;
    }

    private static void copiaClaseSetari(Stage owner, Parent target) {
        if (owner == null || owner.getScene() == null || owner.getScene().getRoot() == null) {
            return;
        }

        for (String styleClass : owner.getScene().getRoot().getStyleClass()) {
            if ((styleClass.startsWith("theme-")
                    || styleClass.startsWith("text-")
                    || styleClass.startsWith("table-"))
                    && !target.getStyleClass().contains(styleClass)) {
                target.getStyleClass().add(styleClass);
            }
        }
    }
}
