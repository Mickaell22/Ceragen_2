package com.example.ceragen_2.controller;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.GridPane;
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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadController {

    // ----------------- Service -----------------
    private final EspecialidadService especialidadService = EspecialidadService.getInstance();

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
    private TableColumn<Especialidad, String> colDescripcion;
    @FXML
    private TableColumn<Especialidad, String> colCostoConsulta;
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
    private TextArea txtCrearDescripcion;
    @FXML
    private TextField txtCrearCostoConsulta;

    // ----------------- Campos de edición (Tab Editar) -----------------
    @FXML
    private TextField txtEditarId;
    @FXML
    private TextField txtEditarNombre;
    @FXML
    private TextArea txtEditarDescripcion;
    @FXML
    private TextField txtEditarCostoConsulta;

    // ----------------- Datos internos / paginación -----------------
    private final ObservableList<Especialidad> especialidades = FXCollections.observableArrayList();
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
        cargarEspecialidades();
    }

    // =====================================================
    // CONFIGURACIÓN TABLA
    // =====================================================
    private void configurarTabla() {
        colId.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getId())
        );

        colNombre.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombre())
        );

        colDescripcion.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getDescripcion())
        );

        colCostoConsulta.setCellValueFactory(cellData -> {
            BigDecimal costo = cellData.getValue().getCostoConsulta();
            String texto = (costo != null) ? costo.toPlainString() : "";
            return new SimpleStringProperty(texto);
        });

        configurarColumnaAcciones();
        tableEspecialidades.setItems(especialidades);
    }

    private void configurarColumnaAcciones() {
        Callback<TableColumn<Especialidad, Void>, TableCell<Especialidad, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<Especialidad, Void> call(TableColumn<Especialidad, Void> param) {
                        return new TableCell<>() {

                            private final Button btnVer = new Button("Ver");
                            private final Button btnEditar = new Button("Editar");
                            private final Button btnEliminar = new Button("Eliminar");
                            private final javafx.scene.layout.HBox container =
                                    new javafx.scene.layout.HBox(5, btnVer, btnEditar, btnEliminar);

                            {
                                container.getStyleClass().add("actions-container");
                                btnVer.getStyleClass().addAll("btn", "btn-secondary");
                                btnEditar.getStyleClass().addAll("btn", "btn-warning");
                                btnEliminar.getStyleClass().addAll("btn", "btn-danger");

                                btnVer.setOnAction(e -> {
                                    Especialidad esp = getTableView().getItems().get(getIndex());
                                    handleVerEspecialidad(esp);
                                });

                                btnEditar.setOnAction(e -> {
                                    Especialidad esp = getTableView().getItems().get(getIndex());
                                    handleEditarEspecialidad(esp);
                                });

                                btnEliminar.setOnAction(e -> {
                                    Especialidad esp = getTableView().getItems().get(getIndex());
                                    handleEliminarEspecialidad(esp);
                                });
                            }

                            @Override
                            protected void updateItem(Void item, boolean empty) {
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
        cmbRegistrosPorPagina.setItems(FXCollections.observableArrayList("10", "25", "50"));
        cmbRegistrosPorPagina.getSelectionModel().select("10");

        cmbRegistrosPorPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrosPorPagina = Integer.parseInt(newVal);
                paginaActual = 1;
                cargarEspecialidades();
            }
        });

        actualizarTextoPaginacion();
    }

    private void actualizarTextoPaginacion() {
        if (txtPaginacion != null) {
            txtPaginacion.setText("Página " + paginaActual + " de " + totalPaginas);
        }
    }

    // =====================================================
    // CARGA DE DATOS
    // =====================================================
    private void cargarEspecialidades() {
        mostrarLoading(true);

        String nombreFiltro = trimOrNull(txtFiltroNombre != null ? txtFiltroNombre.getText() : null);
        String descripcionFiltro = trimOrNull(txtFiltroDescripcion != null ? txtFiltroDescripcion.getText() : null);

        new Thread(() -> {
            List<Especialidad> todos = especialidadService.getAllEspecialidades();

            // Filtros en memoria
            List<Especialidad> filtrados = new ArrayList<>();
            for (Especialidad e : todos) {

                if (nombreFiltro != null &&
                        (e.getNombre() == null ||
                                !e.getNombre().toLowerCase().contains(nombreFiltro.toLowerCase()))) {
                    continue;
                }

                if (descripcionFiltro != null &&
                        (e.getDescripcion() == null ||
                                !e.getDescripcion().toLowerCase().contains(descripcionFiltro.toLowerCase()))) {
                    continue;
                }

                filtrados.add(e);
            }

            // Paginación en memoria
            listaCompleta = filtrados;
            totalRegistros = listaCompleta.size();
            totalPaginas = (int) Math.max(1, Math.ceil((double) totalRegistros / registrosPorPagina));

            if (paginaActual > totalPaginas) paginaActual = totalPaginas;
            if (paginaActual < 1) paginaActual = 1;

            int fromIndex = (paginaActual - 1) * registrosPorPagina;
            int toIndex = Math.min(fromIndex + registrosPorPagina, listaCompleta.size());

            List<Especialidad> pagina = new ArrayList<>();
            if (fromIndex < toIndex) {
                pagina = listaCompleta.subList(fromIndex, toIndex);
            }

            List<Especialidad> paginaFinal = pagina;

            Platform.runLater(() -> {
                especialidades.setAll(paginaFinal);
                actualizarTextoPaginacion();
                mostrarLoading(false);
            });
        }).start();
    }

    private void mostrarLoading(boolean mostrar) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisible(mostrar);
            loadingIndicator.setManaged(mostrar);
        }
    }

    // =====================================================
    // HANDLERS - LISTAR
    // =====================================================
    @FXML
    private void handleBuscar(ActionEvent event) {
        paginaActual = 1;
        cargarEspecialidades();
    }

    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        if (txtFiltroNombre != null) txtFiltroNombre.clear();
        if (txtFiltroDescripcion != null) txtFiltroDescripcion.clear();

        paginaActual = 1;
        cargarEspecialidades();
    }

    @FXML
    private void handlePrimeraPagina(ActionEvent event) {
        if (paginaActual != 1) {
            paginaActual = 1;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handlePaginaAnterior(ActionEvent event) {
        if (paginaActual > 1) {
            paginaActual--;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handlePaginaSiguiente(ActionEvent event) {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handleUltimaPagina(ActionEvent event) {
        if (paginaActual != totalPaginas) {
            paginaActual = totalPaginas;
            cargarEspecialidades();
        }
    }

    @FXML
    private void handleCambioRegistrosPorPagina(ActionEvent event) {
        String value = cmbRegistrosPorPagina.getSelectionModel().getSelectedItem();
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
    private void handleNuevaEspecialidad(ActionEvent event) {
        handleLimpiarFormCrear(null);
        if (tabCrear != null) {
            tabCrear.setDisable(false);
            tabPane.getSelectionModel().select(tabCrear);
        }
    }

    @FXML
    private void handleCrearEspecialidad(ActionEvent event) {
        String nombre = trimOrNull(txtCrearNombre.getText());
        String descripcion = trimOrNull(txtCrearDescripcion.getText());
        String costoText = trimOrNull(txtCrearCostoConsulta.getText());

        if (nombre == null) {
            new Alert(Alert.AlertType.WARNING,
                    "El nombre de la especialidad es obligatorio.").showAndWait();
            return;
        }

        BigDecimal costo = null;
        if (costoText != null) {
            try {
                costo = new BigDecimal(costoText.replace(",", "."));
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "El costo de la consulta no tiene un formato válido. Use por ejemplo 25.00")
                        .showAndWait();
                return;
            }
        }

        Especialidad e = new Especialidad();
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        e.setCostoConsulta(costo);

        boolean ok = especialidadService.crearEspecialidad(e);

        if (ok) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Especialidad creada correctamente.").showAndWait();
            handleLimpiarFormCrear(null);
            tabPane.getSelectionModel().selectFirst();
            cargarEspecialidades();
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo crear la especialidad. Revisa la consola para más detalles.")
                    .showAndWait();
        }
    }

    @FXML
    private void handleLimpiarFormCrear(ActionEvent event) {
        if (txtCrearNombre != null) txtCrearNombre.clear();
        if (txtCrearDescripcion != null) txtCrearDescripcion.clear();
        if (txtCrearCostoConsulta != null) txtCrearCostoConsulta.clear();
    }

    // =====================================================
    // ACCIONES COLUMNA "ACCIONES"
    // =====================================================
    private void handleVerEspecialidad(Especialidad esp) {
        if (esp == null) return;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Detalle de la especialidad");
        dialog.setHeaderText(null); // usamos nuestro propio header

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);

        // Fondo y estilo general del diálogo
        pane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f4f7fb, #ffffff);" +
                        "-fx-padding: 20;" +
                        "-fx-font-family: 'Segoe UI', sans-serif;"
        );

        // Contenedor principal
        VBox root = new VBox(12);
        root.setFillWidth(true);

        // Título con el nombre de la especialidad
        Label lblNombre = new Label(esp.getNombre() != null ? esp.getNombre() : "Especialidad");
        lblNombre.setStyle(
                "-fx-font-size: 18px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: #00695c;"
        );

        // Subtítulo
        Label lblSubtitulo = new Label("Información de la especialidad");
        lblSubtitulo.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #607d8b;" +
                        "-fx-font-weight: bold;"
        );

        Separator separator = new Separator();

        // Grid con los datos
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);

        // Helper para label de título (izquierda)
        java.util.function.Function<String, Label> leftLabel = text -> {
            Label l = new Label(text);
            l.setStyle(
                    "-fx-font-weight: bold;" +
                            "-fx-text-fill: #455a64;" +
                            "-fx-font-size: 13px;"
            );
            return l;
        };

        // Helper para label de valor (derecha)
        java.util.function.Function<String, Label> rightLabel = text -> {
            Label l = new Label(text != null && !text.isBlank() ? text : "-");
            l.setStyle(
                    "-fx-font-size: 13px;" +
                            "-fx-text-fill: #37474f;"
            );
            return l;
        };

        String idText = esp.getId() != null ? esp.getId().toString() : "-";
        String descText = esp.getDescripcion();
        String costoText;
        if (esp.getCostoConsulta() != null) {
            costoText = "$ " + esp.getCostoConsulta().toPlainString();
        } else {
            costoText = "No definido";
        }

        int row = 0;
        grid.add(leftLabel.apply("ID:"), 0, row);
        grid.add(rightLabel.apply(idText), 1, row++);

        grid.add(leftLabel.apply("Nombre:"), 0, row);
        grid.add(rightLabel.apply(esp.getNombre()), 1, row++);

        grid.add(leftLabel.apply("Descripción:"), 0, row);
        grid.add(rightLabel.apply(descText), 1, row++);

        grid.add(leftLabel.apply("Costo de consulta:"), 0, row);
        grid.add(rightLabel.apply(costoText), 1, row++);

        // Armamos el contenido
        root.getChildren().addAll(lblNombre, lblSubtitulo, separator, grid);
        pane.setContent(root);

        // Botón Cerrar con estilo
        Button btnCerrar = (Button) pane.lookupButton(ButtonType.CLOSE);
        if (btnCerrar != null) {
            btnCerrar.setText("Cerrar");
            btnCerrar.setStyle(
                    "-fx-background-color: #00695c;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 6 20 6 20;" +
                            "-fx-background-radius: 20;"
            );
        }

        dialog.showAndWait();
    }


    private void handleEditarEspecialidad(Especialidad esp) {
        if (esp == null) return;

        if (esp.getId() != null) {
            Especialidad desdeBd = especialidadService.getEspecialidadById(esp.getId());
            if (desdeBd != null) {
                esp = desdeBd;
            }
        }

        txtEditarId.setText(esp.getId() != null ? esp.getId().toString() : "");
        txtEditarNombre.setText(esp.getNombre());
        txtEditarDescripcion.setText(esp.getDescripcion());
        txtEditarCostoConsulta.setText(
                esp.getCostoConsulta() != null ? esp.getCostoConsulta().toPlainString() : ""
        );

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void handleEliminarEspecialidad(Especialidad esp) {
        if (esp == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Especialidad");
        confirm.setHeaderText("¿Desea eliminar la especialidad seleccionada?");
        confirm.setContentText(esp.getNombre());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                boolean ok = especialidadService.eliminarEspecialidad(esp.getId());
                if (ok) {
                    especialidades.remove(esp);
                } else {
                    new Alert(Alert.AlertType.ERROR,
                            "No se pudo eliminar la especialidad.").showAndWait();
                }
            }
        });
    }

    // =====================================================
    // HANDLERS - EDITAR
    // =====================================================
    @FXML
    private void handleActualizarEspecialidad(ActionEvent event) {
        String nombre = trimOrNull(txtEditarNombre.getText());
        String descripcion = trimOrNull(txtEditarDescripcion.getText());
        String costoText = trimOrNull(txtEditarCostoConsulta.getText());

        if (nombre == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos obligatorios");
            alert.setHeaderText("Faltan datos obligatorios");
            alert.setContentText("El nombre de la especialidad es obligatorio.");
            alert.showAndWait();
            return;
        }

        Integer id = null;
        if (!txtEditarId.getText().isBlank()) {
            try {
                id = Integer.parseInt(txtEditarId.getText());
            } catch (NumberFormatException ignored) {}
        }

        BigDecimal costo = null;
        if (costoText != null) {
            try {
                costo = new BigDecimal(costoText.replace(",", "."));
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.WARNING,
                        "El costo de la consulta no tiene un formato válido. Use por ejemplo 25.00")
                        .showAndWait();
                return;
            }
        }

        Especialidad e = new Especialidad();
        e.setId(id);
        e.setNombre(nombre);
        e.setDescripcion(descripcion);
        e.setCostoConsulta(costo);

        boolean ok;
        if (id == null) {
            ok = especialidadService.crearEspecialidad(e);
        } else {
            ok = especialidadService.actualizarEspecialidad(e);
        }

        if (ok) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Éxito");
            alert.setHeaderText(null);
            alert.setContentText("Los datos de la especialidad se guardaron correctamente.");
            alert.showAndWait();

            tabPane.getSelectionModel().selectFirst();
            tabEditar.setDisable(true);
            cargarEspecialidades();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo guardar la especialidad");
            alert.setContentText("Revisa la consola para más detalles.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleCancelarEdicion(ActionEvent event) {
        tabPane.getSelectionModel().selectFirst();
        tabEditar.setDisable(true);
    }

    // =====================================================
    // UTILS
    // =====================================================
    private String trimOrNull(String text) {
        if (text == null) return null;
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }
}
