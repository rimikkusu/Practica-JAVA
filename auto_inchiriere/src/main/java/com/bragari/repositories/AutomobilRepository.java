package com.bragari.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bragari.database.DatabaseManager;
import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;

public class AutomobilRepository implements CrudRepository<Automobil> {

    @Override
    public void adauga(Automobil automobil) {
        String sql = """
                INSERT INTO automobile 
                (categorie_id, marca, model, numar_inmatriculare, pret_pe_zi, disponibil)
                VALUES (?, ?, ?, ?, ?, ?)
                RETURNING id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, automobil.getCategorie().getId());
            statement.setString(2, automobil.getMarca());
            statement.setString(3, automobil.getModel());
            statement.setString(4, automobil.getNumarInmatriculare());
            statement.setDouble(5, automobil.getPretPeZi());
            statement.setBoolean(6, automobil.isDisponibil());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    automobil.setId(resultSet.getInt("id"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la adăugarea automobilului: " + e.getMessage());
        }
    }

    @Override
    public List<Automobil> obtineToate() {
        List<Automobil> automobile = new ArrayList<>();

        String sql = """
                SELECT 
                    a.id AS automobil_id,
                    a.marca,
                    a.model,
                    a.numar_inmatriculare,
                    a.pret_pe_zi,
                    a.disponibil,
                    c.id AS categorie_id,
                    c.denumire,
                    c.descriere
                FROM automobile a
                JOIN categorii_auto c ON a.categorie_id = c.id
                ORDER BY a.id
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
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

                automobile.add(automobil);
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea automobilelor: " + e.getMessage());
        }

        return automobile;
    }

    @Override
    public Automobil cautaDupaId(int id) {
        String sql = """
                SELECT 
                    a.id AS automobil_id,
                    a.marca,
                    a.model,
                    a.numar_inmatriculare,
                    a.pret_pe_zi,
                    a.disponibil,
                    c.id AS categorie_id,
                    c.denumire,
                    c.descriere
                FROM automobile a
                JOIN categorii_auto c ON a.categorie_id = c.id
                WHERE a.id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    CategorieAuto categorie = new CategorieAuto(
                            resultSet.getInt("categorie_id"),
                            resultSet.getString("denumire"),
                            resultSet.getString("descriere")
                    );

                    return new Automobil(
                            resultSet.getInt("automobil_id"),
                            categorie,
                            resultSet.getString("marca"),
                            resultSet.getString("model"),
                            resultSet.getString("numar_inmatriculare"),
                            resultSet.getDouble("pret_pe_zi"),
                            resultSet.getBoolean("disponibil")
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la căutarea automobilului: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void actualizeaza(Automobil automobil) {
        String sql = """
                UPDATE automobile
                SET categorie_id = ?, marca = ?, model = ?, numar_inmatriculare = ?, pret_pe_zi = ?, disponibil = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, automobil.getCategorie().getId());
            statement.setString(2, automobil.getMarca());
            statement.setString(3, automobil.getModel());
            statement.setString(4, automobil.getNumarInmatriculare());
            statement.setDouble(5, automobil.getPretPeZi());
            statement.setBoolean(6, automobil.isDisponibil());
            statement.setInt(7, automobil.getId());

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la actualizarea automobilului: " + e.getMessage());
        }
    }

    @Override
    public void sterge(int id) {
        String sql = "DELETE FROM automobile WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la ștergerea automobilului: " + e.getMessage());
        }
    }
}