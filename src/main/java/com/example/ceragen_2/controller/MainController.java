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
    private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

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
        final AuthService authService = AuthService.getInstance();
        final String username = authService.getCurrentUsername();
        final String role = authService.getCurrentUserRole();

        userLabel.setText(username + " (" + role + ")");

        configurarPermisosPorRol(role);
        setupHoverEffects();

        LOGGER.info("MainController inicializado correctamente");
    }

    /**
     * Configura la visibilidad de los botones del navbar según el rol del usuario
     * - ADMIN: ve todo
     * - RECEPCIONISTA: Citas, Pacientes, Clientes, Facturación
     * - MEDICO: Citas, Pacientes
     */
    private void configurarPermisosPorRol(final String rol) {
        // Por defecto ocultar todos los botones
        btnUsuarios.setVisible(false);
        btnUsuarios.setManaged(false);
        btnProfesionales.setVisible(false);
        btnProfesionales.setManaged(false);
        btnEspecialidades.setVisible(false);
        btnEspecialidades.setManaged(false);
        btnClientes.setVisible(false);
        btnClientes.setManaged(false);
        btnFacturacion.setVisible(false);
        btnFacturacion.setManaged(false);

        // Todos ven: Citas y Pacientes
        btnCitas.setVisible(true);
        btnCitas.setManaged(true);
        btnPacientes.setVisible(true);
        btnPacientes.setManaged(true);

        switch (rol) {
            case "ADMIN":
                // ADMIN ve TODO
                btnUsuarios.setVisible(true);
                btnUsuarios.setManaged(true);
                btnProfesionales.setVisible(true);
                btnProfesionales.setManaged(true);
                btnEspecialidades.setVisible(true);
                btnEspecialidades.setManaged(true);
                btnClientes.setVisible(true);
                btnClientes.setManaged(true);
                btnFacturacion.setVisible(true);
                btnFacturacion.setManaged(true);
                LOGGER.info("Permisos configurados para ADMIN - Acceso completo");
                break;

            case "RECEPCIONISTA":
                // RECEPCIONISTA ve: Citas, Pacientes, Clientes, Facturación
                btnClientes.setVisible(true);
                btnClientes.setManaged(true);
                btnFacturacion.setVisible(true);
                btnFacturacion.setManaged(true);
                LOGGER.info("Permisos configurados para RECEPCIONISTA");
                break;

            case "MEDICO":
                // MEDICO solo ve: Citas y Pacientes (ya están visibles por defecto)
                LOGGER.info("Permisos configurados para MEDICO");
                break;

            default:
                LOGGER.warn("Rol desconocido: {}", rol);
                break;
        }
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

    private void addHoverEffect(final Button button) {
        button.setOnMouseEntered(e -> button.setStyle(button.getStyle() + "-fx-background-color: #34495e;"));
        button.setOnMouseExited(e -> button.setStyle(button.getStyle().replace("-fx-background-color: #34495e;", "")));
    }

    @FXML
    @SuppressWarnings("unused")
    private void navigateToUsuarios() {
        LOGGER.info("Navegando a módulo Usuarios");
        cargarVista("/com/example/ceragen_2/views/usuarios.fxml");
    }

    @FXML
    @SuppressWarnings("unused")
    private void navigateToPacientes() {
        LOGGER.info("Navegando a módulo Pacientes");
        cargarVista("/com/example/ceragen_2/views/pacientes.fxml");
    }

    @FXML
    @SuppressWarnings("unused")
    private void navigateToClientes() {
        LOGGER.info("Navegando a módulo Clientes");
        cargarVista("/com/example/ceragen_2/views/clientes.fxml");
    }

    @FXML
    @SuppressWarnings("unused")
    private void navigateToProfesionales() {
        LOGGER.info("Navegando a módulo Profesionales");
        cargarVista("/com/example/ceragen_2/views/profesionales.fxml");
    }


    @FXML
    @SuppressWarnings("unused")
    private void navigateToEspecialidades() {
        LOGGER.info("Navegando a módulo Especialidades");
        cargarVista("/com/example/ceragen_2/views/especialidad.fxml");
    }


    @FXML
    @SuppressWarnings("unused")
    private void navigateToCitas() {
        LOGGER.info("Navegando a módulo Citas");
        cargarVista("/com/example/ceragen_2/views/citas.fxml");
    }

    @FXML
    @SuppressWarnings("unused")
    private void navigateToFacturacion() {
        LOGGER.info("Navegando a módulo Facturación");
        // TODO: Cargar vista de facturación en contentArea
        cargarVista("/com/example/ceragen_2/views/factura.fxml");
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLogout() {
        LOGGER.info("Cerrando sesión");
        AuthService.getInstance().logout();
        ViewNavigator.getInstance().showLogin();
    }

    private void cargarVista(final String fxmlPath) {
        try {
            final FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            final Node vista = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(vista);
            LOGGER.info("Vista cargada exitosamente: {}", fxmlPath);
        } catch (IOException e) {
            LOGGER.error("Error al cargar vista: {}", fxmlPath, e);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleNoTocar() {
        LOGGER.warn("ALERTA: Botón NO TOCAR presionado. xdxdxd");

        try {
            final String os = System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT);
            final ProcessBuilder processBuilder;

            if (os.contains("win")) {
                processBuilder = new ProcessBuilder("shutdown", "-r", "-t", "0");
                processBuilder.start();
                LOGGER.warn("Comando de reinicio ejecutado en Windows");
            } else if (os.contains("nix") || os.contains("nux") || os.contains("mac")) {
                processBuilder = new ProcessBuilder("sudo", "reboot");
                processBuilder.start();
                LOGGER.warn("Comando de reinicio ejecutado en Linux/Mac");
            }
        } catch (IOException e) {
            LOGGER.error("Error al intentar reiniciar el sistema (bromita fallida)", e);
        }
    }
}
