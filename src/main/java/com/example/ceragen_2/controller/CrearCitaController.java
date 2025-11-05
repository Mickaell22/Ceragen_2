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
    private static final Logger logger = LoggerFactory.getLogger(CrearCitaController.class);
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
        logger.info("Inicializando CrearCitaController");
        cargarCatalogos();
        // Configurar fecha actual por defecto
        dpCrearFecha.setValue(LocalDate.now());
    }

    // Método para recibir la referencia del FacturaController
    public void setFacturaController(FacturaController facturaController) {
        this.facturaController = facturaController;
        logger.info("Referencia de FacturaController recibida");
    }

    private void cargarCatalogos() {
        Task<CatalogosResult> task = new Task<>() {
            @Override
            protected CatalogosResult call() {
                List<Paciente> pacientes = pacienteService.getAllPacientes();
                List<Profesional> profesionales = profesionalService.getAllProfesionales();
                return new CatalogosResult(pacientes, profesionales);
            }
        };

        task.setOnSucceeded(event -> {
            CatalogosResult resultado = task.getValue();
            listaPacientes = resultado.pacientes;
            listaProfesionales = resultado.profesionales;

            cmbCrearPaciente.setItems(FXCollections.observableArrayList(listaPacientes));
            cmbCrearProfesional.setItems(FXCollections.observableArrayList(listaProfesionales));

            logger.info("Catálogos cargados: {} pacientes, {} profesionales",
                    listaPacientes.size(), listaProfesionales.size());
        });

        task.setOnFailed(event -> {
            logger.error("Error al cargar catálogos", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los catálogos");
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCrearCita() {
        logger.info("Intentando crear nueva cita...");

        if (!validarCampos()) {
            return;
        }

        try {
            Paciente paciente = cmbCrearPaciente.getValue();
            Profesional profesional = cmbCrearProfesional.getValue();
            LocalDate fecha = dpCrearFecha.getValue();
            LocalTime hora = LocalTime.parse(txtCrearHora.getText().trim(), TIME_FORMATTER);
            LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);
            String motivo = txtCrearMotivo.getText().trim();

            // Obtener el costo de la especialidad del profesional
            EspecialidadService especialidadService = EspecialidadService.getInstance();
            Especialidad especialidad = especialidadService.getEspecialidadById(profesional.getEspecialidadId());

            if (especialidad == null) {
                mostrarAlerta("Error", "No se pudo obtener el costo de la especialidad");
                return;
            }

            // Crear nueva cita con el costo de la especialidad
            Cita nuevaCita = new Cita();
            nuevaCita.setPacienteId(paciente.getId());
            nuevaCita.setProfesionalId(profesional.getId());
            nuevaCita.setFechaHora(fechaHora);
            nuevaCita.setMotivo(motivo);
            nuevaCita.setEstado("PENDIENTE");
            nuevaCita.setPacienteNombre(paciente.getNombreCompleto());
            nuevaCita.setProfesionalNombre(profesional.getNombreCompleto());
            nuevaCita.setCosto(especialidad.getCostoConsulta()); // Asignar costo de la especialidad

            // Agregar cita a la factura
            if (facturaController != null) {
                facturaController.agregarCita(nuevaCita);
                logger.info("Cita agregada a la factura. Costo: {}", nuevaCita.getCosto());
            } else {
                logger.error("No hay referencia a FacturaController");
                mostrarAlerta("Error", "No se pudo conectar con la factura");
                return;
            }

            // Mostrar éxito y cerrar
            mostrarAlerta("Éxito", "Cita creada correctamente\nProfesional: " + profesional.getNombreCompleto() +
                    "\nEspecialidad: " + especialidad.getNombre() +
                    "\nCosto: $" + nuevaCita.getCosto());
            cerrarVentana();

        } catch (DateTimeParseException e) {
            mostrarAlerta("Error", "Formato de hora inválido. Use HH:mm (ej: 14:30)");
        } catch (Exception e) {
            logger.error("Error al crear cita", e);
            mostrarAlerta("Error", "No se pudo crear la cita: " + e.getMessage());
        }
    }

    @FXML
    private void handleLimpiar() {
        logger.info("Limpiando formulario de cita");
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
        if (txtCrearHora.getText().trim().isEmpty()) {
            mostrarAlerta("Error", "Ingrese la hora");
            return false;
        }
        if (txtCrearMotivo.getText().trim().isEmpty()) {
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
            Stage stage = (Stage) txtCrearMotivo.getScene().getWindow(); // Usa cualquier control que sí esté inicializado
            stage.close();
            logger.info("Ventana cerrada exitosamente");
        } catch (Exception e) {
            logger.error("Error al cerrar ventana: {}", e.getMessage());
            // Fallback: cerrar la aplicación si no se puede obtener el stage
            Platform.exit();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private record CatalogosResult(List<Paciente> pacientes, List<Profesional> profesionales) {
    }
}