package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Factura;
import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;

import com.example.ceragen_2.service.ClienteService;
import com.example.ceragen_2.service.FacturaService;

import com.example.ceragen_2.service.PacienteService;
import com.example.ceragen_2.service.ProfesionalService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador principal para la gestión de facturas.
 * Maneja la creación, visualización, listado y anulación de facturas.
 */
public class FacturaController {

    /** Logger para el controlador. */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(FacturaController.class);

    /** Formato de fecha y hora para mostrar en la interfaz. */
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /** Formato de fecha para mostrar en la interfaz. */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Servicio para operaciones relacionadas con clientes. */
    private final ClienteService clienteService = ClienteService.getInstance();

    /** Lista de clientes disponibles para facturación. */
    private List<Cliente> listaClientes;

    /** Lista de citas seleccionadas para la factura actual. */
    private List<Cita> listaCitas;

    /** Lista de facturas obtenidas del sistema. */
    private List<Factura> listaFacturas;

    // --- Componentes FXML ---

    /** Panel de pestañas principal. */
    @FXML private TabPane tabPane;

    // --- Campos para creación de factura ---

    /** Texto que muestra el número de la nueva factura. */
    @FXML private Text txtNumeroFacturaNueva;

    /** Campo de texto para la fecha de realización de la nueva factura. */
    @FXML private TextField txtFechaRealizacionFacturaNueva;

    /** Campo de texto para la ciudad de la nueva factura. */
    @FXML private TextField txtCiudadFacturaNueva;

    /** Combo box para seleccionar el cliente de la nueva factura. */
    @FXML private ComboBox<Cliente> cmbClienteFacturaNueva;

    /** Tabla que muestra las citas asociadas a la factura en creación. */
    @FXML private TableView<Cita> tableCitasFactura;

    /** Columna de la tabla que muestra el nombre del paciente. */
    @FXML private TableColumn<Cita, String> colCitaPaciente;

    /** Columna de la tabla que muestra el nombre del profesional. */
    @FXML private TableColumn<Cita, String> colCitaProfesional;

    /** Columna de la tabla que muestra la fecha de la cita. */
    @FXML private TableColumn<Cita, String> colCitaFecha;

    /** Columna de la tabla que muestra el motivo de la cita. */
    @FXML private TableColumn<Cita, String> colCitaMotivo;

    /** Columna de la tabla que muestra acciones para cada cita. */
    @FXML private TableColumn<Cita, Void> colCitaAcciones;

    /** Columna de la tabla que muestra el costo de la cita. */
    @FXML private TableColumn<Cita, String> colCitaCosto;

    /** Combo box para seleccionar el método de pago. */
    @FXML private ComboBox<String> cmbMetodoPagoFacturaNueva;

    /** Campo de texto que muestra el subtotal de la factura. */
    @FXML private TextField txtSubtotalFacturaNueva;

    /** Campo de texto que muestra el IVA de la factura. */
    @FXML private TextField txtIvaFacturaNueva;

    /** Campo de texto que muestra el descuento de la factura. */
    @FXML private TextField txtDescuentoFacturaNueva;

    /** Campo de texto que muestra el total de la factura. */
    @FXML private TextField txtTotalFacturaNueva;

    /** Botón para guardar la nueva factura. */
    @FXML private Button btnGuardarNuevaFactura;

    /** Botón para cancelar la creación de la nueva factura. */
    @FXML private Button btnCancelarNuevaFactura;

    // --- Campos para vista de factura ---

    /** Texto que muestra el número de factura. */
    @FXML private Text txtFacturaNumero;

    /** Campo de texto que muestra la fecha de realización. */
    @FXML private TextField txtFechaRealizacion;

    /** Campo de texto que muestra la ciudad. */
    @FXML private TextField txtCiudad;

    /** Campo de texto que muestra el nombre del cliente. */
    @FXML private TextField txtCliente;

    /** Campo de texto que muestra la cédula del cliente. */
    @FXML private TextField txtCedula;

    /** Campo de texto que muestra el teléfono del cliente. */
    @FXML private TextField txtTelefono;

    /** Campo de texto que muestra la dirección del cliente. */
    @FXML private TextField txtDireccion;

    /** Tabla que muestra los productos/servicios de la factura. */
    @FXML private TableView<Cita> tableProducts;

    /** Columna de la tabla que muestra el producto/servicio. */
    @FXML private TableColumn<Cita, String> colProducto;

    /** Columna de la tabla que muestra el precio unitario. */
    @FXML private TableColumn<Cita, String> colPrecio;

    /** Columna de la tabla que muestra la cantidad. */
    @FXML private TableColumn<Cita, String> colCantidad;

    /** Columna de la tabla que muestra el total por ítem. */
    @FXML private TableColumn<Cita, String> colTotal;

    /** Campo de texto que muestra el subtotal. */
    @FXML private TextField txtSubtotal;

    /** Campo de texto que muestra el IVA. */
    @FXML private TextField txtIva;

    /** Campo de texto que muestra el descuento. */
    @FXML private TextField txtDescuento;

    /** Campo de texto que muestra el total. */
    @FXML private TextField txtTotal;

    /** Campo de texto que muestra el método de pago. */
    @FXML private TextField txtMetodoPago;

    /** Botón para volver a la vista anterior. */
    @FXML private Button btnVolver;

    /** Botón para anular la factura (actualmente sin funcionalidad). */
    @FXML private Button btnAnular;

    // --- Campos para el tab de listar facturas ---

    /** Tabla que muestra la lista de facturas. */
    @FXML private TableView<Factura> tableFacturas;

    /** Columna de la tabla que muestra el número de factura. */
    @FXML private TableColumn<Factura, String> colNumeroFactura;

    /** Columna de la tabla que muestra el nombre del cliente. */
    @FXML private TableColumn<Factura, String> colCliente;

    /** Columna de la tabla que muestra la fecha de emisión. */
    @FXML private TableColumn<Factura, String> colFecha;

    /** Columna de la tabla que muestra el total a pagar. */
    @FXML private TableColumn<Factura, String> colTotalAPagar;

    /** Columna de la tabla que muestra el estado de la factura. */
    @FXML private TableColumn<Factura, String> colEstado;

    /** Columna de la tabla que muestra las acciones disponibles. */
    @FXML private TableColumn<Factura, Void> colAcciones;

    /** Combo box para filtrar facturas por estado. */
    @FXML private ComboBox<String> cmbFiltroEstado;

    // --- Campos para formulario de creación de cita (no utilizados en esta vista) ---

    /** Combo box para seleccionar paciente (no utilizado). */
    @FXML private ComboBox<Paciente> cmbCrearPaciente;

    /** Combo box para seleccionar profesional (no utilizado). */
    @FXML private ComboBox<Profesional> cmbCrearProfesional;

    /** Selector de fecha para creación de cita (no utilizado). */
    @FXML private DatePicker dpCrearFecha;

    /** Campo de texto para la hora de la cita (no utilizado). */
    @FXML private TextField txtCrearHora;

    /** Área de texto para el motivo de la cita (no utilizado). */
    @FXML private TextArea txtCrearMotivo;

    /**
     * Método de inicialización del controlador.
     * Se ejecuta automáticamente después de cargar el FXML.
     */
    @FXML
    public void initialize() {
        LOGGER.info("Inicializando FacturaController");
        // Inicializar las listas
        listaCitas = new ArrayList<>();
        listaFacturas = new ArrayList<>();

        cargarCatalogos();
        configurarTablaCitas();
        configurarTablaProductosFactura();
        configurarTablaFacturas();
        cargarFacturas();

        // Configurar ComboBox de filtro
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("TODAS", "ACTIVA", "ANULADA"));
        cmbFiltroEstado.setValue("TODAS");

        // Inicializar fecha actual
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaRealizacionFacturaNueva.setText(LocalDate.now().format(formatter));
        txtFechaRealizacion.setText(LocalDate.now().format(formatter));

        // Inicializar ciudad por defecto
        txtCiudadFacturaNueva.setText("Guayaquil");
        txtCiudad.setText("Guayaquil");

        // Inicializar valores por defecto
        txtSubtotalFacturaNueva.setText("$0.00");
        txtIvaFacturaNueva.setText("$0.00");
        txtDescuentoFacturaNueva.setText("$0.00");
        txtTotalFacturaNueva.setText("$0.00");

        txtSubtotal.setText("$0.00");
        txtIva.setText("$0.00");
        txtDescuento.setText("$0.00");
        txtTotal.setText("$0.00");
    }

    /**
     * Record interno para almacenar resultados de catálogos.
     */
    private record CatalogosResult(List<Cliente> clientes) {
    }

    /**
     * Configura las columnas de la tabla de facturas.
     */
    private void configurarTablaFacturas() {
        // Configurar columnas
        colNumeroFactura.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getNumeroFactura()));

        colCliente.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getClienteNombre()));

        colFecha.setCellValueFactory(data -> {
            if (data.getValue().getFechaEmision() != null) {
                return new SimpleStringProperty(
                        data.getValue().getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );
            }
            return new SimpleStringProperty("");
        });

        colTotalAPagar.setCellValueFactory(data -> {
            if (data.getValue().getTotal() != null) {
                return new SimpleStringProperty(String.format("$%.2f",
                        data.getValue().getTotal()));
            }
            return new SimpleStringProperty("$0.00");
        });

        colEstado.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEstado()));

        // Columna de acciones
        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");
            private final Button btnAnular = new Button("Anular");
            private final HBox pane = new HBox(5, btnVer, btnAnular);

            {
                pane.setAlignment(Pos.CENTER);

                btnVer.setStyle("-fx-background-color: #3498db; " +
                        "-fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3;"
                        + " -fx-padding: 5 10;");
                btnAnular.setStyle("-fx-background-color: #e74c3c;" +
                        " -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; "
                        + "-fx-padding: 5 10;");

                btnVer.setOnAction(event -> {
                    Factura factura = getTableView().getItems().get(getIndex());
                    verFactura(factura.getId());
                });

                btnAnular.setOnAction(event -> {
                    Factura factura = getTableView().getItems().get(getIndex());
                    anularFactura(factura.getId());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Factura factura = getTableView().getItems().get(getIndex());
                    // Solo mostrar botón Anular si la factura está activa
                    btnAnular.setDisable(!"ACTIVA".equals(factura.getEstado()));
                    setGraphic(pane);
                }
            }
        });
    }

    /**
     * Configura las columnas de la tabla de productos en la vista de factura.
     */
    private void configurarTablaProductosFactura() {
        // Configurar columnas de la tabla tableProducts.
        colProducto.setCellValueFactory(data -> {
            Cita cita = data.getValue();
            return new SimpleStringProperty(
                    cita.getProfesionalNombre() != null ? cita.getProfesionalNombre() : "Sin datos"
            );
        });

        colPrecio.setCellValueFactory(data -> {
            Cita cita = data.getValue();
            BigDecimal costo = cita.getCosto() != null ? cita.getCosto() : BigDecimal.ZERO;
            return new SimpleStringProperty(String.format("$%.2f", costo));
        });

        // Cantidad siempre 1 por cita.
        colCantidad.setCellValueFactory(data ->
                new SimpleStringProperty("1")
        );

        // Total = costo * cantidad (1).
        colTotal.setCellValueFactory(data -> {
            BigDecimal total = data.getValue().getCosto() != null ? data.getValue().getCosto() : BigDecimal.ZERO;
            return new SimpleStringProperty(String.format("$%.2f", total));
        });
    }

    /**
     * Carga las facturas desde el servicio y actualiza la tabla.
     */
    private void cargarFacturas() {
        Task<List<Factura>> task = new Task<>() {
            @Override
            protected List<Factura> call() {
                return FacturaService.getInstance().getAllFacturasResumen();
            }
        };

        task.setOnSucceeded(event -> {
            listaFacturas = task.getValue();
            actualizarTablaFacturas();
            LOGGER.info("Se cargaron {} facturas", listaFacturas.size());
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar facturas", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar las facturas");
        });

        new Thread(task).start();
    }

    /**
     * Actualiza la tabla de facturas con la lista actual.
     */
    private void actualizarTablaFacturas() {
        tableFacturas.getItems().setAll(listaFacturas);
    }

    /**
     * Maneja la búsqueda de facturas según el filtro de estado seleccionado.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleBuscarFacturas() {
        String estadoFiltro = cmbFiltroEstado.getValue();

        Task<List<Factura>> task = new Task<>() {
            @Override
            protected List<Factura> call() {
                if (estadoFiltro == null || "TODAS".equals(estadoFiltro)) {
                    return FacturaService.getInstance().getAllFacturasResumen();
                } else {
                    return FacturaService.getInstance().getFacturasPorEstado(estadoFiltro);
                }
            }
        };

        task.setOnSucceeded(event -> {
            listaFacturas = task.getValue();
            actualizarTablaFacturas();
            LOGGER.info("Filtro aplicado: {} facturas encontradas", listaFacturas.size());
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al filtrar facturas", task.getException());
            mostrarAlerta("Error", "No se pudieron filtrar las facturas");
        });

        new Thread(task).start();
    }

    /**
     * Muestra los detalles de una factura específica.
     *
     * @param facturaId ID de la factura a visualizar
     */
    private void verFactura(final Integer facturaId) {
        LOGGER.info("Abriendo vista de factura ID: {}", facturaId);
        // Cambiar al tab "Ver" y cargar los datos de la factura
        tabPane.getSelectionModel().select(2); // Índice del tab "Ver"
        cargarDatosFacturaCompleta(facturaId);
    }

    /**
     * Anula una factura específica.
     *
     * @param facturaId ID de la factura a anular
     */
    private void anularFactura(final Integer facturaId) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Anular Factura");
        confirmacion.setHeaderText("¿Está seguro de anular esta factura?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                Task<Boolean> task = new Task<>() {
                    @Override
                    protected Boolean call() {
                        return FacturaService.getInstance().anularFactura(facturaId);
                    }
                };

                task.setOnSucceeded(event -> {
                    boolean exito = task.getValue();
                    if (exito) {
                        LOGGER.info("Factura anulada exitosamente. ID: {}", facturaId);
                        mostrarAlerta("Éxito", "Factura anulada correctamente");
                        cargarFacturas(); // Recargar la lista
                    } else {
                        mostrarAlerta("Error", "No se pudo anular la factura");
                    }
                });

                task.setOnFailed(event -> {
                    LOGGER.error("Error al anular factura", task.getException());
                    mostrarAlerta("Error", "Error al anular la factura");
                });

                new Thread(task).start();
            }
        });
    }

    /**
     * Carga todos los datos de una factura específica para mostrar en la vista detallada.
     *
     * @param facturaId ID de la factura a cargar
     */
    private void cargarDatosFacturaCompleta(Integer facturaId) {
        LOGGER.info("Cargando datos completos de factura ID: {}", facturaId);

        Task<Factura> task = new Task<>() {
            @Override
            protected Factura call() {
                return FacturaService.getInstance().getFacturaById(facturaId);
            }
        };

        task.setOnSucceeded(event -> {
            Factura factura = task.getValue();
            if (factura != null) {
                LOGGER.info("Factura cargada: {}", factura);

                // Llenar los campos del tab "Ver"
                txtFacturaNumero.setText("Factura N° " + factura.getNumeroFactura());
                txtFechaRealizacion.setText(
                        factura.getFechaEmision().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                );

                txtCliente.setText(factura.getClienteNombre() != null ? factura.getClienteNombre() : "Sin cliente");
                txtCiudad.setText(factura.getCiudad() != null ? factura.getCiudad() : "-");
                var obj = clienteService.getClienteById(factura.getClienteId());
                txtCedula.setText(obj.getCedula());
                txtTelefono.setText(obj.getCedula());
                txtDireccion.setText(obj.getDireccion());

                txtSubtotal.setText(String.format("$%.2f", factura.getSubtotal() != null ? factura.getSubtotal() : 0.0));
                txtIva.setText(String.format("$%.2f", factura.getIva() != null ? factura.getIva() : 0.0));
                txtDescuento.setText(String.format("$%.2f", factura.getDescuento() != null ? factura.getDescuento() : 0.0));
                txtMetodoPago.setText(factura.getMetodoPago() != null ? factura.getMetodoPago() : "No hay datos");
                txtTotal.setText(String.format("$%.2f", factura.getTotal() != null ? factura.getTotal() : 0.0));

                // Llenar la tabla con las citas asociadas
                if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
                    ObservableList<Cita> citas = FXCollections.observableArrayList(factura.getDetalles());
                    tableProducts.setItems(citas);
                } else {
                    tableProducts.setItems(FXCollections.observableArrayList());
                    LOGGER.warn("La factura ID {} no tiene citas asociadas", facturaId);
                }

            } else {
                LOGGER.warn("No se encontró factura con ID {}", facturaId);
                mostrarAlerta("Aviso", "No se encontró información para la factura seleccionada.");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar factura completa", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los datos de la factura.");
        });

        new Thread(task).start();
    }

    /**
     * Configura las columnas de la tabla de citas en el formulario de creación.
     */
    private void configurarTablaCitas() {
        // Configurar las columnas existentes.
        colCitaPaciente.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPacienteNombre()));

        colCitaProfesional.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getProfesionalNombre()));

        colCitaFecha.setCellValueFactory(data -> {
            if (data.getValue().getFechaHora() != null) {
                return new SimpleStringProperty(data.getValue().getFechaHora().format(DATE_TIME_FORMATTER));
            }
            return new SimpleStringProperty("");
        });

        colCitaMotivo.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getMotivo()));

        // Columna para el costo.
        colCitaCosto.setCellValueFactory(data -> {
            if (data.getValue().getCosto() != null) {
                return new SimpleStringProperty(String.format("$%.2f", data.getValue().getCosto()));
            }
            return new SimpleStringProperty("$0.00");
        });

        // Columna de acciones (eliminar cita).
        colCitaAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnEliminar.setOnAction(event -> {
                    Cita cita = getTableView().getItems().get(getIndex());
                    eliminarCita(cita);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnEliminar);
                }
            }
        });
    }

    /**
     * Agrega una nueva cita a la lista de citas para la factura actual.
     *
     * @param nuevaCita La cita a agregar
     */
    public void agregarCita(Cita nuevaCita) {
        this.listaCitas.add(nuevaCita);
        actualizarTablaCitas();
        calcularTotales();
        LOGGER.info("Cita agregada. Total citas: {}", listaCitas.size());
    }

    /**
     * Actualiza la tabla de citas con la lista actual.
     */
    private void actualizarTablaCitas() {
        tableCitasFactura.getItems().setAll(listaCitas);
        // Forzar actualización del layout
        tableCitasFactura.requestLayout();
        LOGGER.info("Tabla de citas actualizada con {} citas", listaCitas.size());
    }

    /**
     * Elimina una cita de la lista de citas para la factura actual.
     *
     * @param cita La cita a eliminar
     */
    private void eliminarCita(Cita cita) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Cita");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta cita?");
        confirmacion.setContentText("Paciente: " + cita.getPacienteNombre());

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                listaCitas.remove(cita);
                actualizarTablaCitas();
                calcularTotales();
                LOGGER.info("Cita eliminada");
            }
        });
    }

    /**
     * Calcula los totales de la factura basándose en las citas seleccionadas.
     */
    private void calcularTotales() {
        // Calcular subtotal sumando los costos de todas las citas
        double subtotal = listaCitas.stream()
                .mapToDouble(cita -> cita.getCosto() != null ? cita.getCosto().doubleValue() : 0.0)
                .sum();

        double iva = subtotal * 0.15; // 15% IVA
        double descuento = 0.0;
        double total = subtotal + iva - descuento;

        // Actualizar campos
        txtSubtotalFacturaNueva.setText(String.format("$%.2f", subtotal));
        txtIvaFacturaNueva.setText(String.format("$%.2f", iva));
        txtDescuentoFacturaNueva.setText(String.format("$%.2f", descuento));
        txtTotalFacturaNueva.setText(String.format("$%.2f", total));

        LOGGER.info("Totales calculados - Subtotal: ${}, IVA: ${}, Total: ${}", subtotal, iva, total);
    }

    /**
     * Carga los catálogos necesarios para el funcionamiento del controlador.
     */
    private void cargarCatalogos() {
        Task<CatalogosResult> task = new Task<>() {
            @Override
            protected CatalogosResult call() {
                List<Cliente> clientes = clienteService.getAllClientes();
                return new CatalogosResult(clientes);
            }
        };

        task.setOnSucceeded(event -> {
            CatalogosResult resultado = task.getValue();
            listaClientes = resultado.clientes;

            // Configurar ComboBox de filtros
            cmbClienteFacturaNueva.setItems(FXCollections.observableArrayList(listaClientes));
            cmbMetodoPagoFacturaNueva.setItems(FXCollections.observableArrayList("EFECTIVO", "TARJETA", "TRANSFERENCIA"));
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar catálogos", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los catálogos");
        });

        new Thread(task).start();
    }

    /**
     * Maneja la apertura del modal para crear una nueva cita.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleModal() {
        try {
            LOGGER.info("Abriendo modal para crear cita...");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/ceragen_2/views/modalNuevaCita.fxml"));
            Parent root = loader.load();

            // Obtener el controlador del modal
            CrearCitaController crearCitaController = loader.getController();

            // Pasar la referencia de FacturaController al modal
            crearCitaController.setFacturaController(this);

            Stage stage = new Stage();
            stage.setTitle("Crear Nueva Cita");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setResizable(false);

            stage.showAndWait();

            LOGGER.info("Modal de crear cita cerrado. Citas agregadas: {}", listaCitas != null ? listaCitas.size() : 0);

        } catch (Exception e) {
            LOGGER.error("Error al abrir modal de crear cita", e);
            mostrarAlerta("Error", "No se pudo abrir la ventana para crear citas: " + e.getMessage());
        }
    }

    /**
     * Maneja la cancelación de la creación de una nueva factura.
     */
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleCancelar() {
        LOGGER.info("Cancelando creación de factura");

        // Verificar si hay datos no guardados
        if (!listaCitas.isEmpty() || cmbClienteFacturaNueva.getValue() != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Cancelar Factura");
            alert.setHeaderText("¿Está seguro que desea cancelar?");
            alert.setContentText("Se perderán todos los datos ingresados.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    limpiarFormularioCreacion();
                    cambiarATabListado();
                    LOGGER.info("Creación de factura cancelada");
                } else {
                    LOGGER.info("Cancelación abortada por el usuario");
                }
            });
        } else {
            // Si no hay datos, simplemente cambiar al tab de listado
            cambiarATabListado();
        }
    }

    /**
     * Limpia el formulario de creación de factura.
     */
    private void limpiarFormularioCreacion() {
        // Limpiar lista de citas
        listaCitas.clear();
        actualizarTablaCitas();

        // Limpiar selecciones
        cmbClienteFacturaNueva.setValue(null);
        cmbMetodoPagoFacturaNueva.setValue(null);

        // Limpiar campos de texto
        txtCiudadFacturaNueva.setText("Guayaquil");

        // Restablecer totales
        txtSubtotalFacturaNueva.setText("$0.00");
        txtIvaFacturaNueva.setText("$0.00");
        txtDescuentoFacturaNueva.setText("$0.00");
        txtTotalFacturaNueva.setText("$0.00");

        // Restablecer fecha actual
        txtFechaRealizacionFacturaNueva.setText(LocalDate.now().format(DATE_FORMATTER));

        LOGGER.info("Formulario de creación de factura limpiado");
    }

    /**
     * Cambia al tab de listado de facturas.
     */
    private void cambiarATabListado() {
        // Cambiar al tab de listado (primero)
        if (tabPane != null && tabPane.getTabs().size() > 0) {
            tabPane.getSelectionModel().select(0);
            LOGGER.info("Cambiado al tab de listado de facturas");
        }
    }

    /**
     * Maneja la creación de una nueva factura.
     */
    @FXML
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    private void handleCrearFactura() {
        if (!validarCampos()) {
            return;
        }

        // Validar que haya citas
        if (listaCitas == null || listaCitas.isEmpty()) {
            mostrarAlerta("Error", "Debe agregar al menos una cita a la factura");
            return;
        }

        int fctIdCliente = cmbClienteFacturaNueva.getValue().getId();
        String fctCiudad = txtCiudadFacturaNueva.getText().trim();

        try {
            double fctSubtotal = Double.parseDouble(limpiarNumero(txtSubtotalFacturaNueva.getText()));
            double fctIva = Double.parseDouble(limpiarNumero(txtIvaFacturaNueva.getText()));
            double fctDescuento = Double.parseDouble(limpiarNumero(txtDescuentoFacturaNueva.getText()));
            double fctTotal = Double.parseDouble(limpiarNumero(txtTotalFacturaNueva.getText()));

            String metodoPago = cmbMetodoPagoFacturaNueva.getValue();

            // Verificar los valores que se enviarán al servicio.
            LOGGER.info("DEBUG - Enviando al servicio - Subtotal: {}, IVA: {}, Descuento: {}, Total: {}",
                    fctSubtotal, fctIva, fctDescuento, fctTotal);

            // Usar el método que crea factura con citas.
            Integer facturaId = FacturaService.getInstance().crearFactura(
                    fctIdCliente, fctCiudad, fctSubtotal, fctIva, fctDescuento, fctTotal,
                    metodoPago, listaCitas
            );

            if (facturaId != null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Factura creada");
                alert.setHeaderText(null);
                alert.setContentText("Factura creada correctamente con " + listaCitas.size() + " citas.\nID de Factura: " + facturaId);
                alert.showAndWait();

                // Limpiar el formulario después de crear
                limpiarFormulario();
            } else {
                mostrarAlerta("Error", "No se pudo crear la factura.");
            }

        } catch (NumberFormatException e) {
            LOGGER.error("Error en handleCrearFactura al parsear números: {}", e.getMessage());
            mostrarAlerta("Error", "Error interno al procesar los valores numéricos.");
        }
    }

    /**
     * Limpia el formulario después de crear una factura.
     */
    private void limpiarFormulario() {
        // Limpiar lista de citas
        listaCitas.clear();
        actualizarTablaCitas();
        // Limpiar selección de cliente
        cmbClienteFacturaNueva.setValue(null);
        // Resetear totales
        calcularTotales();

        LOGGER.info("Formulario de factura limpiado");
    }

    /**
     * Muestra una alerta al usuario.
     *
     * @param titulo El título de la alerta
     * @param mensaje El mensaje de la alerta
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    /**
     * Valida los campos del formulario de creación de factura.
     *
     * @return true si todos los campos son válidos, false en caso contrario
     */
    private boolean validarCampos() {
        // Validar cliente seleccionado
        if (cmbClienteFacturaNueva.getValue() == null) {
            mostrarAlerta("Error", "Debe seleccionar un cliente");
            return false;
        }
        // Validar que haya citas
        if (listaCitas == null || listaCitas.isEmpty()) {
            mostrarAlerta("Error", "Debe agregar al menos una cita");
            return false;
        }
        String ciudad = txtCiudadFacturaNueva.getText().trim();
        // Limpiar los valores numéricos de símbolos y caracteres no numéricos
        String subtotal = limpiarNumero(txtSubtotalFacturaNueva.getText());
        String iva = limpiarNumero(txtIvaFacturaNueva.getText());
        String descuento = limpiarNumero(txtDescuentoFacturaNueva.getText());
        String total = limpiarNumero(txtTotalFacturaNueva.getText());
        String metodoPago = cmbMetodoPagoFacturaNueva.getValue();

        // Validaciones básicas vacíos
        if (ciudad.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe ingresar la ciudad.");
            return false;
        }
        if (subtotal.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe ingresar el subtotal.");
            return false;
        }
        if (iva.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe ingresar el IVA.");
            return false;
        }
        if (descuento.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe ingresar el descuento (0 si no aplica).");
            return false;
        }
        if (total.isEmpty()) {
            mostrarAlerta("Campo requerido", "Debe ingresar el total.");
            return false;
        }
        if (metodoPago == null) {
            mostrarAlerta("Campo requerido", "Debe seleccionar un método de pago.");
            return false;
        }

        // Validar números
        try {
            Double.parseDouble(subtotal);
            Double.parseDouble(iva);
            Double.parseDouble(descuento);
            Double.parseDouble(total);
        } catch (NumberFormatException e) {
            LOGGER.error("Error al parsear números - "
                            + "Subtotal: {}, IVA: {}, Descuento: {}, Total: {}",
                            subtotal, iva, descuento, total);
            mostrarAlerta("Valor inválido", "Subtotal, "
                    + "IVA, descuento y total deben ser números válidos.");
            return false;
        }

        return true;
    }

    /**
     * Limpia un texto numérico eliminando símbolos de moneda y comas.
     *
     * @param texto El texto a limpiar
     * @return El texto limpio, listo para parsear a número
     */
    private String limpiarNumero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        // Remover símbolos de moneda, comas y espacios
        return texto.replace("$", "")
                .replace(",", ".")
                .trim();
    }
}