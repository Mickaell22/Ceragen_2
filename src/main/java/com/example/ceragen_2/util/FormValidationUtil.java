package com.example.ceragen_2.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;

import java.util.function.UnaryOperator;
import java.util.regex.Pattern;

/**
 * Clase utilitaria para validaciones de formularios en JavaFX.
 * Proporciona metodos para validar campos, aplicar estilos de error/exito,
 * y configurar filtros de entrada.
 */
public final class FormValidationUtil {

    // Patrones de validacion
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
    );

    private static final Pattern DIGITS_ONLY_PATTERN = Pattern.compile("\\d*");

    private static final Pattern LETTERS_ONLY_PATTERN = Pattern.compile(
            "[a-zA-ZáéíóúÁÉÍÓÚñÑüÜ\\s]*"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern CEDULA_PATTERN = Pattern.compile("^\\d{10}$");

    // Clases CSS para estados
    private static final String CSS_ERROR = "text-field-error";
    private static final String CSS_SUCCESS = "text-field-success";
    private static final String CSS_ERROR_AREA = "text-area-error";
    private static final String CSS_SUCCESS_AREA = "text-area-success";
    private static final String CSS_ERROR_COMBO = "combo-box-error";

    private FormValidationUtil() {
        // Clase utilitaria, no instanciable
    }

    // ========================================
    // VALIDACIONES DE CAMPOS
    // ========================================

    /**
     * Valida que un campo de texto no este vacio.
     *
     * @param campo       Campo a validar
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si el campo tiene contenido
     */
    public static boolean validarCampoRequerido(final TextField campo, final boolean mostrarFeedback) {
        final boolean esValido = campo.getText() != null && !campo.getText().trim().isEmpty();

        if (mostrarFeedback) {
            aplicarEstadoValidacion(campo, esValido);
        }

        return esValido;
    }

    /**
     * Valida que un TextArea no este vacio.
     *
     * @param campo       Campo a validar
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si el campo tiene contenido
     */
    public static boolean validarCampoRequerido(final TextArea campo, final boolean mostrarFeedback) {
        final boolean esValido = campo.getText() != null && !campo.getText().trim().isEmpty();

        if (mostrarFeedback) {
            aplicarEstadoValidacionArea(campo, esValido);
        }

        return esValido;
    }

    /**
     * Valida que un ComboBox tenga un valor seleccionado.
     *
     * @param combo       ComboBox a validar
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si hay un valor seleccionado
     */
    public static boolean validarComboRequerido(final ComboBox<?> combo, final boolean mostrarFeedback) {
        final boolean esValido = combo.getValue() != null;

        if (mostrarFeedback) {
            aplicarEstadoValidacionCombo(combo, esValido);
        }

        return esValido;
    }

    /**
     * Valida formato de email.
     *
     * @param campo       Campo a validar
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si el email es valido o el campo esta vacio
     */
    public static boolean validarEmail(final TextField campo, final boolean mostrarFeedback) {
        final String texto = campo.getText();

        // Email es opcional, si esta vacio es valido
        if (texto == null || texto.trim().isEmpty()) {
            if (mostrarFeedback) {
                limpiarEstadoValidacion(campo);
            }
            return true;
        }

        final boolean esValido = EMAIL_PATTERN.matcher(texto.trim()).matches();

        if (mostrarFeedback) {
            aplicarEstadoValidacion(campo, esValido);
            if (!esValido) {
                mostrarTooltipError(campo, "Formato de email invalido");
            }
        }

        return esValido;
    }

    /**
     * Valida formato de telefono (10 digitos).
     *
     * @param campo       Campo a validar
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si el telefono es valido o el campo esta vacio
     */
    public static boolean validarTelefono(final TextField campo, final boolean mostrarFeedback) {
        final String texto = campo.getText();

        // Telefono es opcional
        if (texto == null || texto.trim().isEmpty()) {
            if (mostrarFeedback) {
                limpiarEstadoValidacion(campo);
            }
            return true;
        }

        final boolean esValido = PHONE_PATTERN.matcher(texto.trim()).matches();

        if (mostrarFeedback) {
            aplicarEstadoValidacion(campo, esValido);
            if (!esValido) {
                mostrarTooltipError(campo, "Telefono debe tener exactamente 10 digitos");
            }
        }

        return esValido;
    }

    /**
     * Valida formato de cedula (10 digitos).
     *
     * @param campo       Campo a validar
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si la cedula es valida
     */
    public static boolean validarCedula(final TextField campo, final boolean mostrarFeedback) {
        final String texto = campo.getText();

        if (texto == null || texto.trim().isEmpty()) {
            if (mostrarFeedback) {
                aplicarEstadoValidacion(campo, false);
                mostrarTooltipError(campo, "Cedula es requerida");
            }
            return false;
        }

        final boolean esValido = CEDULA_PATTERN.matcher(texto.trim()).matches();

        if (mostrarFeedback) {
            aplicarEstadoValidacion(campo, esValido);
            if (!esValido) {
                mostrarTooltipError(campo, "Cedula debe tener exactamente 10 digitos");
            }
        }

        return esValido;
    }

    /**
     * Valida longitud minima de un campo.
     *
     * @param campo       Campo a validar
     * @param minLength   Longitud minima requerida
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si cumple con la longitud minima
     */
    public static boolean validarLongitudMinima(final TextField campo, final int minLength, final boolean mostrarFeedback) {
        final String texto = campo.getText();
        final boolean esValido = texto != null && texto.length() >= minLength;

        if (mostrarFeedback) {
            aplicarEstadoValidacion(campo, esValido);
            if (!esValido) {
                mostrarTooltipError(campo, "Minimo " + minLength + " caracteres");
            }
        }

        return esValido;
    }

    /**
     * Valida que dos campos de contrasena coincidan.
     *
     * @param password        Campo de contrasena
     * @param confirmPassword Campo de confirmacion
     * @param mostrarFeedback Si es true, aplica estilos visuales
     * @return true si las contrasenas coinciden
     */
    public static boolean validarPasswordsCoinciden(
            final TextField password,
            final TextField confirmPassword,
            final boolean mostrarFeedback) {

        final String pass1 = password.getText();
        final String pass2 = confirmPassword.getText();

        final boolean esValido = pass1 != null && pass1.equals(pass2);

        if (mostrarFeedback) {
            aplicarEstadoValidacion(confirmPassword, esValido);
            if (!esValido) {
                mostrarTooltipError(confirmPassword, "Las contrasenas no coinciden");
            }
        }

        return esValido;
    }

    // ========================================
    // FILTROS DE ENTRADA
    // ========================================

    /**
     * Aplica un filtro que solo permite digitos.
     *
     * @param campo Campo a filtrar
     */
    public static void aplicarFiltroSoloDigitos(final TextField campo) {
        final UnaryOperator<TextFormatter.Change> filter = change -> {
            final String newText = change.getControlNewText();
            return DIGITS_ONLY_PATTERN.matcher(newText).matches() ? change : null;
        };
        campo.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Aplica un filtro que solo permite digitos con longitud maxima.
     *
     * @param campo     Campo a filtrar
     * @param maxLength Longitud maxima permitida
     */
    public static void aplicarFiltroSoloDigitosConLongitud(final TextField campo, final int maxLength) {
        final UnaryOperator<TextFormatter.Change> filter = change -> {
            final String newText = change.getControlNewText();
            if (newText.length() > maxLength) {
                return null;
            }
            return DIGITS_ONLY_PATTERN.matcher(newText).matches() ? change : null;
        };
        campo.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Aplica un filtro que solo permite letras y espacios.
     *
     * @param campo Campo a filtrar
     */
    public static void aplicarFiltroSoloLetras(final TextField campo) {
        final UnaryOperator<TextFormatter.Change> filter = change -> {
            final String newText = change.getControlNewText();
            return LETTERS_ONLY_PATTERN.matcher(newText).matches() ? change : null;
        };
        campo.setTextFormatter(new TextFormatter<>(filter));
    }

    /**
     * Aplica un filtro con longitud maxima.
     *
     * @param campo     Campo a filtrar
     * @param maxLength Longitud maxima permitida
     */
    public static void aplicarFiltroLongitudMaxima(final TextField campo, final int maxLength) {
        final UnaryOperator<TextFormatter.Change> filter = change -> {
            final String newText = change.getControlNewText();
            return newText.length() <= maxLength ? change : null;
        };
        campo.setTextFormatter(new TextFormatter<>(filter));
    }

    // ========================================
    // ESTILOS VISUALES
    // ========================================

    /**
     * Aplica estilo de error o exito a un TextField.
     */
    private static void aplicarEstadoValidacion(final TextField campo, final boolean esValido) {
        campo.getStyleClass().removeAll(CSS_ERROR, CSS_SUCCESS);

        if (esValido) {
            campo.getStyleClass().add(CSS_SUCCESS);
            campo.setTooltip(null);
        } else {
            campo.getStyleClass().add(CSS_ERROR);
        }
    }

    /**
     * Aplica estilo de error o exito a un TextArea.
     */
    private static void aplicarEstadoValidacionArea(final TextArea campo, final boolean esValido) {
        campo.getStyleClass().removeAll(CSS_ERROR_AREA, CSS_SUCCESS_AREA);

        if (esValido) {
            campo.getStyleClass().add(CSS_SUCCESS_AREA);
        } else {
            campo.getStyleClass().add(CSS_ERROR_AREA);
        }
    }

    /**
     * Aplica estilo de error o exito a un ComboBox.
     */
    private static void aplicarEstadoValidacionCombo(final ComboBox<?> combo, final boolean esValido) {
        combo.getStyleClass().removeAll(CSS_ERROR_COMBO);

        if (!esValido) {
            combo.getStyleClass().add(CSS_ERROR_COMBO);
        }
    }

    /**
     * Limpia los estilos de validacion de un TextField.
     */
    public static void limpiarEstadoValidacion(final TextField campo) {
        campo.getStyleClass().removeAll(CSS_ERROR, CSS_SUCCESS);
        campo.setTooltip(null);
    }

    /**
     * Limpia los estilos de validacion de un TextArea.
     */
    public static void limpiarEstadoValidacion(final TextArea campo) {
        campo.getStyleClass().removeAll(CSS_ERROR_AREA, CSS_SUCCESS_AREA);
    }

    /**
     * Limpia los estilos de validacion de un ComboBox.
     */
    public static void limpiarEstadoValidacion(final ComboBox<?> combo) {
        combo.getStyleClass().removeAll(CSS_ERROR_COMBO);
    }

    /**
     * Muestra un tooltip de error en un control.
     */
    private static void mostrarTooltipError(final Control control, final String mensaje) {
        final Tooltip tooltip = new Tooltip(mensaje);
        tooltip.getStyleClass().add("tooltip-error");
        control.setTooltip(tooltip);
    }

    /**
     * Marca visualmente un campo como invalido con un mensaje.
     *
     * @param campo   Campo a marcar
     * @param mensaje Mensaje de error
     */
    public static void marcarCampoInvalido(final TextField campo, final String mensaje) {
        campo.getStyleClass().removeAll(CSS_ERROR, CSS_SUCCESS);
        campo.getStyleClass().add(CSS_ERROR);
        mostrarTooltipError(campo, mensaje);
    }

    /**
     * Marca visualmente un ComboBox como invalido.
     *
     * @param combo ComboBox a marcar
     */
    public static void marcarComboInvalido(final ComboBox<?> combo) {
        combo.getStyleClass().removeAll(CSS_ERROR_COMBO);
        combo.getStyleClass().add(CSS_ERROR_COMBO);
    }
}
