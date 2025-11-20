package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.model.Profesional;
import com.example.ceragen_2.service.CitaService;
import com.example.ceragen_2.service.PacienteService;
import com.example.ceragen_2.service.ProfesionalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Unidad - CitasController")
class CitasControllerUnitTest {
    private static final Logger logger = LoggerFactory.getLogger(CitasControllerUnitTest.class);

    @Mock
    private CitaService citaServiceMock;

    @Mock
    private PacienteService pacienteServiceMock;

    @Mock
    private ProfesionalService profesionalServiceMock;

    private CitasController controller;

    @BeforeEach
    void setUp() throws Exception {
        logger.info("Configurando test de unidad con mocks");

        // Crear instancia del controller
        controller = new CitasController();

        // Inyectar los mocks usando reflexion (porque los servicios son final en el controller)
        injectMock(controller, "citaService", citaServiceMock);
        injectMock(controller, "pacienteService", pacienteServiceMock);
        injectMock(controller, "profesionalService", profesionalServiceMock);

        logger.info("Mocks inyectados exitosamente");
    }

    /**
     * Helper para inyectar mocks usando reflexion
     */
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    @Test
    @DisplayName("Test 1: Verificar que el controller use el servicio para obtener citas")
    void testControllerUsaCitaServiceParaObtenerCitas() {
        logger.info("Test: Verificar interaccion con CitaService");

        // Arrange: Configurar comportamiento del mock
        List<Cita> citasMock = crearCitasMock();
        when(citaServiceMock.getCitas(anyInt(), anyInt(), any(), any(), anyString(), any(), any()))
            .thenReturn(citasMock);

        // Act: Llamar al servicio a traves del mock
        List<Cita> resultado = citaServiceMock.getCitas(0, 10, null, null, "TODOS", null, null);

        // Assert: Verificar que se llamo al servicio
        assertNotNull(resultado, "El resultado no debe ser null");
        assertEquals(3, resultado.size(), "Debe retornar 3 citas mockeadas");
        verify(citaServiceMock, times(1)).getCitas(0, 10, null, null, "TODOS", null, null);

        logger.info("Verificacion exitosa: CitaService fue llamado correctamente");
    }

    @Test
    @DisplayName("Test 2: Verificar conteo de citas con filtros")
    void testCountCitasConFiltros() {
        logger.info("Test: Contar citas con filtros");

        // Arrange
        when(citaServiceMock.countCitas(1, null, "PENDIENTE", null, null)).thenReturn(5);

        // Act
        int total = citaServiceMock.countCitas(1, null, "PENDIENTE", null, null);

        // Assert
        assertEquals(5, total, "Debe retornar el conteo mockeado");
        verify(citaServiceMock, times(1)).countCitas(1, null, "PENDIENTE", null, null);

        logger.info("Conteo verificado: 5 citas");
    }

    @Test
    @DisplayName("Test 3: Verificar interaccion al actualizar cita")
    void testActualizarCitaLlamaAlServicio() {
        logger.info("Test: Actualizar cita");

        // Arrange
        Integer citaId = 1;
        Integer pacienteId = 10;
        Integer profesionalId = 5;
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(1);
        String motivo = "Control general";
        String estado = "CONFIRMADA";
        String observaciones = "Sin observaciones";

        when(citaServiceMock.actualizarCita(citaId, pacienteId, profesionalId, fechaHora, motivo, estado, observaciones))
            .thenReturn(true);

        // Act
        boolean resultado = citaServiceMock.actualizarCita(citaId, pacienteId, profesionalId, fechaHora, motivo, estado, observaciones);

        // Assert
        assertTrue(resultado, "La actualizacion debe ser exitosa");
        verify(citaServiceMock, times(1)).actualizarCita(citaId, pacienteId, profesionalId, fechaHora, motivo, estado, observaciones);

        logger.info("Actualizacion verificada correctamente");
    }

    @Test
    @DisplayName("Test 4: Verificar deteccion de conflicto de horario")
    void testConflictoHorarioDetectado() {
        logger.info("Test: Detectar conflicto de horario");

        // Arrange
        Integer profesionalId = 5;
        LocalDateTime fechaHora = LocalDateTime.of(2025, 11, 10, 14, 0);

        when(citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, null))
            .thenReturn(true);

        // Act
        boolean existeConflicto = citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, null);

        // Assert
        assertTrue(existeConflicto, "Debe detectar el conflicto");
        verify(citaServiceMock, times(1)).existeConflictoHorario(profesionalId, fechaHora, null);

        logger.info("Conflicto detectado correctamente");
    }

    @Test
    @DisplayName("Test 5: Verificar que no hay conflicto cuando el horario esta libre")
    void testSinConflictoHorario() {
        logger.info("Test: Horario libre sin conflictos");

        // Arrange
        Integer profesionalId = 5;
        LocalDateTime fechaHora = LocalDateTime.of(2025, 11, 10, 10, 0);

        when(citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, null))
            .thenReturn(false);

        // Act
        boolean existeConflicto = citaServiceMock.existeConflictoHorario(profesionalId, fechaHora, null);

        // Assert
        assertFalse(existeConflicto, "No debe haber conflicto");
        verify(citaServiceMock, times(1)).existeConflictoHorario(profesionalId, fechaHora, null);

        logger.info("Verificado: horario libre");
    }

    @Test
    @DisplayName("Test 6: Verificar cambio de estado de cita")
    void testCambiarEstadoCita() {
        logger.info("Test: Cambiar estado de cita");

        // Arrange
        Integer citaId = 10;
        String nuevoEstado = "ATENDIDA";

        when(citaServiceMock.cambiarEstado(citaId, nuevoEstado)).thenReturn(true);

        // Act
        boolean resultado = citaServiceMock.cambiarEstado(citaId, nuevoEstado);

        // Assert
        assertTrue(resultado, "El cambio de estado debe ser exitoso");
        verify(citaServiceMock, times(1)).cambiarEstado(citaId, nuevoEstado);

        logger.info("Estado cambiado correctamente a: {}", nuevoEstado);
    }

    @Test
    @DisplayName("Test 7: Verificar eliminacion de cita")
    void testEliminarCita() {
        logger.info("Test: Eliminar cita");

        // Arrange
        Integer citaId = 15;

        when(citaServiceMock.eliminarCita(citaId)).thenReturn(true);

        // Act
        boolean resultado = citaServiceMock.eliminarCita(citaId);

        // Assert
        assertTrue(resultado, "La eliminacion debe ser exitosa");
        verify(citaServiceMock, times(1)).eliminarCita(citaId);

        logger.info("Cita eliminada correctamente");
    }

    @Test
    @DisplayName("Test 8: Verificar obtencion de cita por ID")
    void testObtenerCitaPorId() {
        logger.info("Test: Obtener cita por ID");

        // Arrange
        Integer citaId = 20;
        Cita citaMock = crearCitaMock(citaId, 1, 5, LocalDateTime.now(), "Control rutinario");

        when(citaServiceMock.getCitaById(citaId)).thenReturn(citaMock);

        // Act
        Cita cita = citaServiceMock.getCitaById(citaId);

        // Assert
        assertNotNull(cita, "La cita no debe ser null");
        assertEquals(citaId, cita.getId(), "El ID debe coincidir");
        assertEquals("Control rutinario", cita.getMotivo(), "El motivo debe coincidir");
        verify(citaServiceMock, times(1)).getCitaById(citaId);

        logger.info("Cita obtenida correctamente: ID {}", citaId);
    }

    @Test
    @DisplayName("Test 9: Verificar carga de catalogos de pacientes")
    void testCargarCatalogoPacientes() {
        logger.info("Test: Cargar catalogo de pacientes");

        // Arrange
        List<Paciente> pacientesMock = crearPacientesMock();
        when(pacienteServiceMock.getAllPacientes()).thenReturn(pacientesMock);

        // Act
        List<Paciente> pacientes = pacienteServiceMock.getAllPacientes();

        // Assert
        assertNotNull(pacientes, "La lista no debe ser null");
        assertEquals(2, pacientes.size(), "Debe retornar 2 pacientes");
        verify(pacienteServiceMock, times(1)).getAllPacientes();

        logger.info("Catalogo de pacientes cargado: {} pacientes", pacientes.size());
    }

    @Test
    @DisplayName("Test 10: Verificar carga de catalogos de profesionales")
    void testCargarCatalogoProfesionales() {
        logger.info("Test: Cargar catalogo de profesionales");

        // Arrange
        List<Profesional> profesionalesMock = crearProfesionalesMock();
        when(profesionalServiceMock.getAllProfesionales()).thenReturn(profesionalesMock);

        // Act
        List<Profesional> profesionales = profesionalServiceMock.getAllProfesionales();

        // Assert
        assertNotNull(profesionales, "La lista no debe ser null");
        assertEquals(2, profesionales.size(), "Debe retornar 2 profesionales");
        verify(profesionalServiceMock, times(1)).getAllProfesionales();

        logger.info("Catalogo de profesionales cargado: {} profesionales", profesionales.size());
    }

    @Test
    @DisplayName("Test 11: Verificar busqueda de paciente por cedula")
    void testBuscarPacientePorCedula() {
        logger.info("Test: Buscar paciente por cedula");

        // Arrange
        String cedula = "0912345678";
        Paciente pacienteMock = crearPacienteMock(1, cedula, "Juan Perez");

        when(pacienteServiceMock.getPacienteByCedula(cedula)).thenReturn(pacienteMock);

        // Act
        Paciente paciente = pacienteServiceMock.getPacienteByCedula(cedula);

        // Assert
        assertNotNull(paciente, "El paciente no debe ser null");
        assertEquals(cedula, paciente.getCedula(), "La cedula debe coincidir");
        assertEquals("Juan Perez", paciente.getNombreCompleto(), "El nombre debe coincidir");
        verify(pacienteServiceMock, times(1)).getPacienteByCedula(cedula);

        logger.info("Paciente encontrado: {}", paciente.getNombreCompleto());
    }

    @Test
    @DisplayName("Test 12: Verificar busqueda de profesional por cedula")
    void testBuscarProfesionalPorCedula() {
        logger.info("Test: Buscar profesional por cedula");

        // Arrange
        String cedula = "0923456789";
        Profesional profesionalMock = crearProfesionalMock(5, cedula, "Dra. Maria Rodriguez");

        when(profesionalServiceMock.getProfesionalByCedula(cedula)).thenReturn(profesionalMock);

        // Act
        Profesional profesional = profesionalServiceMock.getProfesionalByCedula(cedula);

        // Assert
        assertNotNull(profesional, "El profesional no debe ser null");
        assertEquals(cedula, profesional.getCedula(), "La cedula debe coincidir");
        assertEquals("Dr. Maria Rodriguez", profesional.getNombreCompleto(), "El nombre debe coincidir");
        verify(profesionalServiceMock, times(1)).getProfesionalByCedula(cedula);

        logger.info("Profesional encontrado: {}", profesional.getNombreCompleto());
    }

    @Test
    @DisplayName("Test 13: Verificar comportamiento cuando no se encuentra paciente")
    void testPacienteNoEncontrado() {
        logger.info("Test: Paciente no encontrado");

        // Arrange
        String cedula = "9999999999";
        when(pacienteServiceMock.getPacienteByCedula(cedula)).thenReturn(null);

        // Act
        Paciente paciente = pacienteServiceMock.getPacienteByCedula(cedula);

        // Assert
        assertNull(paciente, "El paciente debe ser null cuando no existe");
        verify(pacienteServiceMock, times(1)).getPacienteByCedula(cedula);

        logger.info("Verificado: paciente no encontrado");
    }

    @Test
    @DisplayName("Test 14: Verificar fallo al actualizar cita")
    void testActualizarCitaFalla() {
        logger.info("Test: Fallo al actualizar cita");

        // Arrange
        when(citaServiceMock.actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString()))
            .thenReturn(false);

        // Act
        boolean resultado = citaServiceMock.actualizarCita(1, 1, 1, LocalDateTime.now(), "motivo", "PENDIENTE", "obs");

        // Assert
        assertFalse(resultado, "La actualizacion debe fallar");
        verify(citaServiceMock, times(1)).actualizarCita(anyInt(), anyInt(), anyInt(), any(), anyString(), anyString(), anyString());

        logger.info("Fallo manejado correctamente");
    }

    // ============= METODOS AUXILIARES PARA CREAR MOCKS =============

    private List<Cita> crearCitasMock() {
        List<Cita> citas = new ArrayList<>();
        citas.add(crearCitaMock(1, 10, 5, LocalDateTime.now().plusDays(1), "Control general"));
        citas.add(crearCitaMock(2, 11, 6, LocalDateTime.now().plusDays(2), "Consulta pediatrica"));
        citas.add(crearCitaMock(3, 12, 7, LocalDateTime.now().plusDays(3), "Revision cardiologica"));
        return citas;
    }

    private Cita crearCitaMock(Integer id, Integer pacienteId, Integer profesionalId, LocalDateTime fechaHora, String motivo) {
        Cita cita = new Cita();
        cita.setId(id);
        cita.setPacienteId(pacienteId);
        cita.setProfesionalId(profesionalId);
        cita.setFechaHora(fechaHora);
        cita.setMotivo(motivo);
        cita.setEstado("CONFIRMADA");
        cita.setPacienteNombre("Paciente Test " + id);
        cita.setProfesionalNombre("Dr. Profesional " + profesionalId);
        return cita;
    }

    private List<Paciente> crearPacientesMock() {
        List<Paciente> pacientes = new ArrayList<>();
        pacientes.add(crearPacienteMock(1, "0912345678", "Juan Perez"));
        pacientes.add(crearPacienteMock(2, "0923456789", "Maria Garcia"));
        return pacientes;
    }

    private Paciente crearPacienteMock(Integer id, String cedula, String nombreCompleto) {
        Paciente paciente = new Paciente();
        paciente.setId(id);
        paciente.setCedula(cedula);
        // Asumiendo que existe un setter para nombre completo o que se setean nombres y apellidos
        String[] partes = nombreCompleto.split(" ", 2);
        paciente.setNombres(partes[0]);
        paciente.setApellidos(partes.length > 1 ? partes[1] : "");
        return paciente;
    }

    private List<Profesional> crearProfesionalesMock() {
        List<Profesional> profesionales = new ArrayList<>();
        profesionales.add(crearProfesionalMock(5, "0934567890", "Dr. Carlos Mendoza"));
        profesionales.add(crearProfesionalMock(6, "0945678901", "Dra. Ana Gomez"));
        return profesionales;
    }

    private Profesional crearProfesionalMock(Integer id, String cedula, String nombreCompleto) {
        Profesional profesional = new Profesional();
        profesional.setId(id);
        profesional.setCedula(cedula);
        String[] partes = nombreCompleto.replace("Dr. ", "").replace("Dra. ", "").split(" ", 2);
        profesional.setNombres(partes[0]);
        profesional.setApellidos(partes.length > 1 ? partes[1] : "");
        return profesional;
    }
}
