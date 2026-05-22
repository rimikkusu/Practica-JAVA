package com.bragari.util;

// CsvExporter transforma datele din aplicatie intr-un fisier CSV.
// CSV-ul poate fi deschis mai usor in Excel sau in alte programe de tabel.

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
        service.obtineClienti().forEach(c ->
                csv.append(csvLine(c.getId(), c.getNume(), c.getTelefon(), c.getEmail())));
        return csv.toString();
    }

    public static String genereazaCsvAutomobile(AutoInchiriereService service) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvLine("id", "categorie", "marca", "model", "numar_inmatriculare", "pret_pe_zi", "disponibil"));
        service.obtineAutomobile().forEach(a ->
                csv.append(csvLine(a.getId(),
                        a.getCategorie() == null ? "" : a.getCategorie().getDenumire(),
                        a.getMarca(), a.getModel(), a.getNumarInmatriculare(),
                        a.getPretPeZi(), a.isDisponibil() ? "DA" : "NU")));
        return csv.toString();
    }

    public static String genereazaCsvInchirieri(AutoInchiriereService service) {
        StringBuilder csv = new StringBuilder();
        csv.append(csvLine("tip", "id", "inchiriere_id", "client", "automobil",
                "data_inceput", "data_sfarsit", "total", "status", "suma", "metoda_plata", "data_plata"));

        service.obtineInchirieri().forEach(i ->
                csv.append(csvLine("INCHIRIERE", i.getId(), "",
                        i.getClient().getNume(),
                        i.getAutomobil().getMarca() + " " + i.getAutomobil().getModel(),
                        i.getDataInceput(), i.getDataSfarsit(),
                        i.calculeazaTotal(), i.getStatus(), "", "", "")));

        service.obtinePlati().forEach(p -> {
            Inchiriere i = p.getInchiriere();
            csv.append(csvLine("PLATA", p.getId(), i.getId(),
                    i.getClient().getNume(),
                    i.getAutomobil().getMarca() + " " + i.getAutomobil().getModel(),
                    i.getDataInceput(), i.getDataSfarsit(),
                    i.calculeazaTotal(), i.getStatus(),
                    p.getSuma(), p.getMetodaPlata(), p.getDataPlata()));
        });

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
