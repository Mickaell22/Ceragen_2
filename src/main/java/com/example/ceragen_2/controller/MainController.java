package com.example.ceragen_2.controller;

import com.example.ceragen_2.service.AuthService;
import com.example.ceragen_2.service.ViewNavigator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
    private Button btnClientes;

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
    private Button btnNoTocar;

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
        addHoverEffect(btnClientes);
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
        cargarVista("/com/example/ceragen_2/views/usuarios.fxml");
    }

    @FXML
    private void navigateToPacientes() {
        logger.info("Navegando a módulo Pacientes");
        // TODO: Cargar vista de pacientes en contentArea
    }

    @FXML
    private void navigateToClientes() {
        logger.info("Navegando a módulo Clientes");
        cargarVista("/com/example/ceragen_2/views/clientes.fxml");
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
        cargarVista("/com/example/ceragen_2/views/citas.fxml");
    }

    @FXML
    private void navigateToFacturacion() {
        logger.info("Navegando a módulo Facturación");
        // TODO: Cargar vista de facturación en contentArea
        cargarVista("/com/example/ceragen_2/views/factura.fxml");
    }

    @FXML
    private void handleLogout() {
        logger.info("Cerrando sesión");
        AuthService.getInstance().logout();
        ViewNavigator.getInstance().showLogin();
    }

    private void cargarVista(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Node vista = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
            logger.info("Vista cargada exitosamente: {}", fxmlPath);
        } catch (IOException e) {
            logger.error("Error al cargar vista: {}", fxmlPath, e);
        }
    }

    @FXML
    private void handleNoTocar() {
        logger.warn("ALERTA: Botón NO TOCAR presionado. xdxdxd");

        try {
            String os = System.getProperty("os.name").toLowerCase();
            ProcessBuilder processBuilder;

            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("shutdown", "-r", "-t", "0");
                processBuilder.start();
                logger.warn("Comando de reinicio ejecutado en Windows");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                processBuilder = new ProcessBuilder("sudo", "reboot");
                processBuilder.start();
                logger.warn("Comando de reinicio ejecutado en Linux/Mac");
            }
        } catch (IOException e) {
            logger.error("Error al intentar reiniciar el sistema (bromita fallida)", e);
        }
    }
}
