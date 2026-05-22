package com.bragari.models;

// Modelul acesta tine datele pentru un automobil din firma de inchirieri.
// Obiectele de tip Automobil ajung apoi in tabele, formulare si baza de date.

public class Automobil extends BaseEntity {
    private CategorieAuto categorie;
    private String marca;
    private String model;
    private String numarInmatriculare;
    private double pretPeZi;
    private boolean disponibil;

    public Automobil() {
    }

    public Automobil(int id, CategorieAuto categorie, String marca, String model, String numarInmatriculare, double pretPeZi, boolean disponibil) {
        super(id);
        this.categorie = categorie;
        this.marca = marca;
        this.model = model;
        this.numarInmatriculare = numarInmatriculare;
        this.pretPeZi = pretPeZi;
        this.disponibil = disponibil;
    }

    public CategorieAuto getCategorie() {
        return categorie;
    }

    public void setCategorie(CategorieAuto categorie) {
        this.categorie = categorie;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getNumarInmatriculare() {
        return numarInmatriculare;
    }

    public void setNumarInmatriculare(String numarInmatriculare) {
        this.numarInmatriculare = numarInmatriculare;
    }

    public double getPretPeZi() {
        return pretPeZi;
    }

    public void setPretPeZi(double pretPeZi) {
        this.pretPeZi = pretPeZi;
    }

    public boolean isDisponibil() {
        return disponibil;
    }

    public void setDisponibil(boolean disponibil) {
        this.disponibil = disponibil;
    }

    @Override
    public String toString() {
        return getId() + " | " + marca + " | " + model + " | " + numarInmatriculare + " | " + pretPeZi + " lei/zi | " + (disponibil ? "Disponibil" : "Indisponibil");
    }
    
}
