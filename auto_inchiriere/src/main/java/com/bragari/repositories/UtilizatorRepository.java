package com.bragari.repositories;

// Repository-ul pentru utilizatori lucreaza cu tabela de conturi.
// Aici se cauta utilizatorii dupa username si se salveaza parolele criptate.

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.bragari.database.SqlHelper;
import com.bragari.models.Utilizator;

public class UtilizatorRepository {

    private static final String SELECT = "SELECT id, username, password_hash, rol FROM utilizatori";

    private Utilizator map(ResultSet rs) throws SQLException {
        return new Utilizator(rs.getInt("id"), rs.getString("username"),
                rs.getString("password_hash"), rs.getString("rol"));
    }

    public void adauga(Utilizator utilizator) {
        int id = SqlHelper.insertReturningId(
                "INSERT INTO utilizatori (username, password_hash, rol) VALUES (?, ?, ?) RETURNING id",
                ps -> { ps.setString(1, utilizator.getUsername()); ps.setString(2, utilizator.getPasswordHash());
                        ps.setString(3, utilizator.getRol()); }
        );
        utilizator.setId(id);
    }

    public List<Utilizator> obtineToti() {
        return SqlHelper.queryList(SELECT + " ORDER BY id", null, this::map);
    }

    public Utilizator cautaDupaUsername(String username) {
        return SqlHelper.queryOne(SELECT + " WHERE username = ?", ps -> ps.setString(1, username), this::map).orElse(null);
    }

    public Utilizator cautaDupaId(int id) {
        return SqlHelper.queryOne(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), this::map).orElse(null);
    }

    public void actualizeaza(Utilizator utilizator) {
        int rows = SqlHelper.update(
                "UPDATE utilizatori SET username=?, password_hash=?, rol=? WHERE id=?",
                ps -> { ps.setString(1, utilizator.getUsername()); ps.setString(2, utilizator.getPasswordHash());
                        ps.setString(3, utilizator.getRol()); ps.setInt(4, utilizator.getId()); }
        );
        if (rows == 0) throw new RuntimeException("Eroare la actualizarea utilizatorului: Utilizatorul nu exista.");
    }

    public void sterge(int id) {
        int rows = SqlHelper.update("DELETE FROM utilizatori WHERE id = ?", ps -> ps.setInt(1, id));
        if (rows == 0) throw new RuntimeException("Eroare la stergerea utilizatorului: Utilizatorul nu exista.");
    }

    public boolean existaUtilizatori() {
        return SqlHelper.queryOne("SELECT EXISTS (SELECT 1 FROM utilizatori)",
                null, rs -> rs.getBoolean(1)).orElse(false);
    }
}
