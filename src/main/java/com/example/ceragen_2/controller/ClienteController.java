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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.scene.control.Tooltip;

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
    @FXML private TextField txtBuscar;

    // Tabla
    @FXML private TableView<Cliente> tableClientes;
    @FXML private TableColumn<Cliente, String> colCedula;
    @FXML private TableColumn<Cliente, String> colNombres;
    @FXML private TableColumn<Cliente, String> colApellidos;
    @FXML private TableColumn<Cliente, String> colTelefono;
    @FXML private TableColumn<Cliente, String> colEmail;
    @FXML private TableColumn<Cliente, String> colFechaRegistro;
    @FXML private TableColumn<Cliente, Void> colAcciones;

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

    @FXML
    public void initialize() {
        LOGGER.info("Inicializando modulo de Clientes");

        configurarTabla();
        configurarValidaciones();
        configurarTooltips();
        cargarDatos();
    }

    private void configurarTooltips() {
        // Tooltips para filtros
        txtBuscar.setTooltip(new Tooltip("Buscar clientes por cedula, nombre o apellido"));

        // Tooltips para formulario de creacion
        txtCrearCedula.setTooltip(new Tooltip("Numero de cedula del cliente (solo digitos)"));
        txtCrearNombres.setTooltip(new Tooltip("Nombres del cliente (solo letras)"));
        txtCrearApellidos.setTooltip(new Tooltip("Apellidos del cliente (solo letras)"));
        txtCrearTelefono.setTooltip(new Tooltip("Numero de telefono (7-15 digitos)"));
        txtCrearEmail.setTooltip(new Tooltip("Correo electronico (formato: usuario@dominio.com)"));
        txtCrearDireccion.setTooltip(new Tooltip("Direccion completa del cliente (opcional)"));

        // Tooltips para formulario de edicion
        txtEditarCedula.setTooltip(new Tooltip("Numero de cedula del cliente (solo digitos)"));
        txtEditarNombres.setTooltip(new Tooltip("Nombres del cliente (solo letras)"));
        txtEditarApellidos.setTooltip(new Tooltip("Apellidos del cliente (solo letras)"));
        txtEditarTelefono.setTooltip(new Tooltip("Numero de telefono (7-15 digitos)"));
        txtEditarEmail.setTooltip(new Tooltip("Correo electronico (formato: usuario@dominio.com)"));
        txtEditarDireccion.setTooltip(new Tooltip("Direccion completa del cliente (opcional)"));
    }

    private void configurarValidaciones() {
        // Aplicar filtros de entrada a campos de creacion
        FormValidationUtil.aplicarFiltroSoloDigitos(txtCrearCedula);
        FormValidationUtil.aplicarFiltroSoloDigitos(txtCrearTelefono);
        FormValidationUtil.aplicarFiltroSoloLetras(txtCrearNombres);
        FormValidationUtil.aplicarFiltroSoloLetras(txtCrearApellidos);

        // Aplicar filtros de entrada a campos de edicion
        FormValidationUtil.aplicarFiltroSoloDigitos(txtEditarCedula);
        FormValidationUtil.aplicarFiltroSoloDigitos(txtEditarTelefono);
        FormValidationUtil.aplicarFiltroSoloLetras(txtEditarNombres);
        FormValidationUtil.aplicarFiltroSoloLetras(txtEditarApellidos);

        // Configurar validacion en tiempo real para email
        txtCrearEmail.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Cuando pierde el foco
                FormValidationUtil.validarEmail(txtCrearEmail, true);
            }
        });

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

        final Task<List<Cliente>> task = new Task<>() {
            @Override
            protected List<Cliente> call() {
                return clienteService.getAllClientesCompletos();
            }
        };

        task.setOnSucceeded(event -> {
            final List<Cliente> clientes = task.getValue();
            tableClientes.getItems().clear();
            tableClientes.getItems().addAll(clientes);
            LOGGER.info("Datos cargados: {} clientes", clientes.size());
            loadingIndicator.setVisible(false);
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar datos", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error de conexion", "No se pudieron cargar los datos. Verifique su conexion.");
        });

        new Thread(task).start();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscar() {
        final String criterio = txtBuscar.getText().trim();

        if (criterio.isEmpty()) {
            cargarDatos();
            return;
        }

        LOGGER.info("Buscando clientes con criterio: {}", criterio);
        loadingIndicator.setVisible(true);

        final Task<List<Cliente>> task = new Task<>() {
            @Override
            protected List<Cliente> call() {
                return clienteService.buscarClientes(criterio);
            }
        };

        task.setOnSucceeded(event -> {
            final List<Cliente> clientes = task.getValue();
            tableClientes.getItems().clear();
            tableClientes.getItems().addAll(clientes);
            LOGGER.info("Busqueda completa: {} clientes encontrados", clientes.size());
            loadingIndicator.setVisible(false);
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al buscar clientes", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error de busqueda", "No se pudo realizar la busqueda");
        });

        new Thread(task).start();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLimpiarFiltros() {
        LOGGER.info("Limpiando filtros");
        txtBuscar.clear();
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleCrearCliente() {
        // Validaciones con feedback visual
        boolean esValido = true;

        if (!FormValidationUtil.validarCampoRequerido(txtCrearCedula, true)) {
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

        if (!FormValidationUtil.validarCampoRequerido(txtEditarCedula, true)) {
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
