package com.bragari.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bragari.database.DatabaseManager;
import com.bragari.models.Utilizator;

public class UtilizatorRepository {

    public void adauga(Utilizator utilizator) {
        String sql = "INSERT INTO utilizatori (username, password_hash, rol) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, utilizator.getUsername());
            statement.setString(2, utilizator.getPasswordHash());
            statement.setString(3, utilizator.getRol());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    utilizator.setId(resultSet.getInt("id"));
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la adaugarea utilizatorului: " + e.getMessage());
        }
    }

    public List<Utilizator> obtineToti() {
        List<Utilizator> utilizatori = new ArrayList<>();
        String sql = "SELECT id, username, password_hash, rol FROM utilizatori ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                utilizatori.add(creeazaUtilizatorDinResultSet(resultSet));
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea utilizatorilor: " + e.getMessage());
        }

        return utilizatori;
    }

    public Utilizator cautaDupaUsername(String username) {
        String sql = "SELECT id, username, password_hash, rol FROM utilizatori WHERE username = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return creeazaUtilizatorDinResultSet(resultSet);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la cautarea utilizatorului: " + e.getMessage());
        }

        return null;
    }

    public Utilizator cautaDupaId(int id) {
        String sql = "SELECT id, username, password_hash, rol FROM utilizatori WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return creeazaUtilizatorDinResultSet(resultSet);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la cautarea utilizatorului: " + e.getMessage());
        }

        return null;
    }

    public void actualizeaza(Utilizator utilizator) {
        String sql = "UPDATE utilizatori SET username = ?, password_hash = ?, rol = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, utilizator.getUsername());
            statement.setString(2, utilizator.getPasswordHash());
            statement.setString(3, utilizator.getRol());
            statement.setInt(4, utilizator.getId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Utilizatorul nu exista.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la actualizarea utilizatorului: " + e.getMessage());
        }
    }

    public void sterge(int id) {
        String sql = "DELETE FROM utilizatori WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Utilizatorul nu exista.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la stergerea utilizatorului: " + e.getMessage());
        }
    }

    public boolean existaUtilizatori() {
        String sql = "SELECT EXISTS (SELECT 1 FROM utilizatori)";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getBoolean(1);
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la verificarea utilizatorilor: " + e.getMessage());
        }

        return false;
    }

    private Utilizator creeazaUtilizatorDinResultSet(ResultSet resultSet) throws Exception {
        return new Utilizator(
                resultSet.getInt("id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("rol")
        );
    }
}
