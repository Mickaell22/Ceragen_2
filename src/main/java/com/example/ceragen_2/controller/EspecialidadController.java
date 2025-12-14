package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Especialidad;
import com.example.ceragen_2.service.EspecialidadService;
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

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadController {

    private static final int DURACION_MIN = 15;
    private static final int DURACION_MAX = 60;

    // ----------------- Service -----------------
    private final EspecialidadService especialidadService =
            EspecialidadService.getInstance();

    // ----------------- General / Tabs -----------------
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEditar;
    @FXML
    private Tab tabCrear;

    // ----------------- Filtros (Tab Listar) -----------------
    @FXML
    private TextField txtFiltroNombre;
    @FXML
    private TextField txtFiltroDescripcion;

    // ----------------- Tabla y loading -----------------
    @FXML
    private TableView<Especialidad> tableEspecialidades;
    @FXML
    private TableColumn<Especialidad, Integer> colId;
    @FXML
    private TableColumn<Especialidad, String> colNombre;
    @FXML
    private TableColumn<Especialidad, String> colCodigo;
    @FXML
    private TableColumn<Especialidad, String> colDescripcion;
    @FXML
    private TableColumn<Especialidad, String> colDuracion;
    @FXML
    private TableColumn<Especialidad, String> colCostoConsulta;
    @FXML
    private TableColumn<Especialidad, String> colEstado;
    @FXML
    private TableColumn<Especialidad, Void> colAcciones;

    @FXML
    private VBox loadingIndicator;

    // ----------------- Paginación -----------------
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

    // ----------------- Campos de creación (Tab Crear) -----------------
    @FXML
    private TextField txtCrearNombre;
    @FXML
    private TextField txtCrearCodigo;
    @FXML
    private TextArea txtCrearDescripcion;
    @FXML
    private TextField txtCrearDuracion;
    @FXML
    private ComboBox<String> cmbCrearEstado;
    @FXML
    private TextField txtCrearCostoConsulta;
    @FXML
    private TextField txtCrearUsuarioCreadorId;

    // ----------------- Campos de edición (Tab Editar) -----------------
    @FXML
    private TextField txtEditarId;
    @FXML
    private TextField txtEditarNombre;
    @FXML
    private TextField txtEditarCodigo;
    @FXML
    private TextArea txtEditarDescripcion;
    @FXML
    private TextField txtEditarDuracion;
    @FXML
    private ComboBox<String> cmbEditarEstado;
    @FXML
    private TextField txtEditarCostoConsulta;
    @FXML
    private TextField txtEditarUsuarioCreadorId;
    @FXML
    private TextField txtEditarFechaCreacion;

    // ----------------- Datos internos / paginación -----------------
    private final ObservableList<Especialidad> especialidades =
            FXCollections.observableArrayList();
    private List<Especialidad> listaCompleta = new ArrayList<>();

    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int registrosPorPagina = 10;
    private long totalRegistros = 0;

    // ----------------- Inicialización -----------------
    @FXML
    public void initialize() {
        configurarTabla();
        configurarPaginacion();
        configurarCombos();
        cargarEspecialidades();
    }

    private void configurarCombos() {
        if (cmbCrearEstado != null) {
            cmbCrearEstado.setItems(FXCollections.observableArrayList(
                    "ACTIVO", "INACTIVO"
            ));
            cmbCrearEstado.getSelectionModel().select("ACTIVO");
        }

        if (cmbEditarEstado != null) {
            cmbEditarEstado.setItems(FXCollections.observableArrayList(
                    "ACTIVO", "INACTIVO"
            ));
        }

        if (txtCrearUsuarioCreadorId != null) {
            txtCrearUsuarioCreadorId.setText("1");
        }
    }

    // =====================================================
    // CONFIGURACIÓN TABLA
    // =====================================================
    private void configurarTabla() {
        colId.setCellValueFactory(
                cellData -> new ReadOnlyObjectWrapper<>(
                        cellData.getValue().getId()
                )
        );

        colNombre.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getNombre()
                )
        );

        colCodigo.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getCodigo()
                )
        );

        colDescripcion.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getDescripcion()
                )
        );

        colDuracion.setCellValueFactory(
                cellData -> {
                    Integer d = cellData.getValue()
                            .getDuracionEstandarMin();
                    String txt = (d != null)
                            ? d + " min"
                            : "";
                    return new SimpleStringProperty(txt);
                }
        );

        colCostoConsulta.setCellValueFactory(cellData -> {
            BigDecimal costo =
                    cellData.getValue().getTarifaBase();
            String texto = (costo != null)
                    ? costo.toPlainString()
                    : "";
            return new SimpleStringProperty(texto);
        });

        colEstado.setCellValueFactory(
                cellData -> new SimpleStringProperty(
                        cellData.getValue().getEstado()
                )
        );

        configurarColumnaAcciones();
        tableEspecialidades.setItems(especialidades);
    }

    private void configurarColumnaAcciones() {
        Callback<TableColumn<Especialidad, Void>, TableCell<Especialidad, Void>>
                cellFactory = new Callback<>() {
            @Override
            public TableCell<Especialidad, Void> call(
                    final TableColumn<Especialidad, Void> param
            ) {
                return new TableCell<>() {

                    private final Button btnVer =
                            new Button("Ver");
                    private final Button btnEditar =
                            new Button("Editar");
                    private final Button btnEliminar =
                            new Button("Eliminar");
                    private final javafx.scene.layout.HBox container =
                            new javafx.scene.layout.HBox(
                                    5,
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
                            Especialidad esp = getTableView()
                                    .getItems()
                                    .get(getIndex());
                            handleVerEspecialidad(esp);
                        });

                        btnEditar.setOnAction(e -> {
                            Especialidad esp = getTableView()
                                    .getItems()
                                    .get(getIndex());
                            handleEditarEspecialidad(esp);
                        });

                        btnEliminar.setOnAction(e -> {
                            Especialidad esp = getTableView()
                                    .getItems()
                                    .get(getIndex());
                            handleEliminarEspecialidad(esp);
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
    // CONFIGURACIÓN Paginación
    // =====================================================
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
                        cargarEspecialidades();
                    }
                }
        );

        actualizarTextoPaginacion();
    }

    private void actualizarTextoPaginacion() {
        if (txtPaginacion != null) {
            txtPaginacion.setText(
                    "Página " + paginaActual
                            + " de " + totalPaginas
            );
        }
    }

    // =====================================================
    // CARGA DE DATOS
    // =====================================================
    private void cargarEspecialidades() {
        mostrarLoading(true);

        String nombreFiltro =
                trimOrNull(txtFiltroNombre != null
                        ? txtFiltroNombre.getText()
                        : null);
        String descripcionFiltro =
                trimOrNull(txtFiltroDescripcion != null
                        ? txtFiltroDescripcion.getText()
                        : null);

        new Thread(() -> {
            List<Especialidad> todos =
                    especialidadService.getAllEspecialidades();

            List<Especialidad> filtrados = new ArrayList<>();
            for (Especialidad e : todos) {

                if (nombreFiltro != null
                        && (e.getNombre() == null
                        || !e.getNombre()
                        .toLowerCase()
                        .contains(
                                nombreFiltro.toLowerCase()
                        ))) {
                    continue;
                }

                if (descripcionFiltro != null
                        && (e.getDescripcion() == null
                        || !e.getDescripcion()
                        .toLowerCase()
                        .contains(
                                descripcionFiltro.toLowerCase()
                        ))) {
                    continue;
                }

                filtrados.add(e);
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

            List<Especialidad> pagina = new ArrayList<>();
            if (fromIndex < toIndex) {
                pagina = listaCompleta.subList(fromIndex, toIndex);
            }

            final List<Especialidad> paginaFinal = pagina;

            Platform.runLater(() -> {
                especialidades.setAll(paginaFinal);
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

    // =====================================================
    // HANDLERS - LISTAR
    // =====================================================
    @FXML
    private void handleBuscar(final ActionEvent event) {
        paginaActual = 1;
        cargarEspecialidades();
    }

    @FXML
    private void handleLimpiarFiltros(final ActionEvent event) {
        if (txtFiltroNombre != null) {
            txtFiltroNombre.clear();
        }
        if (txtFiltroDescripcion != null) {
            txtFiltroDescripcion.clear();
        }

        paginaActual = 1;
        cargarEspecialidades();
    }

    @FXML
    private void handlePrimeraPagina(final ActionEvent event) {
        if (paginaActual != 1) {
            paginaActual = 1;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handlePaginaAnterior(final ActionEvent event) {
        if (paginaActual > 1) {
            paginaActual--;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handlePaginaSiguiente(final ActionEvent event) {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handleUltimaPagina(final ActionEvent event) {
        if (paginaActual != totalPaginas) {
            paginaActual = totalPaginas;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handleCambioRegistrosPorPagina(final ActionEvent event) {
        String value = cmbRegistrosPorPagina
                .getSelectionModel()
                .getSelectedItem();
        if (value != null) {
            registrosPorPagina = Integer.parseInt(value);
            paginaActual = 1;
            cargarEspecialidades();
        }
    }

    // =====================================================
    // HANDLERS - CREAR
    // =====================================================
    @FXML
    private void handleNuevaEspecialidad(final ActionEvent event) {
        handleLimpiarFormCrear(null);
        if (tabCrear != null) {
            tabCrear.setDisable(false);
            tabPane.getSelectionModel().select(tabCrear);
        }
    }

    @FXML
    private void handleCrearEspecialidad(final ActionEvent event) {
        String nombre = trimOrNull(txtCrearNombre.getText());
        String codigo = trimOrNull(txtCrearCodigo.getText());
        String descripcion =
                trimOrNull(txtCrearDescripcion.getText());
        String duracionText =
                trimOrNull(txtCrearDuracion.getText());
        String estadoSel =
                cmbCrearEstado != null
                        ? cmbCrearEstado
                        .getSelectionModel()
                        .getSelectedItem()
                        : null;
        String costoText =
                trimOrNull(txtCrearCostoConsulta.getText());
        String usuarioCreadorText =
                trimOrNull(txtCrearUsuarioCreadorId.getText());

        List<String> errores = new ArrayList<>();

        if (nombre == null) {
            errores.add("El nombre es obligatorio.");
        }
        if (codigo == null) {
            // si quieres obligatorio
            errores.add("El código es obligatorio.");
        }
        Integer duracion = null;
        if (duracionText != null) {
            try {
                duracion = Integer.parseInt(duracionText);
                if (duracion < DURACION_MIN
                        || duracion > DURACION_MAX) {
                    errores.add(
                            "La duración estándar debe estar entre "
                                    + DURACION_MIN
                                    + " y "
                                    + DURACION_MAX
                                    + " minutos."
                    );
                }
            } catch (NumberFormatException ex) {
                errores.add(
                        "La duración estándar debe ser un número entero."
                );
            }
        } else {
            duracion = 30;
        }

        if (estadoSel == null) {
            estadoSel = "ACTIVO";
        }

        BigDecimal costo = null;
        if (costoText != null) {
            try {
                costo = new BigDecimal(
                        costoText.replace(",", ".")
                );
            } catch (NumberFormatException ex) {
                errores.add(
                        "La tarifa base no tiene un formato válido. "
                                + "Use por ejemplo 25.00."
                );
            }
        }

        Integer usuarioCreadorId = null;
        if (usuarioCreadorText != null) {
            try {
                usuarioCreadorId =
                        Integer.parseInt(usuarioCreadorText);
            } catch (NumberFormatException ex) {
                errores.add(
                        "El ID de usuario creador debe ser numérico."
                );
            }
        } else {
            usuarioCreadorId = 1;
        }

        if (!errores.isEmpty()) {
            mostrarErrores("Validación de especialidad", errores);
            return;
        }

        Especialidad e = new Especialidad();
        e.setNombre(nombre);
        e.setCodigo(codigo);
        e.setDescripcion(descripcion);
        e.setDuracionEstandarMin(duracion);
        e.setTarifaBase(costo);
        e.setEstado(estadoSel);
        e.setUsuarioCreadorId(usuarioCreadorId);

        boolean ok = especialidadService.crearEspecialidad(e);

        if (ok) {
            new Alert(
                    Alert.AlertType.INFORMATION,
                    "Especialidad creada correctamente."
            ).showAndWait();
            handleLimpiarFormCrear(null);
            tabPane.getSelectionModel().selectFirst();
            cargarEspecialidades();
        } else {
            new Alert(
                    Alert.AlertType.ERROR,
                    "No se pudo crear la especialidad. "
                            + "Revisa la consola para más detalles."
            ).showAndWait();
        }
    }

    @FXML
    private void handleLimpiarFormCrear(final ActionEvent event) {
        if (txtCrearNombre != null) {
            txtCrearNombre.clear();
        }
        if (txtCrearCodigo != null) {
            txtCrearCodigo.clear();
        }
        if (txtCrearDescripcion != null) {
            txtCrearDescripcion.clear();
        }
        if (txtCrearDuracion != null) {
            txtCrearDuracion.clear();
        }
        if (txtCrearCostoConsulta != null) {
            txtCrearCostoConsulta.clear();
        }
        if (cmbCrearEstado != null) {
            cmbCrearEstado.getSelectionModel().select("ACTIVO");
        }
        if (txtCrearUsuarioCreadorId != null) {
            txtCrearUsuarioCreadorId.setText("1");
        }
    }

    // =====================================================
    // ACCIONES COLUMNA "ACCIONES"
    // =====================================================
    private void handleVerEspecialidad(final Especialidad esp) {
        if (esp == null) {
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de la especialidad");
        dialog.setHeaderText(null);

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        pane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, "
                        + "#f4f7fb, #ffffff);"
                        + "-fx-padding: 20;"
                        + "-fx-font-family: 'Segoe UI', sans-serif;"
        );

        VBox root = new VBox(12);
        root.setFillWidth(true);

        Label lblNombre = new Label(
                esp.getNombre() != null
                        ? esp.getNombre()
                        : "Especialidad"
        );
        lblNombre.setStyle(
                "-fx-font-size: 18px;"
                        + "-fx-font-weight: bold;"
                        + "-fx-text-fill: #00695c;"
        );

        Label lblSubtitulo = new Label("Información de la especialidad");
        lblSubtitulo.setStyle(
                "-fx-font-size: 13px;"
                        + "-fx-text-fill: #607d8b;"
                        + "-fx-font-weight: bold;"
        );

        Separator separator = new Separator();

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

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
            Label l = new Label(
                    text != null && !text.isBlank()
                            ? text
                            : "-"
            );
            l.setStyle(
                    "-fx-font-size: 13px;"
                            + "-fx-text-fill: #37474f;"
            );
            return l;
        };

        String idText =
                esp.getId() != null
                        ? esp.getId().toString()
                        : "-";
        String descText = esp.getDescripcion();

        String costoText;
        if (esp.getTarifaBase() != null) {
            costoText = "$ " + esp.getTarifaBase().toPlainString();
        } else {
            costoText = "No definido";
        }

        String duracionText;
        if (esp.getDuracionEstandarMin() != null) {
            duracionText = esp.getDuracionEstandarMin() + " min";
        } else {
            duracionText = "-";
        }

        String usuarioCreadorText =
                esp.getUsuarioCreadorId() != null
                        ? esp.getUsuarioCreadorId().toString()
                        : "-";

        String fechaCreacionText = "-";
        if (esp.getFechaCreacion() != null) {
            fechaCreacionText = esp.getFechaCreacion()
                    .format(
                            DateTimeFormatter.ofPattern(
                                    "dd/MM/yyyy HH:mm"
                            )
                    );
        }

        int row = 0;
        grid.add(leftLabel.apply("ID:"), 0, row);
        grid.add(rightLabel.apply(idText), 1, row++);
        grid.add(leftLabel.apply("Nombre:"), 0, row);
        grid.add(rightLabel.apply(esp.getNombre()), 1, row++);
        grid.add(leftLabel.apply("Código:"), 0, row);
        grid.add(rightLabel.apply(esp.getCodigo()), 1, row++);
        grid.add(leftLabel.apply("Descripción:"), 0, row);
        grid.add(rightLabel.apply(descText), 1, row++);
        grid.add(leftLabel.apply("Duración estándar:"), 0, row);
        grid.add(rightLabel.apply(duracionText), 1, row++);
        grid.add(leftLabel.apply("Tarifa base:"), 0, row);
        grid.add(rightLabel.apply(costoText), 1, row++);
        grid.add(leftLabel.apply("Estado:"), 0, row);
        grid.add(rightLabel.apply(esp.getEstado()), 1, row++);
        grid.add(leftLabel.apply("Usuario creador ID:"), 0, row);
        grid.add(rightLabel.apply(usuarioCreadorText), 1, row++);
        grid.add(leftLabel.apply("Fecha creación:"), 0, row);
        grid.add(rightLabel.apply(fechaCreacionText), 1, row++);

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

    private void handleEditarEspecialidad(final Especialidad esp) {
        if (esp == null) {
            return;
        }

        Especialidad especialidadMostrar = esp;
        if (esp.getId() != null) {
            Especialidad desdeBd =
                    especialidadService.getEspecialidadById(esp.getId());
            if (desdeBd != null) {
                especialidadMostrar = desdeBd;
            }
        }

        txtEditarId.setText(
                especialidadMostrar.getId() != null
                        ? especialidadMostrar.getId().toString()
                        : ""
        );
        txtEditarNombre.setText(
                especialidadMostrar.getNombre()
        );
        txtEditarCodigo.setText(
                especialidadMostrar.getCodigo()
        );
        txtEditarDescripcion.setText(
                especialidadMostrar.getDescripcion()
        );
        txtEditarDuracion.setText(
                especialidadMostrar.getDuracionEstandarMin() != null
                        ? especialidadMostrar.getDuracionEstandarMin()
                        .toString()
                        : ""
        );
        txtEditarCostoConsulta.setText(
                especialidadMostrar.getTarifaBase() != null
                        ? especialidadMostrar.getTarifaBase()
                        .toPlainString()
                        : ""
        );

        if (especialidadMostrar.getEstado() != null) {
            cmbEditarEstado
                    .getSelectionModel()
                    .select(especialidadMostrar.getEstado());
        } else {
            cmbEditarEstado
                    .getSelectionModel()
                    .clearSelection();
        }

        txtEditarUsuarioCreadorId.setText(
                especialidadMostrar.getUsuarioCreadorId() != null
                        ? especialidadMostrar.getUsuarioCreadorId()
                        .toString()
                        : ""
        );

        String fechaCreacionText = "";
        if (especialidadMostrar.getFechaCreacion() != null) {
            fechaCreacionText =
                    especialidadMostrar.getFechaCreacion()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "dd/MM/yyyy HH:mm"
                                    )
                            );
        }
        txtEditarFechaCreacion.setText(fechaCreacionText);

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void handleEliminarEspecialidad(final Especialidad esp) {
        if (esp == null) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Especialidad");
        confirm.setHeaderText(
                "¿Desea eliminar la especialidad seleccionada?"
        );
        confirm.setContentText(esp.getNombre());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean ok =
                        especialidadService.eliminarEspecialidad(
                                esp.getId()
                        );
                if (ok) {
                    especialidades.remove(esp);
                } else {
                    new Alert(
                            Alert.AlertType.ERROR,
                            "No se pudo eliminar la especialidad."
                    ).showAndWait();
                }
            }
        });
    }

    // =====================================================
    // HANDLERS - EDITAR
    // =====================================================
    @FXML
    private void handleActualizarEspecialidad(final ActionEvent event) {
        String nombre = trimOrNull(txtEditarNombre.getText());
        String codigo = trimOrNull(txtEditarCodigo.getText());
        String descripcion =
                trimOrNull(txtEditarDescripcion.getText());
        String duracionText =
                trimOrNull(txtEditarDuracion.getText());
        String estadoSel =
                cmbEditarEstado != null
                        ? cmbEditarEstado
                        .getSelectionModel()
                        .getSelectedItem()
                        : null;
        String costoText =
                trimOrNull(txtEditarCostoConsulta.getText());

        List<String> errores = new ArrayList<>();

        if (nombre == null) {
            errores.add("El nombre es obligatorio.");
        }
        if (codigo == null) {
            errores.add("El código es obligatorio.");
        }

        Integer id = null;
        if (txtEditarId.getText() != null
                && !txtEditarId.getText().isBlank()) {
            try {
                id = Integer.parseInt(txtEditarId.getText());
            } catch (NumberFormatException ex) {
                errores.add("El ID no es válido.");
            }
        }

        Integer duracion = null;
        if (duracionText != null) {
            try {
                duracion = Integer.parseInt(duracionText);
                if (duracion < DURACION_MIN
                        || duracion > DURACION_MAX) {
                    errores.add(
                            "La duración estándar debe estar entre "
                                    + DURACION_MIN
                                    + " y "
                                    + DURACION_MAX
                                    + " minutos."
                    );
                }
            } catch (NumberFormatException ex) {
                errores.add(
                        "La duración estándar debe ser un número entero."
                );
            }
        } else {
            duracion = 30;
        }

        if (estadoSel == null) {
            estadoSel = "ACTIVO";
        }

        BigDecimal costo = null;
        if (costoText != null) {
            try {
                costo = new BigDecimal(
                        costoText.replace(",", ".")
                );
            } catch (NumberFormatException ex) {
                errores.add(
                        "La tarifa base no tiene un formato válido. "
                                + "Use por ejemplo 25.00."
                );
            }
        }

        if (!errores.isEmpty()) {
            mostrarErrores(
                    "Validación de especialidad",
                    errores
            );
            return;
        }

        Especialidad e = new Especialidad();
        e.setId(id);
        e.setNombre(nombre);
        e.setCodigo(codigo);
        e.setDescripcion(descripcion);
        e.setDuracionEstandarMin(duracion);
        e.setTarifaBase(costo);
        e.setEstado(estadoSel);

        if (id != null) {
            Especialidad original =
                    especialidadService.getEspecialidadById(id);
            if (original != null) {
                e.setUsuarioCreadorId(
                        original.getUsuarioCreadorId()
                );
                e.setFechaCreacion(original.getFechaCreacion());
            }
        }

        boolean ok;

        if (id == null) {
            if (e.getUsuarioCreadorId() == null) {
                e.setUsuarioCreadorId(1);
            }
            ok = especialidadService.crearEspecialidad(e);
        } else {
            ok = especialidadService.actualizarEspecialidad(e);
        }

        if (ok) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText(
                    "Los datos de la especialidad "
                            + "se guardaron correctamente."
            );
            alert.showAndWait();

            tabPane.getSelectionModel().selectFirst();
            tabEditar.setDisable(true);
            cargarEspecialidades();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(
                    "No se pudo guardar la especialidad"
            );
            alert.setContentText(
                    "Revisa la consola para más detalles."
            );
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancelarEdicion(final ActionEvent event) {
        tabPane.getSelectionModel().selectFirst();
        tabEditar.setDisable(true);
    }

    // =====================================================
    // UTILS
    // =====================================================
    private String trimOrNull(final String text) {
        if (text == null) {
            return null;
        }
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }

    private void mostrarErrores(
            final String titulo,
            final List<String> errores
    ) {
        StringBuilder sb = new StringBuilder();
        for (String e : errores) {
            sb.append("• ").append(e).append("\n");
        }
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText("Por favor corrige los siguientes errores:");
        alert.setContentText(sb.toString());
        alert.showAndWait();
    }
}
