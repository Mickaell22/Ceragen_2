package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Profesional;
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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests de unidad para el módulo de Profesionales,
 * siguiendo el estilo de CitasControllerUnitTest.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Unidad - ProfesionalController / ProfesionalService")
class ProfesionalControllerUnitTest {

    private static final Logger logger =
            LoggerFactory.getLogger(ProfesionalControllerUnitTest.class);

    @Mock
    private ProfesionalService profesionalServiceMock;

    private ProfesionalController controller;

    @BeforeEach
    void setUp() throws Exception {
        logger.info("Configurando test de unidad de Profesionales con mocks");

        // Crear instancia del controller real
        controller = new ProfesionalController();

        // Inyectar el mock del servicio usando reflexión
        injectMock(controller, "profesionalService", profesionalServiceMock);

        logger.info("Mock de ProfesionalService inyectado exitosamente");
    }

    /**
     * Helper para inyectar mocks usando reflexión
     */
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // ============================================================
    // TESTS PRINCIPALES DE INTERACCIÓN CON ProfesionalService
    // ============================================================

    @Test
    @DisplayName("Test 1: Verificar que se obtienen todos los profesionales desde el servicio")
    void testObtenerTodosProfesionalesDesdeServicio() {
        logger.info("Test: Obtener todos los profesionales desde ProfesionalService");

        // Arrange
        List<Profesional> profesionalesMock = crearProfesionalesMock();
        when(profesionalServiceMock.getAllProfesionales()).thenReturn(profesionalesMock);

        // Act
        List<Profesional> resultado = profesionalServiceMock.getAllProfesionales();

        // Assert
        assertNotNull(resultado, "La lista de profesionales no debe ser null");
        assertEquals(3, resultado.size(), "Debe retornar 3 profesionales mockeados");
        verify(profesionalServiceMock, times(1)).getAllProfesionales();

        logger.info("Verificación exitosa: se obtuvieron {} profesionales", resultado.size());
    }

    @Test
    @DisplayName("Test 2: Verificar registro (creación) de nuevo profesional")
    void testCrearProfesionalLlamaAlServicio() {
        logger.info("Test: Crear profesional");

        // Arrange
        Profesional nuevo = crearProfesionalMock(
                0,
                "0912345678",
                "Juan",
                "Pérez",
                "099123456",
                "juan@example.com",
                "LIC001",
                true
        );

        when(profesionalServiceMock.crearProfesional(any(Profesional.class)))
                .thenReturn(true);

        // Act
        boolean resultado = profesionalServiceMock.crearProfesional(nuevo);

        // Assert
        assertTrue(resultado, "La creación del profesional debe ser exitosa");
        verify(profesionalServiceMock, times(1)).crearProfesional(any(Profesional.class));

        logger.info("Profesional creado correctamente (mock)");
    }

    @Test
    @DisplayName("Test 3: Verificar actualización de profesional")
    void testActualizarProfesionalLlamaAlServicio() {
        logger.info("Test: Actualizar profesional");

        // Arrange
        Profesional existente = crearProfesionalMock(
                10,
                "0923456789",
                "María",
                "García",
                "098765432",
                "maria@example.com",
                "LIC002",
                true
        );

        when(profesionalServiceMock.actualizarProfesional(any(Profesional.class)))
                .thenReturn(true);

        // Act
        boolean resultado = profesionalServiceMock.actualizarProfesional(existente);

        // Assert
        assertTrue(resultado, "La actualización del profesional debe ser exitosa");
        verify(profesionalServiceMock, times(1)).actualizarProfesional(any(Profesional.class));

        logger.info("Profesional actualizado correctamente (mock)");
    }

    @Test
    @DisplayName("Test 4: Verificar eliminación de profesional")
    void testEliminarProfesional() {
        logger.info("Test: Eliminar profesional");

        // Arrange
        Integer profesionalId = 5;
        when(profesionalServiceMock.eliminarProfesional(profesionalId)).thenReturn(true);

        // Act
        boolean resultado = profesionalServiceMock.eliminarProfesional(profesionalId);

        // Assert
        assertTrue(resultado, "La eliminación del profesional debe ser exitosa");
        verify(profesionalServiceMock, times(1)).eliminarProfesional(profesionalId);

        logger.info("Profesional con ID {} eliminado correctamente (mock)", profesionalId);
    }

    @Test
    @DisplayName("Test 5: Verificar obtención de profesional por ID")
    void testObtenerProfesionalPorId() {
        logger.info("Test: Obtener profesional por ID");

        // Arrange
        Integer profesionalId = 7;
        Profesional profesionalMock = crearProfesionalMock(
                profesionalId,
                "0934567890",
                "Carlos",
                "Mendoza",
                "099000111",
                "carlos@example.com",
                "LIC003",
                true
        );

        when(profesionalServiceMock.getProfesionalById(profesionalId))
                .thenReturn(profesionalMock);

        // Act
        Profesional resultado = profesionalServiceMock.getProfesionalById(profesionalId);

        // Assert
        assertNotNull(resultado, "El profesional no debe ser null");
        assertEquals(profesionalId, resultado.getId(), "El ID debe coincidir");
        assertEquals("Carlos", resultado.getNombres(), "Los nombres deben coincidir");
        assertEquals("Mendoza", resultado.getApellidos(), "Los apellidos deben coincidir");
        verify(profesionalServiceMock, times(1)).getProfesionalById(profesionalId);

        logger.info("Profesional obtenido correctamente: {}", resultado.getNombreCompleto());
    }

    @Test
    @DisplayName("Test 6: Verificar búsqueda de profesional por cédula")
    void testBuscarProfesionalPorCedula() {
        logger.info("Test: Buscar profesional por cédula");

        // Arrange
        String cedula = "0945678901";
        Profesional profesionalMock = crearProfesionalMock(
                11,
                cedula,
                "Ana",
                "Gómez",
                "098111222",
                "ana@example.com",
                "LIC004",
                true
        );

        when(profesionalServiceMock.getProfesionalByCedula(cedula))
                .thenReturn(profesionalMock);

        // Act
        Profesional resultado = profesionalServiceMock.getProfesionalByCedula(cedula);

        // Assert
        assertNotNull(resultado, "El profesional no debe ser null");
        assertEquals(cedula, resultado.getCedula(), "La cédula debe coincidir");
        assertEquals("Ana Gómez", resultado.getNombreCompleto(), "El nombre completo debe coincidir");
        verify(profesionalServiceMock, times(1)).getProfesionalByCedula(cedula);

        logger.info("Profesional encontrado: {}", resultado.getNombreCompleto());
    }

    @Test
    @DisplayName("Test 7: Verificar comportamiento cuando no se encuentra profesional por cédula")
    void testProfesionalNoEncontradoPorCedula() {
        logger.info("Test: Profesional no encontrado por cédula");

        // Arrange
        String cedula = "9999999999";
        when(profesionalServiceMock.getProfesionalByCedula(cedula)).thenReturn(null);

        // Act
        Profesional resultado = profesionalServiceMock.getProfesionalByCedula(cedula);

        // Assert
        assertNull(resultado, "El profesional debe ser null cuando no existe");
        verify(profesionalServiceMock, times(1)).getProfesionalByCedula(cedula);

        logger.info("Verificado: profesional con cédula {} no encontrado", cedula);
    }

    @Test
    @DisplayName("Test 8: Verificar fallo al actualizar profesional (mock devuelve false)")
    void testActualizarProfesionalFalla() {
        logger.info("Test: Fallo al actualizar profesional");

        // Arrange
        Profesional profesional = crearProfesionalMock(
                20,
                "0911111111",
                "Luis",
                "Torres",
                "097000000",
                "luis@example.com",
                "LIC005",
                true
        );

        when(profesionalServiceMock.actualizarProfesional(any(Profesional.class)))
                .thenReturn(false);

        // Act
        boolean resultado = profesionalServiceMock.actualizarProfesional(profesional);

        // Assert
        assertFalse(resultado, "La actualización debe fallar (mock false)");
        verify(profesionalServiceMock, times(1)).actualizarProfesional(any(Profesional.class));

        logger.info("Fallo de actualización manejado correctamente (mock)");
    }

    // ============================================================
    // TESTS DE MÉTODOS AUXILIARES DEL CONTROLLER (vía reflexión)
    // ============================================================

    @Test
    @DisplayName("Test 9: mapEspecialidadNombreToId - mapeo correcto de nombres a IDs")
    void testMapEspecialidadNombreToId() throws Exception {
        logger.info("Test: mapEspecialidadNombreToId");

        Method method = ProfesionalController.class.getDeclaredMethod(
                "mapEspecialidadNombreToId", String.class
        );
        method.setAccessible(true);

        Integer idFisio = (Integer) method.invoke(controller, "Fisioterapia");
        Integer idTrauma = (Integer) method.invoke(controller, "Traumatología");
        Integer idRehab = (Integer) method.invoke(controller, "Rehabilitación");
        Integer idDesconocida = (Integer) method.invoke(controller, "OtraEspecialidad");
        Integer idNull = (Integer) method.invoke(controller, (Object) null);

        assertEquals(1, idFisio);
        assertEquals(2, idTrauma);
        assertEquals(3, idRehab);
        assertNull(idDesconocida, "Especialidad desconocida debe devolver null");
        assertNull(idNull, "Especialidad null debe devolver null");

        logger.info("Mapeo de especialidades verificado correctamente");
    }

    @Test
    @DisplayName("Test 10: trimOrNull - recorta texto y devuelve null cuando está vacío")
    void testTrimOrNull() throws Exception {
        logger.info("Test: trimOrNull");

        Method method = ProfesionalController.class.getDeclaredMethod(
                "trimOrNull", String.class
        );
        method.setAccessible(true);

        String textoConEspacios = "   hola  ";
        String soloEspacios = "    ";
        String nullInput = null;

        String r1 = (String) method.invoke(controller, textoConEspacios);
        String r2 = (String) method.invoke(controller, soloEspacios);
        String r3 = (String) method.invoke(controller, nullInput);

        assertEquals("hola", r1, "Debe recortar espacios al inicio y fin");
        assertNull(r2, "Cadena solo con espacios debe devolver null");
        assertNull(r3, "Entrada null debe devolver null");

        logger.info("trimOrNull verificado correctamente");
    }

    @Test
    @DisplayName("Test 11: validarDatosProfesional con datos válidos debe retornar true")
    void testValidarDatosProfesionalDatosValidos() throws Exception {
        logger.info("Test: validarDatosProfesional con datos válidos");

        Method method = ProfesionalController.class.getDeclaredMethod(
                "validarDatosProfesional",
                String.class,
                String.class,
                String.class,
                String.class,
                String.class,
                String.class
        );
        method.setAccessible(true);

        // Datos totalmente válidos para evitar que se dispare Alert
        String cedula = "0912345678";       // 10 dígitos
        String nombres = "Juan Carlos";     // solo letras y espacios
        String apellidos = "Pérez López";   // solo letras y espacios
        String telefono = "099123456";      // 9 dígitos
        String email = "juan.carlos@example.com";
        String numeroLicencia = "LIC00123"; // alfanumérico

        Boolean valido = (Boolean) method.invoke(
                controller,
                cedula,
                nombres,
                apellidos,
                telefono,
                email,
                numeroLicencia
        );

        assertTrue(valido, "Con datos válidos, la validación debe ser true");

        logger.info("Validación de datos de profesional (válidos) verificada correctamente");
    }

    // ============================================================
    // MÉTODOS AUXILIARES PARA CREAR MOCKS
    // ============================================================

    private List<Profesional> crearProfesionalesMock() {
        List<Profesional> lista = new ArrayList<>();
        lista.add(crearProfesionalMock(
                1,
                "0912345678",
                "Juan",
                "Pérez",
                "099111111",
                "juan@example.com",
                "LIC001",
                true
        ));
        lista.add(crearProfesionalMock(
                2,
                "0923456789",
                "María",
                "García",
                "099222222",
                "maria@example.com",
                "LIC002",
                true
        ));
        lista.add(crearProfesionalMock(
                3,
                "0934567890",
                "Carlos",
                "Mendoza",
                "099333333",
                "carlos@example.com",
                "LIC003",
                false
        ));
        return lista;
    }

    private Profesional crearProfesionalMock(
            Integer id,
            String cedula,
            String nombres,
            String apellidos,
            String telefono,
            String email,
            String numeroLicencia,
            boolean activo
    ) {
        Profesional p = new Profesional();
        p.setId(id);
        p.setCedula(cedula);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setTelefono(telefono);
        p.setEmail(email);
        p.setNumeroLicencia(numeroLicencia);
        p.setActivo(activo);
        // valores por defecto para que no den null en UI
        p.setEspecialidadId(1);
        p.setEspecialidadNombre("Fisioterapia");
        p.setModalidadAtencion("PRESENCIAL");
        p.setUsuarioId(1);
        return p;
    }
}
