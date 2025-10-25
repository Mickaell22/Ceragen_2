package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;
import com.example.ceragen_2.service.CitaService;
import com.example.ceragen_2.service.PacienteService;
import com.example.ceragen_2.service.ProfesionalService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

public class CitasController {
    private static final Logger logger = LoggerFactory.getLogger(CitasController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final CitaService citaService = CitaService.getInstance();
    private final PacienteService pacienteService = PacienteService.getInstance();
    private final ProfesionalService profesionalService = ProfesionalService.getInstance();

    private int paginaActual = 0;
    private int registrosPorPagina = 10;
    private int totalPaginas = 0;

    @FXML private TabPane tabPane;
    @FXML private Tab tabCrear;
    @FXML private Tab tabEditar;

    // Filtros
    @FXML private ComboBox<Paciente> cmbPacienteFiltro;
    @FXML private ComboBox<Profesional> cmbProfesionalFiltro;
    @FXML private ComboBox<String> cmbEstadoFiltro;

    // Tabla
    @FXML private TableView<Cita> tableCitas;
    @FXML private TableColumn<Cita, String> colId;
    @FXML private TableColumn<Cita, String> colPaciente;
    @FXML private TableColumn<Cita, String> colProfesional;
    @FXML private TableColumn<Cita, String> colFechaHora;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private TableColumn<Cita, String> colMotivo;
    @FXML private TableColumn<Cita, Void> colAcciones;

    // Paginación
    @FXML private Button btnPrimera;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnUltima;
    @FXML private Text txtPaginacion;
    @FXML private ComboBox<String> cmbRegistrosPorPagina;
    @FXML private VBox loadingIndicator;

    // Formulario Crear
    @FXML private ComboBox<Paciente> cmbCrearPaciente;
    @FXML private ComboBox<Profesional> cmbCrearProfesional;
    @FXML private DatePicker dpCrearFecha;
    @FXML private TextField txtCrearHora;
    @FXML private TextArea txtCrearMotivo;

    // Formulario Editar
    @FXML private TextField txtEditarId;
    @FXML private ComboBox<Paciente> cmbEditarPaciente;
    @FXML private ComboBox<Profesional> cmbEditarProfesional;
    @FXML private DatePicker dpEditarFecha;
    @FXML private TextField txtEditarHora;
    @FXML private TextArea txtEditarMotivo;
    @FXML private ComboBox<String> cmbEditarEstado;
    @FXML private TextArea txtEditarObservaciones;

    private Cita citaEnEdicion;
    private List<Paciente> listaPacientes;
    private List<Profesional> listaProfesionales;

    @FXML
    public void initialize() {
        logger.info("Inicializando módulo de Citas");
        configurarTabla();
        configurarFiltros();
        configurarPaginacion();
        cargarCatalogos();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
        colPaciente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPacienteNombre()));
        colProfesional.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProfesionalNombre()));
        colFechaHora.setCellValueFactory(data -> {
            if (data.getValue().getFechaHora() != null) {
                return new SimpleStringProperty(data.getValue().getFechaHora().format(DATE_TIME_FORMATTER));
            }
            return new SimpleStringProperty("");
        });
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));
        colMotivo.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getMotivo() != null && data.getValue().getMotivo().length() > 50 ?
            data.getValue().getMotivo().substring(0, 50) + "..." : data.getValue().getMotivo()
        ));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final Button btnConfirmar = new Button("Confirmar");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox pane = new HBox(8, btnEditar, btnEliminar, btnConfirmar, btnCancelar);

            {
                pane.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnConfirmar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");

                btnEditar.setOnAction(event -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(event -> eliminarCita(getTableView().getItems().get(getIndex())));
                btnConfirmar.setOnAction(event -> cambiarEstado(getTableView().getItems().get(getIndex()), "CONFIRMADA"));
                btnCancelar.setOnAction(event -> cambiarEstado(getTableView().getItems().get(getIndex()), "CANCELADA"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void configurarFiltros() {
        cmbEstadoFiltro.setValue("TODOS");
    }

    private void configurarPaginacion() {
        cmbRegistrosPorPagina.setValue("10");
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

            // Configurar ComboBox de filtros
            cmbPacienteFiltro.setItems(FXCollections.observableArrayList(listaPacientes));
            cmbProfesionalFiltro.setItems(FXCollections.observableArrayList(listaProfesionales));

            // Configurar ComboBox de formularios
            cmbCrearPaciente.setItems(FXCollections.observableArrayList(listaPacientes));
            cmbCrearProfesional.setItems(FXCollections.observableArrayList(listaProfesionales));
            cmbEditarPaciente.setItems(FXCollections.observableArrayList(listaPacientes));
            cmbEditarProfesional.setItems(FXCollections.observableArrayList(listaProfesionales));

            cargarDatos();
        });

        task.setOnFailed(event -> {
            logger.error("Error al cargar catálogos", task.getException());
            mostrarAlerta("Error", "No se pudieron cargar los catálogos", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void cargarDatos() {
        loadingIndicator.setVisible(true);
        deshabilitarControles(true);

        final Integer pacienteId = cmbPacienteFiltro.getValue() != null ? cmbPacienteFiltro.getValue().getId() : null;
        final Integer profesionalId = cmbProfesionalFiltro.getValue() != null ? cmbProfesionalFiltro.getValue().getId() : null;
        final String estadoFilter = cmbEstadoFiltro.getValue();
        final int offset = paginaActual * registrosPorPagina;

        Task<DatosCitasResult> task = new Task<>() {
            @Override
            protected DatosCitasResult call() {
                int totalRegistros = citaService.countCitas(pacienteId, profesionalId, estadoFilter, null, null);
                int totalPaginasTemp = (int) Math.ceil((double) totalRegistros / registrosPorPagina);
                if (totalPaginasTemp == 0) totalPaginasTemp = 1;

                List<Cita> citas = citaService.getCitas(offset, registrosPorPagina, pacienteId, profesionalId, estadoFilter, null, null);
                return new DatosCitasResult(citas, totalPaginasTemp);
            }
        };

        task.setOnSucceeded(event -> {
            DatosCitasResult resultado = task.getValue();
            totalPaginas = resultado.totalPaginas;

            if (paginaActual >= totalPaginas) paginaActual = Math.max(0, totalPaginas - 1);

            tableCitas.getItems().clear();
            tableCitas.getItems().addAll(resultado.citas);
            actualizarInfoPaginacion();

            logger.info("Datos cargados: {} citas en página {}/{}", resultado.citas.size(), paginaActual + 1, totalPaginas);
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
        });

        task.setOnFailed(event -> {
            logger.error("Error al cargar datos", task.getException());
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
            mostrarAlerta("Error", "No se pudieron cargar los datos", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void deshabilitarControles(boolean deshabilitar) {
        btnPrimera.setDisable(deshabilitar);
        btnAnterior.setDisable(deshabilitar);
        btnSiguiente.setDisable(deshabilitar);
        btnUltima.setDisable(deshabilitar);
        cmbRegistrosPorPagina.setDisable(deshabilitar);
    }

    private void actualizarInfoPaginacion() {
        txtPaginacion.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);
        btnPrimera.setDisable(paginaActual == 0);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnUltima.setDisable(paginaActual >= totalPaginas - 1);
    }

    @FXML
    private void handleBuscar() {
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handleLimpiarFiltros() {
        cmbPacienteFiltro.setValue(null);
        cmbProfesionalFiltro.setValue(null);
        cmbEstadoFiltro.setValue("TODOS");
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handlePrimeraPagina() {
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handlePaginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }

    @FXML
    private void handlePaginaSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarDatos();
        }
    }

    @FXML
    private void handleUltimaPagina() {
        paginaActual = totalPaginas - 1;
        cargarDatos();
    }

    @FXML
    private void handleCambioRegistrosPorPagina() {
        registrosPorPagina = Integer.parseInt(cmbRegistrosPorPagina.getValue());
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handleCrearCita() {
        final Paciente paciente = cmbCrearPaciente.getValue();
        final Profesional profesional = cmbCrearProfesional.getValue();
        final LocalDate fecha = dpCrearFecha.getValue();
        final String horaStr = txtCrearHora.getText().trim();
        final String motivo = txtCrearMotivo.getText().trim();

        if (paciente == null || profesional == null || fecha == null || horaStr.isEmpty() || motivo.isEmpty()) {
            mostrarAlerta("Error", "Todos los campos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

        LocalTime hora;
        try {
            hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            mostrarAlerta("Error", "Formato de hora inválido. Use HH:mm (ej: 14:30)", Alert.AlertType.ERROR);
            return;
        }

        final LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        loadingIndicator.setVisible(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (citaService.existeConflictoHorario(profesional.getId(), fechaHora, null)) {
                    return null;
                }
                return citaService.crearCita(paciente.getId(), profesional.getId(), fechaHora, motivo);
            }
        };

        task.setOnSucceeded(event -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                mostrarAlerta("Error", "Ya existe una cita para este profesional en ese horario", Alert.AlertType.ERROR);
            } else if (exito) {
                logger.info("Cita creada exitosamente");
                mostrarAlerta("Éxito", "Cita creada exitosamente", Alert.AlertType.INFORMATION);
                limpiarFormularioCrear();
                cargarDatos();
                tabPane.getSelectionModel().select(0);
            } else {
                logger.error("Error al crear cita");
                mostrarAlerta("Error", "No se pudo crear la cita", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            logger.error("Error al crear cita", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo crear la cita", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleLimpiarFormCrear() {
        limpiarFormularioCrear();
    }

    private void limpiarFormularioCrear() {
        cmbCrearPaciente.setValue(null);
        cmbCrearProfesional.setValue(null);
        dpCrearFecha.setValue(null);
        txtCrearHora.clear();
        txtCrearMotivo.clear();
    }

    private void abrirEdicion(Cita cita) {
        logger.info("Abriendo edición para cita ID: {}", cita.getId());
        citaEnEdicion = cita;

        txtEditarId.setText(cita.getId().toString());

        // Buscar y seleccionar paciente
        for (Paciente p : listaPacientes) {
            if (p.getId().equals(cita.getPacienteId())) {
                cmbEditarPaciente.setValue(p);
                break;
            }
        }

        // Buscar y seleccionar profesional
        for (Profesional p : listaProfesionales) {
            if (p.getId().equals(cita.getProfesionalId())) {
                cmbEditarProfesional.setValue(p);
                break;
            }
        }

        if (cita.getFechaHora() != null) {
            dpEditarFecha.setValue(cita.getFechaHora().toLocalDate());
            txtEditarHora.setText(cita.getFechaHora().toLocalTime().format(TIME_FORMATTER));
        }

        txtEditarMotivo.setText(cita.getMotivo());
        cmbEditarEstado.setValue(cita.getEstado());
        txtEditarObservaciones.setText(cita.getObservaciones());

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    @FXML
    private void handleActualizarCita() {
        if (citaEnEdicion == null) return;

        final Paciente paciente = cmbEditarPaciente.getValue();
        final Profesional profesional = cmbEditarProfesional.getValue();
        final LocalDate fecha = dpEditarFecha.getValue();
        final String horaStr = txtEditarHora.getText().trim();
        final String motivo = txtEditarMotivo.getText().trim();
        final String estado = cmbEditarEstado.getValue();
        final String observaciones = txtEditarObservaciones.getText().trim();
        final Integer citaId = citaEnEdicion.getId();

        if (paciente == null || profesional == null || fecha == null || horaStr.isEmpty() || motivo.isEmpty() || estado == null) {
            mostrarAlerta("Error", "Todos los campos obligatorios deben estar llenos", Alert.AlertType.ERROR);
            return;
        }

        LocalTime hora;
        try {
            hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            mostrarAlerta("Error", "Formato de hora inválido. Use HH:mm (ej: 14:30)", Alert.AlertType.ERROR);
            return;
        }

        final LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        loadingIndicator.setVisible(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (citaService.existeConflictoHorario(profesional.getId(), fechaHora, citaId)) {
                    return null;
                }
                return citaService.actualizarCita(citaId, paciente.getId(), profesional.getId(), fechaHora, motivo, estado, observaciones);
            }
        };

        task.setOnSucceeded(event -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                mostrarAlerta("Error", "Ya existe una cita para este profesional en ese horario", Alert.AlertType.ERROR);
            } else if (exito) {
                logger.info("Cita actualizada exitosamente");
                mostrarAlerta("Éxito", "Cita actualizada exitosamente", Alert.AlertType.INFORMATION);
                cargarDatos();
                handleCancelarEdicion();
            } else {
                logger.error("Error al actualizar cita");
                mostrarAlerta("Error", "No se pudo actualizar la cita", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            logger.error("Error al actualizar cita", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo actualizar la cita", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCancelarEdicion() {
        citaEnEdicion = null;
        tabEditar.setDisable(true);
        tabPane.getSelectionModel().select(0);
    }

    private void eliminarCita(Cita cita) {
        logger.info("Intentando eliminar cita ID: {}", cita.getId());

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar esta cita?");
        confirmacion.setContentText("Paciente: " + cita.getPacienteNombre() + "\nFecha: " + cita.getFechaHora().format(DATE_TIME_FORMATTER));

        Optional<ButtonType> resultado = confirmacion.showAndWait();

        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            final Integer citaId = cita.getId();

            loadingIndicator.setVisible(true);

            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return citaService.eliminarCita(citaId);
                }
            };

            task.setOnSucceeded(event -> {
                Boolean exito = task.getValue();
                loadingIndicator.setVisible(false);

                if (exito) {
                    logger.info("Cita eliminada exitosamente");
                    mostrarAlerta("Éxito", "Cita eliminada exitosamente", Alert.AlertType.INFORMATION);
                    cargarDatos();
                } else {
                    logger.error("Error al eliminar cita");
                    mostrarAlerta("Error", "No se pudo eliminar la cita", Alert.AlertType.ERROR);
                }
            });

            task.setOnFailed(event -> {
                logger.error("Error al eliminar cita", task.getException());
                loadingIndicator.setVisible(false);
                mostrarAlerta("Error", "No se pudo eliminar la cita", Alert.AlertType.ERROR);
            });

            new Thread(task).start();
        }
    }

    private void cambiarEstado(Cita cita, String nuevoEstado) {
        logger.info("Cambiando estado de cita ID {} a {}", cita.getId(), nuevoEstado);

        final Integer citaId = cita.getId();

        loadingIndicator.setVisible(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return citaService.cambiarEstado(citaId, nuevoEstado);
            }
        };

        task.setOnSucceeded(event -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito) {
                logger.info("Estado cambiado exitosamente");
                mostrarAlerta("Éxito", "Estado cambiado a " + nuevoEstado, Alert.AlertType.INFORMATION);
                cargarDatos();
            } else {
                logger.error("Error al cambiar estado");
                mostrarAlerta("Error", "No se pudo cambiar el estado", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(event -> {
            logger.error("Error al cambiar estado", task.getException());
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo cambiar el estado", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private static class DatosCitasResult {
        List<Cita> citas;
        int totalPaginas;

        DatosCitasResult(List<Cita> citas, int totalPaginas) {
            this.citas = citas;
            this.totalPaginas = totalPaginas;
        }
    }

    private static class CatalogosResult {
        List<Paciente> pacientes;
        List<Profesional> profesionales;

        CatalogosResult(List<Paciente> pacientes, List<Profesional> profesionales) {
            this.pacientes = pacientes;
            this.profesionales = profesionales;
        }
    }
}
