package com.bragari.models;

public class Client extends BaseEntity {
    private String nume;
    private String telefon;
    private String email;

    public Client() {
    }

    public Client(int id, String nume, String telefon, String email) {
        super(id);
        this.nume = nume;
        this.telefon = telefon;
        this.email = email;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return getId() + " | " + nume + " | " + telefon + " | " + email;
    }
}