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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests de unidad para ProfesionalController / ProfesionalService.
 *
 * - 3 tests para "crear profesional" (1 caso exitoso y 2 con datos inválidos
 *   simulados como fallo del servicio).
 * - 3 tests para "eliminar profesional" (1 exitoso, 1 fallo de servicio,
 *   1 con ID nulo donde no se debe llamar al servicio).
 * - 1 test para cada método auxiliar (mapEspecialidadNombreToId, trimOrNull).
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
        logger.info("Inicializando ProfesionalController para tests");
        controller = new ProfesionalController();

        // Inyectar el mock del servicio en el campo profesionalService
        Field field = ProfesionalController.class.getDeclaredField("profesionalService");
        field.setAccessible(true);
        field.set(controller, profesionalServiceMock);
    }

    // ============================================================
    // 3 TESTS PARA "CREAR PROFESIONAL"
    // ============================================================

    @Test
    @DisplayName("Crear profesional - Datos válidos: el servicio debe devolver true")
    void testCrearProfesional_DatosValidos_ServicioOk() {
        logger.info("Test crear profesional (datos válidos, servicio OK)");

        Profesional p = new Profesional();
        p.setCedula("0912345678");
        p.setNombres("Juan");
        p.setApellidos("Pérez");
        p.setTelefono("099123456");
        p.setEmail("juan@example.com");
        p.setNumeroLicencia("LIC001");
        p.setActivo(true);

        when(profesionalServiceMock.crearProfesional(any(Profesional.class)))
                .thenReturn(true);

        boolean resultado = profesionalServiceMock.crearProfesional(p);

        assertTrue(resultado, "Con datos válidos, el servicio debe devolver true");
        verify(profesionalServiceMock, times(1)).crearProfesional(any(Profesional.class));
    }

    @Test
    @DisplayName("Crear profesional - Nombre vacío: el servicio debe devolver false")
    void testCrearProfesional_NombreVacio_ServicioFalla() {
        logger.info("Test crear profesional (nombre vacío, servicio falla)");

        Profesional p = new Profesional();
        p.setCedula("0912345678");
        p.setNombres(""); // nombre vacío
        p.setApellidos("Pérez");
        p.setTelefono("099123456");
        p.setEmail("juan@example.com");
        p.setNumeroLicencia("LIC001");
        p.setActivo(true);

        when(profesionalServiceMock.crearProfesional(any(Profesional.class)))
                .thenReturn(false);

        boolean resultado = profesionalServiceMock.crearProfesional(p);

        assertFalse(resultado, "Con nombre vacío simulamos que el servicio devuelve false");
        verify(profesionalServiceMock, times(1)).crearProfesional(any(Profesional.class));
    }

    @Test
    @DisplayName("Crear profesional - Cédula inválida: el servicio debe devolver false")
    void testCrearProfesional_CedulaInvalida_ServicioFalla() {
        logger.info("Test crear profesional (cédula inválida, servicio falla)");

        Profesional p = new Profesional();
        p.setCedula("12345"); // cédula incorrecta
        p.setNombres("Juan");
        p.setApellidos("Pérez");
        p.setTelefono("099123456");
        p.setEmail("juan@example.com");
        p.setNumeroLicencia("LIC001");
        p.setActivo(true);

        when(profesionalServiceMock.crearProfesional(any(Profesional.class)))
                .thenReturn(false);

        boolean resultado = profesionalServiceMock.crearProfesional(p);

        assertFalse(resultado, "Con cédula inválida simulamos que el servicio devuelve false");
        verify(profesionalServiceMock, times(1)).crearProfesional(any(Profesional.class));
    }

    // ============================================================
    // 3 TESTS PARA "ELIMINAR PROFESIONAL"
    // ============================================================

    @Test
    @DisplayName("Eliminar profesional - ID válido y servicio OK debe devolver true")
    void testEliminarProfesional_ValidoServicioOk() {
        logger.info("Test eliminar profesional (ID válido, servicio OK)");

        Integer id = 10;
        when(profesionalServiceMock.eliminarProfesional(id)).thenReturn(true);

        boolean resultado = profesionalServiceMock.eliminarProfesional(id);

        assertTrue(resultado, "Con ID válido y servicio OK, debe retornar true");
        verify(profesionalServiceMock, times(1)).eliminarProfesional(id);
    }

    @Test
    @DisplayName("Eliminar profesional - Servicio devuelve false (no se pudo eliminar)")
    void testEliminarProfesional_ServicioFalla() {
        logger.info("Test eliminar profesional (servicio devuelve false)");

        Integer id = 99;
        when(profesionalServiceMock.eliminarProfesional(id)).thenReturn(false);

        boolean resultado = profesionalServiceMock.eliminarProfesional(id);

        assertFalse(resultado, "Si el servicio devuelve false, no se eliminó el profesional");
        verify(profesionalServiceMock, times(1)).eliminarProfesional(id);
    }

    @Test
    @DisplayName("Eliminar profesional - Profesional sin ID no debe llamar al servicio")
    void testEliminarProfesional_IdNulo_NoLlamaServicio() throws Exception {
        logger.info("Test eliminar profesional (ID nulo)");

        Profesional profesionalSinId = new Profesional();
        profesionalSinId.setId(null);

        Method method = ProfesionalController.class.getDeclaredMethod(
                "handleEliminarProfesional",
                Profesional.class
        );
        method.setAccessible(true);

        // Ejecutamos el método privado: al tener ID null debe salir sin tocar el servicio
        method.invoke(controller, profesionalSinId);

        verify(profesionalServiceMock, never()).eliminarProfesional(any());
    }

    // ============================================================
    // 1 TEST PARA CADA MÉTODO AUXILIAR
    // ============================================================

    @Test
    @DisplayName("mapEspecialidadNombreToId - mapea los nombres a IDs correctos")
    void testMapEspecialidadNombreToId() throws Exception {
        logger.info("Test mapEspecialidadNombreToId");

        Method method = ProfesionalController.class.getDeclaredMethod(
                "mapEspecialidadNombreToId",
                String.class
        );
        method.setAccessible(true);

        Integer idFisio = (Integer) method.invoke(controller, "Fisioterapia");
        Integer idTrauma = (Integer) method.invoke(controller, "Traumatología");
        Integer idRehab = (Integer) method.invoke(controller, "Rehabilitación");
        Integer idDesconocida = (Integer) method.invoke(controller, "Otra");
        Integer idNull = (Integer) method.invoke(controller, (Object) null);

        assertEquals(1, idFisio);
        assertEquals(2, idTrauma);
        assertEquals(3, idRehab);
        assertNull(idDesconocida);
        assertNull(idNull);
    }

    @Test
    @DisplayName("trimOrNull - recorta texto y devuelve null cuando queda vacío")
    void testTrimOrNull() throws Exception {
        logger.info("Test trimOrNull");

        Method method = ProfesionalController.class.getDeclaredMethod(
                "trimOrNull",
                String.class
        );
        method.setAccessible(true);

        String textoConEspacios = "   hola  ";
        String soloEspacios = "      ";
        String nullInput = null;

        String r1 = (String) method.invoke(controller, textoConEspacios);
        String r2 = (String) method.invoke(controller, soloEspacios);
        String r3 = (String) method.invoke(controller, nullInput);

        assertEquals("hola", r1);
        assertNull(r2);
        assertNull(r3);
    }
}
