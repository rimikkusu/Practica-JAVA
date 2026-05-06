package com.bragari.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://ep-withered-wave-alhl392p.c-3.eu-central-1.aws.neon.tech/auto_inchirieri_db?user=neondb_owner&password=npg_6hL8FpIaTWmN&sslmode=require&channelBinding=require";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}