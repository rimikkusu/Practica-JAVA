package com.bragari.repositories;

import java.util.List;

import com.bragari.database.SqlHelper;
import com.bragari.models.CategorieAuto;

public class CategorieAutoRepository implements CrudRepository<CategorieAuto> {

    private static final String SELECT = "SELECT id, denumire, descriere FROM categorii_auto";

    private CategorieAuto map(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new CategorieAuto(rs.getInt("id"), rs.getString("denumire"), rs.getString("descriere"));
    }

    @Override
    public void adauga(CategorieAuto categorie) {
        int id = SqlHelper.insertReturningId(
                "INSERT INTO categorii_auto (denumire, descriere) VALUES (?, ?) RETURNING id",
                ps -> { ps.setString(1, categorie.getDenumire()); ps.setString(2, categorie.getDescriere()); }
        );
        categorie.setId(id);
    }

    @Override
    public List<CategorieAuto> obtineToate() {
        return SqlHelper.queryList(SELECT + " ORDER BY id", null, this::map);
    }

    @Override
    public CategorieAuto cautaDupaId(int id) {
        return SqlHelper.queryOne(SELECT + " WHERE id = ?", ps -> ps.setInt(1, id), this::map).orElse(null);
    }

    @Override
    public void actualizeaza(CategorieAuto categorie) {
        SqlHelper.update(
                "UPDATE categorii_auto SET denumire = ?, descriere = ? WHERE id = ?",
                ps -> { ps.setString(1, categorie.getDenumire()); ps.setString(2, categorie.getDescriere());
                        ps.setInt(3, categorie.getId()); }
        );
    }

    @Override
    public void sterge(int id) {
        SqlHelper.update("DELETE FROM categorii_auto WHERE id = ?", ps -> ps.setInt(1, id));
    }
}
