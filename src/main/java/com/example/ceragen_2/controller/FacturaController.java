package com.example.ceragen_2.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class FacturaController {
    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);

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

    @FXML
    public void initialize() {
        logger.info("Inicializando FacturaController");

        // Inicializar fecha actual
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtFechaRealizacion.setText(LocalDate.now().format(formatter));

        // Inicializar ciudad por defecto
        txtCiudad.setText("Guayaquil-Ecuador");

        // Inicializar valores por defecto
        txtSubtotal.setText("$0.00");
        txtIva.setText("$0.00");
        txtDescuento.setText("$0.00");
        txtTotal.setText("$0.00");
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
