package com.bragari;

import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.repositories.CategorieAutoRepository;
import com.bragari.repositories.ClientRepository;

public class Main {
    public static void main(String[] args) {
        ClientRepository clientRepository = new ClientRepository();
        CategorieAutoRepository categorieRepository = new CategorieAutoRepository();

        try {
            Client client = new Client(
                    0,
                    "Ion Popescu",
                    "069123456",
                    "ion@gmail.com"
            );

            clientRepository.adauga(client);

            System.out.println("Client adaugat cu ID: " + client.getId());

        } catch (Exception e) {
            System.out.println("Clientul nu a fost adaugat: " + e.getMessage());
        }

        System.out.println("=== Lista clientilor ===");
        for (Client c : clientRepository.obtineToate()) {
            System.out.println(c);
        }

        try {
            CategorieAuto categorie = new CategorieAuto(
                    0,
                    "SUV",
                    "Automobile mari și confortabile"
            );

            categorieRepository.adauga(categorie);

            System.out.println("Categorie adăugată cu ID: " + categorie.getId());

        } catch (Exception e) {
            System.out.println("Categoria nu a fost adăugată: " + e.getMessage());
        }

        System.out.println("=== Categorii auto ===");
        for (CategorieAuto c : categorieRepository.obtineToate()) {
            System.out.println(c);
        }
    }
}