package com.example.ceragen_2.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static DatabaseConfig instance;
    private Connection connection;
    private final Dotenv dotenv;

    private DatabaseConfig() {
        // Cargar variables de entorno desde .env
        dotenv = Dotenv.configure()
                .ignoreIfMissing()
                .load();
        logger.info("Configuración de base de datos inicializada");
    }

    public static DatabaseConfig getInstance() {
        if (instance == null) {
            synchronized (DatabaseConfig.class) {
                if (instance == null) {
                    instance = new DatabaseConfig();
                }
            }
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                String host = dotenv.get("MYSQLHOST");
                String port = dotenv.get("MYSQLPORT");
                String database = dotenv.get("MYSQLDATABASE");
                String user = dotenv.get("MYSQLUSER");
                String password = dotenv.get("MYSQLPASSWORD");

                String url = String.format("jdbc:mysql://%s:%s/%s?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true",
                        host, port, database);

                connection = DriverManager.getConnection(url, user, password);
                logger.info("Conexión a la base de datos establecida exitosamente");
            } catch (ClassNotFoundException e) {
                logger.error("Driver de MySQL no encontrado", e);
                throw new SQLException("Driver de MySQL no encontrado", e);
            } catch (SQLException e) {
                logger.error("Error al conectar con la base de datos", e);
                throw e;
            }
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.info("Conexión a la base de datos cerrada");
            } catch (SQLException e) {
                logger.error("Error al cerrar la conexión", e);
            }
        }
    }
}