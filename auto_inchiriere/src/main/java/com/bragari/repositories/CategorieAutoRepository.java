package com.bragari.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bragari.database.DatabaseManager;
import com.bragari.models.CategorieAuto;

public class CategorieAutoRepository implements CrudRepository<CategorieAuto> {

    @Override
    public void adauga(CategorieAuto categorie) {
        String sql = "INSERT INTO categorii_auto (denumire, descriere) VALUES (?, ?) RETURNING id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, categorie.getDenumire());
            statement.setString(2, categorie.getDescriere());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    categorie.setId(resultSet.getInt("id"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la adăugarea categoriei: " + e.getMessage());
        }
    }

    @Override
    public List<CategorieAuto> obtineToate() {
        List<CategorieAuto> categorii = new ArrayList<>();
        String sql = "SELECT id, denumire, descriere FROM categorii_auto ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                CategorieAuto categorie = new CategorieAuto(
                        resultSet.getInt("id"),
                        resultSet.getString("denumire"),
                        resultSet.getString("descriere")
                );

                categorii.add(categorie);
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea categoriilor: " + e.getMessage());
        }

        return categorii;
    }

    @Override
    public CategorieAuto cautaDupaId(int id) {
        String sql = "SELECT id, denumire, descriere FROM categorii_auto WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new CategorieAuto(
                            resultSet.getInt("id"),
                            resultSet.getString("denumire"),
                            resultSet.getString("descriere")
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la căutarea categoriei: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void actualizeaza(CategorieAuto categorie) {
        String sql = "UPDATE categorii_auto SET denumire = ?, descriere = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, categorie.getDenumire());
            statement.setString(2, categorie.getDescriere());
            statement.setInt(3, categorie.getId());

            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la actualizarea categoriei: " + e.getMessage());
        }
    }

    @Override
    public void sterge(int id) {
        String sql = "DELETE FROM categorii_auto WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            statement.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Eroare la ștergerea categoriei: " + e.getMessage());
        }
    }
}