package com.bragari.repositories;

// Repository-ul de inchirieri lucreaza cu tabela principala pentru contracte.
// Aici se fac si legaturile dintre client, automobil, status si datele perioadei.

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bragari.database.DatabaseManager;
import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;
import com.bragari.models.Client;
import com.bragari.models.Inchiriere;
import com.bragari.models.StatusInchiriere;

public class InchiriereRepository implements CrudRepository<Inchiriere> {

    @Override
    public void adauga(Inchiriere inchiriere) {
        String sql = """
                INSERT INTO inchirieri
                (client_id, automobil_id, data_inceput, data_sfarsit, status)
                VALUES (?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, inchiriere.getClient().getId());
            statement.setInt(2, inchiriere.getAutomobil().getId());
            statement.setDate(3, Date.valueOf(inchiriere.getDataInceput()));
            statement.setDate(4, Date.valueOf(inchiriere.getDataSfarsit()));
            statement.setString(5, inchiriere.getStatus().name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    inchiriere.setId(resultSet.getInt("id"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la adăugarea inchirierii: " + e.getMessage());
        }
    }

    @Override
    public List<Inchiriere> obtineToate() {
        List<Inchiriere> inchirieri = new ArrayList<>();

        String sql = """
                SELECT
                    i.id AS inchiriere_id,
                    i.data_inceput,
                    i.data_sfarsit,
                    i.status,

                    cl.id AS client_id,
                    cl.nume,
                    cl.telefon,
                    cl.email,

                    a.id AS automobil_id,
                    a.marca,
                    a.model,
                    a.numar_inmatriculare,
                    a.pret_pe_zi,
                    a.disponibil,

                    c.id AS categorie_id,
                    c.denumire,
                    c.descriere
                FROM inchirieri i
                JOIN clienti cl ON i.client_id = cl.id
                JOIN automobile a ON i.automobil_id = a.id
                JOIN categorii_auto c ON a.categorie_id = c.id
                ORDER BY i.id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Client client = new Client(
                        resultSet.getInt("client_id"),
                        resultSet.getString("nume"),
                        resultSet.getString("telefon"),
                        resultSet.getString("email")
                );

                CategorieAuto categorie = new CategorieAuto(
                        resultSet.getInt("categorie_id"),
                        resultSet.getString("denumire"),
                        resultSet.getString("descriere")
                );

                Automobil automobil = new Automobil(
                        resultSet.getInt("automobil_id"),
                        categorie,
                        resultSet.getString("marca"),
                        resultSet.getString("model"),
                        resultSet.getString("numar_inmatriculare"),
                        resultSet.getDouble("pret_pe_zi"),
                        resultSet.getBoolean("disponibil")
                );

                Inchiriere inchiriere = new Inchiriere(
                        resultSet.getInt("inchiriere_id"),
                        client,
                        automobil,
                        resultSet.getDate("data_inceput").toLocalDate(),
                        resultSet.getDate("data_sfarsit").toLocalDate(),
                        StatusInchiriere.valueOf(resultSet.getString("status"))
                );

                inchirieri.add(inchiriere);
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea inchirierilor: " + e.getMessage());
        }

        return inchirieri;
    }

    @Override
    public Inchiriere cautaDupaId(int id) {
        return obtineToate().stream().filter(i -> i.getId() == id).findFirst().orElse(null);
    }

    @Override
    public void actualizeaza(Inchiriere inchiriere) {
        String sql = """
                UPDATE inchirieri
                SET client_id = ?, automobil_id = ?, data_inceput = ?, data_sfarsit = ?, status = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, inchiriere.getClient().getId());
            statement.setInt(2, inchiriere.getAutomobil().getId());
            statement.setDate(3, Date.valueOf(inchiriere.getDataInceput()));
            statement.setDate(4, Date.valueOf(inchiriere.getDataSfarsit()));
            statement.setString(5, inchiriere.getStatus().name());
            statement.setInt(6, inchiriere.getId());

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la actualizarea inchirierii: " + e.getMessage());
        }
    }

    @Override
    public void sterge(int id) {
        String sql = "DELETE FROM inchirieri WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la stergerea inchirierii: " + e.getMessage());
        }
    }
}
