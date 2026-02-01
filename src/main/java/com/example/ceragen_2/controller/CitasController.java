package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;
import com.example.ceragen_2.service.AuthService;
import com.example.ceragen_2.service.CitaService;
import com.example.ceragen_2.service.PacienteService;
import com.example.ceragen_2.service.ProfesionalService;
import com.example.ceragen_2.util.DialogUtil;
import com.example.ceragen_2.util.FormValidationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.util.Callback;
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

public class CitasController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CitasController.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final AuthService authService = AuthService.getInstance();
    private final CitaService citaService = CitaService.getInstance();
    private final PacienteService pacienteService = PacienteService.getInstance();
    private final ProfesionalService profesionalService = ProfesionalService.getInstance();

    private int paginaActual;
    private int registrosPorPagina = 10;
    private int totalPaginas;

    private String rolUsuario;
    private Integer profesionalIdUsuario;

    @FXML private TabPane tabPane;
    @FXML private Tab tabEditar;

    // Filtros
    @FXML private TextField txtFiltroCedulaPaciente;
    @FXML private Label lblPacienteFiltro;
    @FXML private TextField txtFiltroCedulaProfesional;
    @FXML private Label lblProfesionalFiltro;
    @FXML private ComboBox<String> cmbEstadoFiltro;

    // Variables para almacenar paciente/profesional seleccionados en filtros
    private Paciente pacienteFiltroSeleccionado;
    private Profesional profesionalFiltroSeleccionado;

    // Tabla
    @FXML private TableView<Cita> tableCitas;
    @FXML private TableColumn<Cita, String> colPaciente;
    @FXML private TableColumn<Cita, String> colProfesional;
    @FXML private TableColumn<Cita, String> colFechaHora;
    @FXML private TableColumn<Cita, String> colEstado;
    @FXML private TableColumn<Cita, String> colMotivo;
    @FXML private TableColumn<Cita, Void> colAcciones;

    // Paginacion
    @FXML private Button btnPrimera;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnUltima;
    @FXML private Text txtPaginacion;
    @FXML private ComboBox<String> cmbRegistrosPorPagina;
    @FXML private VBox loadingIndicator;

    // Formulario Editar
    @FXML private TextField txtEditarId;
    @FXML private TextField txtEditarCedulaPaciente;
    @FXML private Label lblEditarPaciente;
    @FXML private TextField txtEditarCedulaProfesional;
    @FXML private Label lblEditarProfesional;
    @FXML private DatePicker dpEditarFecha;
    @FXML private TextField txtEditarHora;
    @FXML private TextArea txtEditarMotivo;
    @FXML private ComboBox<String> cmbEditarEstado;
    @FXML private TextArea txtEditarObservaciones;

    // Variables para almacenar paciente/profesional seleccionados en edicion
    private Paciente pacienteEditarSeleccionado;
    private Profesional profesionalEditarSeleccionado;

    // Vista Horario
    @FXML private DatePicker dpFechaHorario;
    @FXML private TextField txtHorarioCedulaProfesional;
    @FXML private Label lblHorarioProfesional;
    @FXML private VBox vboxHorario;

    private Cita citaEnEdicion;
    private Profesional profesionalSeleccionadoHorario;

    @FXML
    public void initialize() {
        LOGGER.info("Inicializando modulo de Citas");

        rolUsuario = authService.getCurrentUserRole();
        profesionalIdUsuario = authService.getCurrentProfesionalId();

        LOGGER.info("Usuario con rol: {} - ProfesionalId: {}", rolUsuario, profesionalIdUsuario);

        configurarTabla();
        configurarFiltros();
        configurarPaginacion();
        configurarVistaHorario();
        configurarPermisosPorRol();
        configurarValidaciones();
        configurarTooltips();
        cargarCatalogos();
    }

    private void configurarValidaciones() {
        // Aplicar filtros de entrada para campos de cedula (solo digitos)
        FormValidationUtil.aplicarFiltroSoloDigitos(txtFiltroCedulaPaciente);
        FormValidationUtil.aplicarFiltroSoloDigitos(txtFiltroCedulaProfesional);
        FormValidationUtil.aplicarFiltroSoloDigitos(txtEditarCedulaPaciente);
        FormValidationUtil.aplicarFiltroSoloDigitos(txtEditarCedulaProfesional);
        FormValidationUtil.aplicarFiltroSoloDigitos(txtHorarioCedulaProfesional);

        // Validacion en tiempo real para hora al perder foco
        txtEditarHora.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !txtEditarHora.getText().trim().isEmpty()) {
                validarFormatoHora(txtEditarHora);
            }
        });

        // Validacion en tiempo real para motivo
        txtEditarMotivo.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) {
                FormValidationUtil.validarCampoRequerido(txtEditarMotivo, true);
            }
        });

        // Configurar DatePicker para no permitir fechas pasadas (solo desde hoy)
        configurarDatePickerEdicion();
    }

    private void configurarDatePickerEdicion() {
        final LocalDate hoy = LocalDate.now();
        final Callback<DatePicker, DateCell> dayCellFactory = new Callback<>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(final LocalDate item, final boolean empty) {
                        super.updateItem(item, empty);
                        // Deshabilitar fechas anteriores a hoy
                        if (item.isBefore(hoy)) {
                            setDisable(true);
                            setStyle("-fx-background-color: #d3d3d3;");
                        }
                    }
                };
            }
        };
        dpEditarFecha.setDayCellFactory(dayCellFactory);
    }

    private void configurarTooltips() {
        // Tooltips para filtros
        txtFiltroCedulaPaciente.setTooltip(new Tooltip("Ingrese la cedula del paciente para buscar"));
        txtFiltroCedulaProfesional.setTooltip(new Tooltip("Ingrese la cedula del profesional para buscar"));
        cmbEstadoFiltro.setTooltip(new Tooltip("Filtrar citas por estado: Pendiente, Confirmada, Atendida o Cancelada"));

        // Tooltips para formulario de edicion
        txtEditarCedulaPaciente.setTooltip(new Tooltip("Buscar paciente por numero de cedula"));
        txtEditarCedulaProfesional.setTooltip(new Tooltip("Buscar profesional por numero de cedula"));
        dpEditarFecha.setTooltip(new Tooltip("Seleccione la fecha de la cita (solo fechas futuras)"));
        txtEditarHora.setTooltip(new Tooltip("Ingrese la hora en formato 24h (ej: 14:30) - Horario: 08:00-17:00"));
        txtEditarMotivo.setTooltip(new Tooltip("Describa el motivo de la consulta"));
        cmbEditarEstado.setTooltip(new Tooltip("PENDIENTE: Sin confirmar | CONFIRMADA: Paciente confirmado | ATENDIDA: Cita realizada | CANCELADA: Cita cancelada"));
        txtEditarObservaciones.setTooltip(new Tooltip("Notas adicionales del profesional sobre la consulta"));

        // Tooltips para vista de horario
        dpFechaHorario.setTooltip(new Tooltip("Seleccione una fecha para ver la semana correspondiente"));
        txtHorarioCedulaProfesional.setTooltip(new Tooltip("Filtrar horario por cedula de profesional (dejar vacio para ver todos)"));
    }

    private boolean validarFormatoHora(final TextField campo) {
        final String horaStr = campo.getText().trim();
        try {
            LocalTime.parse(horaStr, TIME_FORMATTER);
            FormValidationUtil.limpiarEstadoValidacion(campo);
            return true;
        } catch (DateTimeParseException e) {
            FormValidationUtil.marcarCampoInvalido(campo, "Formato invalido. Use HH:mm (ej: 14:30)");
            return false;
        }
    }

    private boolean validarHorarioLaboral(final TextField campo) {
        final String horaStr = campo.getText().trim();
        try {
            final LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
            final LocalTime horaInicio = LocalTime.of(8, 0);
            final LocalTime horaFin = LocalTime.of(17, 0);

            if (hora.isBefore(horaInicio) || hora.isAfter(horaFin)) {
                FormValidationUtil.marcarCampoInvalido(campo, "Horario laboral: 08:00 - 17:00");
                DialogUtil.mostrarAdvertencia("Horario invalido", "La hora debe estar en horario laboral (08:00 - 17:00)");
                return false;
            }
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void configurarTabla() {
        colPaciente.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPacienteNombre()));
        colProfesional.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProfesionalNombre()));
        colFechaHora.setCellValueFactory(data -> {
            if (data.getValue().getFechaHora() != null) {
                return new SimpleStringProperty(data.getValue().getFechaHora().format(DATE_TIME_FORMATTER));
            }
            return new SimpleStringProperty("");
        });

        // Columna de estado con badge
        colEstado.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(final String item, final boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    final Cita cita = getTableRow().getItem();
                    final String estado = cita.getEstado();
                    final Label badge = new Label(estado);
                    badge.getStyleClass().add("status-badge");
                    badge.getStyleClass().add(getBadgeClassForEstado(estado));
                    setGraphic(badge);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));

        colMotivo.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getMotivo() != null && data.getValue().getMotivo().length() > 50
            ? data.getValue().getMotivo().substring(0, 50) + "..."
            : data.getValue().getMotivo()
        ));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final Button btnConfirmar = new Button("Confirmar");
            private final Button btnCancelar = new Button("Cancelar");
            private final HBox pane = new HBox(6, btnEditar, btnEliminar, btnConfirmar, btnCancelar);

            {
                pane.setAlignment(Pos.CENTER);
                // Usar clases CSS en lugar de estilos inline
                btnEditar.getStyleClass().addAll("btn-table-action", "btn-table-edit");
                btnEliminar.getStyleClass().addAll("btn-table-action", "btn-table-delete");
                btnConfirmar.getStyleClass().addAll("btn-table-action", "btn-table-confirm");
                btnCancelar.getStyleClass().addAll("btn-table-action", "btn-table-cancel");

                // Tooltips para botones de accion
                btnEditar.setTooltip(new Tooltip("Editar los detalles de la cita"));
                btnEliminar.setTooltip(new Tooltip("Eliminar permanentemente la cita"));
                btnConfirmar.setTooltip(new Tooltip("Marcar la cita como confirmada"));
                btnCancelar.setTooltip(new Tooltip("Cancelar la cita"));

                btnEditar.setOnAction(event -> abrirEdicion(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(event -> eliminarCita(getTableView().getItems().get(getIndex())));
                btnConfirmar.setOnAction(event -> cambiarEstado(getTableView().getItems().get(getIndex()), "CONFIRMADA"));
                btnCancelar.setOnAction(event -> cambiarEstado(getTableView().getItems().get(getIndex()), "CANCELADA"));
            }

            @Override
            protected void updateItem(final Void item, final boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private String getBadgeClassForEstado(final String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "status-badge-pending";
            case "CONFIRMADA" -> "status-badge-confirmed";
            case "ATENDIDA" -> "status-badge-attended";
            case "CANCELADA" -> "status-badge-cancelled";
            default -> "status-badge-inactive";
        };
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

    private void configurarPermisosPorRol() {
        if ("MEDICO".equals(rolUsuario)) {
            // Si es medico, deshabilitar filtro de profesional y ocultarlo
            txtFiltroCedulaProfesional.setVisible(false);
            txtFiltroCedulaProfesional.setManaged(false);
            lblProfesionalFiltro.setVisible(false);
            lblProfesionalFiltro.setManaged(false);

            // Deshabilitar seleccion de profesional en edicion
            txtEditarCedulaProfesional.setDisable(true);

            LOGGER.info("Filtros configurados para MEDICO - Solo vera sus propias citas");
        }
    }

    private void cargarCatalogos() {
        // Ya no se cargan todos los pacientes/profesionales
        // Se usa busqueda por cedula para seleccionar
        cargarDatos();
    }

    private void cargarDatos() {
        loadingIndicator.setVisible(true);
        deshabilitarControles(true);

        final Integer pacienteId = pacienteFiltroSeleccionado != null ? pacienteFiltroSeleccionado.getId() : null;
        Integer profesionalId = profesionalFiltroSeleccionado != null ? profesionalFiltroSeleccionado.getId() : null;

        // Si es MEDICO, forzar filtro por su profesional_id
        if ("MEDICO".equals(rolUsuario) && profesionalIdUsuario != null) {
            profesionalId = profesionalIdUsuario;
            LOGGER.info("Aplicando filtro automatico para MEDICO - ProfesionalId: {}", profesionalId);
        }

        final String estadoFilter = cmbEstadoFiltro.getValue();
        final int offset = paginaActual * registrosPorPagina;
        final Integer profesionalIdFinal = profesionalId;

        final Task<DatosCitasResult> task = new Task<>() {
            @Override
            protected DatosCitasResult call() {
                final int totalRegistros = citaService.countCitas(pacienteId, profesionalIdFinal, estadoFilter, null, null);
                int totalPaginasTemp = (int) Math.ceil((double) totalRegistros / registrosPorPagina);
                if (totalPaginasTemp == 0) {
                    totalPaginasTemp = 1;
                }

                final List<Cita> citas = citaService.getCitas(offset, registrosPorPagina, pacienteId, profesionalIdFinal, estadoFilter, null, null);
                return new DatosCitasResult(citas, totalPaginasTemp);
            }
        };

        task.setOnSucceeded(event -> {
            final DatosCitasResult resultado = task.getValue();
            totalPaginas = resultado.totalPaginas;

            if (paginaActual >= totalPaginas) {
                paginaActual = Math.max(0, totalPaginas - 1);
            }

            tableCitas.getItems().clear();
            tableCitas.getItems().addAll(resultado.citas);
            actualizarInfoPaginacion();

            LOGGER.info("Datos cargados: {} citas en pagina {}/{}", resultado.citas.size(), paginaActual + 1, totalPaginas);
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar datos", task.getException());
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
            DialogUtil.mostrarError("Error de conexion", "No se pudieron cargar los datos. Verifique su conexion.");
        });

        new Thread(task).start();
    }

    private void deshabilitarControles(final boolean deshabilitar) {
        btnPrimera.setDisable(deshabilitar);
        btnAnterior.setDisable(deshabilitar);
        btnSiguiente.setDisable(deshabilitar);
        btnUltima.setDisable(deshabilitar);
        cmbRegistrosPorPagina.setDisable(deshabilitar);
    }

    private void actualizarInfoPaginacion() {
        txtPaginacion.setText("Pagina " + (paginaActual + 1) + " de " + totalPaginas);
        btnPrimera.setDisable(paginaActual == 0);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnUltima.setDisable(paginaActual >= totalPaginas - 1);
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleLimpiarFiltros() {
        pacienteFiltroSeleccionado = null;
        profesionalFiltroSeleccionado = null;
        lblPacienteFiltro.setText("Sin seleccionar");
        lblProfesionalFiltro.setText("Sin seleccionar");
        txtFiltroCedulaPaciente.clear();
        txtFiltroCedulaProfesional.clear();
        cmbEstadoFiltro.setValue("TODOS");
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handlePrimeraPagina() {
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handlePaginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handlePaginaSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarDatos();
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleUltimaPagina() {
        paginaActual = totalPaginas - 1;
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleCambioRegistrosPorPagina() {
        registrosPorPagina = Integer.parseInt(cmbRegistrosPorPagina.getValue());
        paginaActual = 0;
        cargarDatos();
    }

    private void abrirEdicion(final Cita cita) {
        LOGGER.info("Abriendo edicion para cita ID: {}", cita.getId());

        // Si es MEDICO, verificar que la cita le pertenece
        if ("MEDICO".equals(rolUsuario) && profesionalIdUsuario != null) {
            if (!profesionalIdUsuario.equals(cita.getProfesionalId())) {
                DialogUtil.mostrarAdvertencia("Acceso restringido", "Solo puede editar sus propias citas");
                return;
            }
        }

        citaEnEdicion = cita;

        txtEditarId.setText(cita.getId().toString());

        // Obtener paciente y profesional por ID para mostrar en labels
        final Paciente paciente = pacienteService.getPacienteById(cita.getPacienteId());
        if (paciente != null) {
            pacienteEditarSeleccionado = paciente;
            lblEditarPaciente.setText(paciente.getNombreCompleto());
            txtEditarCedulaPaciente.setText(paciente.getCedula());
        } else {
            pacienteEditarSeleccionado = null;
            lblEditarPaciente.setText(cita.getPacienteNombre());
        }

        final Profesional profesional = profesionalService.getProfesionalById(cita.getProfesionalId());
        if (profesional != null) {
            profesionalEditarSeleccionado = profesional;
            lblEditarProfesional.setText(profesional.getNombreCompleto());
            txtEditarCedulaProfesional.setText(profesional.getCedula());
        } else {
            profesionalEditarSeleccionado = null;
            lblEditarProfesional.setText(cita.getProfesionalNombre());
        }

        if (cita.getFechaHora() != null) {
            dpEditarFecha.setValue(cita.getFechaHora().toLocalDate());
            txtEditarHora.setText(cita.getFechaHora().toLocalTime().format(TIME_FORMATTER));
        }

        txtEditarMotivo.setText(cita.getMotivo());
        cmbEditarEstado.setValue(cita.getEstado());
        txtEditarObservaciones.setText(cita.getObservaciones() != null ? cita.getObservaciones() : "");

        // Limpiar estilos de validacion previos
        limpiarValidacionesFormulario();

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void limpiarValidacionesFormulario() {
        FormValidationUtil.limpiarEstadoValidacion(txtEditarHora);
        FormValidationUtil.limpiarEstadoValidacion(txtEditarMotivo);
        FormValidationUtil.limpiarEstadoValidacion(cmbEditarEstado);
        lblEditarPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        lblEditarProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleActualizarCita() {
        if (citaEnEdicion == null) {
            return;
        }

        // Validaciones con feedback visual
        boolean esValido = true;

        if (pacienteEditarSeleccionado == null) {
            lblEditarPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ffcccc; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
            esValido = false;
        } else {
            lblEditarPaciente.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        }
        if (profesionalEditarSeleccionado == null) {
            lblEditarProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ffcccc; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
            esValido = false;
        } else {
            lblEditarProfesional.setStyle("-fx-font-size: 14px; -fx-padding: 10; -fx-background-color: #ecf0f1; -fx-background-radius: 3; -fx-pref-width: 380; -fx-pref-height: 40;");
        }
        if (dpEditarFecha.getValue() == null) {
            DialogUtil.mostrarAdvertencia("Campo requerido", "Debe seleccionar una fecha para la cita");
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtEditarHora, true)) {
            esValido = false;
        } else if (!validarFormatoHora(txtEditarHora)) {
            esValido = false;
        } else if (!validarHorarioLaboral(txtEditarHora)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarCampoRequerido(txtEditarMotivo, true)) {
            esValido = false;
        }
        if (!FormValidationUtil.validarComboRequerido(cmbEditarEstado, true)) {
            esValido = false;
        }

        if (!esValido) {
            DialogUtil.mostrarError("Campos invalidos", "Por favor, corrija los campos marcados en rojo");
            return;
        }

        final Paciente paciente = pacienteEditarSeleccionado;
        final Profesional profesional = profesionalEditarSeleccionado;
        final LocalDate fecha = dpEditarFecha.getValue();
        final String horaStr = txtEditarHora.getText().trim();
        final String motivo = txtEditarMotivo.getText().trim();
        final String estado = cmbEditarEstado.getValue();
        final String observacionesText = txtEditarObservaciones.getText();
        final String observaciones = observacionesText != null ? observacionesText.trim() : null;
        final Integer citaId = citaEnEdicion.getId();

        final LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        final LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        // Validar que la cita no sea en el pasado
        if (fechaHora.isBefore(LocalDateTime.now())) {
            DialogUtil.mostrarAdvertencia("Fecha/hora invalida", "No se puede agendar una cita en el pasado");
            return;
        }

        loadingIndicator.setVisible(true);

        final Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (citaService.existeConflictoHorario(profesional.getId(), fechaHora, citaId)) {
                    return null;
                }
                return citaService.actualizarCita(citaId, paciente.getId(), profesional.getId(), fechaHora, motivo, estado, observaciones);
            }
        };

        task.setOnSucceeded(event -> {
            final Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                DialogUtil.mostrarError("Conflicto de horario", "Ya existe una cita para este profesional en ese horario");
            } else if (exito) {
                LOGGER.info("Cita actualizada exitosamente");
                DialogUtil.mostrarExito("Cita actualizada", "Los datos de la cita se han actualizado correctamente");
                cargarDatos();
                handleCancelarEdicion();
            } else {
                LOGGER.error("Error al actualizar cita");
                DialogUtil.mostrarError("Error", "No se pudo actualizar la cita");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al actualizar cita", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error", "No se pudo actualizar la cita");
        });

        new Thread(task).start();
    }

    @FXML
    private void handleCancelarEdicion() {
        citaEnEdicion = null;
        tabEditar.setDisable(true);
        tabPane.getSelectionModel().select(0);
    }

    private void eliminarCita(final Cita cita) {
        LOGGER.info("Intentando eliminar cita ID: {}", cita.getId());

        final boolean confirmar = DialogUtil.mostrarConfirmacionPersonalizada(
                "Confirmar Eliminacion",
                "Esta seguro de eliminar esta cita?",
                "Paciente: " + cita.getPacienteNombre() + "\nFecha: " + cita.getFechaHora().format(DATE_TIME_FORMATTER) + "\nEsta accion no se puede deshacer.",
                "Eliminar",
                "Cancelar"
        );

        if (confirmar) {
            final Integer citaId = cita.getId();

            loadingIndicator.setVisible(true);

            final Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return citaService.eliminarCita(citaId);
                }
            };

            task.setOnSucceeded(event -> {
                final Boolean exito = task.getValue();
                loadingIndicator.setVisible(false);

                if (exito) {
                    LOGGER.info("Cita eliminada exitosamente");
                    DialogUtil.mostrarExito("Cita eliminada", "La cita ha sido eliminada del sistema");
                    cargarDatos();
                } else {
                    LOGGER.error("Error al eliminar cita");
                    DialogUtil.mostrarError("Error", "No se pudo eliminar la cita");
                }
            });

            task.setOnFailed(event -> {
                LOGGER.error("Error al eliminar cita", task.getException());
                loadingIndicator.setVisible(false);
                DialogUtil.mostrarError("Error", "No se pudo eliminar la cita");
            });

            new Thread(task).start();
        }
    }

    private void cambiarEstado(final Cita cita, final String nuevoEstado) {
        LOGGER.info("Cambiando estado de cita ID {} a {}", cita.getId(), nuevoEstado);

        final Integer citaId = cita.getId();

        loadingIndicator.setVisible(true);

        final Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                return citaService.cambiarEstado(citaId, nuevoEstado);
            }
        };

        task.setOnSucceeded(event -> {
            final Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito) {
                LOGGER.info("Estado cambiado exitosamente");
                DialogUtil.mostrarExito("Estado actualizado", "El estado de la cita se ha cambiado a " + nuevoEstado);
                cargarDatos();
            } else {
                LOGGER.error("Error al cambiar estado");
                DialogUtil.mostrarError("Error", "No se pudo cambiar el estado de la cita");
            }
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cambiar estado", task.getException());
            loadingIndicator.setVisible(false);
            DialogUtil.mostrarError("Error", "No se pudo cambiar el estado de la cita");
        });

        new Thread(task).start();
    }

    private static class DatosCitasResult {
        final List<Cita> citas;
        final int totalPaginas;

        DatosCitasResult(final List<Cita> citas, final int totalPaginas) {
            this.citas = citas;
            this.totalPaginas = totalPaginas;
        }
    }

    // Metodos de busqueda por cedula para filtros
    @FXML
    @SuppressWarnings("unused")
    private void handleBuscar() {
        paginaActual = 0;
        cargarDatos();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscarPacienteFiltro() {
        final String cedula = txtFiltroCedulaPaciente.getText().trim();
        if (cedula.isEmpty()) {
            DialogUtil.mostrarAdvertencia("Campo vacio", "Ingrese una cedula para buscar");
            return;
        }

        final Paciente paciente = pacienteService.getPacienteByCedula(cedula);
        if (paciente != null) {
            pacienteFiltroSeleccionado = paciente;
            lblPacienteFiltro.setText(paciente.getNombreCompleto());
            // Filtrar automaticamente al encontrar
            paginaActual = 0;
            cargarDatos();
        } else {
            pacienteFiltroSeleccionado = null;
            lblPacienteFiltro.setText("No encontrado");
            DialogUtil.mostrarAdvertencia("No encontrado", "No se encontro paciente con cedula: " + cedula);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscarProfesionalFiltro() {
        final String cedula = txtFiltroCedulaProfesional.getText().trim();
        if (cedula.isEmpty()) {
            DialogUtil.mostrarAdvertencia("Campo vacio", "Ingrese una cedula para buscar");
            return;
        }

        final Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
        if (profesional != null) {
            profesionalFiltroSeleccionado = profesional;
            lblProfesionalFiltro.setText(profesional.getNombreCompleto());
            // Filtrar automaticamente al encontrar
            paginaActual = 0;
            cargarDatos();
        } else {
            profesionalFiltroSeleccionado = null;
            lblProfesionalFiltro.setText("No encontrado");
            DialogUtil.mostrarAdvertencia("No encontrado", "No se encontro profesional con cedula: " + cedula);
        }
    }

    // Metodos de busqueda para formulario Editar
    @FXML
    @SuppressWarnings("unused")
    private void handleBuscarPacienteEditar() {
        final String cedula = txtEditarCedulaPaciente.getText().trim();
        if (cedula.isEmpty()) {
            DialogUtil.mostrarAdvertencia("Campo vacio", "Ingrese una cedula para buscar");
            return;
        }

        final Paciente paciente = pacienteService.getPacienteByCedula(cedula);
        if (paciente != null) {
            pacienteEditarSeleccionado = paciente;
            lblEditarPaciente.setText(paciente.getNombreCompleto());
            DialogUtil.mostrarExito("Paciente encontrado", "Se encontro: " + paciente.getNombreCompleto());
        } else {
            pacienteEditarSeleccionado = null;
            lblEditarPaciente.setText("No encontrado");
            DialogUtil.mostrarAdvertencia("No encontrado", "No se encontro paciente con cedula: " + cedula);
        }
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleBuscarProfesionalEditar() {
        final String cedula = txtEditarCedulaProfesional.getText().trim();
        if (cedula.isEmpty()) {
            DialogUtil.mostrarAdvertencia("Campo vacio", "Ingrese una cedula para buscar");
            return;
        }

        final Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
        if (profesional != null) {
            profesionalEditarSeleccionado = profesional;
            lblEditarProfesional.setText(profesional.getNombreCompleto());
            DialogUtil.mostrarExito("Profesional encontrado", "Se encontro: " + profesional.getNombreCompleto());
        } else {
            profesionalEditarSeleccionado = null;
            lblEditarProfesional.setText("No encontrado");
            DialogUtil.mostrarAdvertencia("No encontrado", "No se encontro profesional con cedula: " + cedula);
        }
    }

    // Metodos para vista de horario
    @FXML
    @SuppressWarnings("unused")
    private void handleSemanaAnterior() {
        dpFechaHorario.setValue(dpFechaHorario.getValue().minusWeeks(1));
        cargarVistaHorario();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleSemanaSiguiente() {
        dpFechaHorario.setValue(dpFechaHorario.getValue().plusWeeks(1));
        cargarVistaHorario();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleIrHoy() {
        dpFechaHorario.setValue(LocalDate.now());
        cargarVistaHorario();
    }

    @FXML
    @SuppressWarnings("unused")
    private void handleActualizarHorario() {
        final String cedula = txtHorarioCedulaProfesional.getText().trim();
        if (!cedula.isEmpty()) {
            final Profesional profesional = profesionalService.getProfesionalByCedula(cedula);
            if (profesional != null) {
                profesionalSeleccionadoHorario = profesional;
                lblHorarioProfesional.setText(profesional.getNombreCompleto());
            } else {
                DialogUtil.mostrarAdvertencia("No encontrado", "No se encontro profesional con cedula: " + cedula);
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
        final LocalDate inicioSemana = fechaSeleccionada.with(java.time.DayOfWeek.MONDAY);
        final LocalDate finSemana = inicioSemana.plusDays(6);

        // Cargar citas de la semana
        final Integer profesionalId = profesionalSeleccionadoHorario != null ? profesionalSeleccionadoHorario.getId() : null;

        final Task<List<Cita>> task = new Task<>() {
            @Override
            protected List<Cita> call() {
                final LocalDateTime fechaInicio = inicioSemana.atStartOfDay();
                final LocalDateTime fechaFin = finSemana.atTime(23, 59, 59);
                return citaService.getCitas(0, 1000, null, profesionalId, null, fechaInicio, fechaFin);
            }
        };

        task.setOnSucceeded(event -> {
            final List<Cita> citas = task.getValue();
            generarVistaHorario(inicioSemana, finSemana, citas);
        });

        task.setOnFailed(event -> {
            LOGGER.error("Error al cargar vista de horario", task.getException());
            DialogUtil.mostrarError("Error", "No se pudo cargar la vista de horario");
        });

        new Thread(task).start();
    }

    private void generarVistaHorario(final LocalDate inicioSemana, final LocalDate finSemana, final List<Cita> citas) {
        vboxHorario.getChildren().clear();

        // Crear encabezado con dias de la semana
        final HBox encabezado = new HBox(5);
        encabezado.setStyle("-fx-padding: 10; -fx-background-color: #34495e;");

        final Text lblHora = new Text("Hora");
        lblHora.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        lblHora.setWrappingWidth(60);
        encabezado.getChildren().add(lblHora);

        final DateTimeFormatter diaFormatter = DateTimeFormatter.ofPattern("EEE dd/MM");
        for (int i = 0; i < 7; i++) {
            final LocalDate dia = inicioSemana.plusDays(i);
            final Text lblDia = new Text(dia.format(diaFormatter));
            lblDia.setStyle("-fx-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-text-alignment: center;");
            lblDia.setWrappingWidth(140);
            encabezado.getChildren().add(lblDia);
        }

        vboxHorario.getChildren().add(encabezado);

        // Generar filas de horas (8:00 AM - 6:00 PM)
        for (int hora = 8; hora <= 18; hora++) {
            final HBox filaHora = new HBox(5);
            filaHora.setStyle("-fx-padding: 5; -fx-border-color: #ecf0f1; -fx-border-width: 0 0 1 0;");

            final Text lblHoraTexto = new Text(String.format("%02d:00", hora));
            lblHoraTexto.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            lblHoraTexto.setWrappingWidth(60);
            filaHora.getChildren().add(lblHoraTexto);

            final int horaFinal = hora;
            for (int i = 0; i < 7; i++) {
                final LocalDate dia = inicioSemana.plusDays(i);
                final VBox celda = new VBox(3);
                celda.setStyle("-fx-padding: 5; -fx-border-color: #ecf0f1; -fx-border-width: 0 1 0 0; -fx-background-color: white; -fx-pref-width: 140; -fx-min-height: 60;");

                // Buscar citas para esta hora y dia
                final List<Cita> citasEnHora = citas.stream()
                    .filter(c -> {
                        if (c.getFechaHora() == null) {
                            return false;
                        }
                        return c.getFechaHora().toLocalDate().equals(dia) &&
                               c.getFechaHora().getHour() == horaFinal;
                    })
                    .toList();

                for (final Cita cita : citasEnHora) {
                    final VBox citaBox = new VBox(2);
                    citaBox.setStyle(getEstiloSegunEstado(cita.getEstado()) + "-fx-padding: 5; -fx-background-radius: 3; -fx-cursor: hand;");

                    final Text txtHora = new Text(cita.getFechaHora().toLocalTime().format(TIME_FORMATTER));
                    txtHora.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-fill: white;");

                    final Text txtPaciente = new Text(cita.getPacienteNombre());
                    txtPaciente.setStyle("-fx-font-size: 10px; -fx-fill: white;");
                    txtPaciente.setWrappingWidth(120);

                    citaBox.getChildren().addAll(txtHora, txtPaciente);
                    citaBox.setOnMouseClicked(e -> abrirEdicion(cita));

                    // Tooltip para la cita en el horario
                    Tooltip tooltip = new Tooltip(
                        "Paciente: " + cita.getPacienteNombre() +
                        "\nProfesional: " + cita.getProfesionalNombre() +
                        "\nEstado: " + cita.getEstado() +
                        "\nMotivo: " + (cita.getMotivo() != null ? cita.getMotivo() : "No especificado")
                    );
                    Tooltip.install(citaBox, tooltip);

                    celda.getChildren().add(citaBox);
                }

                filaHora.getChildren().add(celda);
            }

            vboxHorario.getChildren().add(filaHora);
        }
    }

    private String getEstiloSegunEstado(final String estado) {
        return switch (estado) {
            case "PENDIENTE" -> "-fx-background-color: #3498db; ";
            case "CONFIRMADA" -> "-fx-background-color: #27ae60; ";
            case "ATENDIDA" -> "-fx-background-color: #9b59b6; ";
            case "CANCELADA" -> "-fx-background-color: #95a5a6; ";
            default -> "-fx-background-color: #7f8c8d; ";
        };
    }

}
