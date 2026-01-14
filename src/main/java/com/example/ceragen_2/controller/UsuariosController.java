package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Usuario;
import com.example.ceragen_2.service.AuthService;
import com.example.ceragen_2.service.UsuarioService;
import com.example.ceragen_2.util.DialogUtil;
import com.example.ceragen_2.util.FormValidationUtil;
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

public class UsuariosController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsuariosController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final UsuarioService usuarioService = UsuarioService.getInstance();

    // Paginacion
    private int paginaActual;
    private int registrosPorPagina = 10;
    private int totalPaginas;

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

    // Paginacion
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
        LOGGER.info("Inicializando modulo de Usuarios");

        configurarTabla();
        configurarFiltros();
        configurarPaginacion();
        configurarValidaciones();
        configurarTooltips();

        cargarDatos();
    }

    private void configurarValidaciones() {
        // Aplicar filtro de longitud maxima para username
        FormValidationUtil.aplicarFiltroLongitudMaxima(txtCrearUsername, 50);
        FormValidationUtil.aplicarFiltroLongitudMaxima(txtEditarUsername, 50);

        // Validacion en tiempo real para username al perder foco
        txtCrearUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarCampoRequerido(txtCrearUsername, true);
            }
        });

        txtEditarUsername.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarCampoRequerido(txtEditarUsername, true);
            }
        });

        // Validacion en tiempo real para password
        txtCrearPassword.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarLongitudMinima(txtCrearPassword, 4, true);
            }
        });

        // Validacion en tiempo real para confirmar password
        txtCrearPasswordConfirm.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtCrearPassword.getText().isEmpty()) {
                FormValidationUtil.validarPasswordsCoinciden(txtCrearPassword, txtCrearPasswordConfirm, true);
            }
        });
    }

    private void configurarTooltips() {
        // Tooltips de ayuda para campos del formulario de creacion
        txtCrearUsername.setTooltip(new Tooltip("Nombre de usuario unico para acceder al sistema (maximo 50 caracteres)"));
        txtCrearPassword.setTooltip(new Tooltip("Contrasena de acceso (minimo 4 caracteres)"));
        txtCrearPasswordConfirm.setTooltip(new Tooltip("Repita la contrasena para confirmar"));
        cmbCrearRol.setTooltip(new Tooltip("ADMIN: Acceso total | RECEPCIONISTA: Gestion de citas y pacientes | MEDICO: Solo sus citas"));

        // Tooltips de ayuda para campos del formulario de edicion
        txtEditarUsername.setTooltip(new Tooltip("Nombre de usuario unico para acceder al sistema"));
        cmbEditarRol.setTooltip(new Tooltip("ADMIN: Acceso total | RECEPCIONISTA: Gestion de citas y pacientes | MEDICO: Solo sus citas"));
        cmbEditarActivo.setTooltip(new Tooltip("INACTIVO: El usuario no podra acceder al sistema"));

        // Tooltips para filtros
        txtBuscar.setTooltip(new Tooltip("Buscar usuarios por nombre de usuario"));
        cmbRolFiltro.setTooltip(new Tooltip("Filtrar usuarios por rol asignado"));
        cmbActivoFiltro.setTooltip(new Tooltip("Filtrar usuarios por estado activo/inactivo"));
    }

    private void configurarTabla() {
        // Ocultar columna ID (para produccion)
        colId.setVisible(false);

        // Configurar columnas
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
        colUsername.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUsername()));
        colRol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRol()));

        // Columna de estado con badge
        colActivo.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    final Usuario usuario = getTableRow().getItem();
                    final Label badge = new Label(usuario.getActivo() ? "ACTIVO" : "INACTIVO");
                    badge.getStyleClass().add("status-badge");
                    if (usuario.getActivo()) {
                        badge.getStyleClass().add("status-badge-active");
                    } else {
                        badge.getStyleClass().add("status-badge-inactive");
                    }
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
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
            private final Button btnCambiarPassword = new Button("Cambiar Clave");
            private final HBox pane = new HBox(8, btnEditar, btnEliminar, btnCambiarPassword);

            {
                pane.setAlignment(Pos.CENTER);

                // Usar clases CSS en lugar de estilos inline
                btnEditar.getStyleClass().addAll("btn-table-action", "btn-table-edit");
                btnEliminar.getStyleClass().addAll("btn-table-action", "btn-table-delete");
                btnCambiarPassword.getStyleClass().addAll("btn-table-action", "btn-table-password");

                // Tooltips para botones de accion
                btnEditar.setTooltip(new Tooltip("Editar datos del usuario"));
                btnEliminar.setTooltip(new Tooltip("Desactivar usuario (no podra acceder al sistema)"));
                btnCambiarPassword.setTooltip(new Tooltip("Establecer una nueva contrasena para el usuario"));

                btnEditar.setOnAction(event -> {
                    final Usuario usuario = getTableView().getItems().get(getIndex());
                    abrirEdicion(usuario);
                });

                btnEliminar.setOnAction(event -> {
                    final Usuario usuario = getTableView().getItems().get(getIndex());
                    eliminarUsuario(usuario);
                });

                btnCambiarPassword.setOnAction(event -> {
                    final Usuario usuario = getTableView().getItems().get(getIndex());
                    cambiarContrasena(usuario);
                });
            }

            @Override
            protected void updateItem(final Void item, final boolean empty) {
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

            // Asegurar que la pagina actual este en rango
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

            LOGGER.info("Datos cargados: {} usuarios en pagina {}/{}", resultado.usuarios.size(), paginaActual + 1, totalPaginas);

            // Ocultar indicador de carga
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
        });

        // Si hay error
        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar datos", task.getException());
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
            DialogUtil.mostrarError("Error de conexion", "No se pudieron cargar los datos. Verifique su conexion.");
        });

        // Ejecutar el Task en un hilo separado
        new Thread(task).start();
    }

    private void deshabilitarControles(final boolean deshabilitar) {
        btnPrimera.setDisable(deshabilitar);
        btnAnterior.setDisable(deshabilitar);
        btnSiguiente.setDisable(deshabilitar);
        btnUltima.setDisable(deshabilitar);
        cmbRegistrosPorPagina.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        cmbRolFiltro.setDisable(deshabilitar);
        cmbActivoFiltro.setDisable(deshabilitar);
    }

    // Clase interna para retornar multiples valores del Task
    private static class DatosUsuariosResult {
        final List<Usuario> usuarios;
        final int totalPaginas;

        DatosUsuariosResult(final List<Usuario> usuarios, final int totalPaginas) {
            this.usuarios = usuarios;
            this.totalPaginas = totalPaginas;
        }
    }

    private Boolean obtenerFiltroActivo() {
        final String valor = cmbActivoFiltro.getValue();
        if (valor.equals("ACTIVO")) return true;
        if (valor.equals("INACTIVO")) return false;
        return null;
    }

    private void actualizarInfoPaginacion() {
        txtPaginacion.setText("Pagina " + (paginaActual + 1) + " de " + totalPaginas);

        btnPrimera.setDisable(paginaActual == 0);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnUltima.setDisable(paginaActual >= totalPaginas - 1);
    }

    @FXML
    private void handleBuscar() {
        LOGGER.info("Aplicando filtros de busqueda");
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handleLimpiarFiltros() {
        LOGGER.info("Limpiando filtros");
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
        final String valor = cmbRegistrosPorPagina.getValue();
        registrosPorPagina = Integer.parseInt(valor);
        paginaActual = 0;
        LOGGER.info("Registros por pagina cambiado a: {}", registrosPorPagina);
        cargarDatos();
    }

    @FXML
    private void handleCrearUsuario() {
        // Validaciones con feedback visual
        boolean esValido = true;

        if (!FormValidationUtil.validarCampoRequerido(txtCrearUsername, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarLongitudMinima(txtCrearPassword, 4, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtCrearPasswordConfirm, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarComboRequerido(cmbCrearRol, true)) {
            esValido = false;
        }

        // Validar que las contrasenas coincidan
        if (!txtCrearPassword.getText().isEmpty() && !txtCrearPasswordConfirm.getText().isEmpty()) {
            if (!FormValidationUtil.validarPasswordsCoinciden(txtCrearPassword, txtCrearPasswordConfirm, true)) {
                esValido = false;
            }
        }

        if (!esValido) {
            DialogUtil.mostrarError("Campos invalidos", "Por favor, corrija los campos marcados en rojo");
            return;
        }

        final String username = txtCrearUsername.getText().trim();
        final String password = txtCrearPassword.getText();
        final String rol = cmbCrearRol.getValue();

        // Deshabilitar boton mientras se procesa
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
                DialogUtil.mostrarError("Error", "El username ya existe en el sistema");
                FormValidationUtil.marcarCampoInvalido(txtCrearUsername, "Username ya registrado");
            } else if (exito) {
                LOGGER.info("Usuario creado exitosamente: {}", username);
                DialogUtil.mostrarExito("Usuario creado", "El usuario se ha creado exitosamente");
                limpiarFormularioCrear();
                cargarDatos();
                tabPane.getSelectionModel().select(0);
            } else {
                LOGGER.error("Error al crear usuario: {}", username);
                DialogUtil.mostrarError("Error", "No se pudo crear el usuario");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al crear usuario", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error", "No se pudo crear el usuario");
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

        // Limpiar estilos de validacion
        FormValidationUtil.limpiarEstadoValidacion(txtCrearUsername);
        FormValidationUtil.limpiarEstadoValidacion(txtCrearPassword);
        FormValidationUtil.limpiarEstadoValidacion(txtCrearPasswordConfirm);
        FormValidationUtil.limpiarEstadoValidacion(cmbCrearRol);
    }

    private void abrirEdicion(final Usuario usuario) {
        LOGGER.info("Abriendo edicion para usuario: {}", usuario.getUsername());
        usuarioEnEdicion = usuario;

        txtEditarId.setText(usuario.getId().toString());
        txtEditarUsername.setText(usuario.getUsername());
        cmbEditarRol.setValue(usuario.getRol());
        cmbEditarActivo.setValue(usuario.getActivo() ? "ACTIVO" : "INACTIVO");

        // Limpiar estilos de validacion previos
        FormValidationUtil.limpiarEstadoValidacion(txtEditarUsername);
        FormValidationUtil.limpiarEstadoValidacion(cmbEditarRol);
        FormValidationUtil.limpiarEstadoValidacion(cmbEditarActivo);

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    @FXML
    private void handleActualizarUsuario() {
        if (usuarioEnEdicion == null) {
            return;
        }

        // Validaciones con feedback visual
        boolean esValido = true;

        if (!FormValidationUtil.validarCampoRequerido(txtEditarUsername, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarComboRequerido(cmbEditarRol, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarComboRequerido(cmbEditarActivo, true)) {
            esValido = false;
        }

        if (!esValido) {
            DialogUtil.mostrarError("Campos invalidos", "Por favor, corrija los campos marcados en rojo");
            return;
        }

        final String username = txtEditarUsername.getText().trim();
        final String rol = cmbEditarRol.getValue();
        final String estadoStr = cmbEditarActivo.getValue();
        final Integer usuarioId = usuarioEnEdicion.getId();
        final Boolean activo = estadoStr.equals("ACTIVO");

        // Validacion: No se puede auto-desactivar
        if (usuarioId.equals(AuthService.getInstance().getCurrentUserId()) && !activo) {
            DialogUtil.mostrarError("Operacion no permitida", "No puede desactivarse a si mismo");
            return;
        }

        // Validacion: Debe haber al menos 1 ADMIN activo
        if (usuarioEnEdicion.getRol().equals("ADMIN") && !activo) {
            int adminsActivos = usuarioService.countAdminsActivos();
            if (adminsActivos <= 1) {
                DialogUtil.mostrarError("Operacion no permitida", "Debe haber al menos un ADMIN activo en el sistema");
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
                DialogUtil.mostrarError("Error", "El username ya existe en el sistema");
                FormValidationUtil.marcarCampoInvalido(txtEditarUsername, "Username ya registrado");
            } else if (exito) {
                LOGGER.info("Usuario actualizado exitosamente: {}", username);
                DialogUtil.mostrarExito("Usuario actualizado", "Los datos del usuario se han actualizado correctamente");
                cargarDatos();
                handleCancelarEdicion();
            } else {
                LOGGER.error("Error al actualizar usuario: {}", username);
                DialogUtil.mostrarError("Error", "No se pudo actualizar el usuario");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al actualizar usuario", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error", "No se pudo actualizar el usuario");
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCancelarEdicion() {
        usuarioEnEdicion = null;
        tabEditar.setDisable(true);
        tabPane.getSelectionModel().select(0); // Volver a la pestana de listar
    }

    private void eliminarUsuario(final Usuario usuario) {
        LOGGER.info("Intentando desactivar usuario: {}", usuario.getUsername());

        // Validacion: No se puede auto-desactivar
        if (usuario.getId().equals(AuthService.getInstance().getCurrentUserId())) {
            DialogUtil.mostrarError("Operacion no permitida", "No puede desactivarse a si mismo");
            return;
        }

        // Validacion: Debe haber al menos 1 ADMIN activo
        if (usuario.getRol().equals("ADMIN")) {
            int adminsActivos = usuarioService.countAdminsActivos();
            if (adminsActivos <= 1) {
                DialogUtil.mostrarError("Operacion no permitida", "Debe haber al menos un ADMIN activo en el sistema");
                return;
            }
        }

        final boolean confirmar = DialogUtil.mostrarConfirmacionPersonalizada(
                "Confirmar Desactivacion",
                "Esta seguro de desactivar este usuario?",
                "Usuario: " + usuario.getUsername() + "\nEl usuario ya no podra acceder al sistema.",
                "Desactivar",
                "Cancelar"
        );

        if (confirmar) {
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
                    LOGGER.info("Usuario desactivado exitosamente: {}", username);
                    DialogUtil.mostrarExito("Usuario desactivado", "El usuario ha sido desactivado del sistema");
                    cargarDatos();
                } else {
                    LOGGER.error("Error al desactivar usuario: {}", username);
                    DialogUtil.mostrarError("Error", "No se pudo desactivar el usuario");
                }
            });

            task.setOnFailed(event -> {
                LOGGER.error("Error al eliminar usuario", task.getException());
                loadingIndicator.setVisible(false);
                DialogUtil.mostrarError("Error", "No se pudo eliminar el usuario");
            });

            new Thread(task).start();
        }
    }

    private void cambiarContrasena(final Usuario usuario) {
        LOGGER.info("Cambiando contrasena para usuario: {}", usuario.getUsername());

        // Crear dialogo personalizado
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Cambiar Contrasena");
        dialog.setHeaderText("Cambiar contrasena para: " + usuario.getUsername());

        ButtonType btnCambiar = new ButtonType("Cambiar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnCambiar, ButtonType.CANCEL);

        PasswordField txtNuevaPassword = new PasswordField();
        txtNuevaPassword.setPromptText("Nueva contrasena");
        txtNuevaPassword.setPrefWidth(300);
        txtNuevaPassword.setTooltip(new Tooltip("Minimo 4 caracteres"));

        PasswordField txtConfirmarPassword = new PasswordField();
        txtConfirmarPassword.setPromptText("Confirmar contrasena");
        txtConfirmarPassword.setPrefWidth(300);
        txtConfirmarPassword.setTooltip(new Tooltip("Repita la nueva contrasena"));

        VBox vbox = new VBox(10,
            new Label("Nueva contrasena:"), txtNuevaPassword,
            new Label("Confirmar contrasena:"), txtConfirmarPassword
        );
        vbox.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(vbox);

        // Aplicar estilos
        dialog.getDialogPane().getStylesheets().add(
            getClass().getResource("/com/example/ceragen_2/css/styles.css").toExternalForm()
        );

        dialog.setResultConverter(dialogButton -> {
            if (btnCambiar.equals(dialogButton)) {
                return txtNuevaPassword.getText();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(nuevaPassword -> {
            String confirmarPassword = txtConfirmarPassword.getText();

            if (nuevaPassword.isEmpty() || confirmarPassword.isEmpty()) {
                DialogUtil.mostrarError("Campos vacios", "Todos los campos son obligatorios");
                return;
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                DialogUtil.mostrarError("Error de validacion", "Las contrasenas no coinciden");
                return;
            }

            if (nuevaPassword.length() < 4) {
                DialogUtil.mostrarError("Error de validacion", "La contrasena debe tener al menos 4 caracteres");
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
                    LOGGER.info("Contrasena cambiada exitosamente para usuario: {}", username);
                    DialogUtil.mostrarExito("Contrasena actualizada", "La contrasena se ha cambiado exitosamente");
                } else {
                    LOGGER.error("Error al cambiar contrasena para usuario: {}", username);
                    DialogUtil.mostrarError("Error", "No se pudo cambiar la contrasena");
                }
            });

            task.setOnFailed(event -> {
                LOGGER.error("Error al cambiar contrasena", task.getException());
                loadingIndicator.setVisible(false);
                DialogUtil.mostrarError("Error", "No se pudo cambiar la contrasena");
            });

            new Thread(task).start();
        });
    }
}
