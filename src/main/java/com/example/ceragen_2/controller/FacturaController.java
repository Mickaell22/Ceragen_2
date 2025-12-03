package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Factura;
import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;

import com.example.ceragen_2.service.ClienteService;
import com.example.ceragen_2.service.FacturaService;

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

public class FacturaController {
    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final ClienteService clienteService = ClienteService.getInstance();

    private List<Cliente> listaClientes;
    private List<Cita> listaCitas;
    private List<Factura> listaFacturas; // AÑADIDO: Variable faltante

    // AÑADIDO: TabPane principal
    @FXML private TabPane tabPane;

    // CAMPOS PARA CREACION DE FACTURA
    // Encabezado
    @FXML private Text txtNumeroFacturaNueva;
    // Primera fila
    @FXML private TextField txtFechaRealizacionFacturaNueva;
    @FXML private TextField txtCiudadFacturaNueva;
    // Segunda fila - Datos del cliente
    @FXML private ComboBox<Cliente> cmbClienteFacturaNueva;
    // Tabla de productos
    @FXML private TableView<Cita> tableCitasFactura;
    @FXML private TableColumn<Cita, String> colCitaPaciente;
    @FXML private TableColumn<Cita, String> colCitaProfesional;
    @FXML private TableColumn<Cita, String> colCitaFecha;
    @FXML private TableColumn<Cita, String> colCitaMotivo;
    @FXML private TableColumn<Cita, Void> colCitaAcciones;
    @FXML private TableColumn<Cita, String> colCitaCosto;
    // Resumen
    @FXML private ComboBox<String> cmbMetodoPagoFacturaNueva;
    @FXML private TextField txtSubtotalFacturaNueva;
    @FXML private TextField txtIvaFacturaNueva;
    @FXML private TextField txtDescuentoFacturaNueva;
    @FXML private TextField txtTotalFacturaNueva;
    @FXML private Button btnGuardarNuevaFactura;
    @FXML private Button btnCancelarNuevaFactura;

    // CAMPOS PARA VISTA DE FACTURA
    // Encabezado
    @FXML private Text txtFacturaNumero;
    // Primera fila
    @FXML private TextField txtFechaRealizacion;
    @FXML private TextField txtCiudad;
    // Segunda fila - Datos del cliente
    @FXML private TextField txtCliente;
    @FXML private TextField txtCedula;
    // Tercera fila
    @FXML private TextField txtTelefono;
    @FXML private TextField txtDireccion;
    // Tabla de productos
    @FXML private TableView<Cita> tableProducts;
    @FXML private TableColumn<Cita, String> colProducto;
    @FXML private TableColumn<Cita, String> colPrecio;
    @FXML private TableColumn<Cita, String> colCantidad;
    @FXML private TableColumn<Cita, String> colTotal;
    // Resumen
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtIva;
    @FXML private TextField txtDescuento;
    @FXML private TextField txtTotal;
    @FXML private TextField txtMetodoPago;
    // Botones
    @FXML private Button btnVolver;
    @FXML private Button btnAnular;

    // Campos para el tab Listar
    @FXML private TableView<Factura> tableFacturas;
    @FXML private TableColumn<Factura, String> colNumeroFactura;
    @FXML private TableColumn<Factura, String> colCliente;
    @FXML private TableColumn<Factura, String> colFecha;
    @FXML private TableColumn<Factura, String> colTotalAPagar;
    @FXML private TableColumn<Factura, String> colEstado;
    @FXML private TableColumn<Factura, Void> colAcciones;

    @FXML private ComboBox<String> cmbFiltroEstado; // CORREGIDO: Especificar tipo

    // Formulario Crear
    @FXML private ComboBox<Paciente> cmbCrearPaciente;
    @FXML private ComboBox<Profesional> cmbCrearProfesional;
    @FXML private DatePicker dpCrearFecha;
    @FXML private TextField txtCrearHora;
    @FXML private TextArea txtCrearMotivo;

    @FXML
    public void initialize() {
        logger.info("Inicializando FacturaController");
        // Inicializar las listas
        listaCitas = new ArrayList<>();
        listaFacturas = new ArrayList<>(); // AÑADIDO: Inicializar lista de facturas

        cargarCatalogos();
        configurarTablaCitas();
        configurarTablaProductosFactura();
        configurarTablaFacturas();
        cargarFacturas();

        // AÑADIDO: Configurar ComboBox de filtro
        cmbFiltroEstado.setItems(FXCollections.observableArrayList("TODAS", "ACTIVA", "ANULADA"));
        cmbFiltroEstado.setValue("TODAS");

        // Inicializar fecha actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaRealizacionFacturaNueva.setText(LocalDate.now().format(formatter));
        txtFechaRealizacion.setText(LocalDate.now().format(formatter));

        // Inicializar ciudad por defecto
        txtCiudadFacturaNueva.setText("Guayaquil-Ecuador");
        txtCiudad.setText("Guayaquil-Ecuador");

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

    private record CatalogosResult(List<Cliente> clientes) {
    }

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

        // CORREGIDO: Usar colTotalAPagar en lugar de colTotal
        colTotalAPagar.setCellValueFactory(data -> {
            if (data.getValue().getTotal() != null) {
                return new SimpleStringProperty(String.format("$%.2f", data.getValue().getTotal()));
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

                btnVer.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnAnular.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");

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

    private void configurarTablaProductosFactura() {
        // --- Configurar columnas de la tabla tableProducts ---
        colProducto.setCellValueFactory(data -> {
            Cita cita = data.getValue();
            // Mostramos el nombre del servicio o motivo
            return new SimpleStringProperty(
                    cita.getProfesionalNombre() != null ? cita.getProfesionalNombre() : "Sin datos"
            );
        });

        colPrecio.setCellValueFactory(data -> {
            Cita cita = data.getValue();
            BigDecimal costo = cita.getCosto() != null ? cita.getCosto() : BigDecimal.ZERO;
            return new SimpleStringProperty(String.format("$%.2f", costo));
        });

        // Cantidad siempre 1 por cita
        colCantidad.setCellValueFactory(data ->
                new SimpleStringProperty("1")
        );

        // Total = costo * cantidad (1)
        colTotal.setCellValueFactory(data -> {
            BigDecimal total = data.getValue().getCosto() != null ? data.getValue().getCosto() : BigDecimal.ZERO;
            return new SimpleStringProperty(String.format("$%.2f", total));
        });
    }

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
            logger.info("Se cargaron {} facturas", listaFacturas.size());
        });

        task.setOnFailed(event -> {
            logger.error("Error al cargar facturas", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar las facturas");
        });

        new Thread(task).start();
    }

    private void actualizarTablaFacturas() {
        tableFacturas.getItems().setAll(listaFacturas);
    }

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
            logger.info("Filtro aplicado: {} facturas encontradas", listaFacturas.size());
        });

        task.setOnFailed(event -> {
            logger.error("Error al filtrar facturas", task.getException());
            mostrarAlerta("Error", "No se pudieron filtrar las facturas");
        });

        new Thread(task).start();
    }

    private void verFactura(final Integer facturaId) {
        logger.info("Abriendo vista de factura ID: {}", facturaId);
        // Cambiar al tab "Ver" y cargar los datos de la factura
        tabPane.getSelectionModel().select(2); // Índice del tab "Ver"
        cargarDatosFacturaCompleta(facturaId);
    }

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
                        logger.info("Factura anulada exitosamente. ID: {}", facturaId);
                        mostrarAlerta("Éxito", "Factura anulada correctamente");
                        cargarFacturas(); // Recargar la lista
                    } else {
                        mostrarAlerta("Error", "No se pudo anular la factura");
                    }
                });

                task.setOnFailed(event -> {
                    logger.error("Error al anular factura", task.getException());
                    mostrarAlerta("Error", "Error al anular la factura");
                });

                new Thread(task).start();
            }
        });
    }

    private void cargarDatosFacturaCompleta(Integer facturaId) {
        logger.info("Cargando datos completos de factura ID: {}", facturaId);

        Task<Factura> task = new Task<>() {
            @Override
            protected Factura call() {
                return FacturaService.getInstance().getFacturaById(facturaId);
            }
        };

        task.setOnSucceeded(event -> {
            Factura factura = task.getValue();
            if (factura != null) {
                logger.info("Factura cargada: {}", factura);

                // --- Llenar los campos del tab "Ver" ---
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

                // --- Llenar la tabla con las citas asociadas ---
                if (factura.getDetalles() != null && !factura.getDetalles().isEmpty()) {
                    ObservableList<Cita> citas = FXCollections.observableArrayList(factura.getDetalles());
                    tableProducts.setItems(citas);
                } else {
                    tableProducts.setItems(FXCollections.observableArrayList());
                    logger.warn("La factura ID {} no tiene citas asociadas", facturaId);
                }

            } else {
                logger.warn("No se encontró factura con ID {}", facturaId);
                mostrarAlerta("Aviso", "No se encontró información para la factura seleccionada.");
            }
        });

        task.setOnFailed(event -> {
            logger.error("Error al cargar factura completa", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los datos de la factura.");
        });

        new Thread(task).start();
    }

    private void configurarTablaCitas() {
        // Configurar las columnas existentes...
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

        // columna para el costo
        colCitaCosto.setCellValueFactory(data -> {
            if (data.getValue().getCosto() != null) {
                return new SimpleStringProperty(String.format("$%.2f", data.getValue().getCosto()));
            }
            return new SimpleStringProperty("$0.00");
        });

        // Columna de acciones (eliminar cita)
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

    public void agregarCita(Cita nuevaCita){
        this.listaCitas.add(nuevaCita);
        actualizarTablaCitas();
        calcularTotales();
        logger.info("Cita agregada. Total citas: {}", listaCitas.size());
    }

    private void actualizarTablaCitas() {
        tableCitasFactura.getItems().setAll(listaCitas);
        // Forzar actualización del layout
        tableCitasFactura.requestLayout();
        logger.info("Tabla de citas actualizada con {} citas", listaCitas.size());
    }

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
                logger.info("Cita eliminada");
            }
        });
    }

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

        logger.info("Totales calculados - Subtotal: ${}, IVA: ${}, Total: ${}", subtotal, iva, total);
    }

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
            logger.error("Error al cargar catálogos", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los catálogos");
        });

        new Thread(task).start();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleModal(){
        try {
            logger.info("Abriendo modal para crear cita...");

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

            logger.info("Modal de crear cita cerrado. Citas agregadas: {}", listaCitas != null ? listaCitas.size() : 0);

        } catch (Exception e) {
            logger.error("Error al abrir modal de crear cita", e);
            mostrarAlerta("Error", "No se pudo abrir la ventana para crear citas: " + e.getMessage());
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleCancelar() {
        logger.info("Cancelando creación de factura");

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
                    logger.info("Creación de factura cancelada");
                } else {
                    logger.info("Cancelación abortada por el usuario");
                }
            });
        } else {
            // Si no hay datos, simplemente cambiar al tab de listado
            cambiarATabListado();
        }
    }

    private void limpiarFormularioCreacion() {
        // Limpiar lista de citas
        listaCitas.clear();
        actualizarTablaCitas();

        // Limpiar selecciones
        cmbClienteFacturaNueva.setValue(null);
        cmbMetodoPagoFacturaNueva.setValue(null);

        // Limpiar campos de texto
        txtCiudadFacturaNueva.setText("Guayaquil-Ecuador");

        // Restablecer totales
        txtSubtotalFacturaNueva.setText("$0.00");
        txtIvaFacturaNueva.setText("$0.00");
        txtDescuentoFacturaNueva.setText("$0.00");
        txtTotalFacturaNueva.setText("$0.00");

        // Restablecer fecha actual
        txtFechaRealizacionFacturaNueva.setText(LocalDate.now().format(DATE_FORMATTER));

        logger.info("Formulario de creación de factura limpiado");
    }

    private void cambiarATabListado() {
        // Cambiar al tab de listado (primero)
        if (tabPane != null && tabPane.getTabs().size() > 0) {
            tabPane.getSelectionModel().select(0);
            logger.info("Cambiado al tab de listado de facturas");
        }
    }

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

            // DEBUG: Verificar los valores que se enviarán al servicio
            logger.info("DEBUG - Enviando al servicio - Subtotal: {}, IVA: {}, Descuento: {}, Total: {}",
                    fctSubtotal, fctIva, fctDescuento, fctTotal);

            // Usar el nuevo método que crea factura con citas
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
            logger.error("Error en handleCrearFactura al parsear números: {}", e.getMessage());
            mostrarAlerta("Error", "Error interno al procesar los valores numéricos.");
        }
    }

    private void limpiarFormulario() {
        // Limpiar lista de citas
        listaCitas.clear();
        actualizarTablaCitas();
        // Limpiar selección de cliente
        cmbClienteFacturaNueva.setValue(null);
        // Resetear totales
        calcularTotales();

        logger.info("Formulario de factura limpiado");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

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
            logger.error("Error al parsear números - Subtotal: {}, IVA: {}, Descuento: {}, Total: {}",
                    subtotal, iva, descuento, total);
            mostrarAlerta("Valor inválido", "Subtotal, IVA, descuento y total deben ser números válidos.");
            return false;
        }

        return true;
    }

    private String limpiarNumero(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return "";
        }
        // Remover símbolos de moneda, comas y espacios
        return texto.replace("$", "")
                .replace(",", ".")
                .trim();
    }

    public void cargarDatosFactura(String numeroFactura, String cliente, String cedula,
                                   String telefono, String direccion, String ciudad,
                                   double subtotal, double iva, double descuento, double total) {
        txtFacturaNumero.setText("Factura N° " + numeroFactura);
        txtCliente.setText(cliente);
        txtCedula.setText(cedula);
        txtTelefono.setText(telefono);
        txtDireccion.setText(direccion);
        txtCiudad.setText(ciudad);
        txtSubtotal.setText(String.format("$%.2f", subtotal));
        txtIva.setText(String.format("$%.2f", iva));
        txtDescuento.setText(String.format("$%.2f", descuento));
        txtTotal.setText(String.format("$%.2f", total));
    }

    public void cargarDatosFactura(String numeroFactura, String cliente, String cedula,
                                   String telefono, String direccion,
                                   double subtotal, double iva, double descuento, double total) {
        cargarDatosFactura(numeroFactura, cliente, cedula, telefono, direccion,
                "Guayaquil-Ecuador", subtotal, iva, descuento, total);
    }
}
