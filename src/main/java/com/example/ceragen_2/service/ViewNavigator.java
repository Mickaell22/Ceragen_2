package com.example.ceragen_2.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public final class ViewNavigator {
    private static final Logger LOGGER = LoggerFactory.getLogger(ViewNavigator.class);
    private static ViewNavigator instance;
    private Stage primaryStage;

    // Rutas de vistas
    public static final String loginView = "/com/example/ceragen_2/views/login.fxml";
    public static final String mainView = "/com/example/ceragen_2/views/main.fxml";

    private ViewNavigator() {}

    public static synchronized ViewNavigator getInstance() {
        if (instance == null) {
            instance = new ViewNavigator();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    public void navigateTo(String fxmlPath) {
        navigateTo(fxmlPath, null);
    }

    public void navigateTo(String fxmlPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (controller != null) {
                loader.setController(controller);
            }
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);

            LOGGER.info("Navegado a: {}", fxmlPath);
        } catch (IOException e) {
            LOGGER.error("Error al cargar vista: {}", fxmlPath, e);
            throw new IllegalStateException("No se pudo cargar la vista: " + fxmlPath, e);
        }
    }

    public void showLogin() {
        navigateTo(loginView);
        primaryStage.setMaximized(false);
        primaryStage.setWidth(400);
        primaryStage.setHeight(500);
        primaryStage.centerOnScreen();
    }

    public void showMainView() {
        navigateTo(mainView);
        primaryStage.setMaximized(true);
    }
}
