package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Profesional;
import com.example.ceragen_2.service.ProfesionalService;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Controlador JavaFX para la gestión de profesionales.
 */
public final class ProfesionalController {

    /** Registros por página por defecto. */
    private static final int REGISTROS_PAGINA_DEFAULT = 10;
    /** Longitud esperada para el número de cédula. */
    private static final int CEDULA_LENGTH = 10;
    /** Longitud esperada para el número de teléfono. */
    private static final int TELEFONO_LENGTH = 9;
    /** Longitud máxima para nombres. */
    private static final int MAX_NOMBRE_LENGTH = 100;
    /** Longitud máxima para apellidos. */
    private static final int MAX_APELLIDO_LENGTH = 100;
    /** Longitud máxima para email. */
    private static final int MAX_EMAIL_LENGTH = 100;
    /** Longitud máxima para el número de licencia. */
    private static final int MAX_NUMERO_LICENCIA_LENGTH = 30;

    /** Id de especialidad Fisioterapia. */
    private static final int ESPECIALIDAD_FISIOTERAPIA_ID = 1;
    /** Id de especialidad Traumatología. */
    private static final int ESPECIALIDAD_TRAUMATOLOGIA_ID = 2;
    /** Id de especialidad Rehabilitación. */
    private static final int ESPECIALIDAD_REHABILITACION_ID = 3;

    /** Espaciado por defecto para contenedores VBox. */
    private static final int DEFAULT_VBOX_SPACING = 12;
    /** Separación por defecto para filas/columnas en grids. */
    private static final int DEFAULT_GRID_GAP = 8;

    // ----------------- Service -----------------

    /** Servicio para operaciones con profesionales. */
    private final ProfesionalService profesionalService =
            ProfesionalService.getInstance();

    // ----------------- General / Tabs -----------------

    /** Contenedor principal de pestañas. */
    @FXML
    private TabPane tabPane;
    /** Pestaña de edición. */
    @FXML
    private Tab tabEditar;
    /** Pestaña de creación. */
    @FXML
    private Tab tabCrear;

    // ----------------- Filtros (Tab Listar) -----------------

    /** Campo de filtro por cédula. */
    @FXML
    private TextField txtFiltroCedula;
    /** Campo de filtro por nombres. */
    @FXML
    private TextField txtFiltroNombres;
    /** Campo de filtro por apellidos. */
    @FXML
    private TextField txtFiltroApellidos;
    /** Combo de filtro por especialidad. */
    @FXML
    private ComboBox<String> cmbEspecialidadFiltro;
    /** Combo de filtro por estado (activo/inactivo). */
    @FXML
    private ComboBox<String> cmbActivoFiltro;

    // ----------------- Tabla y loading -----------------

    /** Tabla de profesionales. */
    @FXML
    private TableView<Profesional> tableProfesionales;
    /** Columna de id. */
    @FXML
    private TableColumn<Profesional, Integer> colId;
    /** Columna de cédula. */
    @FXML
    private TableColumn<Profesional, String> colCedula;
    /** Columna de nombres. */
    @FXML
    private TableColumn<Profesional, String> colNombres;
    /** Columna de apellidos. */
    @FXML
    private TableColumn<Profesional, String> colApellidos;
    /** Columna de especialidad. */
    @FXML
    private TableColumn<Profesional, String> colEspecialidad;
    /** Columna de teléfono. */
    @FXML
    private TableColumn<Profesional, String> colTelefono;
    /** Columna de email. */
    @FXML
    private TableColumn<Profesional, String> colEmail;
    /** Columna de número de licencia. */
    @FXML
    private TableColumn<Profesional, String> colNumeroLicencia;
    /** Columna de estado activo/inactivo. */
    @FXML
    private TableColumn<Profesional, String> colActivo;
    /** Columna de usuario asociado. */
    @FXML
    private TableColumn<Profesional, String> colUsuario;
    /** Columna de acciones (ver/editar/eliminar). */
    @FXML
    private TableColumn<Profesional, Void> colAcciones;

    /** Indicador visual de carga. */
    @FXML
    private VBox loadingIndicator;

    // ----------------- Paginación -----------------

    /** Botón para ir a la primera página. */
    @FXML
    private Button btnPrimera;
    /** Botón para ir a la página anterior. */
    @FXML
    private Button btnAnterior;
    /** Botón para ir a la página siguiente. */
    @FXML
    private Button btnSiguiente;
    /** Botón para ir a la última página. */
    @FXML
    private Button btnUltima;
    /** Combo para seleccionar registros por página. */
    @FXML
    private ComboBox<String> cmbRegistrosPorPagina;
    /** Texto con el estado de paginación. */
    @FXML
    private Text txtPaginacion;

    // ----------------- Campos de creación (Tab Crear) -----------------

    /** Campo de cédula para creación. */
    @FXML
    private TextField txtCrearCedula;
    /** Campo de nombres para creación. */
    @FXML
    private TextField txtCrearNombres;
    /** Campo de apellidos para creación. */
    @FXML
    private TextField txtCrearApellidos;
    /** Campo de teléfono para creación. */
    @FXML
    private TextField txtCrearTelefono;
    /** Campo de email para creación. */
    @FXML
    private TextField txtCrearEmail;
    /** Campo de número de licencia para creación. */
    @FXML
    private TextField txtCrearNumeroLicencia;
    /** Combo de especialidad para creación. */
    @FXML
    private ComboBox<String> cmbCrearEspecialidad;
    /** Combo de usuarios (solo decorativo) para creación. */
    @FXML
    private ComboBox<String> cmbCrearUsuario;
    /** Check de activo para creación. */
    @FXML
    private CheckBox chkCrearActivo;

    // ----------------- Campos de edición (Tab Editar) -----------------

    /** Campo de id para edición. */
    @FXML
    private TextField txtEditarId;
    /** Campo de cédula para edición. */
    @FXML
    private TextField txtEditarCedula;
    /** Campo de nombres para edición. */
    @FXML
    private TextField txtEditarNombres;
    /** Campo de apellidos para edición. */
    @FXML
    private TextField txtEditarApellidos;
    /** Combo de especialidad para edición. */
    @FXML
    private ComboBox<String> cmbEditarEspecialidad;
    /** Combo de usuarios (solo decorativo) para edición. */
    @FXML
    private ComboBox<String> cmbEditarUsuario;
    /** Campo de teléfono para edición. */
    @FXML
    private TextField txtEditarTelefono;
    /** Campo de email para edición. */
    @FXML
    private TextField txtEditarEmail;
    /** Campo de número de licencia para edición. */
    @FXML
    private TextField txtEditarNumeroLicencia;
    /** Check de activo para edición. */
    @FXML
    private CheckBox chkEditarActivo;

    // ----------------- Datos internos / paginación -----------------

    /** Lista observable de profesionales para la tabla. */
    private final ObservableList<Profesional> profesionales =
            FXCollections.observableArrayList();
    /** Lista completa de profesionales filtrados. */
    private List<Profesional> listaCompleta = new ArrayList<>();

    /** Página actual de la paginación. */
    private int paginaActual = 1;
    /** Total de páginas calculadas. */
    private int totalPaginas = 1;
    /** Cantidad de registros por página. */
    private int registrosPorPagina = REGISTROS_PAGINA_DEFAULT;
    /** Total de registros encontrados. */
    private long totalRegistros = 0;

    // =====================================================
    // Mapeo nombre → ID de especialidad
    // =====================================================

    /**
     * Mapea el nombre de la especialidad a su identificador.
     *
     * @param nombre nombre de la especialidad
     * @return id de la especialidad o {@code null} si no se reconoce
     */
    private Integer mapEspecialidadNombreToId(final String nombre) {
        if (nombre == null) {
            return null;
        }
        switch (nombre) {
            case "Fisioterapia":
                return ESPECIALIDAD_FISIOTERAPIA_ID;
            case "Traumatología":
                return ESPECIALIDAD_TRAUMATOLOGIA_ID;
            case "Rehabilitación":
                return ESPECIALIDAD_REHABILITACION_ID;
            default:
                // por si agregas más y aún no los mapeas
                return null;
        }
    }

    // ----------------- Inicialización -----------------

    /**
     * Método de inicialización del controlador.
     * Se ejecuta automáticamente al cargar el FXML.
     */
    @FXML
    public void initialize() {
        configurarTabla();
        configurarPaginacion();
        configurarCombos();
        cargarProfesionales();
    }

    // =====================================================
    // CONFIGURACIÓN TABLA
    // =====================================================

    /** Configura las columnas y el origen de datos de la tabla. */
    private void configurarTabla() {
        colId.setCellValueFactory(
                cellData ->
                        new ReadOnlyObjectWrapper<>(
                                cellData.getValue().getId()
                        )
        );

        colCedula.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getCedula()
                        )
        );

        colNombres.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getNombres()
                        )
        );

        colApellidos.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getApellidos()
                        )
        );

        colEspecialidad.setCellValueFactory(
                cellData -> {
                    Profesional profesional = cellData.getValue();
                    String especialidadNombre =
                            profesional.getEspecialidadNombre();
                    String value;
                    if (especialidadNombre != null) {
                        value = especialidadNombre;
                    } else if (profesional.getEspecialidadId() != null) {
                        value = "ID " + profesional.getEspecialidadId();
                    } else {
                        value = "";
                    }
                    return new SimpleStringProperty(value);
                }
        );

        colTelefono.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getTelefono()
                        )
        );

        colEmail.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getEmail()
                        )
        );

        colNumeroLicencia.setCellValueFactory(
                cellData ->
                        new SimpleStringProperty(
                                cellData.getValue().getNumeroLicencia()
                        )
        );

        colActivo.setCellValueFactory(
                cellData -> {
                    Boolean activo = cellData.getValue().getActivo();
                    String textoActivo =
                            Boolean.TRUE.equals(activo) ? "Sí" : "No";
                    return new SimpleStringProperty(textoActivo);
                }
        );

        colUsuario.setCellValueFactory(
                cellData -> {
                    Integer usuarioId = cellData.getValue().getUsuarioId();
                    String textoUsuario;
                    if (usuarioId == null) {
                        textoUsuario = "";
                    } else {
                        textoUsuario = "ID " + usuarioId;
                    }
                    return new SimpleStringProperty(textoUsuario);
                }
        );

        configurarColumnaAcciones();
        tableProfesionales.setItems(profesionales);
    }

    /** Configura la columna de acciones (ver, editar, eliminar). */
    private void configurarColumnaAcciones() {
        Callback<TableColumn<Profesional, Void>, TableCell<Profesional, Void>>
                cellFactory =
                new Callback<TableColumn<Profesional, Void>,
                        TableCell<Profesional, Void>>() {
                    @Override
                    public TableCell<Profesional, Void> call(
                            final TableColumn<Profesional, Void> param) {
                        return new TableCell<Profesional, Void>() {

                            private final Button btnVer =
                                    new Button("Ver");
                            private final Button btnEditar =
                                    new Button("Editar");
                            private final Button btnEliminar =
                                    new Button("Eliminar");
                            private final javafx.scene.layout.HBox container =
                                    new javafx.scene.layout.HBox(
                                            DEFAULT_GRID_GAP,
                                            btnVer,
                                            btnEditar,
                                            btnEliminar
                                    );

                            {
                                container.getStyleClass().add(
                                        "actions-container"
                                );
                                btnVer.getStyleClass().addAll(
                                        "btn",
                                        "btn-secondary"
                                );
                                btnEditar.getStyleClass().addAll(
                                        "btn",
                                        "btn-warning"
                                );
                                btnEliminar.getStyleClass().addAll(
                                        "btn",
                                        "btn-danger"
                                );

                                btnVer.setOnAction(e -> {
                                    Profesional profesional =
                                            getTableView()
                                                    .getItems()
                                                    .get(getIndex());
                                    handleVerProfesional(profesional);
                                });

                                btnEditar.setOnAction(e -> {
                                    Profesional profesional =
                                            getTableView()
                                                    .getItems()
                                                    .get(getIndex());
                                    handleEditarProfesional(profesional);
                                });

                                btnEliminar.setOnAction(e -> {
                                    Profesional profesional =
                                            getTableView()
                                                    .getItems()
                                                    .get(getIndex());
                                    handleEliminarProfesional(profesional);
                                });
                            }

                            @Override
                            protected void updateItem(
                                    final Void item,
                                    final boolean empty
                            ) {
                                super.updateItem(item, empty);
                                if (empty) {
                                    setGraphic(null);
                                } else {
                                    setGraphic(container);
                                }
                            }
                        };
                    }
                };

        colAcciones.setCellFactory(cellFactory);
    }

    // =====================================================
    // CONFIGURACIÓN Paginación y Combos
    // =====================================================

    /** Configura el combo y comportamiento de paginación. */
    private void configurarPaginacion() {
        cmbRegistrosPorPagina.setItems(
                FXCollections.observableArrayList("10", "25", "50")
        );
        cmbRegistrosPorPagina.getSelectionModel().select("10");

        cmbRegistrosPorPagina.valueProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        registrosPorPagina = Integer.parseInt(newVal);
                        paginaActual = 1;
                        cargarProfesionales();
                    }
                }
        );

        actualizarTextoPaginacion();
    }

    /** Configura los combos de filtros y selección. */
    private void configurarCombos() {
        if (cmbActivoFiltro.getItems().isEmpty()) {
            cmbActivoFiltro.setItems(
                    FXCollections.observableArrayList(
                            "TODOS",
                            "ACTIVO",
                            "INACTIVO"
                    )
            );
        }
        cmbActivoFiltro.getSelectionModel().selectFirst();

        new Thread(() -> {
            List<Profesional> todos =
                    profesionalService.getAllProfesionales();
            List<String> nombres = new ArrayList<>();
            nombres.add("Todas");
            for (Profesional p : todos) {
                String esp = p.getEspecialidadNombre();
                if (esp != null && !nombres.contains(esp)) {
                    nombres.add(esp);
                }
            }

            Platform.runLater(() -> {
                cmbEspecialidadFiltro.setItems(
                        FXCollections.observableArrayList(nombres)
                );
                cmbEspecialidadFiltro.getSelectionModel().selectFirst();
            });
        }).start();

        ObservableList<String> especialidades =
                FXCollections.observableArrayList(
                        "Fisioterapia",
                        "Traumatología",
                        "Rehabilitación"
                );
        ObservableList<String> usuarios =
                FXCollections.observableArrayList(
                        "1",
                        "2",
                        "3"
                );

        if (cmbEditarEspecialidad != null) {
            cmbEditarEspecialidad.setItems(especialidades);
        }
        if (cmbCrearEspecialidad != null) {
            cmbCrearEspecialidad.setItems(especialidades);
        }

        if (cmbEditarUsuario != null) {
            cmbEditarUsuario.setItems(usuarios);
        }
        if (cmbCrearUsuario != null) {
            cmbCrearUsuario.setItems(usuarios);
        }
    }

    // =====================================================
    // CARGA DE DATOS (usando ProfesionalService)
    // =====================================================

    /** Carga la lista de profesionales aplicando filtros y paginación. */
    private void cargarProfesionales() {
        mostrarLoading(true);

        final String cedulaFiltro = trimOrNull(txtFiltroCedula.getText());
        final String nombresFiltro = trimOrNull(txtFiltroNombres.getText());
        final String apellidosFiltro = trimOrNull(
                txtFiltroApellidos.getText()
        );
        final String especialidadFiltro =
                cmbEspecialidadFiltro
                        .getSelectionModel()
                        .getSelectedItem();
        final String estadoFiltro =
                cmbActivoFiltro
                        .getSelectionModel()
                        .getSelectedItem();

        new Thread(() -> {
            List<Profesional> todos =
                    profesionalService.getAllProfesionales();

            List<Profesional> filtrados = new ArrayList<>();
            for (Profesional p : todos) {

                if (cedulaFiltro != null
                        && (p.getCedula() == null
                        || !p.getCedula().contains(cedulaFiltro))) {
                    continue;
                }

                if (nombresFiltro != null
                        && (p.getNombres() == null
                        || !p.getNombres()
                        .toLowerCase(Locale.ROOT)
                        .contains(
                                nombresFiltro.toLowerCase(Locale.ROOT)
                        ))) {
                    continue;
                }

                if (apellidosFiltro != null
                        && (p.getApellidos() == null
                        || !p.getApellidos()
                        .toLowerCase(Locale.ROOT)
                        .contains(
                                apellidosFiltro.toLowerCase(Locale.ROOT)
                        ))) {
                    continue;
                }

                if (especialidadFiltro != null
                        && !"Todas".equalsIgnoreCase(
                        especialidadFiltro
                )) {
                    String espNombre = p.getEspecialidadNombre();
                    if (espNombre == null
                            || !espNombre.equalsIgnoreCase(
                            especialidadFiltro
                    )) {
                        continue;
                    }
                }

                if ("ACTIVO".equalsIgnoreCase(estadoFiltro)
                        && !Boolean.TRUE.equals(p.getActivo())) {
                    continue;
                }

                if ("INACTIVO".equalsIgnoreCase(estadoFiltro)
                        && Boolean.TRUE.equals(p.getActivo())) {
                    continue;
                }

                filtrados.add(p);
            }

            listaCompleta = filtrados;
            totalRegistros = listaCompleta.size();
            totalPaginas = (int) Math.max(
                    1,
                    Math.ceil(
                            (double) totalRegistros / registrosPorPagina
                    )
            );

            if (paginaActual > totalPaginas) {
                paginaActual = totalPaginas;
            }
            if (paginaActual < 1) {
                paginaActual = 1;
            }

            int fromIndex = (paginaActual - 1) * registrosPorPagina;
            int toIndex = Math.min(
                    fromIndex + registrosPorPagina,
                    listaCompleta.size()
            );

            List<Profesional> pagina = new ArrayList<>();
            if (fromIndex < toIndex) {
                pagina = listaCompleta.subList(fromIndex, toIndex);
            }

            final List<Profesional> paginaFinal = pagina;

            Platform.runLater(() -> {
                profesionales.setAll(paginaFinal);
                actualizarTextoPaginacion();
                mostrarLoading(false);
            });
        }).start();
    }

    /**
     * Muestra u oculta el indicador de carga.
     *
     * @param mostrar {@code true} para mostrar; {@code false} para ocultar
     */
    private void mostrarLoading(final boolean mostrar) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(mostrar);
            loadingIndicator.setManaged(mostrar);
        }
    }

    /** Actualiza el texto de paginación. */
    private void actualizarTextoPaginacion() {
        if (txtPaginacion != null) {
            txtPaginacion.setText(
                    "Página " + paginaActual
                            + " de " + totalPaginas
            );
        }
    }

    // =====================================================
    // HANDLERS - LISTAR
    // =====================================================

    /**
     * Maneja la acción de buscar con filtros.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleBuscar(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        paginaActual = 1;
        cargarProfesionales();
    }

    /**
     * Limpia los filtros y recarga la lista.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleLimpiarFiltros(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        txtFiltroCedula.clear();
        txtFiltroNombres.clear();
        txtFiltroApellidos.clear();

        if (!cmbEspecialidadFiltro.getItems().isEmpty()) {
            cmbEspecialidadFiltro.getSelectionModel().selectFirst();
        }
        if (!cmbActivoFiltro.getItems().isEmpty()) {
            cmbActivoFiltro.getSelectionModel().selectFirst();
        }

        paginaActual = 1;
        cargarProfesionales();
    }

    /**
     * Va a la primera página.
     *
     * @param event evento de acción
     */
    @FXML
    public void handlePrimeraPagina(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual != 1) {
            paginaActual = 1;
            cargarProfesionales();
        }
    }

    /**
     * Va a la página anterior.
     *
     * @param event evento de acción
     */
    @FXML
    public void handlePaginaAnterior(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual > 1) {
            paginaActual--;
            cargarProfesionales();
        }
    }

    /**
     * Va a la página siguiente.
     *
     * @param event evento de acción
     */
    @FXML
    public void handlePaginaSiguiente(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarProfesionales();
        }
    }

    /**
     * Va a la última página.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleUltimaPagina(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual != totalPaginas) {
            paginaActual = totalPaginas;
            cargarProfesionales();
        }
    }

    /**
     * Maneja el cambio de registros por página.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleCambioRegistrosPorPagina(
            final ActionEvent event
    ) {
        Objects.requireNonNull(event, "event");
        String value = cmbRegistrosPorPagina
                .getSelectionModel()
                .getSelectedItem();
        if (value != null) {
            registrosPorPagina = Integer.parseInt(value);
            paginaActual = 1;
            cargarProfesionales();
        }
    }

    // =====================================================
    // HANDLERS - CREAR
    // =====================================================

    /**
     * Abre la pestaña de creación de profesional.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleNuevoProfesional(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        handleLimpiarFormCrear(null);

        if (tabCrear != null) {
            tabCrear.setDisable(false);
            tabPane.getSelectionModel().select(tabCrear);
        }
    }

    /**
     * Maneja la creación de un nuevo profesional.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleCrearProfesional(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        String cedula = trimOrNull(txtCrearCedula.getText());
        String nombres = trimOrNull(txtCrearNombres.getText());
        String apellidos = trimOrNull(txtCrearApellidos.getText());
        String telefono = trimOrNull(txtCrearTelefono.getText());
        String email = trimOrNull(txtCrearEmail.getText());
        String numeroLicencia =
                trimOrNull(txtCrearNumeroLicencia.getText());
        Boolean activo = chkCrearActivo != null
                && chkCrearActivo.isSelected();

        String especialidadNombre =
                cmbCrearEspecialidad
                        .getSelectionModel()
                        .getSelectedItem();
        Integer especialidadId =
                mapEspecialidadNombreToId(especialidadNombre);

        if (!validarDatosProfesional(
                cedula,
                nombres,
                apellidos,
                telefono,
                email,
                numeroLicencia
        )) {
            return;
        }

        if (especialidadId == null) {
            new Alert(
                    Alert.AlertType.WARNING,
                    "Debe seleccionar al menos una especialidad."
            ).showAndWait();
            return;
        }

        Integer usuarioId = null;

        Profesional p = new Profesional();
        p.setCedula(cedula);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setNumeroLicencia(numeroLicencia);
        p.setActivo(activo);
        p.setUsuarioId(usuarioId);
        p.setEspecialidadNombre(especialidadNombre);
        p.setEspecialidadId(especialidadId);

        boolean ok = profesionalService.crearProfesional(p);

        if (ok) {
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Profesional creado correctamente."
            ).showAndWait();
            handleLimpiarFormCrear(null);
            tabPane.getSelectionModel().selectFirst();
            cargarProfesionales();
        } else {
            new Alert(
                    Alert.AlertType.ERROR,
                    "No se pudo crear el profesional. "
                            + "Revisa la consola para más detalles."
            ).showAndWait();
        }
    }

    /**
     * Limpia los campos del formulario de creación.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleLimpiarFormCrear(final ActionEvent event) {
        if (event != null) {
            Objects.requireNonNull(event, "event");
        }

        if (txtCrearCedula != null) {
            txtCrearCedula.clear();
        }
        if (txtCrearNombres != null) {
            txtCrearNombres.clear();
        }
        if (txtCrearApellidos != null) {
            txtCrearApellidos.clear();
        }
        if (txtCrearTelefono != null) {
            txtCrearTelefono.clear();
        }
        if (txtCrearEmail != null) {
            txtCrearEmail.clear();
        }
        if (txtCrearNumeroLicencia != null) {
            txtCrearNumeroLicencia.clear();
        }

        if (cmbCrearEspecialidad != null) {
            cmbCrearEspecialidad
                    .getSelectionModel()
                    .clearSelection();
        }
        if (cmbCrearUsuario != null) {
            cmbCrearUsuario
                    .getSelectionModel()
                    .clearSelection();
        }
        if (chkCrearActivo != null) {
            chkCrearActivo.setSelected(true);
        }
    }

    // =====================================================
    // ACCIONES COLUMNA "ACCIONES"  -> VER PROFESIONAL
    // =====================================================

    /**
     * Muestra el detalle de un profesional en un diálogo.
     *
     * @param profesional profesional a mostrar
     */
    private void handleVerProfesional(final Profesional profesional) {
        if (profesional == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle del profesional");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        pane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, "
                        + "#f4f7fb, #ffffff);"
                        + "-fx-padding: 20;"
                        + "-fx-font-family: 'Segoe UI', sans-serif;"
        );

        VBox root = new VBox(DEFAULT_VBOX_SPACING);
        root.setFillWidth(true);

        Label lblNombre = new Label(profesional.getNombreCompleto());
        lblNombre.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #00695c;"
        );

        Label lblSubtitulo = new Label("Información del profesional");
        lblSubtitulo.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: #607d8b;"
                        + "-fx-font-weight: bold;"
        );

        Separator separator = new Separator();

        GridPane grid = new GridPane();
        grid.setHgap(DEFAULT_GRID_GAP);
        grid.setVgap(DEFAULT_GRID_GAP);

        java.util.function.Function<String, Label> leftLabel = text -> {
            Label l = new Label(text);
            l.setStyle(
                    "-fx-font-weight: bold;"
                            + "-fx-text-fill: #455a64;"
                            + "-fx-font-size: 13px;"
            );
            return l;
        };

        java.util.function.Function<String, Label> rightLabel = text -> {
            String value = text != null ? text : "-";
            Label l = new Label(value);
            l.setStyle(
                    "-fx-font-size: 13px;"
                            + "-fx-text-fill: #37474f;"
            );
            return l;
        };

        String especialidad = profesional.getEspecialidadNombre() != null
                ? profesional.getEspecialidadNombre()
                : profesional.getEspecialidadId() != null
                ? "ID " + profesional.getEspecialidadId()
                : "-";

        String estado;
        if (Boolean.TRUE.equals(profesional.getActivo())) {
            estado = "ACTIVO";
        } else {
            estado = "INACTIVO";
        }

        int row = 0;
        grid.add(leftLabel.apply("Cédula:"), 0, row);
        grid.add(
                rightLabel.apply(profesional.getCedula()),
                1,
                row
        );
        row++;

        grid.add(leftLabel.apply("Especialidad:"), 0, row);
        grid.add(
                rightLabel.apply(especialidad),
                1,
                row
        );
        row++;

        grid.add(leftLabel.apply("Teléfono:"), 0, row);
        grid.add(
                rightLabel.apply(profesional.getTelefono()),
                1,
                row
        );
        row++;

        grid.add(leftLabel.apply("Email:"), 0, row);
        grid.add(
                rightLabel.apply(profesional.getEmail()),
                1,
                row
        );
        row++;

        grid.add(leftLabel.apply("Número de licencia:"), 0, row);
        grid.add(
                rightLabel.apply(profesional.getNumeroLicencia()),
                1,
                row
        );
        row++;

        grid.add(leftLabel.apply("Estado:"), 0, row);
        Label lblEstado = rightLabel.apply(estado);
        lblEstado.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-font-weight: bold;"
                        + (Boolean.TRUE.equals(profesional.getActivo())
                        ? "-fx-text-fill: #2e7d32;"
                        : "-fx-text-fill: #c62828;")
        );
        grid.add(lblEstado, 1, row);

        root.getChildren().addAll(
                lblNombre,
                lblSubtitulo,
                separator,
                grid
        );

        pane.setContent(root);

        Button btnCerrar = (Button) pane.lookupButton(ButtonType.CLOSE);
        if (btnCerrar != null) {
            btnCerrar.setText("Cerrar");
            btnCerrar.setStyle(
                    "-fx-background-color: #00695c;"
                            + "-fx-text-fill: white;"
                            + "-fx-font-weight: bold;"
                            + "-fx-padding: 6 20 6 20;"
                            + "-fx-background-radius: 20;"
            );
        }

        dialog.showAndWait();
    }

    /**
     * Prepara los datos de un profesional para ser editado.
     *
     * @param profesional profesional a editar
     */
    private void handleEditarProfesional(final Profesional profesional) {
        if (profesional == null) {
            return;
        }

        Profesional profesionalMostrar = profesional;
        if (profesional.getId() != null) {
            Profesional desdeBd =
                    profesionalService.getProfesionalById(
                            profesional.getId()
                    );
            if (desdeBd != null) {
                profesionalMostrar = desdeBd;
            }
        }

        txtEditarId.setText(
                profesionalMostrar.getId() != null
                        ? profesionalMostrar.getId().toString()
                        : ""
        );
        txtEditarCedula.setText(profesionalMostrar.getCedula());
        txtEditarNombres.setText(profesionalMostrar.getNombres());
        txtEditarApellidos.setText(profesionalMostrar.getApellidos());
        txtEditarTelefono.setText(profesionalMostrar.getTelefono());
        txtEditarEmail.setText(profesionalMostrar.getEmail());
        txtEditarNumeroLicencia.setText(
                profesionalMostrar.getNumeroLicencia()
        );
        chkEditarActivo.setSelected(
                Boolean.TRUE.equals(profesionalMostrar.getActivo())
        );

        if (profesionalMostrar.getEspecialidadNombre() != null) {
            cmbEditarEspecialidad
                    .getSelectionModel()
                    .select(
                            profesionalMostrar.getEspecialidadNombre()
                    );
        } else {
            cmbEditarEspecialidad
                    .getSelectionModel()
                    .clearSelection();
        }

        if (profesionalMostrar.getUsuarioId() != null) {
            cmbEditarUsuario
                    .getSelectionModel()
                    .select(
                            profesionalMostrar
                                    .getUsuarioId()
                                    .toString()
                    );
        } else {
            cmbEditarUsuario
                    .getSelectionModel()
                    .clearSelection();
        }

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    /**
     * Maneja el borrado de un profesional de la tabla.
     * (Sin eliminar todavía de la base de datos).
     *
     * @param profesional profesional a eliminar
     */
    private void handleEliminarProfesional(
            final Profesional profesional
    ) {
        if (profesional == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Profesional");
        confirm.setHeaderText(
                "¿Desea eliminar al profesional seleccionado?"
        );
        confirm.setContentText(profesional.getNombreCompleto());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // Pendiente de implementar eliminado real en la BD
                // profesionalService.eliminarProfesional(profesional.getId());
                profesionales.remove(profesional);
            }
        });
    }

    // =====================================================
    // HANDLERS - EDITAR
    // =====================================================

    /**
     * Maneja la actualización de los datos del profesional.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleActualizarProfesional(
            final ActionEvent event
    ) {
        Objects.requireNonNull(event, "event");
        String cedula = trimOrNull(txtEditarCedula.getText());
        String nombres = trimOrNull(txtEditarNombres.getText());
        String apellidos = trimOrNull(txtEditarApellidos.getText());
        String telefono = trimOrNull(txtEditarTelefono.getText());
        String email = trimOrNull(txtEditarEmail.getText());
        String numeroLicencia =
                trimOrNull(txtEditarNumeroLicencia.getText());
        Boolean activo = chkEditarActivo.isSelected();

        if (!validarDatosProfesional(
                cedula,
                nombres,
                apellidos,
                telefono,
                email,
                numeroLicencia
        )) {
            return;
        }

        Integer id = null;
        if (!txtEditarId.getText().isBlank()) {
            try {
                id = Integer.parseInt(txtEditarId.getText());
            } catch (NumberFormatException ignored) {
                // ignorar
            }
        }

        String especialidadNombre =
                cmbEditarEspecialidad
                        .getSelectionModel()
                        .getSelectedItem();
        Integer especialidadId =
                mapEspecialidadNombreToId(especialidadNombre);

        Integer usuarioId = null;

        Profesional p = new Profesional();
        p.setId(id);
        p.setCedula(cedula);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setNumeroLicencia(numeroLicencia);
        p.setActivo(activo);
        p.setUsuarioId(usuarioId);
        p.setEspecialidadNombre(especialidadNombre);
        p.setEspecialidadId(especialidadId);

        if (id != null && especialidadId == null) {
            Profesional original =
                    profesionalService.getProfesionalById(id);
            if (original != null) {
                p.setEspecialidadId(original.getEspecialidadId());
            }
        }

        boolean ok;

        if (id == null) {
            ok = profesionalService.crearProfesional(p);
        } else {
            ok = profesionalService.actualizarProfesional(p);
        }

        if (ok) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Los datos del profesional "
                            + "se guardaron correctamente."
            );
            alert.showAndWait();

            tabPane.getSelectionModel().selectFirst();
            tabEditar.setDisable(true);
            cargarProfesionales();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(
                    "No se pudo guardar el profesional"
            );
            alert.setContentText(
                    "Revisa la consola para más detalles."
            );
            alert.showAndWait();
        }
    }

    /**
     * Cancela la edición y vuelve a la pestaña de listado.
     *
     * @param event evento de acción
     */
    @FXML
    public void handleCancelarEdicion(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        tabPane.getSelectionModel().selectFirst();
        tabEditar.setDisable(true);
    }

    // =====================================================
    // VALIDACIONES COMUNES (basadas en el requerimiento)
    // =====================================================

    /**
     * Valida los datos del profesional.
     * <ul>
     *     <li>Cédula: exactamente 10 dígitos, solo números.</li>
     *     <li>Nombres / Apellidos: solo letras y espacios,
     *     máximo 100 caracteres.</li>
     *     <li>Email: opcional, formato válido y máximo 100
     *     caracteres.</li>
     *     <li>Teléfono: opcional, exactamente 9 dígitos.</li>
     *     <li>Número de licencia: obligatorio, alfanumérico,
     *     máximo 30 caracteres.</li>
     * </ul>
     *
     * @param cedula         cédula del profesional
     * @param nombres        nombres del profesional
     * @param apellidos      apellidos del profesional
     * @param telefono       número de teléfono (opcional)
     * @param email          correo electrónico (opcional)
     * @param numeroLicencia número de licencia o registro médico
     * @return {@code true} si todos los datos son válidos;
     *         {@code false} en caso contrario
     */
    private boolean validarDatosProfesional(
            final String cedula,
            final String nombres,
            final String apellidos,
            final String telefono,
            final String email,
            final String numeroLicencia
    ) {

        List<String> errores = new ArrayList<>();

        if (cedula == null || cedula.isEmpty()) {
            errores.add("La cédula es obligatoria.");
        } else if (!cedula.matches("\\d{" + CEDULA_LENGTH + "}")) {
            errores.add(
                    "La cédula debe contener exactamente "
                            + CEDULA_LENGTH
                            + " dígitos (solo números)."
            );
        }

        if (nombres == null || nombres.isEmpty()) {
            errores.add("Los nombres son obligatorios.");
        } else {
            if (nombres.length() > MAX_NOMBRE_LENGTH) {
                errores.add(
                        "Los nombres no pueden superar los "
                                + MAX_NOMBRE_LENGTH
                                + " caracteres."
                );
            }
            if (!nombres.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")) {
                errores.add(
                        "Los nombres solo deben contener "
                                + "letras y espacios."
                );
            }
        }

        if (apellidos == null || apellidos.isEmpty()) {
            errores.add("Los apellidos son obligatorios.");
        } else {
            if (apellidos.length() > MAX_APELLIDO_LENGTH) {
                errores.add(
                        "Los apellidos no pueden superar los "
                                + MAX_APELLIDO_LENGTH
                                + " caracteres."
                );
            }
            if (!apellidos.matches("[A-Za-zÁÉÍÓÚáéíóúÑñ ]+")) {
                errores.add(
                        "Los apellidos solo deben contener "
                                + "letras y espacios."
                );
            }
        }

        if (telefono != null) {
            if (!telefono.matches("\\d{" + TELEFONO_LENGTH + "}")) {
                errores.add(
                        "El número de celular debe contener "
                                + "exactamente "
                                + TELEFONO_LENGTH
                                + " dígitos (solo números)."
                );
            }
        }

        if (email != null) {
            if (email.length() > MAX_EMAIL_LENGTH) {
                errores.add(
                        "El correo electrónico no puede superar "
                                + "los "
                                + MAX_EMAIL_LENGTH
                                + " caracteres."
                );
            }
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                errores.add(
                        "El correo electrónico no tiene un "
                                + "formato válido."
                );
            }
        }

        if (numeroLicencia == null || numeroLicencia.isEmpty()) {
            errores.add(
                    "El número de licencia / registro médico "
                            + "es obligatorio."
            );
        } else {
            if (numeroLicencia.length()
                    > MAX_NUMERO_LICENCIA_LENGTH) {
                errores.add(
                        "El número de licencia no puede superar "
                                + "los "
                                + MAX_NUMERO_LICENCIA_LENGTH
                                + " caracteres."
                );
            }
            if (!numeroLicencia.matches("[A-Za-z0-9]+")) {
                errores.add(
                        "El número de licencia debe ser "
                                + "alfanumérico "
                                + "(sin espacios ni símbolos "
                                + "especiales)."
                );
            }
        }

        if (!errores.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String e : errores) {
                sb.append("• ").append(e).append('\n');
            }
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Validación de datos del profesional");
            alert.setHeaderText(
                    "Por favor, corrige los siguientes campos:"
            );
            alert.setContentText(sb.toString());
            alert.showAndWait();
            return false;
        }

        return true;
    }

    // =====================================================
    // UTILS
    // =====================================================

    /**
     * Aplica trim al texto; devuelve {@code null} si queda vacío.
     *
     * @param text texto a procesar
     * @return texto recortado o {@code null} si está vacío
     */
    private String trimOrNull(final String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }
}
