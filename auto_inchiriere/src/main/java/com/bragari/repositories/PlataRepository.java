package com.bragari.repositories;

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
import com.bragari.models.Plata;
import com.bragari.models.StatusInchiriere;

public class PlataRepository implements CrudRepository<Plata> {

    @Override
    public void adauga(Plata plata) {
        String sql = """
                INSERT INTO plati
                (inchiriere_id, suma, metoda_plata, data_plata)
                VALUES (?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, plata.getInchiriere().getId());
            statement.setDouble(2, plata.getSuma());
            statement.setString(3, plata.getMetodaPlata());
            statement.setDate(4, Date.valueOf(plata.getDataPlata()));

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    plata.setId(resultSet.getInt("id"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la adăugarea plății: " + e.getMessage());
        }
    }

    @Override
    public List<Plata> obtineToate() {
        List<Plata> plati = new ArrayList<>();

        String sql = """
                SELECT
                    p.id AS plata_id,
                    p.suma,
                    p.metoda_plata,
                    p.data_plata,

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
                FROM plati p
                JOIN inchirieri i ON p.inchiriere_id = i.id
                JOIN clienti cl ON i.client_id = cl.id
                JOIN automobile a ON i.automobil_id = a.id
                JOIN categorii_auto c ON a.categorie_id = c.id
                ORDER BY p.id
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

                Plata plata = new Plata(
                        resultSet.getInt("plata_id"),
                        inchiriere,
                        resultSet.getDouble("suma"),
                        resultSet.getString("metoda_plata"),
                        resultSet.getDate("data_plata").toLocalDate()
                );

                plati.add(plata);
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea plăților: " + e.getMessage());
        }

        return plati;
    }

    @Override
    public Plata cautaDupaId(int id) {
        return obtineToate().stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    @Override
    public void actualizeaza(Plata plata) {
        String sql = """
                UPDATE plati
                SET inchiriere_id = ?, suma = ?, metoda_plata = ?, data_plata = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, plata.getInchiriere().getId());
            statement.setDouble(2, plata.getSuma());
            statement.setString(3, plata.getMetodaPlata());
            statement.setDate(4, Date.valueOf(plata.getDataPlata()));
            statement.setInt(5, plata.getId());

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la actualizarea plății: " + e.getMessage());
        }
    }

    @Override
    public void sterge(int id) {
        String sql = "DELETE FROM plati WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la ștergerea plății: " + e.getMessage());
        }
    }
}
