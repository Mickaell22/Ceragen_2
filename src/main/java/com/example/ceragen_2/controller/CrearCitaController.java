package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Especialidad;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;
import com.example.ceragen_2.service.EspecialidadService;
import com.example.ceragen_2.service.PacienteService;
import com.example.ceragen_2.service.ProfesionalService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class CrearCitaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrearCitaController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PacienteService pacienteService = PacienteService.getInstance();
    private final ProfesionalService profesionalService = ProfesionalService.getInstance();

    @FXML private TextField txtCrearCedulaPaciente;
    @FXML private Label lblCrearPaciente;
    @FXML private TextField txtCrearCedulaProfesional;
    @FXML private Label lblCrearProfesional;
    @FXML private DatePicker dpCrearFecha;
    @FXML private ComboBox<String> cmbCrearHora;
    @FXML private TextArea txtCrearMotivo;
    @FXML private Button btnCrearCita;
    @FXML private Button btnLimpiar;

    private Paciente pacienteSeleccionado;
    private Profesional profesionalSeleccionado;
    private FacturaController facturaController;

    @FXML
    public void initialize() {
        LOGGER.info("Inicializando CrearCitaController");
        configurarDatePicker();
    }

    private void configurarDatePicker() {
        final LocalDate manana = LocalDate.now().plusDays(1);
        dpCrearFecha.setValue(manana);

        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(final LocalDate item, final boolean empty) {
                        super.updateItem(item, empty);
                        if (item.isBefore(manana)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #d3d3d3;");
                        }
                    }
                };
            }
        };
        dpCrearFecha.setDayCellFactory(dayCellFactory);
    }

    public void setFacturaController(final FacturaController facturaController) {
        this.facturaController = facturaController;
        LOGGER.info("Referencia de FacturaController recibida");
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscarPaciente() {
        final String cedula = txtCrearCedulaPaciente.getText().trim();
        if (cedula.isEmpty()) {
            mostrarAlerta("Campo vacio", "Ingrese una cedula para buscar");
            return;
        }

        final Paciente paciente = pacienteService.getPacienteByCedula(cedula);
        if (paciente != null) {
            pacienteSeleccionado = paciente;
            lblCrearPaciente.setText(paciente.getNombreCompleto());
            lblCrearPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #d4edda; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        } else {
            pacienteSeleccionado = null;
            lblCrearPaciente.setText("No encontrado");
            lblCrearPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #f8d7da; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
            mostrarAlerta("No encontrado", "No se encontro paciente con cedula: " + cedula);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscarProfesional() {
        final String cedula = txtCrearCedulaProfesional.getText().trim();
        if (cedula.isEmpty()) {
            mostrarAlerta("Campo vacio", "Ingrese una cedula para buscar");
            return;
        }

        final Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
        if (profesional != null) {
            profesionalSeleccionado = profesional;
            lblCrearProfesional.setText(profesional.getNombreCompleto());
            lblCrearProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #d4edda; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        } else {
            profesionalSeleccionado = null;
            lblCrearProfesional.setText("No encontrado");
            lblCrearProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #f8d7da; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
            mostrarAlerta("No encontrado", "No se encontro profesional con cedula: " + cedula);
        }
    }

    @FXML
    @SuppressWarnings({"unused", "PMD.AvoidCatchingGenericException"})
    private void handleCrearCita() {
        LOGGER.info("Intentando crear nueva cita...");

        if (!validarCampos()) {
            return;
        }

        try {
            final Paciente paciente = pacienteSeleccionado;
            final Profesional profesional = profesionalSeleccionado;
            final LocalDate fecha = dpCrearFecha.getValue();
            final LocalTime hora = LocalTime.parse(cmbCrearHora.getValue(), TIME_FORMATTER);
            final LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            final String motivo = txtCrearMotivo.getText().trim();

            final EspecialidadService especialidadService = EspecialidadService.getInstance();
            final Especialidad especialidad = especialidadService.getEspecialidadById(profesional.getEspecialidadId());

            if (especialidad == null) {
                mostrarAlerta("Error", "No se pudo obtener el costo de la especialidad");
                return;
            }

            final Cita nuevaCita = new Cita();
            nuevaCita.setPacienteId(paciente.getId());
            nuevaCita.setProfesionalId(profesional.getId());
            nuevaCita.setFechaHora(fechaHora);
            nuevaCita.setMotivo(motivo);
            nuevaCita.setCosto(especialidad.getCostoConsulta());
            nuevaCita.setPacienteNombre(paciente.getNombreCompleto());
            nuevaCita.setProfesionalNombre(profesional.getNombreCompleto());

            LOGGER.info("Cita creada - PacienteID: {}, ProfesionalID: {}, Costo: {}",
                    nuevaCita.getPacienteId(), nuevaCita.getProfesionalId(), nuevaCita.getCosto());

            if (facturaController != null) {
                facturaController.agregarCita(nuevaCita);
                LOGGER.info("Cita agregada exitosamente a la factura");
            } else {
                LOGGER.error("No hay referencia a FacturaController");
                mostrarAlerta("Error", "No se pudo conectar con la factura");
                return;
            }

            mostrarAlerta("Exito", "Cita creada correctamente\nCosto: $" + nuevaCita.getCosto());
            cerrarVentana();

        } catch (Exception e) {
            LOGGER.error("Error al crear cita:", e);
            mostrarAlerta("Error", "No se pudo crear la cita: " + e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLimpiar() {
        LOGGER.info("Limpiando formulario de cita");
        pacienteSeleccionado = null;
        profesionalSeleccionado = null;
        txtCrearCedulaPaciente.clear();
        txtCrearCedulaProfesional.clear();
        lblCrearPaciente.setText("Sin seleccionar");
        lblCrearPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        lblCrearProfesional.setText("Sin seleccionar");
        lblCrearProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        dpCrearFecha.setValue(LocalDate.now().plusDays(1));
        cmbCrearHora.setValue(null);
        txtCrearMotivo.clear();
    }

    private boolean validarCampos() {
        if (pacienteSeleccionado == null) {
            mostrarAlerta("Error", "Debe buscar y seleccionar un paciente por cedula");
            lblCrearPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #f8d7da; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
            return false;
        }
        if (profesionalSeleccionado == null) {
            mostrarAlerta("Error", "Debe buscar y seleccionar un profesional por cedula");
            lblCrearProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #f8d7da; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
            return false;
        }
        if (dpCrearFecha.getValue() == null) {
            mostrarAlerta("Error", "Seleccione una fecha");
            return false;
        }
        if (cmbCrearHora.getValue() == null) {
            mostrarAlerta("Error", "Seleccione una hora");
            return false;
        }
        if (txtCrearMotivo.getText().isBlank()) {
            mostrarAlerta("Error", "Ingrese el motivo de la consulta");
            return false;
        }

        return true;
    }

    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    private void cerrarVentana() {
        try {
            final Stage stage = (Stage) txtCrearMotivo.getScene().getWindow();
            stage.close();
            LOGGER.info("Ventana cerrada exitosamente");
        } catch (Exception e) {
            LOGGER.error("Error al cerrar ventana: {}", e.getMessage());
            Platform.exit();
        }
    }

    private void mostrarAlerta(final String titulo, final String mensaje) {
        final Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
