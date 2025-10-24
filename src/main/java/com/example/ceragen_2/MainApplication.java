package com.example.ceragen_2;

import com.example.ceragen_2.config.DatabaseConfig;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Iniciando aplicación Ceragen");
        primaryStage.setTitle("Ceragen");
        // TODO: Configurar la vista inicial
        primaryStage.show();
        logger.info("Aplicación iniciada correctamente");
    }

    @Override
    public void stop() {
        logger.info("Cerrando aplicación");
        // Cerrar conexiones de base de datos si es necesario
        DatabaseConfig.getInstance().closeConnection();
        logger.info("Aplicación cerrada");
    }
}