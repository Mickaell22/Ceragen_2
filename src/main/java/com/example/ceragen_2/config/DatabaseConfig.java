package com.example.ceragen_2.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private Connection connection;
    private final Dotenv dotenv;

    private DatabaseConfig() {
        // Cargar variables de entorno desde .env
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        LOGGER.info("Configuración de base de datos inicializada");
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                final String host = dotenv.get("MYSQLHOST");
                final String port = dotenv.get("MYSQLPORT");
                final String database = dotenv.get("MYSQLDATABASE");
                final String user = dotenv.get("MYSQLUSER");
                final String password = dotenv.get("MYSQLPASSWORD");

                final String url = String.format(
                        "jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&useUnicode=true&characterEncoding=utf8&connectionCollation=utf8mb4_unicode_ci",
                        host, port, database);

                connection = DriverManager.getConnection(url, user, password);
                LOGGER.info("Conexión a la base de datos establecida exitosamente");
            } catch (ClassNotFoundException e) {
                LOGGER.error("Driver de MySQL no encontrado", e);
                throw new SQLException("Driver de MySQL no encontrado", e);
            } catch (SQLException e) {
                LOGGER.error("Error al conectar con la base de datos", e);
                throw e;
            }
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                LOGGER.info("Conexión a la base de datos cerrada");
            } catch (SQLException e) {
                LOGGER.error("Error al cerrar la conexión", e);
            }
        }
    }
}