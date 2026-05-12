package com.bragari.util;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
        String stylesheet = DialogHelper.class.getResource("/styles.css").toExternalForm();

        if (!scene.getStylesheets().contains(stylesheet)) {
            scene.getStylesheets().add(stylesheet);
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

        content.getStyleClass().addAll("dialog-content", "content-card");
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

        if (!cancelButton.getStyleClass().contains("secondary-button")) {
            cancelButton.getStyleClass().add("secondary-button");
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

        alert.showAndWait();
    }
}
