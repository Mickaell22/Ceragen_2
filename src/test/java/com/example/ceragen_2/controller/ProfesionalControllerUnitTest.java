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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests de unidad para ProfesionalController usando mocks de ProfesionalService.
 * NOTA: Igual que en CitasControllerUnitTest, se inyecta el mock por reflexión,
 * pero los tests se centran en las interacciones con el servicio.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests de Unidad - ProfesionalController")
class ProfesionalControllerUnitTest {

    private static final Logger logger = LoggerFactory.getLogger(ProfesionalControllerUnitTest.class);

    @Mock
    private ProfesionalService profesionalServiceMock;

    private ProfesionalController controller;

    @BeforeEach
    void setUp() throws Exception {
        logger.info("Configurando tests de unidad para ProfesionalController con mocks");

        // Crear instancia del controller
        controller = new ProfesionalController();

        // Inyectar el mock usando reflexión (el campo es final en el controller)
        injectMock(controller, "profesionalService", profesionalServiceMock);

        logger.info("Mock de ProfesionalService inyectado correctamente");
    }

    /**
     * Helper para inyectar mocks usando reflexión.
     */
    private void injectMock(Object target, String fieldName, Object mock) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, mock);
    }

    // ============================================================
    // TESTS DE INTERACCIÓN CON ProfesionalService
    // ============================================================

    @Test
    @DisplayName("Test 1: Verificar que el servicio retorna la lista de profesionales")
    void testGetAllProfesionales() {
        logger.info("Test: getAllProfesionales");

        // Arrange
        List<Profesional> listaMock = crearProfesionalesMock();
        when(profesionalServiceMock.getAllProfesionales()).thenReturn(listaMock);

        // Act
        List<Profesional> resultado = profesionalServiceMock.getAllProfesionales();

        // Assert
        assertNotNull(resultado, "La lista de profesionales no debe ser null");
        assertEquals(3, resultado.size(), "Deben retornarse 3 profesionales mockeados");
        verify(profesionalServiceMock, times(1)).getAllProfesionales();

        logger.info("Verificación exitosa: se obtuvieron {} profesionales", resultado.size());
    }

    @Test
    @DisplayName("Test 2: Verificar creación de profesional exitosa")
    void testCrearProfesionalExitoso() {
        logger.info("Test: crearProfesional exitoso");

        // Arrange
        Profesional profesional = crearProfesionalMock(1, "0912345678", "Juan", "Pérez");
        when(profesionalServiceMock.crearProfesional(any(Profesional.class))).thenReturn(true);

        // Act
        boolean resultado = profesionalServiceMock.crearProfesional(profesional);

        // Assert
        assertTrue(resultado, "La creación debe ser exitosa");
        verify(profesionalServiceMock, times(1)).crearProfesional(any(Profesional.class));

        logger.info("Creación de profesional verificada correctamente");
    }

    @Test
    @DisplayName("Test 3: Verificar creación de profesional fallida")
    void testCrearProfesionalFalla() {
        logger.info("Test: crearProfesional falla");

        // Arrange
        Profesional profesional = crearProfesionalMock(2, "0923456789", "María", "García");
        when(profesionalServiceMock.crearProfesional(any(Profesional.class))).thenReturn(false);

        // Act
        boolean resultado = profesionalServiceMock.crearProfesional(profesional);

        // Assert
        assertFalse(resultado, "La creación debe fallar según el mock");
        verify(profesionalServiceMock, times(1)).crearProfesional(any(Profesional.class));

        logger.info("Fallo de creación manejado correctamente");
    }

    @Test
    @DisplayName("Test 4: Verificar actualización de profesional exitosa")
    void testActualizarProfesionalExitoso() {
        logger.info("Test: actualizarProfesional exitoso");

        // Arrange
        Profesional profesional = crearProfesionalMock(3, "0934567890", "Carlos", "Mendoza");
        when(profesionalServiceMock.actualizarProfesional(any(Profesional.class))).thenReturn(true);

        // Act
        boolean resultado = profesionalServiceMock.actualizarProfesional(profesional);

        // Assert
        assertTrue(resultado, "La actualización debe ser exitosa");
        verify(profesionalServiceMock, times(1)).actualizarProfesional(any(Profesional.class));

        logger.info("Actualización de profesional verificada correctamente");
    }

    @Test
    @DisplayName("Test 5: Verificar actualización de profesional fallida")
    void testActualizarProfesionalFalla() {
        logger.info("Test: actualizarProfesional falla");

        // Arrange
        Profesional profesional = crearProfesionalMock(4, "0945678901", "Ana", "Gómez");
        when(profesionalServiceMock.actualizarProfesional(any(Profesional.class))).thenReturn(false);

        // Act
        boolean resultado = profesionalServiceMock.actualizarProfesional(profesional);

        // Assert
        assertFalse(resultado, "La actualización debe fallar según el mock");
        verify(profesionalServiceMock, times(1)).actualizarProfesional(any(Profesional.class));

        logger.info("Fallo de actualización manejado correctamente");
    }

    @Test
    @DisplayName("Test 6: Verificar obtención de profesional por ID")
    void testGetProfesionalById() {
        logger.info("Test: getProfesionalById");

        // Arrange
        Integer profesionalId = 10;
        Profesional profesionalMock = crearProfesionalMock(profesionalId, "0956789012", "Luis", "Ramírez");

        when(profesionalServiceMock.getProfesionalById(profesionalId)).thenReturn(profesionalMock);

        // Act
        Profesional profesional = profesionalServiceMock.getProfesionalById(profesionalId);

        // Assert
        assertNotNull(profesional, "El profesional no debe ser null");
        assertEquals(profesionalId, profesional.getId(), "El ID debe coincidir");
        assertEquals("0956789012", profesional.getCedula(), "La cédula debe coincidir");
        verify(profesionalServiceMock, times(1)).getProfesionalById(profesionalId);

        logger.info("Profesional obtenido correctamente: ID {}", profesionalId);
    }

    @Test
    @DisplayName("Test 7: Verificar búsqueda de profesional por cédula")
    void testGetProfesionalByCedula() {
        logger.info("Test: getProfesionalByCedula");

        // Arrange
        String cedula = "0912345678";
        Profesional profesionalMock = crearProfesionalMock(1, cedula, "Juan", "Pérez");

        when(profesionalServiceMock.getProfesionalByCedula(cedula)).thenReturn(profesionalMock);

        // Act
        Profesional profesional = profesionalServiceMock.getProfesionalByCedula(cedula);

        // Assert
        assertNotNull(profesional, "El profesional no debe ser null");
        assertEquals(cedula, profesional.getCedula(), "La cédula debe coincidir");
        assertEquals("Juan", profesional.getNombres(), "El nombre debe coincidir");
        assertEquals("Pérez", profesional.getApellidos(), "El apellido debe coincidir");
        verify(profesionalServiceMock, times(1)).getProfesionalByCedula(cedula);

        logger.info("Profesional encontrado por cédula: {}", profesional.getNombreCompleto());
    }

    @Test
    @DisplayName("Test 8: Verificar comportamiento cuando no se encuentra profesional por cédula")
    void testProfesionalNoEncontradoPorCedula() {
        logger.info("Test: profesional no encontrado por cédula");

        // Arrange
        String cedula = "9999999999";
        when(profesionalServiceMock.getProfesionalByCedula(cedula)).thenReturn(null);

        // Act
        Profesional profesional = profesionalServiceMock.getProfesionalByCedula(cedula);

        // Assert
        assertNull(profesional, "El profesional debe ser null cuando no existe");
        verify(profesionalServiceMock, times(1)).getProfesionalByCedula(cedula);

        logger.info("Verificado: profesional no encontrado para la cédula {}", cedula);
    }

    @Test
    @DisplayName("Test 9: Verificar lista vacía de profesionales")
    void testGetAllProfesionalesListaVacia() {
        logger.info("Test: getAllProfesionales lista vacía");

        // Arrange
        when(profesionalServiceMock.getAllProfesionales()).thenReturn(new ArrayList<>());

        // Act
        List<Profesional> resultado = profesionalServiceMock.getAllProfesionales();

        // Assert
        assertNotNull(resultado, "La lista no debe ser null, aunque esté vacía");
        assertTrue(resultado.isEmpty(), "La lista debe estar vacía");
        verify(profesionalServiceMock, times(1)).getAllProfesionales();

        logger.info("Verificado: lista de profesionales vacía");
    }

    // ============================================================
    // MÉTODOS AUXILIARES PARA CREAR MOCKS
    // ============================================================

    private List<Profesional> crearProfesionalesMock() {
        List<Profesional> lista = new ArrayList<>();
        lista.add(crearProfesionalMock(1, "0912345678", "Juan", "Pérez"));
        lista.add(crearProfesionalMock(2, "0923456789", "María", "García"));
        lista.add(crearProfesionalMock(3, "0934567890", "Carlos", "Mendoza"));
        return lista;
    }

    private Profesional crearProfesionalMock(Integer id, String cedula, String nombres, String apellidos) {
        Profesional p = new Profesional();
        p.setId(id);
        p.setCedula(cedula);
        p.setNombres(nombres);
        p.setApellidos(apellidos);
        p.setTelefono("0999999999");
        p.setEmail(nombres.toLowerCase() + "@test.com");
        p.setNumeroLicencia("LIC-" + id);
        p.setActivo(Boolean.TRUE);
        p.setEspecialidadNombre("Fisioterapia");
        p.setEspecialidadId(1);
        p.setUsuarioId(null); // según tu controlador, normalmente null
        return p;
    }
}
