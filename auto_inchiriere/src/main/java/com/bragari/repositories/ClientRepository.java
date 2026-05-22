package com.bragari.repositories;

// Repository-ul pentru clienti contine codul care lucreaza direct cu tabela clienti.
// Service-ul il foloseste ca sa nu scrie SQL in partea de interfata.

import java.util.List;

import com.bragari.database.SqlHelper;
import com.bragari.models.Client;

public class ClientRepository implements CrudRepository<Client> {

    private static final String SELECT = "SELECT id, nume, telefon, email FROM clienti";

    private Client map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new Client(rs.getInt("id"), rs.getString("nume"),
                rs.getString("telefon"), rs.getString("email"));
    }

    @Override
    public void adauga(Client client) {
        try {
            int id = SqlHelper.insertReturningId(
                    "INSERT INTO clienti (nume, telefon, email) VALUES (?, ?, ?) RETURNING id",
                    ps -> { ps.setString(1, client.getNume()); ps.setString(2, client.getTelefon()); ps.setString(3, client.getEmail()); }
            );
            client.setId(id);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("duplicate key"))
                throw new RuntimeException("Există deja un client cu acest email.");
            throw e;
        }
    }

    @Override
    public List<Client> obtineToate() {
        return SqlHelper.queryList(SELECT + " ORDER BY id", null, this::map);
    }

    @Override
    public Client cautaDupaId(int id) {
        return SqlHelper.queryOne(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), this::map).orElse(null);
    }

    @Override
    public void actualizeaza(Client client) {
        int rows = SqlHelper.update(
                "UPDATE clienti SET nume = ?, telefon = ?, email = ? WHERE id = ?",
                ps -> { ps.setString(1, client.getNume()); ps.setString(2, client.getTelefon());
                        ps.setString(3, client.getEmail()); ps.setInt(4, client.getId()); }
        );
        if (rows == 0) throw new RuntimeException("Eroare la actualizarea clientului: Clientul nu există.");
    }

    @Override
    public void sterge(int id) {
        int count = SqlHelper.queryOne(
                "SELECT COUNT(*) FROM inchirieri WHERE client_id = ?",
                ps -> ps.setInt(1, id),
                rs -> rs.getInt(1)
        ).orElse(0);
        if (count > 0)
            throw new RuntimeException("Clientul nu poate fi sters deoarece are inchirieri inregistrate.");
        SqlHelper.update("DELETE FROM clienti WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public List<Client> cautaDupaNume(String numeCautat) {
        return SqlHelper.queryList(
                SELECT + " WHERE LOWER(nume) LIKE LOWER(?) ORDER BY id",
                ps -> ps.setString(1, "%" + numeCautat + "%"),
                this::map
        );
    }
}
