package com.example.ceragen_2.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

/**
 * Clase utilitaria para mostrar dialogos y alertas con estilos consistentes.
 * Reemplaza el uso de dialogos no estilizados por alertas JavaFX mejoradas.
 */
public final class DialogUtil {

    private DialogUtil() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Muestra una alerta de informacion (exito).
     *
     * @param titulo  Titulo de la alerta
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarExito(final String titulo, final String mensaje) {
        mostrarAlerta(AlertType.INFORMATION, titulo, mensaje);
    }

    /**
     * Muestra una alerta de error.
     *
     * @param titulo  Titulo de la alerta
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarError(final String titulo, final String mensaje) {
        mostrarAlerta(AlertType.ERROR, titulo, mensaje);
    }

    /**
     * Muestra una alerta de advertencia.
     *
     * @param titulo  Titulo de la alerta
     * @param mensaje Mensaje a mostrar
     */
    public static void mostrarAdvertencia(final String titulo, final String mensaje) {
        mostrarAlerta(AlertType.WARNING, titulo, mensaje);
    }

    /**
     * Muestra un dialogo de confirmacion.
     *
     * @param titulo      Titulo del dialogo
     * @param encabezado  Encabezado del dialogo
     * @param mensaje     Mensaje a mostrar
     * @return true si el usuario confirma, false en caso contrario
     */
    public static boolean mostrarConfirmacion(final String titulo, final String encabezado, final String mensaje) {
        final Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);
        aplicarEstilos(alerta);

        final Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.isPresent() && resultado.get() == ButtonType.OK;
    }

    /**
     * Muestra un dialogo de confirmacion con botones personalizados.
     *
     * @param titulo         Titulo del dialogo
     * @param encabezado     Encabezado del dialogo
     * @param mensaje        Mensaje a mostrar
     * @param textoConfirmar Texto del boton de confirmacion
     * @param textoCancelar  Texto del boton de cancelacion
     * @return true si el usuario confirma, false en caso contrario
     */
    public static boolean mostrarConfirmacionPersonalizada(
            final String titulo,
            final String encabezado,
            final String mensaje,
            final String textoConfirmar,
            final String textoCancelar) {

        final Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(encabezado);
        alerta.setContentText(mensaje);

        final ButtonType btnConfirmar = new ButtonType(textoConfirmar, ButtonBar.ButtonData.OK_DONE);
        final ButtonType btnCancelar = new ButtonType(textoCancelar, ButtonBar.ButtonData.CANCEL_CLOSE);

        alerta.getButtonTypes().setAll(btnConfirmar, btnCancelar);
        aplicarEstilos(alerta);

        final Optional<ButtonType> resultado = alerta.showAndWait();
        return resultado.isPresent() && resultado.get() == btnConfirmar;
    }

    /**
     * Muestra un dialogo para ingresar texto.
     *
     * @param titulo       Titulo del dialogo
     * @param encabezado   Encabezado del dialogo
     * @param etiqueta     Etiqueta del campo de texto
     * @param valorInicial Valor inicial del campo
     * @return Optional con el texto ingresado, vacio si se cancela
     */
    public static Optional<String> mostrarInputTexto(
            final String titulo,
            final String encabezado,
            final String etiqueta,
            final String valorInicial) {

        final TextInputDialog dialogo = new TextInputDialog(valorInicial);
        dialogo.setTitle(titulo);
        dialogo.setHeaderText(encabezado);
        dialogo.setContentText(etiqueta);
        aplicarEstilosTextInput(dialogo);

        return dialogo.showAndWait();
    }

    /**
     * Muestra una alerta generica.
     *
     * @param tipo    Tipo de alerta
     * @param titulo  Titulo de la alerta
     * @param mensaje Mensaje a mostrar
     */
    private static void mostrarAlerta(final AlertType tipo, final String titulo, final String mensaje) {
        final Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        aplicarEstilos(alerta);
        alerta.showAndWait();
    }

    /**
     * Aplica estilos personalizados a una alerta.
     *
     * @param alerta Alerta a estilizar
     */
    private static void aplicarEstilos(final Alert alerta) {
        final DialogPane dialogPane = alerta.getDialogPane();
        dialogPane.getStylesheets().add(
                DialogUtil.class.getResource("/com/example/ceragen_2/css/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
    }

    /**
     * Aplica estilos personalizados a un dialogo de texto.
     *
     * @param dialogo Dialogo a estilizar
     */
    private static void aplicarEstilosTextInput(final TextInputDialog dialogo) {
        final DialogPane dialogPane = dialogo.getDialogPane();
        dialogPane.getStylesheets().add(
                DialogUtil.class.getResource("/com/example/ceragen_2/css/styles.css").toExternalForm()
        );
        dialogPane.getStyleClass().add("dialog-pane");
    }
}
