package com.bragari.services;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;

public class ValidatorService {

    public void valideazaClient(Client client) {
        if (client.getNume() == null || client.getNume().isBlank()) {
            throw new IllegalArgumentException("Numele clientului este obligatoriu.");
        }

        if (client.getTelefon() == null || client.getTelefon().isBlank()) {
            throw new IllegalArgumentException("Telefonul clientului este obligatoriu.");
        }

        if (client.getEmail() == null || !client.getEmail().contains("@")) {
            throw new IllegalArgumentException("Emailul clientului nu este valid.");
        }
    }

    public void valideazaCategorie(CategorieAuto categorie) {
        if (categorie.getDenumire() == null || categorie.getDenumire().isBlank()) {
            throw new IllegalArgumentException("Denumirea categoriei este obligatorie.");
        }
    }

    public void valideazaAutomobil(Automobil automobil) {
        if (automobil.getCategorie() == null) {
            throw new IllegalArgumentException("Automobilul trebuie sa aiba categorie.");
        }

        if (automobil.getMarca() == null || automobil.getMarca().isBlank()) {
            throw new IllegalArgumentException("Marca automobilului este obligatorie.");
        }

        if (automobil.getModel() == null || automobil.getModel().isBlank()) {
            throw new IllegalArgumentException("Modelul automobilului este obligatoriu.");
        }

        if (automobil.getNumarInmatriculare() == null || automobil.getNumarInmatriculare().isBlank()) {
            throw new IllegalArgumentException("Numarul de inmatriculare este obligatoriu.");
        }

        if (Double.isNaN(automobil.getPretPeZi()) || Double.isInfinite(automobil.getPretPeZi()) || automobil.getPretPeZi() < 0) {
            throw new IllegalArgumentException("Pretul pe zi trebuie sa fie un numar valid si pozitiv.");
        }
    }

    public void valideazaInchiriere(Inchiriere inchiriere) {
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
        if (plata.getInchiriere() == null) {
            throw new IllegalArgumentException("Plata trebuie sa fie legata de o inchiriere.");
        }

        if (Double.isNaN(plata.getSuma()) || Double.isInfinite(plata.getSuma()) || plata.getSuma() < 0) {
            throw new IllegalArgumentException("Suma platii trebuie sa fie un numar valid si pozitiv.");
        }

        if (plata.getMetodaPlata() == null || plata.getMetodaPlata().isBlank()) {
            throw new IllegalArgumentException("Metoda de plata este obligatorie.");
        }

        if (plata.getDataPlata() == null) {
            throw new IllegalArgumentException("Data platii este obligatorie.");
        }
    }
}
