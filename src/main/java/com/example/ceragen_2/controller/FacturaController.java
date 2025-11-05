package com.example.ceragen_2.controller;
import com.example.ceragen_2.model.*;
import com.example.ceragen_2.service.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FacturaController {
    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final ClienteService clienteService = ClienteService.getInstance();
    private final PacienteService pacienteService = PacienteService.getInstance();
    private final ProfesionalService profesionalService = ProfesionalService.getInstance();

    private List<Cliente> listaClientes;
    private List<Cita> listaCitas;

    //CAMPOS PARA CREACION DE FACTURA
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


    //CAMPOS PARA VISTA DE FACTURA
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
    @FXML private TableView<?> tableProducts;
    @FXML private TableColumn<?, ?> colProducto;
    @FXML private TableColumn<?, ?> colPrecio;
    @FXML private TableColumn<?, ?> colCantidad;
    @FXML private TableColumn<?, ?> colTotal;
    // Resumen
    @FXML private TextField txtSubtotal;
    @FXML private TextField txtIva;
    @FXML private TextField txtDescuento;
    @FXML private TextField txtTotal;
    // Botones
    @FXML private Button btnVolver;
    @FXML private Button btnAnular;

    // Formulario Crear
    @FXML private ComboBox<Paciente> cmbCrearPaciente;
    @FXML private ComboBox<Profesional> cmbCrearProfesional;
    @FXML private DatePicker dpCrearFecha;
    @FXML private TextField txtCrearHora;
    @FXML private TextArea txtCrearMotivo;

    @FXML
    public void initialize() {
        logger.info("Inicializando FacturaController");
        // Inicializar la lista de citas
        listaCitas = new ArrayList<>();
        cargarCatalogos();
        configurarTablaCitas();

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

        // Nueva columna para el costo
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

    @FXML
    private void handleVolver() {
        logger.info("Volviendo desde vista de factura");
        // Cerrar la ventana actual
        if (btnVolver.getScene() != null && btnVolver.getScene().getWindow() != null) {
            btnVolver.getScene().getWindow().hide();
        }
    }

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

    public void setListaCitas(List<Cita> listaCitas) {
        this.listaCitas = listaCitas;
    }

    public List<Cita> getListaCitas() {
        return this.listaCitas;
    }

    @FXML
    private void handleAnular() {
        logger.info("Intentando anular factura");

        // Mostrar confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Anular Factura");
        alert.setHeaderText("¿Está seguro que desea anular esta factura?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logger.info("Factura anulada");
                // TODO: Implementar lógica de anulación en base de datos

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Factura Anulada");
                info.setHeaderText(null);
                info.setContentText("La factura ha sido anulada exitosamente.");
                info.showAndWait();

                // Cerrar la ventana después de anular
                handleVolver();
            } else {
                logger.info("Anulación cancelada por el usuario");
            }
        });
    }

    @FXML
    private void handleCancelar() {
        logger.info("Cancelando factura");

        // Mostrar confirmación
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Cancelando Factura");
        alert.setHeaderText("¿Está seguro que desea cancelar esta factura?");
        alert.setContentText("Esta acción no se puede deshacer.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                logger.info("Factura cancelada");
                // TODO: Implementar lógica de anulación en base de datos

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Factura cancelada");
                info.setHeaderText(null);
                info.setContentText("La factura ha sido cancelada");
                info.showAndWait();

                // Cerrar la ventana después de anular
                handleVolver();
            } else {
                logger.info("Anulación cancelada por el usuario");
            }
        });
    }

    @FXML
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

        // Quitar el símbolo $ antes de parsear
        double fctSubtotal = Double.parseDouble(txtSubtotalFacturaNueva.getText().replace("$", "").trim());
        double fctIva = Double.parseDouble(txtIvaFacturaNueva.getText().replace("$", "").trim());
        double fctDescuento = Double.parseDouble(txtDescuentoFacturaNueva.getText().replace("$", "").trim());
        double fctTotal = Double.parseDouble(txtTotalFacturaNueva.getText().replace("$", "").trim());

        String metodoPago = cmbMetodoPagoFacturaNueva.getValue();

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

        // Resto de validaciones...
        String ciudad = txtCiudadFacturaNueva.getText().trim();
        String subtotal = txtSubtotalFacturaNueva.getText().replace("$", "").trim();
        String iva = txtIvaFacturaNueva.getText().replace("$", "").trim();
        String descuento = txtDescuentoFacturaNueva.getText().replace("$", "").trim();
        String total = txtTotalFacturaNueva.getText().replace("$", "").trim();
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
            mostrarAlerta("Valor inválido", "Subtotal, IVA, descuento y total deben ser números.");
            return false;
        }

        return true;
    }


    // Método para cargar datos de factura (será llamado desde otros controladores)
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

    // Método sobrecargado que usa la ciudad por defecto
    public void cargarDatosFactura(String numeroFactura, String cliente, String cedula,
                                   String telefono, String direccion,
                                   double subtotal, double iva, double descuento, double total) {
        cargarDatosFactura(numeroFactura, cliente, cedula, telefono, direccion,
                "Guayaquil-Ecuador", subtotal, iva, descuento, total);
    }
}
