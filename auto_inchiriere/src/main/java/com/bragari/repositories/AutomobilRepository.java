package com.bragari.repositories;

// Repository-ul pentru automobile face operatiile cu tabela automobile.
// Aici sunt query-urile pentru adaugare, modificare, stergere si cautare masini.

import java.util.List;

import com.bragari.database.SqlHelper;
import com.bragari.models.Automobil;
import com.bragari.models.CategorieAuto;

public class AutomobilRepository implements CrudRepository<Automobil> {

    private static final String SELECT = """
            SELECT a.id AS automobil_id, a.marca, a.model, a.numar_inmatriculare,
                   a.pret_pe_zi, a.disponibil,
                   c.id AS categorie_id, c.denumire, c.descriere
            FROM automobile a
            JOIN categorii_auto c ON a.categorie_id = c.id
            """;

    private Automobil map(java.sql.ResultSet rs) throws java.sql.SQLException {
        CategorieAuto categorie = new CategorieAuto(
                rs.getInt("categorie_id"), rs.getString("denumire"), rs.getString("descriere"));
        return new Automobil(rs.getInt("automobil_id"), categorie,
                rs.getString("marca"), rs.getString("model"),
                rs.getString("numar_inmatriculare"), rs.getDouble("pret_pe_zi"), rs.getBoolean("disponibil"));
    }

    @Override
    public void adauga(Automobil automobil) {
        try {
            int id = SqlHelper.insertReturningId(
                    "INSERT INTO automobile (categorie_id, marca, model, numar_inmatriculare, pret_pe_zi, disponibil) VALUES (?, ?, ?, ?, ?, ?) RETURNING id",
                    ps -> { ps.setInt(1, automobil.getCategorie().getId()); ps.setString(2, automobil.getMarca());
                            ps.setString(3, automobil.getModel()); ps.setString(4, automobil.getNumarInmatriculare());
                            ps.setDouble(5, automobil.getPretPeZi()); ps.setBoolean(6, automobil.isDisponibil()); }
            );
            automobil.setId(id);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("duplicate key"))
                throw new RuntimeException("Există deja un automobil cu acest număr de înmatriculare.");
            throw e;
        }
    }

    @Override
    public List<Automobil> obtineToate() {
        return SqlHelper.queryList(SELECT + " ORDER BY a.id", null, this::map);
    }

    @Override
    public Automobil cautaDupaId(int id) {
        return SqlHelper.queryOne(SELECT + " WHERE a.id = ?", ps -> ps.setInt(1, id), this::map).orElse(null);
    }

    @Override
    public void actualizeaza(Automobil automobil) {
        SqlHelper.update(
                "UPDATE automobile SET categorie_id=?, marca=?, model=?, numar_inmatriculare=?, pret_pe_zi=?, disponibil=? WHERE id=?",
                ps -> { ps.setInt(1, automobil.getCategorie().getId()); ps.setString(2, automobil.getMarca());
                        ps.setString(3, automobil.getModel()); ps.setString(4, automobil.getNumarInmatriculare());
                        ps.setDouble(5, automobil.getPretPeZi()); ps.setBoolean(6, automobil.isDisponibil());
                        ps.setInt(7, automobil.getId()); }
        );
    }

    @Override
    public void sterge(int id) {
        SqlHelper.update("DELETE FROM automobile WHERE id = ?", ps -> ps.setInt(1, id));
    }

    public List<Automobil> obtineDisponibile() {
        return SqlHelper.queryList(SELECT + " WHERE a.disponibil = true ORDER BY a.id", null, this::map);
    }
}
