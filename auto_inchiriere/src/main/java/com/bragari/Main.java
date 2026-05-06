package com.bragari;

import java.sql.Connection;

import com.bragari.database.DatabaseManager;

public class Main {
    public static void main(String[] args) {
        try (Connection connection = DatabaseManager.getConnection()) {
            System.out.println("Conexiune la baza de date realizata cu succes!");
        } catch (Exception e) {
            System.err.println("Eroare la conectarea la baza de date: " + e.getMessage());
        }
    }
}