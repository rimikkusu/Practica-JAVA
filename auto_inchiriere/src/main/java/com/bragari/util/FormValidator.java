package com.bragari.util;

// FormValidator ajuta interfata sa marcheze campurile gresite.
// Practic, aici sunt reguli mici pentru mesaje si validari vizuale.

import java.time.LocalDate;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.StatusInchiriere;

public class FormValidator {

    private FormValidator() {
    }

    public static boolean esteGol(String text) {
        return text == null || text.isBlank();
    }

    public static void valideazaClientForm(String nume, String telefon, String email) {
        if (esteGol(nume)) {
            throw new IllegalArgumentException("Numele clientului este obligatoriu.");
        }

        if (esteGol(telefon)) {
            throw new IllegalArgumentException("Telefonul clientului este obligatoriu.");
        }

        if (esteGol(email) || !email.contains("@")) {
            throw new IllegalArgumentException("Emailul clientului nu este valid.");
        }
    }

    public static void valideazaAutomobilForm(CategorieAuto categorie, String marca, String model, String numar, String pretText) {
        if (categorie == null) {
            throw new IllegalArgumentException("Selecteaza o categorie.");
        }

        if (esteGol(marca)) {
            throw new IllegalArgumentException("Marca automobilului este obligatorie.");
        }

        if (esteGol(model)) {
            throw new IllegalArgumentException("Modelul automobilului este obligatoriu.");
        }

        if (esteGol(numar)) {
            throw new IllegalArgumentException("Numarul de inmatriculare este obligatoriu.");
        }

        if (esteGol(pretText)) {
            throw new IllegalArgumentException("Pretul pe zi este obligatoriu.");
        }
    }

    public static void valideazaInchiriereForm(Client client, Automobil automobil, LocalDate dataInceput,
                                               LocalDate dataSfarsit, StatusInchiriere status) {
        if (client == null) {
            throw new IllegalArgumentException("Selecteaza un client.");
        }

        if (automobil == null) {
            throw new IllegalArgumentException("Selecteaza un automobil disponibil.");
        }

        if (dataInceput == null) {
            throw new IllegalArgumentException("Data de inceput este obligatorie.");
        }

        if (dataSfarsit == null) {
            throw new IllegalArgumentException("Data de sfarsit este obligatorie.");
        }

        if (!dataSfarsit.isAfter(dataInceput)) {
            throw new IllegalArgumentException("Data de sfarsit trebuie sa fie dupa data de inceput.");
        }

        if (status == null) {
            throw new IllegalArgumentException("Selecteaza statusul inchirierii.");
        }
    }

    public static void valideazaPlataForm(Inchiriere inchiriere, String sumaText, String metoda, LocalDate dataPlata) {
        if (inchiriere == null) {
            throw new IllegalArgumentException("Selecteaza o inchiriere.");
        }

        if (esteGol(sumaText)) {
            throw new IllegalArgumentException("Suma este obligatorie.");
        }

        if (esteGol(metoda)) {
            throw new IllegalArgumentException("Selecteaza metoda de plata.");
        }

        if (dataPlata == null) {
            throw new IllegalArgumentException("Data platii este obligatorie.");
        }
    }

    private static double parseazaNumar(String text, String numeCamp) {
        try {
            double val = Double.parseDouble(text);
            if (Double.isNaN(val) || Double.isInfinite(val) || val < 0)
                throw new IllegalArgumentException(numeCamp + " trebuie sa fie un numar valid si pozitiv.");
            return val;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(numeCamp + " trebuie sa fie un numar valid.");
        }
    }

    public static double parseazaPret(String pretText) {
        return parseazaNumar(pretText, "Pretul");
    }

    public static double parseazaSuma(String sumaText) {
        return parseazaNumar(sumaText, "Suma");
    }
}
