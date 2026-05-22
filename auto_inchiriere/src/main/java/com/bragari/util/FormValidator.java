package com.bragari.util;

// FormValidator ajuta interfata sa marcheze campurile gresite.
// Practic, aici sunt reguli mici pentru mesaje si validari vizuale.

import java.time.LocalDate;
import java.util.regex.Pattern;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.StatusInchiriere;

public class FormValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFON_PATTERN = Pattern.compile("^\\+?\\d{8,15}$");
    private static final Pattern NUME_PATTERN = Pattern.compile("^[\\p{L}][\\p{L} .'-]{1,79}$");
    private static final Pattern TEXT_AUTO_PATTERN = Pattern.compile("^[\\p{L}0-9][\\p{L}0-9 .'-]{1,49}$");
    private static final Pattern NUMAR_INMATRICULARE_PATTERN = Pattern.compile("^[A-Z0-9 -]{4,12}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]{3,30}$");
    private static final Pattern NUMAR_DECIMAL_PATTERN = Pattern.compile("^\\d+(?:[.,]\\d{1,2})?$");
    private static final Pattern CONTINE_LITERA = Pattern.compile(".*[\\p{L}].*");
    private static final Pattern CONTINE_CIFRA = Pattern.compile(".*\\d.*");

    private FormValidator() {
    }

    public static boolean esteGol(String text) {
        return text == null || text.isBlank();
    }

    public static void valideazaClientForm(String nume, String telefon, String email) {
        String numeCurat = normalizeazaText(nume);
        String telefonCurat = normalizeazaTelefon(telefon);
        String emailCurat = normalizeazaEmail(email);

        if (esteGol(numeCurat)) {
            throw new IllegalArgumentException("Numele clientului este obligatoriu.");
        }

        if (!NUME_PATTERN.matcher(numeCurat).matches()) {
            throw new IllegalArgumentException("Numele clientului trebuie sa contina doar litere, spatii, punct sau cratima.");
        }

        if (esteGol(telefonCurat)) {
            throw new IllegalArgumentException("Telefonul clientului este obligatoriu.");
        }

        if (!TELEFON_PATTERN.matcher(telefonCurat).matches()) {
            throw new IllegalArgumentException("Telefonul trebuie sa contina 8-15 cifre si poate avea + doar la inceput.");
        }

        if (esteGol(emailCurat) || !EMAIL_PATTERN.matcher(emailCurat).matches()) {
            throw new IllegalArgumentException("Emailul trebuie sa fie de forma nume@domeniu.com.");
        }
    }

    public static void valideazaCategorieForm(String denumire, String descriere) {
        String denumireCurata = normalizeazaText(denumire);
        String descriereCurata = normalizeazaText(descriere);

        if (esteGol(denumireCurata)) {
            throw new IllegalArgumentException("Denumirea categoriei este obligatorie.");
        }

        if (!TEXT_AUTO_PATTERN.matcher(denumireCurata).matches()) {
            throw new IllegalArgumentException("Denumirea categoriei trebuie sa aiba 2-50 caractere si sa nu contina simboluri speciale.");
        }

        if (!esteGol(descriereCurata) && descriereCurata.length() > 150) {
            throw new IllegalArgumentException("Descrierea categoriei poate avea cel mult 150 de caractere.");
        }
    }

    public static void valideazaAutomobilForm(CategorieAuto categorie, String marca, String model, String numar, String pretText) {
        String marcaCurata = normalizeazaText(marca);
        String modelCurat = normalizeazaText(model);
        String numarCurat = normalizeazaNumarInmatriculare(numar);

        if (categorie == null) {
            throw new IllegalArgumentException("Selecteaza o categorie.");
        }

        if (esteGol(marcaCurata)) {
            throw new IllegalArgumentException("Marca automobilului este obligatorie.");
        }

        if (!TEXT_AUTO_PATTERN.matcher(marcaCurata).matches()) {
            throw new IllegalArgumentException("Marca trebuie sa aiba 2-50 caractere si sa nu contina simboluri speciale.");
        }

        if (esteGol(modelCurat)) {
            throw new IllegalArgumentException("Modelul automobilului este obligatoriu.");
        }

        if (!TEXT_AUTO_PATTERN.matcher(modelCurat).matches()) {
            throw new IllegalArgumentException("Modelul trebuie sa aiba 2-50 caractere si sa nu contina simboluri speciale.");
        }

        if (esteGol(numarCurat)) {
            throw new IllegalArgumentException("Numarul de inmatriculare este obligatoriu.");
        }

        if (!NUMAR_INMATRICULARE_PATTERN.matcher(numarCurat).matches()
                || !CONTINE_LITERA.matcher(numarCurat).matches()
                || !CONTINE_CIFRA.matcher(numarCurat).matches()) {
            throw new IllegalArgumentException("Numarul de inmatriculare trebuie sa contina doar litere, cifre, spatii sau cratima.");
        }

        if (esteGol(pretText)) {
            throw new IllegalArgumentException("Pretul pe zi este obligatoriu.");
        }

        parseazaPret(pretText);
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
        String metodaCurata = normalizeazaText(metoda).toUpperCase();

        if (inchiriere == null) {
            throw new IllegalArgumentException("Selecteaza o inchiriere.");
        }

        if (esteGol(sumaText)) {
            throw new IllegalArgumentException("Suma este obligatorie.");
        }

        if (esteGol(metodaCurata)) {
            throw new IllegalArgumentException("Selecteaza metoda de plata.");
        }

        if (!"CARD".equals(metodaCurata) && !"CASH".equals(metodaCurata) && !"TRANSFER".equals(metodaCurata)) {
            throw new IllegalArgumentException("Metoda de plata trebuie sa fie CARD, CASH sau TRANSFER.");
        }

        if (dataPlata == null) {
            throw new IllegalArgumentException("Data platii este obligatorie.");
        }

        if (dataPlata.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data platii nu poate fi in viitor.");
        }

        parseazaSuma(sumaText);
    }

    public static void valideazaUtilizatorForm(String username, String parola, String rol) {
        valideazaUsername(username);
        valideazaParola(parola);

        if (esteGol(rol) || (!"ADMIN".equalsIgnoreCase(rol.trim()) && !"USER".equalsIgnoreCase(rol.trim()))) {
            throw new IllegalArgumentException("Rolul trebuie sa fie ADMIN sau USER.");
        }
    }

    public static void valideazaUsername(String username) {
        String usernameCurat = normalizeazaText(username);

        if (esteGol(usernameCurat)) {
            throw new IllegalArgumentException("Username este obligatoriu.");
        }

        if (!USERNAME_PATTERN.matcher(usernameCurat).matches()) {
            throw new IllegalArgumentException("Username trebuie sa aiba 3-30 caractere si poate contine doar litere, cifre, punct, _ sau -.");
        }
    }

    public static void valideazaParola(String parola) {
        if (esteGol(parola)) {
            throw new IllegalArgumentException("Parola este obligatorie.");
        }

        if (parola.length() < 6 || !CONTINE_LITERA.matcher(parola).matches() || !CONTINE_CIFRA.matcher(parola).matches()) {
            throw new IllegalArgumentException("Parola trebuie sa aiba minim 6 caractere, cel putin o litera si cel putin o cifra.");
        }
    }

    private static double parseazaNumar(String text, String numeCamp) {
        String textCurat = normalizeazaText(text).replace(',', '.');

        if (!NUMAR_DECIMAL_PATTERN.matcher(normalizeazaText(text)).matches()) {
            throw new IllegalArgumentException(numeCamp + " trebuie sa fie un numar valid, cu maxim 2 zecimale.");
        }

        try {
            double val = Double.parseDouble(textCurat);
            if (Double.isNaN(val) || Double.isInfinite(val) || val <= 0) {
                throw new IllegalArgumentException(numeCamp + " trebuie sa fie un numar valid si mai mare decat 0.");
            }
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

    public static String normalizeazaText(String text) {
        return text == null ? "" : text.trim();
    }

    public static String normalizeazaEmail(String email) {
        return normalizeazaText(email).toLowerCase();
    }

    public static String normalizeazaTelefon(String telefon) {
        return normalizeazaText(telefon).replaceAll("[\\s-]", "");
    }

    public static String normalizeazaNumarInmatriculare(String numar) {
        return normalizeazaText(numar).toUpperCase().replaceAll("\\s+", " ");
    }
}
