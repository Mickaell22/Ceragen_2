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
    @FXML private Tab tabEditar;

    // Filtros
    @FXML private ComboBox<Paciente> cmbPacienteFiltro;
    @FXML private TextField txtFiltroCedulaPaciente;
    @FXML private ComboBox<Profesional> cmbProfesionalFiltro;
    @FXML private TextField txtFiltroCedulaProfesional;
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

    // Formulario Editar
    @FXML private TextField txtEditarId;
    @FXML private ComboBox<Paciente> cmbEditarPaciente;
    @FXML private TextField txtEditarCedulaPaciente;
    @FXML private ComboBox<Profesional> cmbEditarProfesional;
    @FXML private TextField txtEditarCedulaProfesional;
    @FXML private DatePicker dpEditarFecha;
    @FXML private TextField txtEditarHora;
    @FXML private TextArea txtEditarMotivo;
    @FXML private ComboBox<String> cmbEditarEstado;
    @FXML private TextArea txtEditarObservaciones;

    // Vista Horario
    @FXML private DatePicker dpFechaHorario;
    @FXML private TextField txtHorarioCedulaProfesional;
    @FXML private Label lblHorarioProfesional;
    @FXML private VBox vboxHorario;

    private Cita citaEnEdicion;
    private List<Paciente> listaPacientes;
    private List<Profesional> listaProfesionales;
    private Profesional profesionalSeleccionadoHorario;

    @FXML
    public void initialize() {
        logger.info("Inicializando módulo de Citas");
        configurarTabla();
        configurarFiltros();
        configurarPaginacion();
        configurarVistaHorario();
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

    private void configurarVistaHorario() {
        dpFechaHorario.setValue(LocalDate.now());
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

            // Configurar ComboBox de formulario editar
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
    private void handleLimpiarFiltros() {
        cmbPacienteFiltro.setValue(null);
        cmbProfesionalFiltro.setValue(null);
        txtFiltroCedulaPaciente.clear();
        txtFiltroCedulaProfesional.clear();
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

    private void abrirEdicion(Cita cita) {
        logger.info("Abriendo edición para cita ID: {}", cita.getId());
        citaEnEdicion = cita;

        txtEditarId.setText(cita.getId().toString());

        // Buscar y seleccionar paciente en ComboBox
        for (Paciente p : listaPacientes) {
            if (p.getId().equals(cita.getPacienteId())) {
                cmbEditarPaciente.setValue(p);
                break;
            }
        }

        // Buscar y seleccionar profesional en ComboBox
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

    // Métodos de búsqueda por cédula para filtros
    @FXML
    private void handleBuscar() {
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    private void handleBuscarPacienteFiltro() {
        String cedula = txtFiltroCedulaPaciente.getText().trim();
        if (cedula.isEmpty()) {
            mostrarAlerta("Error", "Ingrese una cédula", Alert.AlertType.ERROR);
            return;
        }

        Paciente paciente = pacienteService.getPacienteByCedula(cedula);
        if (paciente != null) {
            cmbPacienteFiltro.setValue(paciente);
            mostrarAlerta("Éxito", "Paciente encontrado: " + paciente.getNombreCompleto(), Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se encontró paciente con cédula: " + cedula, Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarProfesionalFiltro() {
        String cedula = txtFiltroCedulaProfesional.getText().trim();
        if (cedula.isEmpty()) {
            mostrarAlerta("Error", "Ingrese una cédula", Alert.AlertType.ERROR);
            return;
        }

        Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
        if (profesional != null) {
            cmbProfesionalFiltro.setValue(profesional);
            mostrarAlerta("Éxito", "Profesional encontrado: " + profesional.getNombreCompleto(), Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se encontró profesional con cédula: " + cedula, Alert.AlertType.ERROR);
        }
    }

    // Métodos de búsqueda para formulario Editar
    @FXML
    private void handleBuscarPacienteEditar() {
        String cedula = txtEditarCedulaPaciente.getText().trim();
        if (cedula.isEmpty()) {
            mostrarAlerta("Error", "Ingrese una cédula", Alert.AlertType.ERROR);
            return;
        }

        Paciente paciente = pacienteService.getPacienteByCedula(cedula);
        if (paciente != null) {
            cmbEditarPaciente.setValue(paciente);
            mostrarAlerta("Éxito", "Paciente encontrado: " + paciente.getNombreCompleto(), Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se encontró paciente con cédula: " + cedula, Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleBuscarProfesionalEditar() {
        String cedula = txtEditarCedulaProfesional.getText().trim();
        if (cedula.isEmpty()) {
            mostrarAlerta("Error", "Ingrese una cédula", Alert.AlertType.ERROR);
            return;
        }

        Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
        if (profesional != null) {
            cmbEditarProfesional.setValue(profesional);
            mostrarAlerta("Éxito", "Profesional encontrado: " + profesional.getNombreCompleto(), Alert.AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se encontró profesional con cédula: " + cedula, Alert.AlertType.ERROR);
        }
    }

    // Métodos para vista de horario
    @FXML
    private void handleSemanaAnterior() {
        dpFechaHorario.setValue(dpFechaHorario.getValue().minusWeeks(1));
        cargarVistaHorario();
    }

    @FXML
    private void handleSemanaSiguiente() {
        dpFechaHorario.setValue(dpFechaHorario.getValue().plusWeeks(1));
        cargarVistaHorario();
    }

    @FXML
    private void handleIrHoy() {
        dpFechaHorario.setValue(LocalDate.now());
        cargarVistaHorario();
    }

    @FXML
    private void handleActualizarHorario() {
        String cedula = txtHorarioCedulaProfesional.getText().trim();
        if (!cedula.isEmpty()) {
            Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
            if (profesional != null) {
                profesionalSeleccionadoHorario = profesional;
                lblHorarioProfesional.setText(profesional.getNombreCompleto());
            } else {
                mostrarAlerta("Advertencia", "No se encontró profesional con cédula: " + cedula, Alert.AlertType.WARNING);
                profesionalSeleccionadoHorario = null;
                lblHorarioProfesional.setText("No encontrado");
            }
        } else {
            profesionalSeleccionadoHorario = null;
            lblHorarioProfesional.setText("Todos");
        }
        cargarVistaHorario();
    }

    private void cargarVistaHorario() {
        LocalDate fechaSeleccionada = dpFechaHorario.getValue();
        if (fechaSeleccionada == null) {
            fechaSeleccionada = LocalDate.now();
            dpFechaHorario.setValue(fechaSeleccionada);
        }

        // Obtener el inicio de la semana (lunes)
        LocalDate inicioSemana = fechaSeleccionada.with(java.time.DayOfWeek.MONDAY);
        LocalDate finSemana = inicioSemana.plusDays(6);

        // Cargar citas de la semana
        Integer profesionalId = profesionalSeleccionadoHorario != null ? profesionalSeleccionadoHorario.getId() : null;

        Task<List<Cita>> task = new Task<>() {
            @Override
            protected List<Cita> call() {
                LocalDateTime fechaInicio = inicioSemana.atStartOfDay();
                LocalDateTime fechaFin = finSemana.atTime(23, 59, 59);
                return citaService.getCitas(0, 1000, null, profesionalId, null, fechaInicio, fechaFin);
            }
        };

        task.setOnSucceeded(event -> {
            List<Cita> citas = task.getValue();
            generarVistaHorario(inicioSemana, finSemana, citas);
        });

        task.setOnFailed(event -> {
            logger.error("Error al cargar vista de horario", task.getException());
            mostrarAlerta("Error", "No se pudo cargar la vista de horario", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void generarVistaHorario(LocalDate inicioSemana, LocalDate finSemana, List<Cita> citas) {
        vboxHorario.getChildren().clear();

        // Crear encabezado con días de la semana
        HBox encabezado = new HBox(5);
        encabezado.setStyle("-fx-padding: 10; -fx-background-color: #34495e;");

        Text lblHora = new Text("Hora");
        lblHora.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblHora.setWrappingWidth(60);
        encabezado.getChildren().add(lblHora);

        DateTimeFormatter diaFormatter = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int i = 0; i < 7; i++) {
            LocalDate dia = inicioSemana.plusDays(i);
            Text lblDia = new Text(dia.format(diaFormatter));
            lblDia.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center;");
            lblDia.setWrappingWidth(140);
            encabezado.getChildren().add(lblDia);
        }

        vboxHorario.getChildren().add(encabezado);

        // Generar filas de horas (8:00 AM - 6:00 PM)
        for (int hora = 8; hora <= 18; hora++) {
            HBox filaHora = new HBox(5);
            filaHora.setStyle("-fx-padding: 5; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");

            Text lblHoraTexto = new Text(String.format("%02d:00", hora));
            lblHoraTexto.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            lblHoraTexto.setWrappingWidth(60);
            filaHora.getChildren().add(lblHoraTexto);

            final int horaFinal = hora;
            for (int i = 0; i < 7; i++) {
                LocalDate dia = inicioSemana.plusDays(i);
                VBox celda = new VBox(3);
                celda.setStyle("-fx-padding: 5; -fx-border-color: #ecf0f1; -fx-border-width: 0 1 0 0; -fx-background-color: white; -fx-pref-width: 140; -fx-min-height: 60;");

                // Buscar citas para esta hora y día
                List<Cita> citasEnHora = citas.stream()
                    .filter(c -> {
                        if (c.getFechaHora() == null) return false;
                        return c.getFechaHora().toLocalDate().equals(dia) &&
                               c.getFechaHora().getHour() == horaFinal;
                    })
                    .toList();

                for (Cita cita : citasEnHora) {
                    VBox citaBox = new VBox(2);
                    citaBox.setStyle(getEstiloSegunEstado(cita.getEstado()) + "-fx-padding: 5; -fx-background-radius: 3; -fx-cursor: hand;");

                    Text txtHora = new Text(cita.getFechaHora().toLocalTime().format(TIME_FORMATTER));
                    txtHora.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-fill: white;");

                    Text txtPaciente = new Text(cita.getPacienteNombre());
                    txtPaciente.setStyle("-fx-font-size: 10px; -fx-fill: white;");
                    txtPaciente.setWrappingWidth(120);

                    citaBox.getChildren().addAll(txtHora, txtPaciente);
                    citaBox.setOnMouseClicked(e -> abrirEdicion(cita));

                    celda.getChildren().add(citaBox);
                }

                filaHora.getChildren().add(celda);
            }

            vboxHorario.getChildren().add(filaHora);
        }
    }

    private String getEstiloSegunEstado(String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "-fx-background-color: #3498db; ";
            case "CONFIRMADA" -> "-fx-background-color: #27ae60; ";
            case "ATENDIDA" -> "-fx-background-color: #9b59b6; ";
            case "CANCELADA" -> "-fx-background-color: #95a5a6; ";
            default -> "-fx-background-color: #7f8c8d; ";
        };
    }

    private record CatalogosResult(List<Paciente> pacientes, List<Profesional> profesionales) {
    }
}
