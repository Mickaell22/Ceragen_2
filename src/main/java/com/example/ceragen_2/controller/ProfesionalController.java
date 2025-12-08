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
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public final class ProfesionalController {

    private static final int REGISTROS_PAGINA_DEFAULT = 10;
    private static final int CEDULA_LENGTH = 10;
    private static final int TELEFONO_LENGTH = 9;
    private static final int MAX_NOMBRE_LENGTH = 100;
    private static final int MAX_APELLIDO_LENGTH = 100;
    private static final int MAX_EMAIL_LENGTH = 100;
    private static final int MAX_NUMERO_LICENCIA_LENGTH = 30;

    private static final int ESPECIALIDAD_FISIOTERAPIA_ID = 1;
    private static final int ESPECIALIDAD_TRAUMATOLOGIA_ID = 2;
    private static final int ESPECIALIDAD_REHABILITACION_ID = 3;

    private static final int DEFAULT_VBOX_SPACING = 12;
    private static final int DEFAULT_GRID_GAP = 8;

    private final ProfesionalService profesionalService =
            ProfesionalService.getInstance();

    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEditar;
    @FXML
    private Tab tabCrear;

    @FXML
    private TextField txtFiltroCedula;
    @FXML
    private TextField txtFiltroNombres;
    @FXML
    private TextField txtFiltroApellidos;
    @FXML
    private ComboBox<String> cmbEspecialidadFiltro;
    @FXML
    private ComboBox<String> cmbActivoFiltro;

    @FXML
    private TableView<Profesional> tableProfesionales;
    @FXML
    private TableColumn<Profesional, Integer> colId;
    @FXML
    private TableColumn<Profesional, String> colCedula;
    @FXML
    private TableColumn<Profesional, String> colNombres;
    @FXML
    private TableColumn<Profesional, String> colApellidos;
    @FXML
    private TableColumn<Profesional, String> colEspecialidad;
    @FXML
    private TableColumn<Profesional, String> colTelefono;
    @FXML
    private TableColumn<Profesional, String> colEmail;
    @FXML
    private TableColumn<Profesional, String> colNumeroLicencia;
    @FXML
    private TableColumn<Profesional, String> colModalidad;
    @FXML
    private TableColumn<Profesional, String> colActivo;
    @FXML
    private TableColumn<Profesional, String> colUsuario;
    @FXML
    private TableColumn<Profesional, Void> colAcciones;

    @FXML
    private VBox loadingIndicator;

    @FXML
    private Button btnPrimera;
    @FXML
    private Button btnAnterior;
    @FXML
    private Button btnSiguiente;
    @FXML
    private Button btnUltima;
    @FXML
    private ComboBox<String> cmbRegistrosPorPagina;
    @FXML
    private Text txtPaginacion;

    @FXML
    private TextField txtCrearCedula;
    @FXML
    private TextField txtCrearNombres;
    @FXML
    private TextField txtCrearApellidos;
    @FXML
    private TextField txtCrearTelefono;
    @FXML
    private TextField txtCrearEmail;
    @FXML
    private TextField txtCrearNumeroLicencia;
    @FXML
    private ComboBox<String> cmbCrearEspecialidad;
    @FXML
    private ComboBox<String> cmbCrearUsuario;
    @FXML
    private ComboBox<String> cmbCrearModalidad;
    @FXML
    private CheckBox chkCrearActivo;

    @FXML
    private TextField txtEditarId;
    @FXML
    private TextField txtEditarCedula;
    @FXML
    private TextField txtEditarNombres;
    @FXML
    private TextField txtEditarApellidos;
    @FXML
    private ComboBox<String> cmbEditarEspecialidad;
    @FXML
    private ComboBox<String> cmbEditarUsuario;
    @FXML
    private TextField txtEditarTelefono;
    @FXML
    private TextField txtEditarEmail;
    @FXML
    private TextField txtEditarNumeroLicencia;
    @FXML
    private ComboBox<String> cmbEditarModalidad;
    @FXML
    private CheckBox chkEditarActivo;

    private final ObservableList<Profesional> profesionales =
            FXCollections.observableArrayList();
    private List<Profesional> listaCompleta = new ArrayList<>();

    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int registrosPorPagina = REGISTROS_PAGINA_DEFAULT;
    private long totalRegistros = 0;

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
                return null;
        }
    }

    @FXML
    public void initialize() {
        configurarTabla();
        configurarPaginacion();
        configurarCombos();
        cargarProfesionales();
    }

    private void configurarTabla() {
        colId.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getId()
                )
        );

        colCedula.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getCedula()
                )
        );

        colNombres.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getNombres()
                )
        );

        colApellidos.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getApellidos()
                )
        );

        colEspecialidad.setCellValueFactory(
                cellData -> {
                    Profesional p = cellData.getValue();
                    String espNombre = p.getEspecialidadNombre();
                    String value;
                    if (espNombre != null) {
                        value = espNombre;
                    } else if (p.getEspecialidadId() != null) {
                        value = "ID " + p.getEspecialidadId();
                    } else {
                        value = "";
                    }
                    return new SimpleStringProperty(value);
                }
        );

        colTelefono.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getTelefono()
                )
        );

        colEmail.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getEmail()
                )
        );

        colNumeroLicencia.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getNumeroLicencia()
                )
        );

        colModalidad.setCellValueFactory(
                cellData -> {
                    String mod = cellData.getValue().getModalidadAtencion();
                    if (mod == null) {
                        return new SimpleStringProperty("");
                    }
                    String texto;
                    switch (mod) {
                        case "PRESENCIAL":
                            texto = "Presencial";
                            break;
                        case "TELECONSULTA":
                            texto = "Teleconsulta";
                            break;
                        case "MIXTA":
                            texto = "Mixta";
                            break;
                        default:
                            texto = mod;
                    }
                    return new SimpleStringProperty(texto);
                }
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
                    String textoUsuario =
                            usuarioId == null ? "" : "ID " + usuarioId;
                    return new SimpleStringProperty(textoUsuario);
                }
        );

        configurarColumnaAcciones();
        tableProfesionales.setItems(profesionales);
    }

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
                                container.getStyleClass()
                                        .add("actions-container");
                                btnVer.getStyleClass().addAll(
                                        "btn", "btn-secondary"
                                );
                                btnEditar.getStyleClass().addAll(
                                        "btn", "btn-warning"
                                );
                                btnEliminar.getStyleClass().addAll(
                                        "btn", "btn-danger"
                                );

                                btnVer.setOnAction(e -> {
                                    Profesional p =
                                            getTableView().getItems()
                                                    .get(getIndex());
                                    handleVerProfesional(p);
                                });

                                btnEditar.setOnAction(e -> {
                                    Profesional p =
                                            getTableView().getItems()
                                                    .get(getIndex());
                                    handleEditarProfesional(p);
                                });

                                btnEliminar.setOnAction(e -> {
                                    Profesional p =
                                            getTableView().getItems()
                                                    .get(getIndex());
                                    handleEliminarProfesional(p);
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

    private void configurarCombos() {
        if (cmbActivoFiltro.getItems().isEmpty()) {
            cmbActivoFiltro.setItems(
                    FXCollections.observableArrayList(
                            "TODOS", "ACTIVO", "INACTIVO"
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
                        "1", "2", "3"
                );

        ObservableList<String> modalidades =
                FXCollections.observableArrayList(
                        "PRESENCIAL",
                        "TELECONSULTA",
                        "MIXTA"
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

        if (cmbCrearModalidad != null) {
            cmbCrearModalidad.setItems(modalidades);
            cmbCrearModalidad.getSelectionModel().select("PRESENCIAL");
        }
        if (cmbEditarModalidad != null) {
            cmbEditarModalidad.setItems(modalidades);
        }
    }

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
                                apellidosFiltro
                                        .toLowerCase(Locale.ROOT)
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

    private void mostrarLoading(final boolean mostrar) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(mostrar);
            loadingIndicator.setManaged(mostrar);
        }
    }

    private void actualizarTextoPaginacion() {
        if (txtPaginacion != null) {
            txtPaginacion.setText(
                    "Página " + paginaActual
                            + " de " + totalPaginas
            );
        }
    }

    @FXML
    public void handleBuscar(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        paginaActual = 1;
        cargarProfesionales();
    }

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

    @FXML
    public void handlePrimeraPagina(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual != 1) {
            paginaActual = 1;
            cargarProfesionales();
        }
    }

    @FXML
    public void handlePaginaAnterior(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual > 1) {
            paginaActual--;
            cargarProfesionales();
        }
    }

    @FXML
    public void handlePaginaSiguiente(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarProfesionales();
        }
    }

    @FXML
    public void handleUltimaPagina(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        if (paginaActual != totalPaginas) {
            paginaActual = totalPaginas;
            cargarProfesionales();
        }
    }

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

    @FXML
    public void handleNuevoProfesional(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        handleLimpiarFormCrear(null);

        if (tabCrear != null) {
            tabCrear.setDisable(false);
            tabPane.getSelectionModel().select(tabCrear);
        }
    }

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

        String modalidad =
                cmbCrearModalidad != null
                        ? cmbCrearModalidad.getSelectionModel()
                        .getSelectedItem()
                        : null;
        if (modalidad == null || modalidad.isBlank()) {
            modalidad = "PRESENCIAL";
        }

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
        if (cmbCrearUsuario != null
                && cmbCrearUsuario.getSelectionModel()
                .getSelectedItem() != null) {
            try {
                usuarioId = Integer.parseInt(
                        cmbCrearUsuario
                                .getSelectionModel()
                                .getSelectedItem()
                );
            } catch (NumberFormatException ignored) {
                usuarioId = null;
            }
        }

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
        p.setModalidadAtencion(modalidad);

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
        if (cmbCrearModalidad != null) {
            cmbCrearModalidad.getSelectionModel().select("PRESENCIAL");
        }
        if (chkCrearActivo != null) {
            chkCrearActivo.setSelected(true);
        }
    }

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

        String estado =
                Boolean.TRUE.equals(profesional.getActivo())
                        ? "ACTIVO"
                        : "INACTIVO";

        String modalidad = profesional.getModalidadAtencion();
        String modalidadTexto;
        if (modalidad == null) {
            modalidadTexto = "-";
        } else {
            switch (modalidad) {
                case "PRESENCIAL":
                    modalidadTexto = "Presencial";
                    break;
                case "TELECONSULTA":
                    modalidadTexto = "Teleconsulta";
                    break;
                case "MIXTA":
                    modalidadTexto = "Mixta";
                    break;
                default:
                    modalidadTexto = modalidad;
            }
        }

        String tipoUsuarioRegistra =
                profesional.getTipoUsuarioRegistra() != null
                        ? profesional.getTipoUsuarioRegistra()
                        : "-";

        LocalDateTime fechaRegistro = profesional.getFechaRegistro();
        String fechaRegistroTexto = "-";
        if (fechaRegistro != null) {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm"
            );
            fechaRegistroTexto = fechaRegistro.format(fmt);
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

        grid.add(leftLabel.apply("Modalidad de atención:"), 0, row);
        grid.add(
                rightLabel.apply(modalidadTexto),
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

        grid.add(leftLabel.apply("Tipo usuario registra:"), 0, row);
        grid.add(
                rightLabel.apply(tipoUsuarioRegistra),
                1,
                row
        );
        row++;

        grid.add(leftLabel.apply("Fecha registro:"), 0, row);
        grid.add(
                rightLabel.apply(fechaRegistroTexto),
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

        if (profesionalMostrar.getModalidadAtencion() != null) {
            cmbEditarModalidad
                    .getSelectionModel()
                    .select(
                            profesionalMostrar.getModalidadAtencion()
                    );
        } else {
            cmbEditarModalidad
                    .getSelectionModel()
                    .clearSelection();
        }

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void handleEliminarProfesional(
            final Profesional profesional
    ) {
        if (profesional == null || profesional.getId() == null) {
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
                boolean ok =
                        profesionalService.eliminarProfesional(
                                profesional.getId()
                        );
                if (ok) {
                    profesionales.remove(profesional);
                    new Alert(
                            Alert.AlertType.INFORMATION,
                            "Profesional eliminado correctamente."
                    ).showAndWait();
                    cargarProfesionales();
                } else {
                    new Alert(
                            Alert.AlertType.ERROR,
                            "No se pudo eliminar el profesional. "
                                    + "Revise si tiene citas asociadas."
                    ).showAndWait();
                }
            }
        });
    }

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
        if (cmbEditarUsuario != null
                && cmbEditarUsuario.getSelectionModel()
                .getSelectedItem() != null) {
            try {
                usuarioId = Integer.parseInt(
                        cmbEditarUsuario
                                .getSelectionModel()
                                .getSelectedItem()
                );
            } catch (NumberFormatException ignored) {
                usuarioId = null;
            }
        }

        String modalidad =
                cmbEditarModalidad != null
                        ? cmbEditarModalidad.getSelectionModel()
                        .getSelectedItem()
                        : null;
        if (modalidad == null || modalidad.isBlank()) {
            modalidad = "PRESENCIAL";
        }

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
        p.setModalidadAtencion(modalidad);

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

    @FXML
    public void handleCancelarEdicion(final ActionEvent event) {
        Objects.requireNonNull(event, "event");
        tabPane.getSelectionModel().selectFirst();
        tabEditar.setDisable(true);
    }

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

    private String trimOrNull(final String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }
}
