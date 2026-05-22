package com.bragari.services;

// ValidatorService verifica datele introduse in formulare.
// Asa prindem greselile inainte sa ajunga datele in baza de date.

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.util.FormValidator;

public class ValidatorService {

    public void valideazaClient(Client client) {
        if (client == null) {
            throw new IllegalArgumentException("Clientul este obligatoriu.");
        }

        FormValidator.valideazaClientForm(client.getNume(), client.getTelefon(), client.getEmail());
        client.setNume(FormValidator.normalizeazaText(client.getNume()));
        client.setTelefon(FormValidator.normalizeazaTelefon(client.getTelefon()));
        client.setEmail(FormValidator.normalizeazaEmail(client.getEmail()));
    }

    public void valideazaCategorie(CategorieAuto categorie) {
        if (categorie == null) {
            throw new IllegalArgumentException("Categoria este obligatorie.");
        }

        FormValidator.valideazaCategorieForm(categorie.getDenumire(), categorie.getDescriere());
        categorie.setDenumire(FormValidator.normalizeazaText(categorie.getDenumire()));
        categorie.setDescriere(FormValidator.normalizeazaText(categorie.getDescriere()));
    }

    public void valideazaAutomobil(Automobil automobil) {
        if (automobil == null) {
            throw new IllegalArgumentException("Automobilul este obligatoriu.");
        }

        if (automobil.getCategorie() == null) {
            throw new IllegalArgumentException("Automobilul trebuie sa aiba categorie.");
        }

        FormValidator.valideazaAutomobilForm(
                automobil.getCategorie(),
                automobil.getMarca(),
                automobil.getModel(),
                automobil.getNumarInmatriculare(),
                String.valueOf(automobil.getPretPeZi())
        );
        automobil.setMarca(FormValidator.normalizeazaText(automobil.getMarca()));
        automobil.setModel(FormValidator.normalizeazaText(automobil.getModel()));
        automobil.setNumarInmatriculare(FormValidator.normalizeazaNumarInmatriculare(automobil.getNumarInmatriculare()));
    }

    public void valideazaInchiriere(Inchiriere inchiriere) {
        if (inchiriere == null) {
            throw new IllegalArgumentException("Inchirierea este obligatorie.");
        }

        if (inchiriere.getClient() == null) {
            throw new IllegalArgumentException("Inchirierea trebuie sa aiba client.");
        }

        if (inchiriere.getAutomobil() == null) {
            throw new IllegalArgumentException("Inchirierea trebuie sa aiba automobil.");
        }

        if (inchiriere.getDataInceput() == null) {
            throw new IllegalArgumentException("Data de inceput este obligatorie.");
        }

        if (inchiriere.getDataSfarsit() == null) {
            throw new IllegalArgumentException("Data de sfarsit este obligatorie.");
        }

        if (!inchiriere.getDataSfarsit().isAfter(inchiriere.getDataInceput())) {
            throw new IllegalArgumentException("Data de sfarsit trebuie sa fie dupa data de inceput.");
        }

        if (inchiriere.getStatus() == null) {
            throw new IllegalArgumentException("Statusul inchirierii este obligatoriu.");
        }
    }

    public void valideazaPlata(Plata plata) {
        if (plata == null) {
            throw new IllegalArgumentException("Plata este obligatorie.");
        }

        if (plata.getInchiriere() == null) {
            throw new IllegalArgumentException("Plata trebuie sa fie legata de o inchiriere.");
        }

        FormValidator.valideazaPlataForm(
                plata.getInchiriere(),
                String.valueOf(plata.getSuma()),
                plata.getMetodaPlata(),
                plata.getDataPlata()
        );
        plata.setMetodaPlata(FormValidator.normalizeazaText(plata.getMetodaPlata()).toUpperCase());
    }
}
