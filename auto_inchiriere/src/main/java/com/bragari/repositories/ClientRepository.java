package com.bragari.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.bragari.database.DatabaseManager;
import com.bragari.models.Client;

public class ClientRepository implements CrudRepository<Client> {

    @Override
    public void adauga(Client client) {
        String sql = "INSERT INTO clienti (nume, telefon, email) VALUES (?, ?, ?) RETURNING id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getNume());
            statement.setString(2, client.getTelefon());
            statement.setString(3, client.getEmail());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    client.setId(resultSet.getInt("id"));
                }
            }

        } catch (Exception e) {
    if (e.getMessage().contains("duplicate key")) {
        throw new RuntimeException("Există deja un client cu acest email.");
    }

    throw new RuntimeException("Eroare la adăugarea clientului: " + e.getMessage());
        }
    }

    @Override
    public List<Client> obtineToate() {
        List<Client> clienti = new ArrayList<>();
        String sql = "SELECT id, nume, telefon, email FROM clienti ORDER BY id";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Client client = new Client(
                        resultSet.getInt("id"),
                        resultSet.getString("nume"),
                        resultSet.getString("telefon"),
                        resultSet.getString("email")
                );

                clienti.add(client);
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la citirea clienților: " + e.getMessage());
        }

        return clienti;
    }

    @Override
    public Client cautaDupaId(int id) {
        String sql = "SELECT id, nume, telefon, email FROM clienti WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new Client(
                            resultSet.getInt("id"),
                            resultSet.getString("nume"),
                            resultSet.getString("telefon"),
                            resultSet.getString("email")
                    );
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la căutarea clientului: " + e.getMessage());
        }

        return null;
    }

    @Override
    public void actualizeaza(Client client) {
        String sql = "UPDATE clienti SET nume = ?, telefon = ?, email = ? WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, client.getNume());
            statement.setString(2, client.getTelefon());
            statement.setString(3, client.getEmail());
            statement.setInt(4, client.getId());

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Clientul nu există.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la actualizarea clientului: " + e.getMessage());
        }
    }

    @Override
    public void sterge(int id) {
        String sql = "DELETE FROM clienti WHERE id = ?";

        try (Connection connection = DatabaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("Clientul nu există.");
            }

        } catch (Exception e) {
            throw new RuntimeException("Eroare la ștergerea clientului: " + e.getMessage());
        }
    }

    public List<Client> cautaDupaNume(String numeCautat) {
    List<Client> clienti = new ArrayList<>();

    String sql = """
            SELECT id, nume, telefon, email
            FROM clienti
            WHERE LOWER(nume) LIKE LOWER(?)
            ORDER BY id
            """;

    try (Connection connection = DatabaseManager.getConnection();
         PreparedStatement statement = connection.prepareStatement(sql)) {

        statement.setString(1, "%" + numeCautat + "%");

        try (ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Client client = new Client(
                        resultSet.getInt("id"),
                        resultSet.getString("nume"),
                        resultSet.getString("telefon"),
                        resultSet.getString("email")
                );

                clienti.add(client);
            }
        }

    } catch (Exception e) {
        throw new RuntimeException("Eroare la cautarea clientilor: " + e.getMessage());
    }

    return clienti;
}
}