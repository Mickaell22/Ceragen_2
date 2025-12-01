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
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.List;

public class ProfesionalController {

    // ----------------- Service -----------------
    private final ProfesionalService profesionalService = ProfesionalService.getInstance();

    // ----------------- General / Tabs -----------------
    @FXML
    private TabPane tabPane;
    @FXML
    private Tab tabEditar;
    @FXML
    private Tab tabCrear;

    // ----------------- Filtros (Tab Listar) -----------------
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

    // ----------------- Tabla y loading -----------------
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
    private TableColumn<Profesional, String> colActivo;
    @FXML
    private TableColumn<Profesional, String> colUsuario;
    @FXML
    private TableColumn<Profesional, Void> colAcciones;

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
    private ComboBox<String> cmbCrearUsuario; // visual, pero NO se usa para guardar
    @FXML
    private CheckBox chkCrearActivo;

    // ----------------- Campos de edición (Tab Editar) -----------------
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
    private ComboBox<String> cmbEditarUsuario; // visual, pero NO se usa para guardar
    @FXML
    private TextField txtEditarTelefono;
    @FXML
    private TextField txtEditarEmail;
    @FXML
    private TextField txtEditarNumeroLicencia;
    @FXML
    private CheckBox chkEditarActivo;

    // ----------------- Datos internos / paginación -----------------
    private final ObservableList<Profesional> profesionales = FXCollections.observableArrayList();
    private List<Profesional> listaCompleta = new ArrayList<>();

    private int paginaActual = 1;
    private int totalPaginas = 1;
    private int registrosPorPagina = 10;
    private long totalRegistros = 0;

    // =====================================================
    // Mapeo nombre → ID de especialidad
    // =====================================================
    private Integer mapEspecialidadNombreToId(String nombre) {
        if (nombre == null) return null;
        switch (nombre) {
            case "Fisioterapia":
                return 1;
            case "Traumatología":
                return 2;
            case "Rehabilitación":
                return 3;
            default:
                return null; // por si agregas más y aún no los mapeas
        }
    }

    // ----------------- Inicialización -----------------
    @FXML
    public void initialize() {
        configurarTabla();
        configurarPaginacion();
        configurarCombos();
        cargarProfesionales(); // Primera carga
    }

    // =====================================================
    // CONFIGURACIÓN TABLA
    // =====================================================
    private void configurarTabla() {
        colId.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(cellData.getValue().getId())
        );

        colCedula.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getCedula())
        );

        colNombres.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNombres())
        );

        colApellidos.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getApellidos())
        );

        colEspecialidad.setCellValueFactory(cellData ->
                new SimpleStringProperty(
                        cellData.getValue().getEspecialidadNombre() != null
                                ? cellData.getValue().getEspecialidadNombre()
                                : (cellData.getValue().getEspecialidadId() != null
                                ? "ID " + cellData.getValue().getEspecialidadId()
                                : "")
                )
        );

        colTelefono.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getTelefono())
        );

        colEmail.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getEmail())
        );

        colNumeroLicencia.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getNumeroLicencia())
        );

        colActivo.setCellValueFactory(cellData -> {
            Boolean activo = cellData.getValue().getActivo();
            String texto = Boolean.TRUE.equals(activo) ? "Sí" : "No";
            return new SimpleStringProperty(texto);
        });

        colUsuario.setCellValueFactory(cellData -> {
            Integer usuarioId = cellData.getValue().getUsuarioId();
            String texto = (usuarioId == null) ? "" : "ID " + usuarioId;
            return new SimpleStringProperty(texto);
        });

        configurarColumnaAcciones();
        tableProfesionales.setItems(profesionales);
    }

    private void configurarColumnaAcciones() {
        Callback<TableColumn<Profesional, Void>, TableCell<Profesional, Void>> cellFactory =
                new Callback<>() {
                    @Override
                    public TableCell<Profesional, Void> call(TableColumn<Profesional, Void> param) {
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
                                    Profesional profesional = getTableView().getItems().get(getIndex());
                                    handleVerProfesional(profesional);
                                });

                                btnEditar.setOnAction(e -> {
                                    Profesional profesional = getTableView().getItems().get(getIndex());
                                    handleEditarProfesional(profesional);
                                });

                                btnEliminar.setOnAction(e -> {
                                    Profesional profesional = getTableView().getItems().get(getIndex());
                                    handleEliminarProfesional(profesional);
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
    // CONFIGURACIÓN Paginación y Combos
    // =====================================================
    private void configurarPaginacion() {
        cmbRegistrosPorPagina.setItems(FXCollections.observableArrayList("10", "25", "50"));
        cmbRegistrosPorPagina.getSelectionModel().select("10");

        cmbRegistrosPorPagina.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                registrosPorPagina = Integer.parseInt(newVal);
                paginaActual = 1;
                cargarProfesionales();
            }
        });

        actualizarTextoPaginacion();
    }

    private void configurarCombos() {
        // Estado filtro
        if (cmbActivoFiltro.getItems().isEmpty()) {
            cmbActivoFiltro.setItems(FXCollections.observableArrayList("TODOS", "ACTIVO", "INACTIVO"));
        }
        cmbActivoFiltro.getSelectionModel().selectFirst();

        // Especialidades en filtro: las construimos a partir de los profesionales
        new Thread(() -> {
            List<Profesional> todos = profesionalService.getAllProfesionales();
            List<String> nombres = new ArrayList<>();
            nombres.add("Todas");
            for (Profesional p : todos) {
                String esp = p.getEspecialidadNombre();
                if (esp != null && !nombres.contains(esp)) {
                    nombres.add(esp);
                }
            }

            Platform.runLater(() -> {
                cmbEspecialidadFiltro.setItems(FXCollections.observableArrayList(nombres));
                cmbEspecialidadFiltro.getSelectionModel().selectFirst();
            });
        }).start();

        // Combos de edición/creación (aquí podrías cargar catálogos reales desde BD)
        ObservableList<String> especialidades =
                FXCollections.observableArrayList("Fisioterapia", "Traumatología", "Rehabilitación");
        ObservableList<String> usuarios =
                FXCollections.observableArrayList("1", "2", "3"); // solo decorativo

        if (cmbEditarEspecialidad != null) cmbEditarEspecialidad.setItems(especialidades);
        if (cmbCrearEspecialidad != null) cmbCrearEspecialidad.setItems(especialidades);

        if (cmbEditarUsuario != null) cmbEditarUsuario.setItems(usuarios);
        if (cmbCrearUsuario != null) cmbCrearUsuario.setItems(usuarios);
    }

    // =====================================================
    // CARGA DE DATOS (usando ProfesionalService)
    // =====================================================
    private void cargarProfesionales() {
        mostrarLoading(true);

        String cedulaFiltro = trimOrNull(txtFiltroCedula.getText());
        String nombresFiltro = trimOrNull(txtFiltroNombres.getText());
        String apellidosFiltro = trimOrNull(txtFiltroApellidos.getText());
        String especialidadFiltro = cmbEspecialidadFiltro.getSelectionModel().getSelectedItem();
        String estadoFiltro = cmbActivoFiltro.getSelectionModel().getSelectedItem();

        new Thread(() -> {
            List<Profesional> todos = profesionalService.getAllProfesionales();

            // Filtros en memoria
            List<Profesional> filtrados = new ArrayList<>();
            for (Profesional p : todos) {

                if (cedulaFiltro != null &&
                        (p.getCedula() == null || !p.getCedula().contains(cedulaFiltro))) {
                    continue;
                }

                if (nombresFiltro != null &&
                        (p.getNombres() == null ||
                                !p.getNombres().toLowerCase().contains(nombresFiltro.toLowerCase()))) {
                    continue;
                }

                if (apellidosFiltro != null &&
                        (p.getApellidos() == null ||
                                !p.getApellidos().toLowerCase().contains(apellidosFiltro.toLowerCase()))) {
                    continue;
                }

                if (especialidadFiltro != null &&
                        !"Todas".equalsIgnoreCase(especialidadFiltro)) {
                    String espNombre = p.getEspecialidadNombre();
                    if (espNombre == null ||
                            !espNombre.equalsIgnoreCase(especialidadFiltro)) {
                        continue;
                    }
                }

                if ("ACTIVO".equalsIgnoreCase(estadoFiltro) &&
                        !Boolean.TRUE.equals(p.getActivo())) {
                    continue;
                }

                if ("INACTIVO".equalsIgnoreCase(estadoFiltro) &&
                        Boolean.TRUE.equals(p.getActivo())) {
                    continue;
                }

                filtrados.add(p);
            }

            // Paginación en memoria
            listaCompleta = filtrados;
            totalRegistros = listaCompleta.size();
            totalPaginas = (int) Math.max(1, Math.ceil((double) totalRegistros / registrosPorPagina));

            if (paginaActual > totalPaginas) paginaActual = totalPaginas;
            if (paginaActual < 1) paginaActual = 1;

            int fromIndex = (paginaActual - 1) * registrosPorPagina;
            int toIndex = Math.min(fromIndex + registrosPorPagina, listaCompleta.size());

            List<Profesional> pagina = new ArrayList<>();
            if (fromIndex < toIndex) {
                pagina = listaCompleta.subList(fromIndex, toIndex);
            }

            List<Profesional> paginaFinal = pagina;

            Platform.runLater(() -> {
                profesionales.setAll(paginaFinal);
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

    private void actualizarTextoPaginacion() {
        if (txtPaginacion != null) {
            txtPaginacion.setText("Página " + paginaActual + " de " + totalPaginas);
        }
    }

    // =====================================================
    // HANDLERS - LISTAR
    // =====================================================
    @FXML
    private void handleBuscar(ActionEvent event) {
        paginaActual = 1;
        cargarProfesionales();
    }

    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
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
    private void handlePrimeraPagina(ActionEvent event) {
        if (paginaActual != 1) {
            paginaActual = 1;
            cargarProfesionales();
        }
    }

    @FXML
    private void handlePaginaAnterior(ActionEvent event) {
        if (paginaActual > 1) {
            paginaActual--;
            cargarProfesionales();
        }
    }

    @FXML
    private void handlePaginaSiguiente(ActionEvent event) {
        if (paginaActual < totalPaginas) {
            paginaActual++;
            cargarProfesionales();
        }
    }

    @FXML
    private void handleUltimaPagina(ActionEvent event) {
        if (paginaActual != totalPaginas) {
            paginaActual = totalPaginas;
            cargarProfesionales();
        }
    }

    @FXML
    private void handleCambioRegistrosPorPagina(ActionEvent event) {
        String value = cmbRegistrosPorPagina.getSelectionModel().getSelectedItem();
        if (value != null) {
            registrosPorPagina = Integer.parseInt(value);
            paginaActual = 1;
            cargarProfesionales();
        }
    }

    // =====================================================
    // HANDLERS - CREAR
    // =====================================================
    @FXML
    private void handleNuevoProfesional(ActionEvent event) {
        // Limpia el formulario de creación
        handleLimpiarFormCrear(null);

        // Habilita (por si acaso) y selecciona la pestaña Crear
        if (tabCrear != null) {
            tabCrear.setDisable(false);
            tabPane.getSelectionModel().select(tabCrear);
        }
    }

    @FXML
    private void handleCrearProfesional(ActionEvent event) {
        String cedula = trimOrNull(txtCrearCedula.getText());
        String nombres = trimOrNull(txtCrearNombres.getText());
        String apellidos = trimOrNull(txtCrearApellidos.getText());

        if (cedula == null || nombres == null || apellidos == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Cédula, Nombres y Apellidos son obligatorios.").showAndWait();
            return;
        }

        String telefono = trimOrNull(txtCrearTelefono.getText());
        String email = trimOrNull(txtCrearEmail.getText());
        String numeroLicencia = trimOrNull(txtCrearNumeroLicencia.getText());
        Boolean activo = chkCrearActivo != null && chkCrearActivo.isSelected();

        String especialidadNombre = cmbCrearEspecialidad.getSelectionModel().getSelectedItem();
        Integer especialidadId = mapEspecialidadNombreToId(especialidadNombre);
        if (especialidadId == null) {
            new Alert(Alert.AlertType.WARNING,
                    "Debe seleccionar una especialidad.").showAndWait();
            return;
        }

        // <<< NO DEPENDEMOS DE USUARIOS >>>
        Integer usuarioId = null;

        Profesional p = new Profesional();
        p.setCedula(cedula);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setNumeroLicencia(numeroLicencia);
        p.setActivo(activo);
        p.setUsuarioId(usuarioId); // siempre null
        p.setEspecialidadNombre(especialidadNombre);
        p.setEspecialidadId(especialidadId);

        boolean ok = profesionalService.crearProfesional(p);

        if (ok) {
            new Alert(Alert.AlertType.INFORMATION,
                    "Profesional creado correctamente.").showAndWait();
            handleLimpiarFormCrear(null);
            tabPane.getSelectionModel().selectFirst(); // vuelve a Listar
            cargarProfesionales();
        } else {
            new Alert(Alert.AlertType.ERROR,
                    "No se pudo crear el profesional. Revisa la consola para más detalles.")
                    .showAndWait();
        }
    }

    @FXML
    private void handleLimpiarFormCrear(ActionEvent event) {
        if (txtCrearCedula != null) txtCrearCedula.clear();
        if (txtCrearNombres != null) txtCrearNombres.clear();
        if (txtCrearApellidos != null) txtCrearApellidos.clear();
        if (txtCrearTelefono != null) txtCrearTelefono.clear();
        if (txtCrearEmail != null) txtCrearEmail.clear();
        if (txtCrearNumeroLicencia != null) txtCrearNumeroLicencia.clear();

        if (cmbCrearEspecialidad != null) cmbCrearEspecialidad.getSelectionModel().clearSelection();
        if (cmbCrearUsuario != null) cmbCrearUsuario.getSelectionModel().clearSelection();
        if (chkCrearActivo != null) chkCrearActivo.setSelected(true);
    }

    // =====================================================
    // ACCIONES COLUMNA "ACCIONES"
    // =====================================================
    private void handleVerProfesional(Profesional profesional) {
        if (profesional == null) return;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Detalle Profesional");
        alert.setHeaderText(profesional.getNombreCompleto());
        alert.setContentText(
                "Cédula: " + profesional.getCedula() + "\n" +
                        "Especialidad: " + (profesional.getEspecialidadNombre() != null
                        ? profesional.getEspecialidadNombre()
                        : profesional.getEspecialidadId()) + "\n" +
                        "Teléfono: " + profesional.getTelefono() + "\n" +
                        "Email: " + profesional.getEmail()
        );
        alert.showAndWait();
    }

    private void handleEditarProfesional(Profesional profesional) {
        if (profesional == null) return;

        // Opcional pero recomendado: refrescar desde BD para traer todo actualizado
        if (profesional.getId() != null) {
            Profesional desdeBd = profesionalService.getProfesionalById(profesional.getId());
            if (desdeBd != null) {
                profesional = desdeBd;
            }
        }

        txtEditarId.setText(profesional.getId() != null ? profesional.getId().toString() : "");
        txtEditarCedula.setText(profesional.getCedula());
        txtEditarNombres.setText(profesional.getNombres());
        txtEditarApellidos.setText(profesional.getApellidos());
        txtEditarTelefono.setText(profesional.getTelefono());
        txtEditarEmail.setText(profesional.getEmail());
        txtEditarNumeroLicencia.setText(profesional.getNumeroLicencia());
        chkEditarActivo.setSelected(Boolean.TRUE.equals(profesional.getActivo()));

        if (profesional.getEspecialidadNombre() != null) {
            cmbEditarEspecialidad.getSelectionModel().select(profesional.getEspecialidadNombre());
        } else {
            cmbEditarEspecialidad.getSelectionModel().clearSelection();
        }

        if (profesional.getUsuarioId() != null) {
            cmbEditarUsuario.getSelectionModel().select(profesional.getUsuarioId().toString());
        } else {
            cmbEditarUsuario.getSelectionModel().clearSelection();
        }

        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void handleEliminarProfesional(Profesional profesional) {
        if (profesional == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar Profesional");
        confirm.setHeaderText("¿Desea eliminar al profesional seleccionado?");
        confirm.setContentText(profesional.getNombreCompleto());

        confirm.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                // TODO: implementar eliminado real en la BD si lo deseas
                // profesionalService.eliminarProfesional(profesional.getId());
                profesionales.remove(profesional);
            }
        });
    }

    // =====================================================
    // HANDLERS - EDITAR
    // =====================================================
    @FXML
    private void handleActualizarProfesional(ActionEvent event) {
        String cedula = trimOrNull(txtEditarCedula.getText());
        String nombres = trimOrNull(txtEditarNombres.getText());
        String apellidos = trimOrNull(txtEditarApellidos.getText());

        if (cedula == null || nombres == null || apellidos == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Campos obligatorios");
            alert.setHeaderText("Faltan datos obligatorios");
            alert.setContentText("Cédula, Nombres y Apellidos son obligatorios.");
            alert.showAndWait();
            return;
        }

        Integer id = null;
        if (!txtEditarId.getText().isBlank()) {
            try {
                id = Integer.parseInt(txtEditarId.getText());
            } catch (NumberFormatException ignored) {}
        }

        String telefono = trimOrNull(txtEditarTelefono.getText());
        String email = trimOrNull(txtEditarEmail.getText());
        String numeroLicencia = trimOrNull(txtEditarNumeroLicencia.getText());
        Boolean activo = chkEditarActivo.isSelected();

        String especialidadNombre = cmbEditarEspecialidad.getSelectionModel().getSelectedItem();
        Integer especialidadId = mapEspecialidadNombreToId(especialidadNombre);

        // <<< NO DEPENDEMOS DE USUARIOS TAMPOCO AL EDITAR >>>
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
        p.setUsuarioId(usuarioId); // siempre null
        p.setEspecialidadNombre(especialidadNombre);
        p.setEspecialidadId(especialidadId);

        // Respaldo: si estás editando y el mapa devolvió null, conserva el ID original
        if (id != null && especialidadId == null) {
            Profesional original = profesionalService.getProfesionalById(id);
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
            alert.setContentText("Los datos del profesional se guardaron correctamente.");
            alert.showAndWait();

            tabPane.getSelectionModel().selectFirst();
            tabEditar.setDisable(true);
            cargarProfesionales();
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("No se pudo guardar el profesional");
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
