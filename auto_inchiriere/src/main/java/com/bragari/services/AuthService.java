package com.bragari.services;

import java.util.List;

import com.bragari.models.Utilizator;
import com.bragari.repositories.UtilizatorRepository;
import com.bragari.util.PasswordUtil;

public class AuthService {

    private final UtilizatorRepository utilizatorRepository;

    public AuthService() {
        this.utilizatorRepository = new UtilizatorRepository();
    }

    public Utilizator login(String username, String parola) {
        if (esteGol(username) || esteGol(parola)) {
            return null;
        }

        Utilizator utilizator = utilizatorRepository.cautaDupaUsername(username.trim());

        if (utilizator == null) {
            return null;
        }

        if (!PasswordUtil.verifyPassword(parola, utilizator.getPasswordHash())) {
            return null;
        }

        return utilizator;
    }

    public void creeazaUtilizator(String username, String parola, String rol) {
        if (esteGol(username)) {
            throw new IllegalArgumentException("Username este obligatoriu.");
        }

        if (esteGol(parola)) {
            throw new IllegalArgumentException("Parola este obligatorie.");
        }

        String rolFinal = normalizeazaRol(rol);
        String passwordHash = PasswordUtil.hashPassword(parola);

        Utilizator utilizator = new Utilizator(0, username.trim(), passwordHash, rolFinal);
        utilizatorRepository.adauga(utilizator);
    }

    public void schimbaParola(int userId, String parolaNoua) {
        if (esteGol(parolaNoua)) {
            throw new IllegalArgumentException("Parola noua este obligatorie.");
        }

        Utilizator utilizator = utilizatorRepository.cautaDupaId(userId);

        if (utilizator == null) {
            throw new IllegalArgumentException("Utilizatorul nu exista.");
        }

        utilizator.setPasswordHash(PasswordUtil.hashPassword(parolaNoua));
        utilizatorRepository.actualizeaza(utilizator);
    }

    public void stergeUtilizator(int userId) {
        Utilizator utilizator = utilizatorRepository.cautaDupaId(userId);

        if (utilizator == null) {
            throw new IllegalArgumentException("Utilizatorul nu exista.");
        }

        if ("ADMIN".equalsIgnoreCase(utilizator.getRol()) && numaraAdmini() <= 1) {
            throw new IllegalArgumentException("Nu poti sterge ultimul utilizator ADMIN.");
        }

        utilizatorRepository.sterge(userId);
    }

    public List<Utilizator> obtineUtilizatori() {
        return utilizatorRepository.obtineToti();
    }

    public void creeazaAdminImplicitDacaNuExista() {
        if (!utilizatorRepository.existaUtilizatori()) {
            creeazaUtilizator("admin", "admin123", "ADMIN");
        }
    }

    private boolean esteGol(String text) {
        return text == null || text.isBlank();
    }

    private String normalizeazaRol(String rol) {
        String rolFinal = esteGol(rol) ? "USER" : rol.trim().toUpperCase();

        if (!"ADMIN".equals(rolFinal) && !"USER".equals(rolFinal)) {
            throw new IllegalArgumentException("Rolul trebuie sa fie ADMIN sau USER.");
        }

        return rolFinal;
    }

    private int numaraAdmini() {
        int count = 0;

        for (Utilizator utilizator : utilizatorRepository.obtineToti()) {
            if ("ADMIN".equalsIgnoreCase(utilizator.getRol())) {
                count++;
            }
        }

        return count;
    }
}
