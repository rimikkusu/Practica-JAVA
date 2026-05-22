package com.bragari.models;

// Modelul Utilizator tine datele pentru conturile din aplicatie.
// Rolul din acest model decide ce optiuni poate vedea utilizatorul.

public class Utilizator extends BaseEntity {
    private String username;
    private String passwordHash;
    private String rol;

    public Utilizator() {
    }

    public Utilizator(int id, String username, String passwordHash, String rol) {
        super(id);
        this.username = username;
        this.passwordHash = passwordHash;
        this.rol = rol;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    @Override
    public String toString() {
        return username + " (" + rol + ")";
    }
}
