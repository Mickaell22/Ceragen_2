package com.example.ceragen_2.controller;

import com.example.ceragen_2.service.AuthService;
import com.example.ceragen_2.service.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainController {
    private static final Logger logger = LoggerFactory.getLogger(MainController.class);

    @FXML
    private Text userLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private Button btnUsuarios;

    @FXML
    private Button btnPacientes;

    @FXML
    private Button btnProfesionales;

    @FXML
    private Button btnEspecialidades;

    @FXML
    private Button btnCitas;

    @FXML
    private Button btnFacturacion;

    @FXML
    private Button btnLogout;

    @FXML
    public void initialize() {
        AuthService authService = AuthService.getInstance();
        String username = authService.getCurrentUsername();
        String role = authService.getCurrentUserRole();

        userLabel.setText(username + " (" + role + ")");

        setupHoverEffects();
    }

    private void setupHoverEffects() {
        addHoverEffect(btnUsuarios);
        addHoverEffect(btnPacientes);
        addHoverEffect(btnProfesionales);
        addHoverEffect(btnEspecialidades);
        addHoverEffect(btnCitas);
        addHoverEffect(btnFacturacion);
    }

    private void addHoverEffect(Button button) {
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-background-color: #34495e;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-background-color: #34495e;", "")));
    }

    @FXML
    private void navigateToUsuarios() {
        logger.info("Navegando a módulo Usuarios");
        // TODO: Cargar vista de usuarios en contentArea
    }

    @FXML
    private void navigateToPacientes() {
        logger.info("Navegando a módulo Pacientes");
        // TODO: Cargar vista de pacientes en contentArea
    }

    @FXML
    private void navigateToProfesionales() {
        logger.info("Navegando a módulo Profesionales");
        // TODO: Cargar vista de profesionales en contentArea
    }

    @FXML
    private void navigateToEspecialidades() {
        logger.info("Navegando a módulo Especialidades");
        // TODO: Cargar vista de especialidades en contentArea
    }

    @FXML
    private void navigateToCitas() {
        logger.info("Navegando a módulo Citas");
        // TODO: Cargar vista de citas en contentArea
    }

    @FXML
    private void navigateToFacturacion() {
        logger.info("Navegando a módulo Facturación");
        // TODO: Cargar vista de facturación en contentArea
    }

    @FXML
    private void handleLogout() {
        logger.info("Cerrando sesión");
        AuthService.getInstance().logout();
        ViewNavigator.getInstance().showLogin();
    }
}
