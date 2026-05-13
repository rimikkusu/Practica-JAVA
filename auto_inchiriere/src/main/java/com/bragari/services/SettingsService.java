package com.bragari.services;

import java.util.prefs.Preferences;

public class SettingsService {

    public static final String TEMA_LIGHT = "Light Mode";
    public static final String TEMA_DARK = "Dark Mode";
    public static final String TEMA_AMBER = "Amber Noir";

    public static final String TEXT_MIC = "Mic";
    public static final String TEXT_NORMAL = "Normal";
    public static final String TEXT_MARE = "Mare";

    public static final String DENSITATE_COMPACT = "Compact";
    public static final String DENSITATE_NORMAL = "Normal";

    public static final String LOADING_SKELETON = "Skeleton loading";
    public static final String LOADING_SIMPLU = "Loading simplu";

    public static final String PAGINA_DASHBOARD = "Dashboard";
    public static final String PAGINA_CLIENTI = "Clienti";
    public static final String PAGINA_AUTOMOBILE = "Automobile";
    public static final String PAGINA_INCHIRIERI = "Inchirieri";
    public static final String PAGINA_PLATI = "Plati";
    public static final String PAGINA_RAPOARTE = "Rapoarte";

    private static final String KEY_TEMA = "tema";
    private static final String KEY_DIMENSIUNE_TEXT = "dimensiuneText";
    private static final String KEY_DENSITATE_TABELE = "densitateTabele";
    private static final String KEY_CONFIRMARE_STERGERE = "confirmareStergere";
    private static final String KEY_LOADING = "tipLoading";
    private static final String KEY_PAGINA_IMPLICITA = "paginaImplicita";

    private final Preferences preferences = Preferences.userNodeForPackage(SettingsService.class);

    public String getTema() {
        return preferences.get(KEY_TEMA, TEMA_AMBER);
    }

    public String getDimensiuneText() {
        return preferences.get(KEY_DIMENSIUNE_TEXT, TEXT_NORMAL);
    }

    public String getDensitateTabele() {
        return preferences.get(KEY_DENSITATE_TABELE, DENSITATE_NORMAL);
    }

    public boolean isConfirmareStergereActiva() {
        return preferences.getBoolean(KEY_CONFIRMARE_STERGERE, true);
    }

    public String getTipLoading() {
        return preferences.get(KEY_LOADING, LOADING_SKELETON);
    }

    public String getPaginaImplicita() {
        return preferences.get(KEY_PAGINA_IMPLICITA, PAGINA_DASHBOARD);
    }

    public boolean folosesteSkeletonLoading() {
        return LOADING_SKELETON.equals(getTipLoading());
    }

    public void salveazaSetari(String tema, String dimensiuneText, String densitateTabele,
                               boolean confirmareStergere, String tipLoading, String paginaImplicita) {
        preferences.put(KEY_TEMA, valoareSauDefault(tema, TEMA_AMBER));
        preferences.put(KEY_DIMENSIUNE_TEXT, valoareSauDefault(dimensiuneText, TEXT_NORMAL));
        preferences.put(KEY_DENSITATE_TABELE, valoareSauDefault(densitateTabele, DENSITATE_NORMAL));
        preferences.putBoolean(KEY_CONFIRMARE_STERGERE, confirmareStergere);
        preferences.put(KEY_LOADING, valoareSauDefault(tipLoading, LOADING_SKELETON));
        preferences.put(KEY_PAGINA_IMPLICITA, valoareSauDefault(paginaImplicita, PAGINA_DASHBOARD));
    }

    public void reseteazaSetari() {
        salveazaSetari(
                TEMA_AMBER,
                TEXT_NORMAL,
                DENSITATE_NORMAL,
                true,
                LOADING_SKELETON,
                PAGINA_DASHBOARD
        );
    }

    public String getTemaCssClass() {
        if (TEMA_LIGHT.equals(getTema())) {
            return "theme-light";
        }

        if (TEMA_DARK.equals(getTema())) {
            return "theme-dark";
        }

        return "theme-amber";
    }

    public String getTextCssClass() {
        if (TEXT_MIC.equals(getDimensiuneText())) {
            return "text-small";
        }

        if (TEXT_MARE.equals(getDimensiuneText())) {
            return "text-large";
        }

        return "text-normal";
    }

    public String getTableCssClass() {
        if (DENSITATE_COMPACT.equals(getDensitateTabele())) {
            return "table-compact";
        }

        return "table-normal";
    }

    public String getPaginaImplicitaKey() {
        return switch (getPaginaImplicita()) {
            case PAGINA_CLIENTI -> "clienti";
            case PAGINA_AUTOMOBILE -> "automobile";
            case PAGINA_INCHIRIERI -> "inchirieri";
            case PAGINA_PLATI -> "plati";
            case PAGINA_RAPOARTE -> "rapoarte";
            default -> "dashboard";
        };
    }

    private String valoareSauDefault(String valoare, String defaultValue) {
        return valoare == null || valoare.isBlank() ? defaultValue : valoare;
    }
}
