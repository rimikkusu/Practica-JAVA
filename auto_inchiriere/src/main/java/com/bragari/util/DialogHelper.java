package com.bragari.util;

import java.net.URL;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class DialogHelper {

    private DialogHelper() {
    }

    public static void aplicaCss(Scene scene) {
        adaugaCss(scene.getStylesheets(), "/styles.css");
        adaugaCss(scene.getStylesheets(), "/com/bragari/styles/application.css");
    }

    public static void aplicaCss(Parent parent) {
        adaugaCss(parent.getStylesheets(), "/styles.css");
        adaugaCss(parent.getStylesheets(), "/com/bragari/styles/application.css");
    }

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
        ButtonType stergeButton = new ButtonType("Da, sterge", ButtonBar.ButtonData.OK_DONE);
        ButtonType anuleazaButton = new ButtonType("Anuleaza", ButtonBar.ButtonData.CANCEL_CLOSE);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmare");
        alert.setHeaderText(null);
        alert.setContentText(mesaj);
        alert.getButtonTypes().setAll(stergeButton, anuleazaButton);

        if (owner != null) {
            alert.initOwner(owner);
        }

        stilizeazaAlert(alert, owner);

        return alert.showAndWait().orElse(anuleazaButton) == stergeButton;
    }

    public static void showError(Stage owner, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Eroare");
        alert.setHeaderText(null);
        alert.setContentText(curataMesajEroare(message));

        if (owner != null) {
            alert.initOwner(owner);
        }

        stilizeazaAlert(alert, owner);

        alert.showAndWait();
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
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Succes");
        alert.setHeaderText(null);
        alert.setContentText(message);

        if (owner != null) {
            alert.initOwner(owner);
        }

        stilizeazaAlert(alert, owner);

        alert.showAndWait();
    }

    private static void stilizeazaAlert(Alert alert, Stage owner) {
        aplicaCss(alert.getDialogPane());
        copiaClaseSetari(owner, alert.getDialogPane());
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
