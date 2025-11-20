package com.example.ceragen_2;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.service.ViewNavigator;
import javafx.application.Application;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainApplication extends Application {
    private static final Logger logger = LoggerFactory.getLogger(MainApplication.class);

    @Override
    public void start(final Stage primaryStage) {
        logger.info("Iniciando aplicación Ceragen");

        primaryStage.setTitle("Ceragen - Sistema de Gestión Médica");

        final ViewNavigator navigator = ViewNavigator.getInstance();
        navigator.setPrimaryStage(primaryStage);
        navigator.showLogin();

        primaryStage.show();
        logger.info("Aplicación iniciada correctamente");
    }

    @Override
    public void stop() {
        logger.info("Cerrando aplicación");
        DatabaseConfig.getInstance().closeConnection();
        logger.info("Aplicación cerrada");
    }
}