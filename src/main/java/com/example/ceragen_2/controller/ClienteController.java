package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.service.ClienteService;
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

public class ClienteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final ClienteService clienteService = ClienteService.getInstance();

    // Tab pane
    @FXML private TabPane tabPane;
    @FXML private Tab tabCrear;
    @FXML private Tab tabEditar;

    // Filtros
    @FXML private TextField txtBuscarCedula;
    @FXML private TextField txtBuscarNombre;
    @FXML private TextField txtBuscarApellido;

    // Tabla
    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, String> colCedula;
    @FXML private TableColumn<Cliente, String> colNombres;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colFechaRegistro;
    @FXML private TableColumn<Cliente, Void> colAcciones;

    // Paginacion
    @FXML private Button btnPrimera;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnUltima;
    @FXML private Text txtPaginacion;
    @FXML private ComboBox<String> cmbRegistrosPorPagina;

    // Formulario Crear
    @FXML private TextField txtCrearCedula;
    @FXML private TextField txtCrearNombres;
    @FXML private TextField txtCrearApellidos;
    @FXML private TextField txtCrearTelefono;
    @FXML private TextField txtCrearEmail;
    @FXML private TextArea txtCrearDireccion;

    // Formulario Editar
    @FXML private TextField txtEditarId;
    @FXML private TextField txtEditarCedula;
    @FXML private TextField txtEditarNombres;
    @FXML private TextField txtEditarApellidos;
    @FXML private TextField txtEditarTelefono;
    @FXML private TextField txtEditarEmail;
    @FXML private TextArea txtEditarDireccion;

    // Indicador de carga
    @FXML private VBox loadingIndicator;

    private Cliente clienteEnEdicion;

    // Variables de paginacion
    private int paginaActual = 1;
    private int registrosPorPagina = 10;
    private int totalRegistros = 0;
    private int totalPaginas = 1;

    @FXML
    public void initialize() {
        LOGGER.info("Inicializando modulo de Clientes");

        configurarTabla();
        configurarValidaciones();
        configurarTooltips();
        configurarPaginacion();
        cargarDatos();
    }

    private void configurarPaginacion() {
        cmbRegistrosPorPagina.setValue("10");
    }

    private void configurarTooltips() {
        // Tooltips para filtros
        txtBuscarCedula.setTooltip(new Tooltip("Buscar clientes por cedula"));
        txtBuscarNombre.setTooltip(new Tooltip("Buscar clientes por nombre"));
        txtBuscarApellido.setTooltip(new Tooltip("Buscar clientes por apellido"));

        // Tooltips para formulario de creacion
        txtCrearCedula.setTooltip(new Tooltip("Numero de cedula del cliente (10 digitos)"));
        txtCrearNombres.setTooltip(new Tooltip("Nombres del cliente (solo letras)"));
        txtCrearApellidos.setTooltip(new Tooltip("Apellidos del cliente (solo letras)"));
        txtCrearTelefono.setTooltip(new Tooltip("Numero de telefono (10 digitos)"));
        txtCrearEmail.setTooltip(new Tooltip("Correo electronico (formato: usuario@dominio.com)"));
        txtCrearDireccion.setTooltip(new Tooltip("Direccion completa del cliente (opcional)"));

        // Tooltips para formulario de edicion
        txtEditarCedula.setTooltip(new Tooltip("Numero de cedula del cliente (10 digitos)"));
        txtEditarNombres.setTooltip(new Tooltip("Nombres del cliente (solo letras)"));
        txtEditarApellidos.setTooltip(new Tooltip("Apellidos del cliente (solo letras)"));
        txtEditarTelefono.setTooltip(new Tooltip("Numero de telefono (10 digitos)"));
        txtEditarEmail.setTooltip(new Tooltip("Correo electronico (formato: usuario@dominio.com)"));
        txtEditarDireccion.setTooltip(new Tooltip("Direccion completa del cliente (opcional)"));
    }

    private void configurarValidaciones() {
        // Aplicar filtros de entrada a campos de creacion (solo digitos, max 10)
        FormValidationUtil.aplicarFiltroSoloDigitosConLongitud(txtCrearCedula, 10);
        FormValidationUtil.aplicarFiltroSoloDigitosConLongitud(txtCrearTelefono, 10);
        FormValidationUtil.aplicarFiltroSoloLetras(txtCrearNombres);
        FormValidationUtil.aplicarFiltroSoloLetras(txtCrearApellidos);

        // Aplicar filtros de entrada a campos de edicion (solo digitos, max 10)
        FormValidationUtil.aplicarFiltroSoloDigitosConLongitud(txtEditarCedula, 10);
        FormValidationUtil.aplicarFiltroSoloDigitosConLongitud(txtEditarTelefono, 10);
        FormValidationUtil.aplicarFiltroSoloLetras(txtEditarNombres);
        FormValidationUtil.aplicarFiltroSoloLetras(txtEditarApellidos);

        // Validacion en tiempo real para cedula (crear)
        txtCrearCedula.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarCedula(txtCrearCedula, true);
            }
        });

        // Validacion en tiempo real para cedula (editar)
        txtEditarCedula.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarCedula(txtEditarCedula, true);
            }
        });

        // Validacion en tiempo real para telefono (crear)
        txtCrearTelefono.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarTelefono(txtCrearTelefono, true);
            }
        });

        // Validacion en tiempo real para telefono (editar)
        txtEditarTelefono.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarTelefono(txtEditarTelefono, true);
            }
        });

        // Validacion en tiempo real para email (crear)
        txtCrearEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarEmail(txtCrearEmail, true);
            }
        });

        // Validacion en tiempo real para email (editar)
        txtEditarEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarEmail(txtEditarEmail, true);
            }
        });
    }

    private void configurarTabla() {
        // Configurar columnas
        colCedula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCedula()));
        colNombres.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombres()));
        colApellidos.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getApellidos()));
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colFechaRegistro.setCellValueFactory(data -> {
            if (data.getValue().getFechaRegistro() != null) {
                return new SimpleStringProperty(data.getValue().getFechaRegistro().format(DATE_FORMATTER));
            }
            return new SimpleStringProperty("");
        });

        // Configurar columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox pane = new HBox(8, btnEditar, btnEliminar);

            {
                pane.setAlignment(Pos.CENTER);

                // Usar clases CSS en lugar de estilos inline
                btnEditar.getStyleClass().addAll("btn-table-action", "btn-table-edit");
                btnEliminar.getStyleClass().addAll("btn-table-action", "btn-table-delete");

                // Tooltips para botones de accion
                btnEditar.setTooltip(new Tooltip("Editar datos del cliente"));
                btnEliminar.setTooltip(new Tooltip("Eliminar cliente del sistema"));

                btnEditar.setOnAction(event -> {
                    final Cliente cliente = getTableView().getItems().get(getIndex());
                    abrirEdicion(cliente);
                });

                btnEliminar.setOnAction(event -> {
                    final Cliente cliente = getTableView().getItems().get(getIndex());
                    eliminarCliente(cliente);
                });
            }

            @Override
            protected void updateItem(final Void item, final boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void cargarDatos() {
        loadingIndicator.setVisible(true);

        final String cedula = txtBuscarCedula.getText().trim();
        final String nombre = txtBuscarNombre.getText().trim();
        final String apellido = txtBuscarApellido.getText().trim();
        final int offset = (paginaActual - 1) * registrosPorPagina;

        final Task<List<Cliente>> task = new Task<>() {
            @Override
            protected List<Cliente> call() {
                totalRegistros = clienteService.countClientesConFiltros(cedula, nombre, apellido);
                return clienteService.getClientesPaginadosConFiltros(offset, registrosPorPagina, cedula, nombre, apellido);
            }
        };

        task.setOnSucceeded(event -> {
            final List<Cliente> clientes = task.getValue();
            tableClientes.getItems().clear();
            tableClientes.getItems().addAll(clientes);

            // Calcular total de paginas
            totalPaginas = (int) Math.ceil((double) totalRegistros / registrosPorPagina);
            if (totalPaginas == 0) totalPaginas = 1;

            actualizarControlesPaginacion();

            LOGGER.info("Datos cargados: {} clientes (Pagina {} de {})", clientes.size(), paginaActual, totalPaginas);
            loadingIndicator.setVisible(false);
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar datos", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error de conexion", "No se pudieron cargar los datos. Verifique su conexion.");
        });

        new Thread(task).start();
    }

    private void actualizarControlesPaginacion() {
        txtPaginacion.setText("Pagina " + paginaActual + " de " + totalPaginas);

        btnPrimera.setDisable(paginaActual <= 1);
        btnAnterior.setDisable(paginaActual <= 1);
        btnSiguiente.setDisable(paginaActual >= totalPaginas);
        btnUltima.setDisable(paginaActual >= totalPaginas);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handlePrimeraPagina() {
        if (paginaActual > 1) {
            paginaActual = 1;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handlePaginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handlePaginaSiguiente() {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleUltimaPagina() {
        if (paginaActual < totalPaginas) {
            paginaActual = totalPaginas;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleCambioRegistrosPorPagina() {
        String valor = cmbRegistrosPorPagina.getValue();
        if (valor != null) {
            registrosPorPagina = Integer.parseInt(valor);
            paginaActual = 1;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscar() {
        LOGGER.info("Buscando clientes con filtros");
        paginaActual = 1;
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLimpiarFiltros() {
        LOGGER.info("Limpiando filtros");
        txtBuscarCedula.clear();
        txtBuscarNombre.clear();
        txtBuscarApellido.clear();
        paginaActual = 1;
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleCrearCliente() {
        // Validaciones con feedback visual
        boolean esValido = true;

        if (!FormValidationUtil.validarCedula(txtCrearCedula, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtCrearNombres, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtCrearApellidos, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarEmail(txtCrearEmail, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarTelefono(txtCrearTelefono, true)) {
            esValido = false;
        }

        if (!esValido) {
            DialogUtil.mostrarError("Campos invalidos", "Por favor, corrija los campos marcados en rojo");
            return;
        }

        final String cedula = txtCrearCedula.getText().trim();
        final String nombres = txtCrearNombres.getText().trim();
        final String apellidos = txtCrearApellidos.getText().trim();
        final String telefono = txtCrearTelefono.getText().trim();
        final String email = txtCrearEmail.getText().trim();
        final String direccion = txtCrearDireccion.getText().trim();

        loadingIndicator.setVisible(true);

        final Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                // Verificar si ya existe
                final Cliente existente = clienteService.getClienteByCedula(cedula);
                if (existente != null) {
                    return null; // Cedula ya existe
                }

                final Cliente cliente = new Cliente();
                cliente.setCedula(cedula);
                cliente.setNombres(nombres);
                cliente.setApellidos(apellidos);
                cliente.setTelefono(telefono);
                cliente.setEmail(email);
                cliente.setDireccion(direccion);

                return clienteService.crearCliente(cliente);
            }
        };

        task.setOnSucceeded(event -> {
            final Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                DialogUtil.mostrarError("Error", "Ya existe un cliente con esta cedula");
                FormValidationUtil.marcarCampoInvalido(txtCrearCedula, "Cedula ya registrada");
            } else if (exito) {
                LOGGER.info("Cliente creado exitosamente: {} {}", nombres, apellidos);
                DialogUtil.mostrarExito("Cliente creado", "El cliente se ha registrado exitosamente");
                limpiarFormularioCrear();
                cargarDatos();
                tabPane.getSelectionModel().select(0);
            } else {
                LOGGER.error("Error al crear cliente");
                DialogUtil.mostrarError("Error", "No se pudo crear el cliente");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al crear cliente", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error", "No se pudo crear el cliente");
        });

        new Thread(task).start();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLimpiarFormCrear() {
        limpiarFormularioCrear();
    }

    private void limpiarFormularioCrear() {
        txtCrearCedula.clear();
        txtCrearNombres.clear();
        txtCrearApellidos.clear();
        txtCrearTelefono.clear();
        txtCrearEmail.clear();
        txtCrearDireccion.clear();

        // Limpiar estilos de validacion
        FormValidationUtil.limpiarEstadoValidacion(txtCrearCedula);
        FormValidationUtil.limpiarEstadoValidacion(txtCrearNombres);
        FormValidationUtil.limpiarEstadoValidacion(txtCrearApellidos);
        FormValidationUtil.limpiarEstadoValidacion(txtCrearTelefono);
        FormValidationUtil.limpiarEstadoValidacion(txtCrearEmail);
    }

    private void abrirEdicion(final Cliente cliente) {
        LOGGER.info("Abriendo edicion para cliente: {}", cliente.getNombreCompleto());
        clienteEnEdicion = cliente;

        txtEditarId.setText(cliente.getId().toString());
        txtEditarCedula.setText(cliente.getCedula());
        txtEditarNombres.setText(cliente.getNombres());
        txtEditarApellidos.setText(cliente.getApellidos());
        txtEditarTelefono.setText(cliente.getTelefono() != null ? cliente.getTelefono() : "");
        txtEditarEmail.setText(cliente.getEmail() != null ? cliente.getEmail() : "");
        txtEditarDireccion.setText(cliente.getDireccion() != null ? cliente.getDireccion() : "");

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleActualizarCliente() {
        if (clienteEnEdicion == null) {
            return;
        }

        // Validaciones con feedback visual
        boolean esValido = true;

        if (!FormValidationUtil.validarCedula(txtEditarCedula, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtEditarNombres, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtEditarApellidos, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarEmail(txtEditarEmail, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarTelefono(txtEditarTelefono, true)) {
            esValido = false;
        }

        if (!esValido) {
            DialogUtil.mostrarError("Campos invalidos", "Por favor, corrija los campos marcados en rojo");
            return;
        }

        final String cedula = txtEditarCedula.getText().trim();
        final String nombres = txtEditarNombres.getText().trim();
        final String apellidos = txtEditarApellidos.getText().trim();
        final String telefono = txtEditarTelefono.getText().trim();
        final String email = txtEditarEmail.getText().trim();
        final String direccion = txtEditarDireccion.getText().trim();

        loadingIndicator.setVisible(true);

        final Integer clienteId = clienteEnEdicion.getId();

        final Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                // Verificar si la cedula ya existe en otro cliente
                final Cliente existente = clienteService.getClienteByCedula(cedula);
                if (existente != null && !existente.getId().equals(clienteId)) {
                    return null; // Cedula ya existe en otro cliente
                }

                final Cliente cliente = new Cliente();
                cliente.setId(clienteId);
                cliente.setCedula(cedula);
                cliente.setNombres(nombres);
                cliente.setApellidos(apellidos);
                cliente.setTelefono(telefono);
                cliente.setEmail(email);
                cliente.setDireccion(direccion);

                return clienteService.actualizarCliente(cliente);
            }
        };

        task.setOnSucceeded(event -> {
            final Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                DialogUtil.mostrarError("Error", "Ya existe otro cliente con esta cedula");
                FormValidationUtil.marcarCampoInvalido(txtEditarCedula, "Cedula ya registrada");
            } else if (exito) {
                LOGGER.info("Cliente actualizado exitosamente: {} {}", nombres, apellidos);
                DialogUtil.mostrarExito("Cliente actualizado", "Los datos del cliente se han actualizado correctamente");
                cargarDatos();
                handleCancelarEdicion();
            } else {
                LOGGER.error("Error al actualizar cliente");
                DialogUtil.mostrarError("Error", "No se pudo actualizar el cliente");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al actualizar cliente", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error", "No se pudo actualizar el cliente");
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCancelarEdicion() {
        clienteEnEdicion = null;
        tabEditar.setDisable(true);
        tabPane.getSelectionModel().select(0);

        // Limpiar estilos de validacion
        FormValidationUtil.limpiarEstadoValidacion(txtEditarCedula);
        FormValidationUtil.limpiarEstadoValidacion(txtEditarNombres);
        FormValidationUtil.limpiarEstadoValidacion(txtEditarApellidos);
        FormValidationUtil.limpiarEstadoValidacion(txtEditarTelefono);
        FormValidationUtil.limpiarEstadoValidacion(txtEditarEmail);
    }

    private void eliminarCliente(final Cliente cliente) {
        LOGGER.info("Intentando eliminar cliente: {}", cliente.getNombreCompleto());

        final boolean confirmar = DialogUtil.mostrarConfirmacionPersonalizada(
                "Confirmar Eliminacion",
                "Esta seguro de eliminar este cliente?",
                "Cliente: " + cliente.getNombreCompleto() + "\nCedula: " + cliente.getCedula() + "\nEsta accion no se puede deshacer.",
                "Eliminar",
                "Cancelar"
        );

        if (confirmar) {
            final Integer clienteId = cliente.getId();

            loadingIndicator.setVisible(true);

            final Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return clienteService.eliminarCliente(clienteId);
                }
            };

            task.setOnSucceeded(event -> {
                final Boolean exito = task.getValue();
                loadingIndicator.setVisible(false);

                if (exito) {
                    LOGGER.info("Cliente eliminado exitosamente: {}", cliente.getNombreCompleto());
                    DialogUtil.mostrarExito("Cliente eliminado", "El cliente ha sido eliminado del sistema");
                    cargarDatos();
                } else {
                    LOGGER.error("Error al eliminar cliente: {}", cliente.getNombreCompleto());
                    DialogUtil.mostrarError("Error", "No se pudo eliminar el cliente");
                }
            });

            task.setOnFailed(event -> {
                LOGGER.error("Error al eliminar cliente", task.getException());
                loadingIndicator.setVisible(false);
                DialogUtil.mostrarError("Error", "No se pudo eliminar el cliente");
            });

            new Thread(task).start();
        }
    }

}
