package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.service.ClienteService;
import com.example.ceragen_2.util.DialogUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class ModalClienteController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClienteController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final ClienteService clienteService = ClienteService.getInstance();
    // Indicador de carga
    @FXML private VBox loadingIndicator;
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

    private FacturaController facturaController; // Referencia al controlador padre

    // Filtros
    @FXML private TextField txtBuscar;

    @FXML
    public void initialize() {
        //LOGGER.info("Inicializando modulo de Clientes");
        configurarTabla();
        cargarDatos();
    }

    // Método para recibir la referencia del FacturaController
    public void setFacturaController(final FacturaController facturaController) {
        this.facturaController = facturaController;
        LOGGER.info("Referencia de FacturaController recibida");
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
            private final Button btnSeleccionar = new Button("Seleccionar");
            private final HBox pane = new HBox(8, btnSeleccionar);

            {
                pane.setAlignment(Pos.CENTER);

                // Usar clases CSS en lugar de estilos inline
                btnSeleccionar.getStyleClass().addAll("btn-table-action", "btn-table-edit");

                btnSeleccionar.setOnAction(event -> {
                    final Cliente cliente = getTableView().getItems().get(getIndex());

                    // Enviar cliente al controlador padre
                    facturaController.agregarCliente(cliente);

                    // Mostrar confirmación
                    DialogUtil.mostrarExito(
                            "Cliente seleccionado",
                            "Se seleccionó correctamente el cliente:\n"
                                    + cliente.getNombres() + " " + cliente.getApellidos()
                    );

                    // Cerrar el modal
                    Stage stage = (Stage) btnSeleccionar.getScene().getWindow();
                    stage.close();
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
}
