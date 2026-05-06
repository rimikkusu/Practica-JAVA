package com.bragari.models;

public class CategorieAuto extends BaseEntity {
    private String denumire;
    private String descriere;
    
    public CategorieAuto() {
    }

    public CategorieAuto(int id, String denumire, String descriere) {
        super(id);
        this.denumire = denumire;
        this.descriere = descriere;
    }
    
    public String getDenumire() {
        return denumire;
    }

    public void setDenumire(String denumire) {
        this.denumire = denumire;
    }

    public String getDescriere() {
        return descriere;
    }

    public void setDescriere(String descriere) {
        this.descriere = descriere;
    }

    @Override
    public String toString() {
        return getId() + " | " + denumire + " | " + descriere;
    }
}
