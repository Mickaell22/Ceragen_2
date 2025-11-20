package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Especialidad;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;
import com.example.ceragen_2.service.EspecialidadService;
import com.example.ceragen_2.service.PacienteService;
import com.example.ceragen_2.service.ProfesionalService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class CrearCitaController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CrearCitaController.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final PacienteService pacienteService = PacienteService.getInstance();
    private final ProfesionalService profesionalService = ProfesionalService.getInstance();

    @FXML private ComboBox<Paciente> cmbCrearPaciente;
    @FXML private ComboBox<Profesional> cmbCrearProfesional;
    @FXML private DatePicker dpCrearFecha;
    @FXML private TextField txtCrearHora;
    @FXML private TextArea txtCrearMotivo;
    @FXML private Button btnCrearCita;
    @FXML private Button btnLimpiar;

    private List<Paciente> listaPacientes;
    private List<Profesional> listaProfesionales;
    private FacturaController facturaController; // Referencia al controlador padre

    @FXML
    public void initialize() {
        LOGGER.info("Inicializando CrearCitaController");
        cargarCatalogos();
        // Configurar fecha actual por defecto
        dpCrearFecha.setValue(LocalDate.now());
    }

    // Método para recibir la referencia del FacturaController
    public void setFacturaController(final FacturaController facturaController) {
        this.facturaController = facturaController;
        LOGGER.info("Referencia de FacturaController recibida");
    }

    private void cargarCatalogos() {
        final Task<CatalogosResult> task = new Task<>() {
            @Override
            protected CatalogosResult call() {
                final List<Paciente> pacientes = pacienteService.getAllPacientes();
                final List<Profesional> profesionales = profesionalService.getAllProfesionales();
                return new CatalogosResult(pacientes, profesionales);
            }
        };

        task.setOnSucceeded(event -> {
            final CatalogosResult resultado = task.getValue();
            listaPacientes = resultado.pacientes;
            listaProfesionales = resultado.profesionales;

            cmbCrearPaciente.setItems(FXCollections.observableArrayList(listaPacientes));
            cmbCrearProfesional.setItems(FXCollections.observableArrayList(listaProfesionales));

            LOGGER.info("Catálogos cargados: {} pacientes, {} profesionales",
                    listaPacientes.size(), listaProfesionales.size());
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar catálogos", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los catálogos");
        });

        new Thread(task).start();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleCrearCita() {
        LOGGER.info("Intentando crear nueva cita...");

        if (!validarCampos()) {
            return;
        }

        try {
            final Paciente paciente = cmbCrearPaciente.getValue();
            final Profesional profesional = cmbCrearProfesional.getValue();
            final LocalDate fecha = dpCrearFecha.getValue();
            final LocalTime hora = LocalTime.parse(txtCrearHora.getText().trim(), TIME_FORMATTER);
            final LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            final String motivo = txtCrearMotivo.getText().trim();

            // DEBUG: Mostrar todos los valores
            //LOGGER.info("DEBUG - Paciente ID: {}", paciente != null ? paciente.getId() : "NULL");
            //LOGGER.info("DEBUG - Profesional ID: {}", profesional != null ? profesional.getId() : "NULL");
            //LOGGER.info("DEBUG - Profesional Especialidad: {}", profesional != null ? profesional.getEspecialidadId() : "NULL");
            //LOGGER.info("DEBUG - FechaHora: {}", fechaHora);
            //LOGGER.info("DEBUG - Motivo: {}", motivo);

            // Obtener el costo de la especialidad
            final EspecialidadService especialidadService = EspecialidadService.getInstance();
            //LOGGER.info("DEBUG - EspecialidadService: {}", especialidadService != null ? "especialidadService Activo" : "NULL");
            //LOGGER.info("DEBUG - profesional: {}", profesional != null ? "existe profesional" : "NULL");
            //LOGGER.info("DEBUG - ID de la especialidad: {}", profesional.getEspecialidadId() != null ? "existe especialidad" : "NULL");
            final Especialidad especialidad = especialidadService.getEspecialidadById(profesional.getEspecialidadId());

            //LOGGER.info("DEBUG - Especialidad: {}", especialidad != null ? especialidad.getNombre() : "NULL");
            //LOGGER.info("DEBUG - Costo: {}", especialidad != null ? especialidad.getCostoConsulta() : "NULL");

            if (especialidad == null) {
                mostrarAlerta("Error", "No se pudo obtener el costo de la especialidad");
                return;
            }

            // Crear nueva cita
            final Cita nuevaCita = new Cita();
            nuevaCita.setPacienteId(paciente.getId());
            nuevaCita.setProfesionalId(profesional.getId());
            nuevaCita.setFechaHora(fechaHora);
            nuevaCita.setMotivo(motivo);
            nuevaCita.setCosto(especialidad.getCostoConsulta());
            nuevaCita.setPacienteNombre(paciente.getNombreCompleto());
            nuevaCita.setProfesionalNombre(profesional.getNombreCompleto());

            // DEBUG: Verificar la cita creada
            LOGGER.info("DEBUG - Cita creada - PacienteID: {}, ProfesionalID: {}, Costo: {}",
                    nuevaCita.getPacienteId(), nuevaCita.getProfesionalId(), nuevaCita.getCosto());

            // Agregar cita a la factura
            if (facturaController != null) {
                facturaController.agregarCita(nuevaCita);
                LOGGER.info("Cita agregada exitosamente a la factura");
            } else {
                LOGGER.error("No hay referencia a FacturaController");
                mostrarAlerta("Error", "No se pudo conectar con la factura");
                return;
            }

            // Mostrar éxito y cerrar
            mostrarAlerta("Éxito", "Cita creada correctamente\nCosto: $" + nuevaCita.getCosto());
            cerrarVentana();

        } catch (DateTimeParseException e) {
            LOGGER.error("Error de formato de hora: {}", e.getMessage());
            mostrarAlerta("Error", "Formato de hora inválido. Use HH:mm (ej: 14:30)");
        } catch (Exception e) {
            LOGGER.error("Error detallado al crear cita:", e);
            mostrarAlerta("Error", "No se pudo crear la cita: " + e.getMessage());
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLimpiar() {
        LOGGER.info("Limpiando formulario de cita");
        cmbCrearPaciente.setValue(null);
        cmbCrearProfesional.setValue(null);
        dpCrearFecha.setValue(LocalDate.now());
        txtCrearHora.clear();
        txtCrearMotivo.clear();
    }

    private boolean validarCampos() {
        if (cmbCrearPaciente.getValue() == null) {
            mostrarAlerta("Error", "Seleccione un paciente");
            return false;
        }
        if (cmbCrearProfesional.getValue() == null) {
            mostrarAlerta("Error", "Seleccione un profesional");
            return false;
        }
        if (dpCrearFecha.getValue() == null) {
            mostrarAlerta("Error", "Seleccione una fecha");
            return false;
        }
        if (txtCrearHora.getText().isBlank()) {
            mostrarAlerta("Error", "Ingrese la hora");
            return false;
        }
        if (txtCrearMotivo.getText().isBlank()) {
            mostrarAlerta("Error", "Ingrese el motivo de la consulta");
            return false;
        }

        // Validar formato de hora
        try {
            LocalTime.parse(txtCrearHora.getText().trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            mostrarAlerta("Error", "Formato de hora inválido. Use HH:mm (ej: 14:30)");
            return false;
        }

        return true;
    }

    private void cerrarVentana() {
        try {
            // Obtener el Stage desde cualquier nodo de la escena
            final Stage stage = (Stage) txtCrearMotivo.getScene().getWindow(); // Usa cualquier control que sí esté inicializado
            stage.close();
            LOGGER.info("Ventana cerrada exitosamente");
        } catch (Exception e) {
            LOGGER.error("Error al cerrar ventana: {}", e.getMessage());
            // Fallback: cerrar la aplicación si no se puede obtener el stage
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

    private record CatalogosResult(List<Paciente> pacientes, List<Profesional> profesionales) {
    }
}