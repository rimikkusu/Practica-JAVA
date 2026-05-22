package com.bragari;

// TestRunner este o clasa simpla pentru verificari rapide in consola.
// O folosim cand vrem sa testam logica fara sa pornim toata interfata JavaFX.

import java.time.LocalDate;
import java.util.List;

import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.Plata;
import com.bragari.models.StatusInchiriere;
import com.bragari.services.AutoInchiriereService;

public class TestRunner {
    private final AutoInchiriereService service = new AutoInchiriereService();

    public static void runAll() {
        TestRunner testRunner = new TestRunner();

        System.out.println("=== TESTE APLICATIE AUTO INCHIRIERE ===");

        testRunner.testAdaugareDateComplete();
        testRunner.testCautareClientDupaNume();
        testRunner.testFiltrareAutomobileDisponibile();
        testRunner.testUpdateDeleteClient();
        testRunner.testValidareClientInvalid();

        System.out.println("=== TESTE FINALIZATE ===");
    }

    private void testAdaugareDateComplete() {
        System.out.println();
        System.out.println("1. Test adaugare client, categorie, automobil, inchiriere si plata");

        Client client = obtineSauCreeazaClient();
        CategorieAuto categorie = obtineSauCreeazaCategorie();

        String numarAuto = "TEST" + System.currentTimeMillis();

        Automobil automobil = new Automobil(
                0,
                categorie,
                "Dacia",
                "Duster",
                numarAuto,
                450,
                true
        );

        service.adaugaAutomobil(automobil);

        Inchiriere inchiriere = new Inchiriere(
                0,
                client,
                automobil,
                LocalDate.of(2026, 5, 10),
                LocalDate.of(2026, 5, 15),
                StatusInchiriere.ACTIVA
        );

        service.adaugaInchiriere(inchiriere);

        Plata plata = new Plata(
                0,
                inchiriere,
                inchiriere.calculeazaTotal(),
                "CARD",
                LocalDate.now()
        );

        service.adaugaPlata(plata);

        System.out.println("OK - Datele au fost adaugate corect.");
        System.out.println("Automobil ID: " + automobil.getId());
        System.out.println("Inchiriere ID: " + inchiriere.getId());
        System.out.println("Plata ID: " + plata.getId());
    }

    private void testCautareClientDupaNume() {
        System.out.println();
        System.out.println("2. Test cautare client dupa nume");

        List<Client> rezultate = service.cautaClientiDupaNume("Ion");

        for (Client client : rezultate) {
            System.out.println(client);
        }

        System.out.println("OK - Cautarea a returnat " + rezultate.size() + " rezultat(e).");
    }

    private void testFiltrareAutomobileDisponibile() {
        System.out.println();
        System.out.println("3. Test filtrare automobile disponibile");

        List<Automobil> automobileDisponibile = service.obtineAutomobileDisponibile();

        for (Automobil automobil : automobileDisponibile) {
            System.out.println(automobil);
        }

        System.out.println("OK - Filtrarea a returnat " + automobileDisponibile.size() + " automobil(e).");
    }

    private void testUpdateDeleteClient() {
        System.out.println();
        System.out.println("4. Test update si delete client");

        Client clientTest = new Client(
                0,
                "Client Test",
                "060000000",
                "test" + System.currentTimeMillis() + "@gmail.com"
        );

        service.adaugaClient(clientTest);
        System.out.println("Client test adaugat: " + clientTest);

        clientTest.setNume("Client Test Editat");
        clientTest.setTelefon("061111111");

        service.actualizeazaClient(clientTest);
        System.out.println("Client test actualizat: " + service.cautaClientDupaId(clientTest.getId()));

        service.stergeClient(clientTest.getId());

        Client clientSters = service.cautaClientDupaId(clientTest.getId());

        if (clientSters == null) {
            System.out.println("OK - Stergerea a functionat corect.");
        } else {
            System.out.println("EROARE - Clientul inca exista: " + clientSters);
        }
    }

    private void testValidareClientInvalid() {
        System.out.println();
        System.out.println("5. Test validare client invalid");

        try {
            Client clientInvalid = new Client(
                    0,
                    "",
                    "",
                    "emailGresit"
            );

            service.adaugaClient(clientInvalid);

            System.out.println("EROARE - Clientul invalid a fost adaugat.");
        } catch (Exception e) {
            System.out.println("OK - Validarea a blocat clientul invalid.");
            System.out.println("Mesaj: " + e.getMessage());
        }
    }

    private Client obtineSauCreeazaClient() {
        List<Client> clienti = service.obtineClienti();

        if (!clienti.isEmpty()) {
            return clienti.get(0);
        }

        Client client = new Client(
                0,
                "Ion Popescu",
                "069123456",
                "ion@gmail.com"
        );

        service.adaugaClient(client);

        return client;
    }

    private CategorieAuto obtineSauCreeazaCategorie() {
        List<CategorieAuto> categorii = service.obtineCategorii();

        if (!categorii.isEmpty()) {
            return categorii.get(0);
        }

        CategorieAuto categorie = new CategorieAuto(
                0,
                "SUV",
                "Automobile mari si confortabile"
        );

        service.adaugaCategorie(categorie);

        return categorie;
    }
}
