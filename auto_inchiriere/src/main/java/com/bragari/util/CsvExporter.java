package com.bragari.util;

import com.bragari.models.Automobil;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.services.AutoInchiriereService;

public class CsvExporter {

    private CsvExporter() {
    }

    public static String genereazaCsvRaport(AutoInchiriereService service, String tipRaport) {
        if ("clienti".equals(tipRaport)) {
            return genereazaCsvClienti(service);
        }

        if ("automobile".equals(tipRaport)) {
            return genereazaCsvAutomobile(service);
        }

        if ("inchirieri".equals(tipRaport)) {
            return genereazaCsvInchirieri(service);
        }

        return "";
    }

    public static String genereazaCsvClienti(AutoInchiriereService service) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvLine("id", "nume", "telefon", "email"));

        for (Client client : service.obtineClienti()) {
            csv.append(csvLine(
                    client.getId(),
                    client.getNume(),
                    client.getTelefon(),
                    client.getEmail()
            ));
        }

        return csv.toString();
    }

    public static String genereazaCsvAutomobile(AutoInchiriereService service) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvLine("id", "categorie", "marca", "model", "numar_inmatriculare", "pret_pe_zi", "disponibil"));

        for (Automobil automobil : service.obtineAutomobile()) {
            String categorie = automobil.getCategorie() == null ? "" : automobil.getCategorie().getDenumire();

            csv.append(csvLine(
                    automobil.getId(),
                    categorie,
                    automobil.getMarca(),
                    automobil.getModel(),
                    automobil.getNumarInmatriculare(),
                    automobil.getPretPeZi(),
                    automobil.isDisponibil() ? "DA" : "NU"
            ));
        }

        return csv.toString();
    }

    public static String genereazaCsvInchirieri(AutoInchiriereService service) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvLine(
                "tip",
                "id",
                "inchiriere_id",
                "client",
                "automobil",
                "data_inceput",
                "data_sfarsit",
                "total",
                "status",
                "suma",
                "metoda_plata",
                "data_plata"
        ));

        for (Inchiriere inchiriere : service.obtineInchirieri()) {
            String automobil = inchiriere.getAutomobil().getMarca() + " " + inchiriere.getAutomobil().getModel();

            csv.append(csvLine(
                    "INCHIRIERE",
                    inchiriere.getId(),
                    "",
                    inchiriere.getClient().getNume(),
                    automobil,
                    inchiriere.getDataInceput(),
                    inchiriere.getDataSfarsit(),
                    inchiriere.calculeazaTotal(),
                    inchiriere.getStatus(),
                    "",
                    "",
                    ""
            ));
        }

        for (Plata plata : service.obtinePlati()) {
            Inchiriere inchiriere = plata.getInchiriere();
            String automobil = inchiriere.getAutomobil().getMarca() + " " + inchiriere.getAutomobil().getModel();

            csv.append(csvLine(
                    "PLATA",
                    plata.getId(),
                    inchiriere.getId(),
                    inchiriere.getClient().getNume(),
                    automobil,
                    inchiriere.getDataInceput(),
                    inchiriere.getDataSfarsit(),
                    inchiriere.calculeazaTotal(),
                    inchiriere.getStatus(),
                    plata.getSuma(),
                    plata.getMetodaPlata(),
                    plata.getDataPlata()
            ));
        }

        return csv.toString();
    }

    public static String csvLine(Object... values) {
        StringBuilder line = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                line.append(",");
            }

            line.append(escapeCsv(values[i]));
        }

        line.append(System.lineSeparator());
        return line.toString();
    }

    public static String escapeCsv(Object value) {
        if (value == null) {
            return "";
        }

        String text = String.valueOf(value);
        String escaped = text.replace("\"", "\"\"");

        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
            return "\"" + escaped + "\"";
        }

        return escaped;
    }
}
