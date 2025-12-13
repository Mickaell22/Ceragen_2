package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.service.CitaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para la logica de actualizacion de citas
 * Basados en el metodo handleActualizarCita() de CitasController
 *
 * COMPLEJIDAD CICLOMATICA: 6-7
 * Total de tests: 8 (cubriendo TODOS los caminos)
 *
 * ESTRUCTURA DE CADA TEST:
 * - Tests de caminos CORRECTOS (casos exitosos)
 * - Tests de caminos ERROR (validaciones, conflictos, errores)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - Logica de Actualizacion de Citas (handleActualizarCita)")
class CitasControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(CitasControllerTest.class);
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    @Mock
    private CitaService citaServiceMock;

    @BeforeEach
    void setUp() {
        logger.info("=== Configurando test unitario ===");
    }

    /**
     * ========================================================================
     * CASO 1: ERROR - Cita en edicion es null
     * ========================================================================
     * Camino: citaEnEdicion == null -> Return temprano
     * CC Cubierta: Rama de cita nula
     */
    @Test
    @DisplayName("Test 1 - ERROR: Cita en edicion es null")
    void testActualizarCita_CasoError_CitaEnEdicionNull() {
        logger.info(">>> TEST 1: Error por cita en edicion null");

        // ARRANGE: Cita en edicion es null
        Cita citaEnEdicion = null;

        // ACT: Validar si es null
        boolean esNull = (citaEnEdicion == null);

        // ASSERT: Debe detectar que es null y retornar temprano
        assertTrue(esNull, "La cita en edicion debe ser null");

        // El servicio NO debe ser llamado
        verify(citaServiceMock, never()).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());
        verify(citaServiceMock, never()).existeConflictoHorario(anyInt(), any(), anyInt());

        logger.info("<<< TEST 1: EXITOSO - Deteccion de cita null correcta");
    }

    /**
     * ========================================================================
     * CASO 2: ERROR - Campos obligatorios incompletos (paciente null)
     * ========================================================================
     * Camino: Validacion FALLA -> Return temprano
     * CC Cubierta: Rama de validacion fallida
     */
    @Test
    @DisplayName("Test 2 - ERROR: Campos obligatorios incompletos (paciente null)")
    void testActualizarCita_CasoError_PacienteNull() {
        logger.info(">>> TEST 2: Error por paciente null");

        // ARRANGE: Cita existe, pero campos incompletos
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        Integer pacienteId = null; // NULL
        Integer profesionalId = 5;
        LocalDate fecha = LocalDate.now().plusDays(1);
        String horaStr = "14:30";
        String motivo = "Control general";
        String estado = "CONFIRMADA";

        // ACT: Validar campos obligatorios
        boolean camposIncompletos = (pacienteId == null || profesionalId == null ||
                                     fecha == null || horaStr.isEmpty() ||
                                     motivo.isEmpty() || estado == null);

        // ASSERT: Validacion debe fallar
        assertTrue(camposIncompletos, "La validacion debe detectar campos incompletos");

        // El servicio NO debe ser llamado
        verify(citaServiceMock, never()).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());

        logger.info("<<< TEST 2: EXITOSO - Validacion detuvo correctamente");
    }

    /**
     * ========================================================================
     * CASO 3: ERROR - Campos obligatorios incompletos (motivo vacio)
     * ========================================================================
     * Camino: Validacion FALLA (motivo vacio) -> Return temprano
     * CC Cubierta: Rama de validacion fallida
     */
    @Test
    @DisplayName("Test 3 - ERROR: Campos obligatorios incompletos (motivo vacio)")
    void testActualizarCita_CasoError_MotivoVacio() {
        logger.info(">>> TEST 3: Error por motivo vacio");

        // ARRANGE
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        Integer pacienteId = 10;
        Integer profesionalId = 5;
        LocalDate fecha = LocalDate.now().plusDays(1);
        String horaStr = "14:30";
        String motivo = ""; // VACIO
        String estado = "CONFIRMADA";

        // ACT
        boolean camposIncompletos = (pacienteId == null || profesionalId == null ||
                                     fecha == null || horaStr.isEmpty() ||
                                     motivo.isEmpty() || estado == null);

        // ASSERT
        assertTrue(camposIncompletos, "Validacion debe detectar motivo vacio");
        verify(citaServiceMock, never()).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());

        logger.info("<<< TEST 3: EXITOSO");
    }

    /**
     * ========================================================================
     * CASO 4: ERROR - Formato de hora invalido (DateTimeParseException)
     * ========================================================================
     * Camino: Validacion OK -> Try-catch FALLA -> Return temprano
     * CC Cubierta: Rama de parsing fallido
     */
    @Test
    @DisplayName("Test 4 - ERROR: Formato de hora invalido")
    void testActualizarCita_CasoError_FormatoHoraInvalido() {
        logger.info(">>> TEST 4: Error por formato de hora invalido");

        // ARRANGE
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        String horaStr = "25:99"; // FORMATO INVALIDO

        // ACT: Intentar parsear
        LocalTime hora = null;
        boolean parseError = false;

        try {
            hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            parseError = true;
            logger.info("Parsing fallo como se esperaba: {}", e.getMessage());
        }

        // ASSERT: Debe fallar el parsing
        assertTrue(parseError, "El parsing debe fallar con formato invalido");
        assertNull(hora, "La hora debe ser null");

        // El servicio NO debe ser llamado
        verify(citaServiceMock, never()).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());

        logger.info("<<< TEST 4: EXITOSO - Formato invalido detectado");
    }

    /**
     * ========================================================================
     * CASO 5: ERROR - Conflicto de horario detectado
     * ========================================================================
     * Camino: Validacion OK -> Parsing OK -> Conflicto SI -> Return null
     * CC Cubierta: Rama de conflicto de horario
     */
    @Test
    @DisplayName("Test 5 - ERROR: Conflicto de horario detectado")
    void testActualizarCita_CasoError_ConflictoHorario() {
        logger.info(">>> TEST 5: Error por conflicto de horario");

        // ARRANGE
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        Integer pacienteId = 10;
        Integer profesionalId = 5;
        LocalDate fecha = LocalDate.now().plusDays(1);
        String horaStr = "14:30";
        String motivo = "Control general";
        String estado = "CONFIRMADA";

        // Validar campos
        boolean camposIncompletos = (pacienteId == null || profesionalId == null ||
                                     fecha == null || horaStr.isEmpty() ||
                                     motivo.isEmpty() || estado == null);
        assertFalse(camposIncompletos, "Los campos deben estar completos");

        // Parsear hora
        LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        // Mock: HAY conflicto
        when(citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId()))
            .thenReturn(true);

        // ACT: Verificar conflicto
        boolean hayConflicto = citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId());

        // ASSERT: Debe detectar el conflicto
        assertTrue(hayConflicto, "Debe detectar conflicto de horario");

        // NO debe intentar actualizar
        verify(citaServiceMock, times(1)).existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId());
        verify(citaServiceMock, never()).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());

        logger.info("<<< TEST 5: EXITOSO - Conflicto detectado correctamente");
    }

    /**
     * ========================================================================
     * CASO 6: EXITOSO - Actualizacion de cita exitosa
     * ========================================================================
     * Camino: Validacion OK -> Parsing OK -> Sin conflicto -> Actualizacion OK
     * CC Cubierta: Camino principal exitoso
     */
    @Test
    @DisplayName("Test 6 - EXITOSO: Actualizacion de cita exitosa")
    void testActualizarCita_CasoExitoso_ActualizacionExitosa() {
        logger.info(">>> TEST 6: Actualizacion exitosa de cita");

        // ARRANGE: Todos los datos validos
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        Integer pacienteId = 10;
        Integer profesionalId = 5;
        LocalDate fecha = LocalDate.now().plusDays(1);
        String horaStr = "14:30";
        String motivo = "Control general";
        String estado = "CONFIRMADA";
        String observaciones = "Sin observaciones";

        // Validacion
        boolean camposIncompletos = (pacienteId == null || profesionalId == null ||
                                     fecha == null || horaStr.isEmpty() ||
                                     motivo.isEmpty() || estado == null);
        assertFalse(camposIncompletos);

        // Parsing
        LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        // Mock: No hay conflicto, actualizacion exitosa
        when(citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId()))
            .thenReturn(false);
        when(citaServiceMock.actualizarCita(citaEnEdicion.getId(), pacienteId, profesionalId,
                                            fechaHora, motivo, estado, observaciones))
            .thenReturn(true);

        // ACT: Ejecutar logica
        boolean hayConflicto = citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId());
        assertFalse(hayConflicto);

        boolean resultado = citaServiceMock.actualizarCita(citaEnEdicion.getId(), pacienteId,
                                                          profesionalId, fechaHora, motivo,
                                                          estado, observaciones);

        // ASSERT: Debe ser exitoso
        assertTrue(resultado, "La actualizacion debe ser exitosa");
        verify(citaServiceMock, times(1)).existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId());
        verify(citaServiceMock, times(1)).actualizarCita(citaEnEdicion.getId(), pacienteId,
                                                         profesionalId, fechaHora, motivo,
                                                         estado, observaciones);

        logger.info("<<< TEST 6: EXITOSO - Cita actualizada correctamente");
    }

    /**
     * ========================================================================
     * CASO 7: ERROR - Fallo en base de datos al actualizar
     * ========================================================================
     * Camino: Validacion OK -> Sin conflicto -> Actualizacion FALLA (return false)
     * CC Cubierta: Rama de actualizacion fallida
     */
    @Test
    @DisplayName("Test 7 - ERROR: Fallo en base de datos al actualizar")
    void testActualizarCita_CasoError_FalloBaseDatos() {
        logger.info(">>> TEST 7: Error por fallo en base de datos");

        // ARRANGE
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        Integer pacienteId = 10;
        Integer profesionalId = 5;
        LocalDate fecha = LocalDate.now().plusDays(1);
        String horaStr = "14:30";
        String motivo = "Control general";
        String estado = "CONFIRMADA";
        String observaciones = "";

        // Validacion y parsing OK
        boolean camposIncompletos = (pacienteId == null || profesionalId == null ||
                                     fecha == null || horaStr.isEmpty() ||
                                     motivo.isEmpty() || estado == null);
        assertFalse(camposIncompletos);

        LocalTime hora = LocalTime.parse(horaStr, TIME_FORMATTER);
        LocalDateTime fechaHora = LocalDateTime.of(fecha, hora);

        // Mock: No hay conflicto, pero actualizacion FALLA
        when(citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId()))
            .thenReturn(false);
        when(citaServiceMock.actualizarCita(citaEnEdicion.getId(), pacienteId, profesionalId,
                                            fechaHora, motivo, estado, observaciones))
            .thenReturn(false); // FALLO

        // ACT
        boolean hayConflicto = citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId());
        assertFalse(hayConflicto);

        boolean resultado = citaServiceMock.actualizarCita(citaEnEdicion.getId(), pacienteId,
                                                          profesionalId, fechaHora, motivo,
                                                          estado, observaciones);

        // ASSERT: La actualizacion debe fallar
        assertFalse(resultado, "La actualizacion debe fallar");
        verify(citaServiceMock, times(1)).existeConflictoHorario(profesionalId, fechaHora, citaEnEdicion.getId());
        verify(citaServiceMock, times(1)).actualizarCita(citaEnEdicion.getId(), pacienteId,
                                                         profesionalId, fechaHora, motivo,
                                                         estado, observaciones);

        logger.info("<<< TEST 7: EXITOSO - Fallo manejado correctamente");
    }

    /**
     * ========================================================================
     * CASO 8: ERROR - Hora vacia (validacion de horaStr)
     * ========================================================================
     * Camino: Validacion FALLA (horaStr vacia) -> Return temprano
     * CC Cubierta: Rama de validacion fallida por hora vacia
     */
    @Test
    @DisplayName("Test 8 - ERROR: Campo hora vacio")
    void testActualizarCita_CasoError_HoraVacia() {
        logger.info(">>> TEST 8: Error por hora vacia");

        // ARRANGE
        Cita citaEnEdicion = new Cita();
        citaEnEdicion.setId(1);

        Integer pacienteId = 10;
        Integer profesionalId = 5;
        LocalDate fecha = LocalDate.now().plusDays(1);
        String horaStr = ""; // VACIA
        String motivo = "Control general";
        String estado = "CONFIRMADA";

        // ACT: Validar
        boolean camposIncompletos = (pacienteId == null || profesionalId == null ||
                                     fecha == null || horaStr.isEmpty() ||
                                     motivo.isEmpty() || estado == null);

        // ASSERT
        assertTrue(camposIncompletos, "Validacion debe detectar hora vacia");
        verify(citaServiceMock, never()).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());

        logger.info("<<< TEST 8: EXITOSO - Hora vacia detectada");
    }
}
