package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Usuario;
import com.example.ceragen_2.service.AuthService;
import com.example.ceragen_2.service.UsuarioService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class UsuariosController {
    private static final Logger logger = LoggerFactory.getLogger(UsuariosController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioService usuarioService = UsuarioService.getInstance();

    // Paginación
    private int paginaActual = 0;
    private int registrosPorPagina = 10;
    private int totalPaginas = 0;

    // Tab pane
    @FXML private TabPane tabPane;
    @FXML private Tab tabCrear;
    @FXML private Tab tabEditar;

    // Filtros
    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbRolFiltro;
    @FXML private ComboBox<String> cmbActivoFiltro;

    // Tabla
    @FXML private TableView<Usuario> tableUsuarios;
    @FXML private TableColumn<Usuario, String> colId;
    @FXML private TableColumn<Usuario, String> colUsername;
    @FXML private TableColumn<Usuario, String> colRol;
    @FXML private TableColumn<Usuario, String> colActivo;
    @FXML private TableColumn<Usuario, String> colFechaCreacion;
    @FXML private TableColumn<Usuario, Void> colAcciones;

    // Paginación
    @FXML private Button btnPrimera;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnUltima;
    @FXML private Text txtPaginacion;
    @FXML private ComboBox<String> cmbRegistrosPorPagina;

    // Formulario Crear
    @FXML private TextField txtCrearUsername;
    @FXML private PasswordField txtCrearPassword;
    @FXML private PasswordField txtCrearPasswordConfirm;
    @FXML private ComboBox<String> cmbCrearRol;

    // Formulario Editar
    @FXML private TextField txtEditarId;
    @FXML private TextField txtEditarUsername;
    @FXML private ComboBox<String> cmbEditarRol;
    @FXML private ComboBox<String> cmbEditarActivo;

    // Indicador de carga
    @FXML private VBox loadingIndicator;

    private Usuario usuarioEnEdicion;

    @FXML
    public void initialize() {
        logger.info("Inicializando módulo de Usuarios");

        configurarTabla();
        configurarFiltros();
        configurarPaginacion();

        cargarDatos();
    }

    private void configurarTabla() {
        // Ocultar columna ID (para producción)
        colId.setVisible(false);

        // Configurar columnas
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colRol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRol()));
        colActivo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getActivo() ? "ACTIVO" : "INACTIVO"));
        colFechaCreacion.setCellValueFactory(data -> {
            if (data.getValue().getFechaCreacion() != null) {
                return new SimpleStringProperty(data.getValue().getFechaCreacion().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final Button btnCambiarPassword = new Button("Cambiar Contraseña");
            private final HBox pane = new HBox(10, btnEditar, btnEliminar, btnCambiarPassword);

            {
                pane.setAlignment(Pos.CENTER);

                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnCambiarPassword.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");

                btnEditar.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    abrirEdicion(usuario);
                });

                btnEliminar.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    eliminarUsuario(usuario);
                });

                btnCambiarPassword.setOnAction(event -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    cambiarContrasena(usuario);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void configurarFiltros() {
        cmbRolFiltro.setValue("TODOS");
        cmbActivoFiltro.setValue("TODOS");
    }

    private void configurarPaginacion() {
        cmbRegistrosPorPagina.setValue("10");
    }

    private void cargarDatos() {
        // Mostrar indicador de carga
        loadingIndicator.setVisible(true);
        deshabilitarControles(true);

        final String searchText = txtBuscar.getText();
        final String rolFilter = cmbRolFiltro.getValue();
        final Boolean activoFilter = obtenerFiltroActivo();
        final int offset = paginaActual * registrosPorPagina;

        // Crear Task para ejecutar en segundo plano
        Task<DatosUsuariosResult> task = new Task<>() {
            @Override
            protected DatosUsuariosResult call() {
                // Operaciones de base de datos en segundo plano
                int totalRegistros = usuarioService.countUsuarios(searchText, rolFilter, activoFilter);
                int totalPaginasTemp = (int) Math.ceil((double) totalRegistros / registrosPorPagina);

                if (totalPaginasTemp == 0) {
                    totalPaginasTemp = 1;
                }

                List<Usuario> usuarios = usuarioService.getUsuarios(offset, registrosPorPagina, searchText, rolFilter, activoFilter);

                return new DatosUsuariosResult(usuarios, totalPaginasTemp);
            }
        };

        // Cuando el Task termine exitosamente
        task.setOnSucceeded(event -> {
            DatosUsuariosResult resultado = task.getValue();
            totalPaginas = resultado.totalPaginas;

            // Asegurar que la página actual esté en rango
            if (paginaActual >= totalPaginas) {
                paginaActual = totalPaginas - 1;
            }
            if (paginaActual < 0) {
                paginaActual = 0;
            }

            // Actualizar UI en el hilo de JavaFX
            tableUsuarios.getItems().clear();
            tableUsuarios.getItems().addAll(resultado.usuarios);
            actualizarInfoPaginacion();

            logger.info("Datos cargados: {} usuarios en página {}/{}", resultado.usuarios.size(), paginaActual + 1, totalPaginas);

            // Ocultar indicador de carga
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
        });

        // Si hay error
        task.setOnFailed(event -> {
            logger.error("Error al cargar datos", task.getException());
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
            mostrarAlerta("Error", "No se pudieron cargar los datos", Alert.AlertType.ERROR);
        });

        // Ejecutar el Task en un hilo separado
        new Thread(task).start();
    }

    private void deshabilitarControles(boolean deshabilitar) {
        btnPrimera.setDisable(deshabilitar);
        btnAnterior.setDisable(deshabilitar);
        btnSiguiente.setDisable(deshabilitar);
        btnUltima.setDisable(deshabilitar);
        cmbRegistrosPorPagina.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        cmbRolFiltro.setDisable(deshabilitar);
        cmbActivoFiltro.setDisable(deshabilitar);
    }

    // Clase interna para retornar múltiples valores del Task
    private static class DatosUsuariosResult {
        List<Usuario> usuarios;
        int totalPaginas;

        DatosUsuariosResult(List<Usuario> usuarios, int totalPaginas) {
            this.usuarios = usuarios;
            this.totalPaginas = totalPaginas;
        }
    }

    private Boolean obtenerFiltroActivo() {
        String valor = cmbActivoFiltro.getValue();
        if (valor.equals("ACTIVO")) return true;
        if (valor.equals("INACTIVO")) return false;
        return null;
    }

    private void actualizarInfoPaginacion() {
        txtPaginacion.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);

        btnPrimera.setDisable(paginaActual == 0);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnUltima.setDisable(paginaActual >= totalPaginas - 1);
    }

    @FXML
    private void handleBuscar() {
        logger.info("Aplicando filtros de búsqueda");
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handleLimpiarFiltros() {
        logger.info("Limpiando filtros");
        txtBuscar.clear();
        cmbRolFiltro.setValue("TODOS");
        cmbActivoFiltro.setValue("TODOS");
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handlePrimeraPagina() {
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handlePaginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }

    @FXML
    private void handlePaginaSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarDatos();
        }
    }

    @FXML
    private void handleUltimaPagina() {
        paginaActual = totalPaginas - 1;
        cargarDatos();
    }

    @FXML
    private void handleCambioRegistrosPorPagina() {
        String valor = cmbRegistrosPorPagina.getValue();
        registrosPorPagina = Integer.parseInt(valor);
        paginaActual = 0;
        logger.info("Registros por página cambiado a: {}", registrosPorPagina);
        cargarDatos();
    }

    @FXML
    private void handleCrearUsuario() {
        final String username = txtCrearUsername.getText().trim();
        final String password = txtCrearPassword.getText();
        final String passwordConfirm = txtCrearPasswordConfirm.getText();
        final String rol = cmbCrearRol.getValue();

        // Validaciones en el hilo principal (son rápidas)
        if (username.isEmpty() || password.isEmpty() || passwordConfirm.isEmpty() || rol == null) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

        if (!password.equals(passwordConfirm)) {
            mostrarAlerta("Error", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
            return;
        }

        if (password.length() < 4) {
            mostrarAlerta("Error", "La contraseña debe tener al menos 4 caracteres", Alert.AlertType.ERROR);
            return;
        }

        // Deshabilitar botón mientras se procesa
        loadingIndicator.setVisible(true);

        // Crear Task para ejecutar en segundo plano
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                // Verificar si existe y crear en segundo plano
                if (usuarioService.existeUsername(username)) {
                    return null; // Username ya existe
                }
                return usuarioService.crearUsuario(username, password, rol);
            }
        };

        task.setOnSucceeded(event -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                mostrarAlerta("Error", "El username ya existe", Alert.AlertType.ERROR);
            } else if (exito) {
                logger.info("Usuario creado exitosamente: {}", username);
                mostrarAlerta("Éxito", "Usuario creado exitosamente", Alert.AlertType.INFORMATION);
                limpiarFormularioCrear();
                cargarDatos();
                tabPane.getSelectionModel().select(0);
            } else {
                logger.error("Error al crear usuario: {}", username);
                mostrarAlerta("Error", "No se pudo crear el usuario", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            logger.error("Error al crear usuario", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo crear el usuario", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleLimpiarFormCrear() {
        limpiarFormularioCrear();
    }

    private void limpiarFormularioCrear() {
        txtCrearUsername.clear();
        txtCrearPassword.clear();
        txtCrearPasswordConfirm.clear();
        cmbCrearRol.setValue(null);
    }

    private void abrirEdicion(Usuario usuario) {
        logger.info("Abriendo edición para usuario: {}", usuario.getUsername());
        usuarioEnEdicion = usuario;

        txtEditarId.setText(usuario.getId().toString());
        txtEditarUsername.setText(usuario.getUsername());
        cmbEditarRol.setValue(usuario.getRol());
        cmbEditarActivo.setValue(usuario.getActivo() ? "ACTIVO" : "INACTIVO");

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    @FXML
    private void handleActualizarUsuario() {
        if (usuarioEnEdicion == null) {
            return;
        }

        final String username = txtEditarUsername.getText().trim();
        final String rol = cmbEditarRol.getValue();
        final String estadoStr = cmbEditarActivo.getValue();
        final Integer usuarioId = usuarioEnEdicion.getId();

        // Validaciones en el hilo principal
        if (username.isEmpty() || rol == null || estadoStr == null) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

        final Boolean activo = estadoStr.equals("ACTIVO");

        // Validación: No se puede auto-desactivar
        if (usuarioId.equals(AuthService.getInstance().getCurrentUserId()) && !activo) {
            mostrarAlerta("Error", "No puede desactivarse a sí mismo", Alert.AlertType.ERROR);
            return;
        }

        // Validación: Debe haber al menos 1 ADMIN activo
        if (rol.equals("ADMIN") && usuarioEnEdicion.getRol().equals("ADMIN") && !activo) {
            int adminsActivos = usuarioService.countAdminsActivos();
            if (adminsActivos <= 1) {
                mostrarAlerta("Error", "Debe haber al menos un ADMIN activo en el sistema", Alert.AlertType.ERROR);
                return;
            }
        }

        loadingIndicator.setVisible(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                // Verificar username y actualizar en segundo plano
                if (usuarioService.existeUsernameExceptoId(username, usuarioId)) {
                    return null; // Username ya existe
                }
                return usuarioService.actualizarUsuario(usuarioId, username, rol, activo);
            }
        };

        task.setOnSucceeded(event -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                mostrarAlerta("Error", "El username ya existe", Alert.AlertType.ERROR);
            } else if (exito) {
                logger.info("Usuario actualizado exitosamente: {}", username);
                mostrarAlerta("Éxito", "Usuario actualizado exitosamente", Alert.AlertType.INFORMATION);
                cargarDatos();
                handleCancelarEdicion();
            } else {
                logger.error("Error al actualizar usuario: {}", username);
                mostrarAlerta("Error", "No se pudo actualizar el usuario", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            logger.error("Error al actualizar usuario", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo actualizar el usuario", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCancelarEdicion() {
        usuarioEnEdicion = null;
        tabEditar.setDisable(true);
        tabPane.getSelectionModel().select(0); // Volver a la pestaña de listar
    }

    private void eliminarUsuario(Usuario usuario) {
        logger.info("Intentando desactivar usuario: {}", usuario.getUsername());

        // Validación: No se puede auto-desactivar
        if (usuario.getId().equals(AuthService.getInstance().getCurrentUserId())) {
            mostrarAlerta("Error", "No puede desactivarse a sí mismo", Alert.AlertType.ERROR);
            return;
        }

        // Validación: Debe haber al menos 1 ADMIN activo
        if (usuario.getRol().equals("ADMIN")) {
            int adminsActivos = usuarioService.countAdminsActivos();
            if (adminsActivos <= 1) {
                mostrarAlerta("Error", "Debe haber al menos un ADMIN activo en el sistema", Alert.AlertType.ERROR);
                return;
            }
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Desactivación");
        confirmacion.setHeaderText("¿Está seguro de desactivar este usuario?");
        confirmacion.setContentText("Usuario: " + usuario.getUsername() + "\nEl usuario ya no podrá acceder al sistema.");

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            final Integer usuarioId = usuario.getId();
            final String username = usuario.getUsername();

            loadingIndicator.setVisible(true);

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return usuarioService.eliminarUsuario(usuarioId);
                }
            };

            task.setOnSucceeded(event -> {
                Boolean exito = task.getValue();
                loadingIndicator.setVisible(false);

                if (exito) {
                    logger.info("Usuario desactivado exitosamente: {}", username);
                    mostrarAlerta("Éxito", "Usuario desactivado exitosamente", Alert.AlertType.INFORMATION);
                    cargarDatos();
                } else {
                    logger.error("Error al desactivar usuario: {}", username);
                    mostrarAlerta("Error", "No se pudo desactivar el usuario", Alert.AlertType.ERROR);
                }
            });

            task.setOnFailed(event -> {
                logger.error("Error al eliminar usuario", task.getException());
                loadingIndicator.setVisible(false);
                mostrarAlerta("Error", "No se pudo eliminar el usuario", Alert.AlertType.ERROR);
            });

            new Thread(task).start();
        }
    }

    private void cambiarContrasena(Usuario usuario) {
        logger.info("Cambiando contraseña para usuario: {}", usuario.getUsername());

        // Crear diálogo personalizado
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Contraseña");
        dialog.setHeaderText("Cambiar contraseña para: " + usuario.getUsername());

        ButtonType btnCambiar = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCambiar, ButtonType.CANCEL);

        PasswordField txtNuevaPassword = new PasswordField();
        txtNuevaPassword.setPromptText("Nueva contraseña");
        txtNuevaPassword.setPrefWidth(300);

        PasswordField txtConfirmarPassword = new PasswordField();
        txtConfirmarPassword.setPromptText("Confirmar contraseña");
        txtConfirmarPassword.setPrefWidth(300);

        javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(10,
            new Label("Nueva contraseña:"), txtNuevaPassword,
            new Label("Confirmar contraseña:"), txtConfirmarPassword
        );
        vbox.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnCambiar) {
                return txtNuevaPassword.getText();
            }
            return null;
        });

        Optional<String> resultado = dialog.showAndWait();

        resultado.ifPresent(nuevaPassword -> {
            String confirmarPassword = txtConfirmarPassword.getText();

            if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
                mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
                return;
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                mostrarAlerta("Error", "Las contraseñas no coinciden", Alert.AlertType.ERROR);
                return;
            }

            if (nuevaPassword.length() < 4) {
                mostrarAlerta("Error", "La contraseña debe tener al menos 4 caracteres", Alert.AlertType.ERROR);
                return;
            }

            final Integer usuarioId = usuario.getId();
            final String username = usuario.getUsername();

            loadingIndicator.setVisible(true);

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return usuarioService.cambiarContrasena(usuarioId, nuevaPassword);
                }
            };

            task.setOnSucceeded(event -> {
                Boolean exito = task.getValue();
                loadingIndicator.setVisible(false);

                if (exito) {
                    logger.info("Contraseña cambiada exitosamente para usuario: {}", username);
                    mostrarAlerta("Éxito", "Contraseña cambiada exitosamente", Alert.AlertType.INFORMATION);
                } else {
                    logger.error("Error al cambiar contraseña para usuario: {}", username);
                    mostrarAlerta("Error", "No se pudo cambiar la contraseña", Alert.AlertType.ERROR);
                }
            });

            task.setOnFailed(event -> {
                logger.error("Error al cambiar contraseña", task.getException());
                loadingIndicator.setVisible(false);
                mostrarAlerta("Error", "No se pudo cambiar la contraseña", Alert.AlertType.ERROR);
            });

            new Thread(task).start();
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
