package com.example.ceragen_2.service;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class ViewNavigator {
    private static final Logger logger = LoggerFactory.getLogger(ViewNavigator.class);
    private static ViewNavigator instance;
    private Stage primaryStage;

    // Rutas de vistas
    public static final String LOGIN_VIEW = "/com/example/ceragen_2/views/login.fxml";
    public static final String MAIN_VIEW = "/com/example/ceragen_2/views/main.fxml";

    private ViewNavigator() {}

    public static ViewNavigator getInstance() {
        if (instance == null) {
            synchronized (ViewNavigator.class) {
                if (instance == null) {
                    instance = new ViewNavigator();
                }
            }
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

            logger.info("Navegado a: {}", fxmlPath);
        } catch (IOException e) {
            logger.error("Error al cargar vista: {}", fxmlPath, e);
            throw new RuntimeException("No se pudo cargar la vista: " + fxmlPath, e);
        }
    }

    public void showLogin() {
        navigateTo(LOGIN_VIEW);
        primaryStage.setMaximized(false);
        primaryStage.setWidth(400);
        primaryStage.setHeight(500);
        primaryStage.centerOnScreen();
    }

    public void showMainView() {
        navigateTo(MAIN_VIEW);
        primaryStage.setMaximized(true);
    }
}
