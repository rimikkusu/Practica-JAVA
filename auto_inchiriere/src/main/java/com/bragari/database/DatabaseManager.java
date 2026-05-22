package com.bragari.database;

// Clasa asta se ocupa de conexiunea la baza de date, ca restul proiectului
// sa nu trebuiasca sa scrie mereu aceleasi setari pentru PostgreSQL.

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseManager {

    private static final String URL = "jdbc:postgresql://ep-withered-wave-alhl392p.c-3.eu-central-1.aws.neon.tech/neondb?user=neondb_owner&password=npg_6hL8FpIaTWmN&sslmode=require&channelBinding=require";
    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(URL);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(1);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(60000);
        config.setMaxLifetime(300000);
        config.setPoolName("AutoInchirierePool");

        dataSource = new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void closePool() {
        if (!dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
