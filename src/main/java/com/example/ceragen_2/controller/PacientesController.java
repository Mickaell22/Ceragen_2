package com.example.ceragen_2.controller;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

import com.example.ceragen_2.model.DocumentoPaciente;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.service.DocumentoPacienteService;
import com.example.ceragen_2.service.PacienteService;

import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class PacientesController {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(PacientesController.class);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final int DEFAULT_PAGE_SIZE = 10;

    private final PacienteService pacienteService = PacienteService.getInstance();
    private final DocumentoPacienteService documentoService = DocumentoPacienteService.getInstance();


    private int paginaActual = 0;
    private int registrosPorPagina = DEFAULT_PAGE_SIZE;
    private int totalPaginas = 0;

    @FXML private TabPane tabPane;
    @FXML private Tab tabCrear;
    @FXML private Tab tabEditar;
    @FXML private Tab tabDocumentos;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<String> cmbGeneroFiltro;

    @FXML private TableView<Paciente> tablePacientes;
    @FXML private TableColumn<Paciente, String> colId;
    @FXML private TableColumn<Paciente, String> colCedula;
    @FXML private TableColumn<Paciente, String> colNombreCompleto;
    @FXML private TableColumn<Paciente, String> colGenero;
    @FXML private TableColumn<Paciente, String> colTelefono;
    @FXML private TableColumn<Paciente, String> colEmail;
    @FXML private TableColumn<Paciente, String> colFechaRegistro;
    @FXML private TableColumn<Paciente, Void> colAcciones;

    @FXML private TableView<DocumentoPaciente> tableDocumentos;
    @FXML private TableColumn<DocumentoPaciente, String> colDocNombre;
    @FXML private TableColumn<DocumentoPaciente, String> colDocTipo;
    @FXML private TableColumn<DocumentoPaciente, String> colDocFecha;
    @FXML private TableColumn<DocumentoPaciente, String> colDocRuta;
    @FXML private TableColumn<DocumentoPaciente, Void> colDocAcciones;
    @FXML private ComboBox<String> cmbTipoDocumento;

    @FXML private Button btnPrimera;
    @FXML private Button btnAnterior;
    @FXML private Button btnSiguiente;
    @FXML private Button btnUltima;
    @FXML private Text txtPaginacion;
    @FXML private ComboBox<String> cmbRegistrosPorPagina;

    @FXML private TextField txtCrearCedula;
    @FXML private TextField txtCrearNombres;
    @FXML private TextField txtCrearApellidos;
    @FXML private DatePicker dpCrearFechaNacimiento;
    @FXML private ComboBox<String> cmbCrearGenero;
    @FXML private TextField txtCrearTelefono;
    @FXML private TextField txtCrearEmail;
    @FXML private TextArea txtCrearDireccion;
    @FXML private TextField txtCrearGrupoSanguineo;
    @FXML private TextArea txtCrearAlergias;

    @FXML private TextField txtEditarId;
    @FXML private TextField txtEditarCedula;
    @FXML private TextField txtEditarNombres;
    @FXML private TextField txtEditarApellidos;
    @FXML private DatePicker dpEditarFechaNacimiento;
    @FXML private ComboBox<String> cmbEditarGenero;
    @FXML private TextField txtEditarTelefono;
    @FXML private TextField txtEditarEmail;
    @FXML private TextArea txtEditarDireccion;
    @FXML private TextField txtEditarGrupoSanguineo;
    @FXML private TextArea txtEditarAlergias;

    @FXML private VBox loadingIndicator;

    private Paciente pacienteEnEdicion;

    @FXML
    public void initialize() {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("Inicializando módulo de Pacientes");
        }

        configurarTabla();
        configurarFiltros();
        configurarPaginacion();
        configurarValidaciones();
        cargarDatos();
    }

    private void abrirArchivoDocumento(final DocumentoPaciente d) {
        try {
            if (d == null || d.getRutaArchivo() == null) {
                mostrarAlerta(
                        "Error",
                        "Ruta de archivo no disponible",
                        Alert.AlertType.ERROR
                );
                return;
            }
            File archivo = new File(d.getRutaArchivo());
            if (!archivo.exists()) {
                mostrarAlerta(
                        "No encontrado",
                        "El archivo no existe en la ruta: " + d.getRutaArchivo(),
                        Alert.AlertType.WARNING
                );
                return;
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(archivo);
            } else {
                mostrarAlerta(
                        "No soportado",
                        "La apertura de archivos no es soportada en este sistema.",
                        Alert.AlertType.ERROR
                );
            }
        } catch (IOException ex) {
            mostrarAlerta(
                    "Error",
                    "No se pudo abrir el archivo: " + ex.getMessage(),
                    Alert.AlertType.ERROR
            );
        }
    }

    private void configurarTabla() {
        colId.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getId().toString()));
        colCedula.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCedula()));
        colNombreCompleto.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreCompleto()));
        colGenero.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getGenero() == null ? "" : data.getValue().getGenero()));
        colTelefono.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTelefono() == null ? "" : data.getValue().getTelefono()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail() == null ? "" : data.getValue().getEmail()));
        colFechaRegistro.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaRegistro() == null ? "" : data.getValue().getFechaRegistro().format(DATE_FORMATTER)
        ));

        colAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnEliminar = new Button("Eliminar");
            private final Button btnDocs = new Button("Documentos");
            private final HBox pane = new HBox(10, btnEditar, btnEliminar, btnDocs);
            {
                pane.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnDocs.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");

                btnEditar.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    abrirEdicion(p);
                });
                btnEliminar.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    eliminarPaciente(p);
                });
                btnDocs.setOnAction(e -> {
                    Paciente p = getTableView().getItems().get(getIndex());
                    abrirDocumentos(p);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });
    }

    private void configurarFiltros() {
        cmbGeneroFiltro.setValue("TODOS");
    }

    private void configurarPaginacion() {
        cmbRegistrosPorPagina.setValue("10");
    }

    private void cargarDatos() {
        loadingIndicator.setVisible(true);
        deshabilitarControles(true);

        final String searchText = txtBuscar.getText();
        final String generoFilter = cmbGeneroFiltro.getValue();
        final int offset = paginaActual * registrosPorPagina;

        Task<DatosPacientesResult> task = new Task<>() {
            @Override
            protected DatosPacientesResult call() {
                int totalRegistros = pacienteService.countPacientes(searchText, generoFilter);
                int totalPaginasTemp = (int) Math.ceil((double) totalRegistros / registrosPorPagina);
                if (totalPaginasTemp == 0) totalPaginasTemp = 1;
                List<Paciente> pacientes = pacienteService.getPacientes(offset, registrosPorPagina, searchText, generoFilter);
                return new DatosPacientesResult(pacientes, totalPaginasTemp);
            }
        };

        task.setOnSucceeded(ev -> {
            DatosPacientesResult res = task.getValue();
            totalPaginas = res.totalPaginas;
            if (paginaActual >= totalPaginas) paginaActual = totalPaginas - 1;
            if (paginaActual < 0) paginaActual = 0;
            tablePacientes.getItems().clear();
            tablePacientes.getItems().addAll(res.pacientes);
            actualizarInfoPaginacion();
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
        });

        task.setOnFailed(ev -> {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Error al cargar pacientes", task.getException());
            }
            loadingIndicator.setVisible(false);
            deshabilitarControles(false);
            mostrarAlerta("Error", "No se pudieron cargar los datos", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    private void deshabilitarControles(final boolean deshabilitar) {
        btnPrimera.setDisable(deshabilitar);
        btnAnterior.setDisable(deshabilitar);
        btnSiguiente.setDisable(deshabilitar);
        btnUltima.setDisable(deshabilitar);
        cmbRegistrosPorPagina.setDisable(deshabilitar);
        txtBuscar.setDisable(deshabilitar);
        cmbGeneroFiltro.setDisable(deshabilitar);
    }

    private static class DatosPacientesResult {
        List<Paciente> pacientes;
        int totalPaginas;
        DatosPacientesResult(final List<Paciente> pacientes, final int totalPaginas) {
            this.pacientes = pacientes;
            this.totalPaginas = totalPaginas;
        }
    }

    private void actualizarInfoPaginacion() {
        txtPaginacion.setText("Página " + (paginaActual + 1) + " de " + totalPaginas);
        btnPrimera.setDisable(paginaActual == 0);
        btnAnterior.setDisable(paginaActual == 0);
        btnSiguiente.setDisable(paginaActual >= totalPaginas - 1);
        btnUltima.setDisable(paginaActual >= totalPaginas - 1);
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handleBuscar() { paginaActual = 0; cargarDatos(); }
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handleLimpiarFiltros() {
        txtBuscar.clear();
        cmbGeneroFiltro.setValue("TODOS");
        paginaActual = 0;
        cargarDatos();
    }
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handlePrimeraPagina() {
        paginaActual = 0;
        cargarDatos();
    }
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handlePaginaAnterior() {
        if (paginaActual > 0) {
            paginaActual--;
            cargarDatos();
        }
    }
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handlePaginaSiguiente() {
        if (paginaActual < totalPaginas - 1) {
            paginaActual++;
            cargarDatos();
        }
    }
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handleUltimaPagina() {
        paginaActual = totalPaginas - 1;
        cargarDatos();
    }
    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handleCambioRegistrosPorPagina() {
        registrosPorPagina = Integer.parseInt(cmbRegistrosPorPagina.getValue());
        paginaActual = 0;
        cargarDatos();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleCrearPaciente() {
        final String cedula = txtCrearCedula.getText().trim();
        final String nombres = txtCrearNombres.getText().trim();
        final String apellidos = txtCrearApellidos.getText().trim();
        final String genero = cmbCrearGenero.getValue();

        if (cedula.isEmpty()) {
            mostrarAlerta("Error", "La cédula es obligatoria", Alert.AlertType.ERROR);
            return;
        }
        if (nombres.isEmpty()) {
            mostrarAlerta("Error", "Los nombres son obligatorios", Alert.AlertType.ERROR);
            return;
        }
        if (apellidos.isEmpty()) {
            mostrarAlerta("Error", "Los apellidos son obligatorios", Alert.AlertType.ERROR);
            return;
        }

        loadingIndicator.setVisible(true);

        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (pacienteService.existeCedula(cedula)) return null;

                Paciente p = new Paciente();
                p.setCedula(cedula);
                p.setNombres(nombres);
                p.setApellidos(apellidos);
                p.setFechaNacimiento(dpCrearFechaNacimiento.getValue());
                p.setGenero(genero);
                p.setTelefono(txtCrearTelefono.getText());
                p.setEmail(txtCrearEmail.getText());
                p.setDireccion(txtCrearDireccion.getText());
                p.setGrupoSanguineo(txtCrearGrupoSanguineo.getText());
                p.setAlergias(txtCrearAlergias.getText());

                return pacienteService.crearPaciente(p);
            }
        };

        task.setOnSucceeded(ev -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);

            if (exito == null) {
                mostrarAlerta("Error", "La cédula ya existe", Alert.AlertType.ERROR);
            } else if (exito) {
                mostrarAlerta("Éxito", "Paciente creado exitosamente", Alert.AlertType.INFORMATION);
                limpiarFormularioCrear();
                cargarDatos();
                tabPane.getSelectionModel().select(0);
            } else {
                mostrarAlerta("Error", "No se pudo crear el paciente", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(ev -> {
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo crear el paciente", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML private void handleLimpiarFormCrear() {
        limpiarFormularioCrear();
    }
    private void limpiarFormularioCrear() {
        txtCrearCedula.clear();
        txtCrearNombres.clear();
        txtCrearApellidos.clear();
        dpCrearFechaNacimiento.setValue(null);
        cmbCrearGenero.setValue(null);
        txtCrearTelefono.clear();
        txtCrearEmail.clear();
        txtCrearDireccion.clear();
        txtCrearGrupoSanguineo.clear();
        txtCrearAlergias.clear();
    }

    private void abrirEdicion(final Paciente p) {
        pacienteEnEdicion = p;
        txtEditarId.setText(String.valueOf(p.getId()));
        txtEditarCedula.setText(p.getCedula());
        txtEditarNombres.setText(p.getNombres());
        txtEditarApellidos.setText(p.getApellidos());
        dpEditarFechaNacimiento.setValue(p.getFechaNacimiento());
        cmbEditarGenero.setValue(p.getGenero());
        txtEditarTelefono.setText(p.getTelefono());
        txtEditarEmail.setText(p.getEmail());
        txtEditarDireccion.setText(p.getDireccion());
        txtEditarGrupoSanguineo.setText(p.getGrupoSanguineo());
        txtEditarAlergias.setText(p.getAlergias());
        tabEditar.setDisable(false);
        tabPane.getSelectionModel().select(tabEditar);
    }

    private void abrirDocumentos(final Paciente p) {
        pacienteEnEdicion = p;
        if (tabDocumentos != null) {
            tabDocumentos.setDisable(false);
        }
        configurarTablaDocumentos();
        cargarDocumentos();
        tabPane.getSelectionModel().select(tabDocumentos);
    }

    private void configurarTablaDocumentos() {
        if (tableDocumentos == null) {
            return;
        }
        colDocNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombreArchivo()));
        colDocTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipoDocumento()));
        colDocFecha.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFechaSubida() == null ? "" : data.getValue().getFechaSubida().format(DATE_FORMATTER))
        );
        colDocRuta.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRutaArchivo()));

        colDocNombre.setCellFactory(col -> createEllipsisCell());
        colDocTipo.setCellFactory(col -> createEllipsisCell());
        colDocRuta.setCellFactory(col -> createEllipsisCell());

        colDocAcciones.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button("Ver");
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox pane = new HBox(10, btnVer, btnEliminar);
            {
                pane.setAlignment(Pos.CENTER);
                btnVer.setStyle("-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 3; -fx-padding: 5 10;");
                btnVer.setOnAction(e -> {
                    DocumentoPaciente d = getTableView().getItems().get(getIndex());
                    abrirArchivoDocumento(d);
                });
                btnEliminar.setOnAction(e -> {
                    DocumentoPaciente d = getTableView().getItems().get(getIndex());
                    eliminarDocumento(d);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : pane);
            }
        });

        if (cmbTipoDocumento != null && cmbTipoDocumento.getItems().isEmpty()) {
            cmbTipoDocumento.getItems().addAll(
                    "HISTORIA_CLINICA",
                    "EXAMEN",
                    "RECETA",
                    "OTRO"
            );
        }
    }

    private TableCell<DocumentoPaciente, String> createEllipsisCell() {
        return new TableCell<>() {
            private final Label label = new Label();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    label.setText(item);
                    label.setMaxWidth(Double.MAX_VALUE);
                    label.setEllipsisString("…");
                    label.setTextOverrun(OverrunStyle.ELLIPSIS);
                    Tooltip.install(label, new Tooltip(item));
                    setGraphic(label);
                }
            }
        };
    }

    private void cargarDocumentos() {
        if (pacienteEnEdicion == null || tableDocumentos == null) {
            return;
        }
        tableDocumentos.getItems().clear();
        tableDocumentos.getItems().addAll(documentoService.listarPorPaciente(pacienteEnEdicion.getId()));
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleSubirDocumento() {
        if (pacienteEnEdicion == null) {
            mostrarAlerta("Error", "Debe seleccionar un paciente para subir documentos", Alert.AlertType.ERROR);
            return;
        }
        String tipo = cmbTipoDocumento.getValue();
        if (tipo == null || tipo.isEmpty()) {
            mostrarAlerta("Error", "Seleccione un tipo de documento", Alert.AlertType.ERROR);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar archivo");
        File archivo = fileChooser.showOpenDialog(tabPane.getScene().getWindow());
        if (archivo == null) return;

        try {
            Path baseDir = Paths.get(System.getProperty("user.home"), "ceragen_docs", "pacientes", String.valueOf(pacienteEnEdicion.getId()));
            Files.createDirectories(baseDir);
            Path destino = baseDir.resolve(archivo.getName());
            Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

            DocumentoPaciente doc = new DocumentoPaciente();
            doc.setPacienteId(pacienteEnEdicion.getId());
            doc.setNombreArchivo(archivo.getName());
            doc.setTipoDocumento(tipo);
            doc.setRutaArchivo(destino.toString());

            boolean ok = documentoService.crearDocumento(doc);
            if (ok) {
                mostrarAlerta("Éxito", "Documento subido", Alert.AlertType.INFORMATION);
                cargarDocumentos();
            } else {
                mostrarAlerta("Error", "No se pudo registrar el documento", Alert.AlertType.ERROR);
            }
        } catch (Exception ex) {
            mostrarAlerta("Error", "Fallo al copiar el archivo: " + ex.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void eliminarDocumento(final DocumentoPaciente d) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Eliminar documento?");
        confirmacion.setContentText(d.getNombreArchivo());
        Optional<ButtonType> res = confirmacion.showAndWait();
        if (res.isPresent() && res.get() == ButtonType.OK) {
            boolean ok = documentoService.eliminarDocumento(d.getId());
            if (ok) {
                cargarDocumentos();
            } else {
                mostrarAlerta(
                        "Error",
                        "No se pudo eliminar el documento",
                        Alert.AlertType.ERROR
                );
            }
        }
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleActualizarPaciente() {
        if (pacienteEnEdicion == null) return;
        final String cedula = txtEditarCedula.getText().trim();
        final String nombres = txtEditarNombres.getText().trim();
        final String apellidos = txtEditarApellidos.getText().trim();
        if (cedula.isEmpty() || nombres.isEmpty() || apellidos.isEmpty()) {
            mostrarAlerta(
                    "Error",
                    "Cédula, nombres y apellidos son obligatorios",
                    Alert.AlertType.ERROR
            );
            return;
        }
        loadingIndicator.setVisible(true);
        Task<Boolean> task = new Task<>() {
            @Override
            protected Boolean call() {
                if (pacienteService.existeCedulaExceptoId(cedula, pacienteEnEdicion.getId())) return null;
                Paciente p = new Paciente();
                p.setId(pacienteEnEdicion.getId());
                p.setCedula(cedula);
                p.setNombres(nombres);
                p.setApellidos(apellidos);
                p.setFechaNacimiento(dpEditarFechaNacimiento.getValue());
                p.setGenero(cmbEditarGenero.getValue());
                p.setTelefono(txtEditarTelefono.getText());
                p.setEmail(txtEditarEmail.getText());
                p.setDireccion(txtEditarDireccion.getText());
                p.setGrupoSanguineo(txtEditarGrupoSanguineo.getText());
                p.setAlergias(txtEditarAlergias.getText());
                return pacienteService.actualizarPaciente(p);
            }
        };

        task.setOnSucceeded(ev -> {
            Boolean exito = task.getValue();
            loadingIndicator.setVisible(false);
            if (exito == null) {
                mostrarAlerta("Error", "La cédula ya existe", Alert.AlertType.ERROR);
            } else if (exito) {
                mostrarAlerta("Éxito", "Paciente actualizado exitosamente", Alert.AlertType.INFORMATION);
                cargarDatos();
                handleCancelarEdicion();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar el paciente", Alert.AlertType.ERROR);
            }
        });

        task.setOnFailed(ev -> {
            loadingIndicator.setVisible(false);
            mostrarAlerta("Error", "No se pudo actualizar el paciente", Alert.AlertType.ERROR);
        });

        new Thread(task).start();
    }

    @SuppressWarnings("PMD.UnusedPrivateMethod")
    @FXML
    private void handleCancelarEdicion() {
        pacienteEnEdicion = null;
        tabEditar.setDisable(true);
        tabPane.getSelectionModel().select(0);
    }

    private void eliminarPaciente(final Paciente p) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar este paciente?");
        confirmacion.setContentText("Paciente: " + p.getNombreCompleto() + " - " + p.getCedula());
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            loadingIndicator.setVisible(true);
            Task<Boolean> task = new Task<>() {
                @Override
                protected Boolean call() {
                    return pacienteService.eliminarPaciente(p.getId());
                }
            };
            task.setOnSucceeded(ev -> {
                Boolean exito = task.getValue();
                loadingIndicator.setVisible(false);
                if (exito) {
                    mostrarAlerta("Éxito", "Paciente eliminado exitosamente", Alert.AlertType.INFORMATION);
                    cargarDatos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el paciente", Alert.AlertType.ERROR);
                }
            });
            task.setOnFailed(ev -> {
                loadingIndicator.setVisible(false);
                mostrarAlerta("Error", "No se pudo eliminar el paciente", Alert.AlertType.ERROR);
            });
            new Thread(task).start();
        }
    }

    private void mostrarAlerta(final String titulo,
                               final String mensaje,
                               final Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    private void configurarValidaciones() {

        UnaryOperator<TextFormatter.Change> digitsFilter = change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        };
        Pattern lettersPattern = Pattern.compile("[a-zA-ZáéíóúÁÉÍÓÚñÑ ]*");
        UnaryOperator<TextFormatter.Change> lettersFilter = change -> {
            String newText = change.getControlNewText();
            return lettersPattern.matcher(newText).matches() ? change : null;
        };

        if (txtCrearCedula != null) txtCrearCedula.setTextFormatter(new TextFormatter<>(digitsFilter));
        if (txtCrearTelefono != null) txtCrearTelefono.setTextFormatter(new TextFormatter<>(digitsFilter));
        if (txtEditarCedula != null) txtEditarCedula.setTextFormatter(new TextFormatter<>(digitsFilter));
        if (txtEditarTelefono != null) txtEditarTelefono.setTextFormatter(new TextFormatter<>(digitsFilter));

        if (txtCrearNombres != null) txtCrearNombres.setTextFormatter(new TextFormatter<>(lettersFilter));
        if (txtCrearApellidos != null) txtCrearApellidos.setTextFormatter(new TextFormatter<>(lettersFilter));
        if (txtEditarNombres != null) txtEditarNombres.setTextFormatter(new TextFormatter<>(lettersFilter));
        if (txtEditarApellidos != null) txtEditarApellidos.setTextFormatter(new TextFormatter<>(lettersFilter));
    }
}
