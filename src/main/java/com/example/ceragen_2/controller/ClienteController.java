package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.service.ClienteService;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

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
    @FXML private TableColumn<Cliente, String> colId;
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
        cargarDatos();
    }

    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
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
            private final HBox pane = new HBox(10, btnEditar, btnEliminar);

            {
                pane.setAlignment(Pos.CENTER);

                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");

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
            mostrarAlerta("Error", "No se pudieron cargar los datos", Alert.AlertType.ERROR);
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
            mostrarAlerta("Error", "No se pudo realizar la busqueda", Alert.AlertType.ERROR);
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
        final String cedula = txtCrearCedula.getText().trim();
        final String nombres = txtCrearNombres.getText().trim();
        final String apellidos = txtCrearApellidos.getText().trim();
        final String telefono = txtCrearTelefono.getText().trim();
        final String email = txtCrearEmail.getText().trim();
        final String direccion = txtCrearDireccion.getText().trim();

        // Validaciones
        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()) {
            mostrarAlerta("Error", "Cedula, nombres y apellidos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

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
                mostrarAlerta("Error", "Ya existe un cliente con esta cedula", Alert.AlertType.ERROR);
            } else if (exito) {
                LOGGER.info("Cliente creado exitosamente: {} {}", nombres, apellidos);
                mostrarAlerta("Exito", "Cliente creado exitosamente", Alert.AlertType.INFORMATION);
                limpiarFormularioCrear();
                cargarDatos();
                tabPane.getSelectionModel().select(0);
            } else {
                LOGGER.error("Error al crear cliente");
                mostrarAlerta("Error", "No se pudo crear el cliente", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al crear cliente", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo crear el cliente", Alert.AlertType.ERROR);
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

        final String cedula = txtEditarCedula.getText().trim();
        final String nombres = txtEditarNombres.getText().trim();
        final String apellidos = txtEditarApellidos.getText().trim();
        final String telefono = txtEditarTelefono.getText().trim();
        final String email = txtEditarEmail.getText().trim();
        final String direccion = txtEditarDireccion.getText().trim();

        // Validaciones
        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()) {
            mostrarAlerta("Error", "Cedula, nombres y apellidos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

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
                mostrarAlerta("Error", "Ya existe otro cliente con esta cedula", Alert.AlertType.ERROR);
            } else if (exito) {
                LOGGER.info("Cliente actualizado exitosamente: {} {}", nombres, apellidos);
                mostrarAlerta("Exito", "Cliente actualizado exitosamente", Alert.AlertType.INFORMATION);
                cargarDatos();
                handleCancelarEdicion();
            } else {
                LOGGER.error("Error al actualizar cliente");
                mostrarAlerta("Error", "No se pudo actualizar el cliente", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al actualizar cliente", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo actualizar el cliente", Alert.AlertType.ERROR);
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

        final Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminacion");
        confirmacion.setHeaderText("Esta seguro de eliminar este cliente?");
        confirmacion.setContentText("Cliente: " + cliente.getNombreCompleto() + "\nCedula: " + cliente.getCedula() + "\nEsta accion no se puede deshacer.");

        final Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
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
                    mostrarAlerta("Exito", "Cliente eliminado exitosamente", Alert.AlertType.INFORMATION);
                    cargarDatos();
                } else {
                    LOGGER.error("Error al eliminar cliente: {}", cliente.getNombreCompleto());
                    mostrarAlerta("Error", "No se pudo eliminar el cliente", Alert.AlertType.ERROR);
                }
            });

            task.setOnFailed(event -> {
                LOGGER.error("Error al eliminar cliente", task.getException());
                loadingIndicator.setVisible(false);
                mostrarAlerta("Error", "No se pudo eliminar el cliente", Alert.AlertType.ERROR);
            });

            new Thread(task).start();
        }
    }

    private void mostrarAlerta(final String titulo, final String mensaje, final Alert.AlertType tipo) {
        final Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
