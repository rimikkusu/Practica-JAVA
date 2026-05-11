package com.bragari.services;

import java.util.List;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.models.StatusInchiriere;
import com.bragari.repositories.AutomobilRepository;
import com.bragari.repositories.CategorieAutoRepository;
import com.bragari.repositories.ClientRepository;
import com.bragari.repositories.InchiriereRepository;
import com.bragari.repositories.PlataRepository;

public class AutoInchiriereService {
    private final ClientRepository clientRepository;
    private final CategorieAutoRepository categorieRepository;
    private final AutomobilRepository automobilRepository;
    private final InchiriereRepository inchiriereRepository;
    private final PlataRepository plataRepository;
    private final ValidatorService validatorService;

    public AutoInchiriereService() {
        this.clientRepository = new ClientRepository();
        this.categorieRepository = new CategorieAutoRepository();
        this.automobilRepository = new AutomobilRepository();
        this.inchiriereRepository = new InchiriereRepository();
        this.plataRepository = new PlataRepository();
        this.validatorService = new ValidatorService();
    }

    // CLIENTI

    public void adaugaClient(Client client) {
        validatorService.valideazaClient(client);
        clientRepository.adauga(client);
    }

    public List<Client> obtineClienti() {
        return clientRepository.obtineToate();
    }

    public List<Client> cautaClientiDupaNume(String nume) {
        return clientRepository.cautaDupaNume(nume);
    }

    public Client cautaClientDupaId(int id) {
        return clientRepository.cautaDupaId(id);
    }

    public void actualizeazaClient(Client client) {
        validatorService.valideazaClient(client);
        clientRepository.actualizeaza(client);
    }

    public void stergeClient(int id) {
        for (Inchiriere inchiriere : inchiriereRepository.obtineToate()) {
            if (inchiriere.getClient().getId() == id) {
                throw new IllegalArgumentException("Clientul nu poate fi sters deoarece are inchirieri.");
            }
        }

        clientRepository.sterge(id);
    }

    // CATEGORII AUTO

    public void adaugaCategorie(CategorieAuto categorie) {
        validatorService.valideazaCategorie(categorie);
        categorieRepository.adauga(categorie);
    }

    public List<CategorieAuto> obtineCategorii() {
        return categorieRepository.obtineToate();
    }

    public CategorieAuto cautaCategorieDupaId(int id) {
        return categorieRepository.cautaDupaId(id);
    }

    public void actualizeazaCategorie(CategorieAuto categorie) {
        validatorService.valideazaCategorie(categorie);
        categorieRepository.actualizeaza(categorie);
    }

    public void stergeCategorie(int id) {
        categorieRepository.sterge(id);
    }

    // AUTOMOBILE

    public void adaugaAutomobil(Automobil automobil) {
        validatorService.valideazaAutomobil(automobil);
        automobilRepository.adauga(automobil);
    }

    public List<Automobil> obtineAutomobile() {
        return automobilRepository.obtineToate();
    }

    public List<Automobil> obtineAutomobileDisponibile() {
        return automobilRepository.obtineDisponibile();
    }

    public Automobil cautaAutomobilDupaId(int id) {
        return automobilRepository.cautaDupaId(id);
    }

    public void actualizeazaAutomobil(Automobil automobil) {
        validatorService.valideazaAutomobil(automobil);
        automobilRepository.actualizeaza(automobil);
    }

    public void stergeAutomobil(int id) {
        for (Inchiriere inchiriere : inchiriereRepository.obtineToate()) {
            if (inchiriere.getAutomobil().getId() == id) {
                throw new IllegalArgumentException("Automobilul nu poate fi sters deoarece are inchirieri.");
            }
        }

        automobilRepository.sterge(id);
    }

    // INCHIRIERI

    public void adaugaInchiriere(Inchiriere inchiriere) {
        validatorService.valideazaInchiriere(inchiriere);

        if (!inchiriere.getAutomobil().isDisponibil()) {
            throw new IllegalArgumentException("Automobilul nu este disponibil.");
        }

        inchiriereRepository.adauga(inchiriere);

        Automobil automobil = inchiriere.getAutomobil();
        automobil.setDisponibil(false);
        automobilRepository.actualizeaza(automobil);
    }

    public List<Inchiriere> obtineInchirieri() {
        return inchiriereRepository.obtineToate();
    }

    public Inchiriere cautaInchiriereDupaId(int id) {
        return inchiriereRepository.cautaDupaId(id);
    }

    public void actualizeazaInchiriere(Inchiriere inchiriere) {
        validatorService.valideazaInchiriere(inchiriere);
        inchiriereRepository.actualizeaza(inchiriere);

        Automobil automobil = inchiriere.getAutomobil();
        StatusInchiriere status = inchiriere.getStatus();

        automobil.setDisponibil(status == StatusInchiriere.FINALIZATA || status == StatusInchiriere.ANULATA);
        automobilRepository.actualizeaza(automobil);
    }

    public void stergeInchiriere(int id) {
        for (Plata plata : plataRepository.obtineToate()) {
            if (plata.getInchiriere().getId() == id) {
                throw new IllegalArgumentException("Inchirierea nu poate fi stearsa deoarece are plati.");
            }
        }

        Inchiriere inchiriere = inchiriereRepository.cautaDupaId(id);
        inchiriereRepository.sterge(id);

        if (inchiriere != null) {
            Automobil automobil = inchiriere.getAutomobil();
            automobil.setDisponibil(true);
            automobilRepository.actualizeaza(automobil);
        }
    }

    // PLATI

    public void adaugaPlata(Plata plata) {
        validatorService.valideazaPlata(plata);
        plataRepository.adauga(plata);
    }

    public List<Plata> obtinePlati() {
        return plataRepository.obtineToate();
    }

    public Plata cautaPlataDupaId(int id) {
        return plataRepository.cautaDupaId(id);
    }

    public void actualizeazaPlata(Plata plata) {
        validatorService.valideazaPlata(plata);
        plataRepository.actualizeaza(plata);
    }

    public void stergePlata(int id) {
        plataRepository.sterge(id);
    }
    
}
