package com.example.ceragen_2.service;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Cliente;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CitaServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(CitaServiceTest.class);
    private static CitaService citaService;
    private static PacienteService pacienteService;
    private static ProfesionalService profesionalService;
    private static ClienteService clienteService;
    private static FacturaService facturaService;
    private static Integer pacienteTestId;
    private static Integer profesionalTestId;
    private static Integer clienteTestId;

    @BeforeAll
    static void setUp() {
        logger.info("Iniciando tests de CitaService");
        citaService = CitaService.getInstance();
        pacienteService = PacienteService.getInstance();
        profesionalService = ProfesionalService.getInstance();
        clienteService = ClienteService.getInstance();
        facturaService = FacturaService.getInstance();

        // Obtener IDs de paciente y profesional existentes para tests
        var pacientes = pacienteService.getAllPacientes();
        var profesionales = profesionalService.getAllProfesionales();
        var clientes = clienteService.getAllClientes();

        if (!pacientes.isEmpty()) {
            pacienteTestId = pacientes.get(0).getId();
        }
        if (!profesionales.isEmpty()) {
            profesionalTestId = profesionales.get(0).getId();
        }
        if (!clientes.isEmpty()) {
            clienteTestId = clientes.get(0).getId();
        } else {
            // Crear un cliente de prueba si no existe
            Cliente clienteTest = new Cliente();
            clienteTest.setCedula("0999999999");
            clienteTest.setNombres("Cliente");
            clienteTest.setApellidos("Test");
            clienteTest.setTelefono("0999999999");
            clienteTest.setEmail("test@test.com");
            clienteTest.setDireccion("Direccion Test");
            if (clienteService.crearCliente(clienteTest)) {
                clienteTestId = clienteTest.getId();
            }
        }

        logger.info("Usando paciente ID: {}, profesional ID: {} y cliente ID: {} para tests",
                    pacienteTestId, profesionalTestId, clienteTestId);
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear cita nueva")
    void testCrearCita() {
        Assumptions.assumeTrue(pacienteTestId != null && profesionalTestId != null && clienteTestId != null,
                              "Se requieren pacientes, profesionales y clientes en la base de datos");

        logger.info("Test: Crear cita nueva");

        LocalDateTime fechaHora = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        String motivo = "Consulta general de prueba";
        BigDecimal costo = new BigDecimal("50.00");

        // Crear factura con cita
        List<Cita> citas = new ArrayList<>();
        Cita cita = new Cita(pacienteTestId, profesionalTestId, fechaHora, motivo, costo);
        citas.add(cita);

        Integer facturaId = facturaService.crearFactura(
            clienteTestId,
            "Guayaquil",
            50.00,
            6.00,
            0.00,
            56.00,
            "EFECTIVO",
            citas
        );

        // Verificar que la factura se creó
        assertNotNull(facturaId, "La factura debería crearse exitosamente");
        logger.info("Factura creada con ID: {}", facturaId);

        // Verificar que la cita se creó en la base de datos
        List<Cita> citasCreadas = citaService.getCitas(0, 10, pacienteTestId, profesionalTestId, "CONFIRMADA", null, null);
        assertFalse(citasCreadas.isEmpty(), "Debe haber al menos una cita creada");

        // Verificar que la cita tiene los datos correctos
        Cita citaCreada = citasCreadas.get(0);
        assertEquals(pacienteTestId, citaCreada.getPacienteId(), "El paciente ID debe coincidir");
        assertEquals(profesionalTestId, citaCreada.getProfesionalId(), "El profesional ID debe coincidir");
        assertEquals(motivo, citaCreada.getMotivo(), "El motivo debe coincidir");
        assertEquals("CONFIRMADA", citaCreada.getEstado(), "El estado debe ser CONFIRMADA");

        logger.info("Cita creada exitosamente con factura ID: {}", facturaId);
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: Obtener citas con paginación")
    void testGetCitasConPaginacion() {
        logger.info("Test: Obtener citas con paginación");

        List<Cita> citas = citaService.getCitas(0, 10, null, null, "TODOS", null, null);

        assertNotNull(citas, "La lista de citas no debería ser null");
        assertTrue(citas.size() <= 10, "No debería retornar más citas que el límite");
        logger.info("Se obtuvieron {} citas", citas.size());
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Contar citas")
    void testCountCitas() {
        logger.info("Test: Contar citas");

        int total = citaService.countCitas(null, null, "TODOS", null, null);

        assertTrue(total >= 0, "El conteo debería ser mayor o igual a 0");
        logger.info("Total de citas: {}", total);
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Filtrar citas por estado")
    void testFiltrarCitasPorEstado() {
        logger.info("Test: Filtrar citas por estado");

        List<Cita> citasPendientes = citaService.getCitas(0, 100, null, null, "PENDIENTE", null, null);

        assertNotNull(citasPendientes, "La lista no debería ser null");

        for (Cita cita : citasPendientes) {
            assertEquals("PENDIENTE", cita.getEstado(), "Todas las citas deberían tener estado PENDIENTE");
        }

        logger.info("Se encontraron {} citas pendientes", citasPendientes.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Filtrar citas por paciente")
    void testFiltrarCitasPorPaciente() {
        Assumptions.assumeTrue(pacienteTestId != null, "Se requiere un paciente para este test");

        logger.info("Test: Filtrar citas por paciente");

        List<Cita> citas = citaService.getCitas(0, 100, pacienteTestId, null, "TODOS", null, null);

        assertNotNull(citas, "La lista no debería ser null");

        for (Cita cita : citas) {
            assertEquals(pacienteTestId, cita.getPacienteId(), "Todas las citas deberían ser del mismo paciente");
        }

        logger.info("Se encontraron {} citas para el paciente ID {}", citas.size(), pacienteTestId);
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Filtrar citas por profesional")
    void testFiltrarCitasPorProfesional() {
        Assumptions.assumeTrue(profesionalTestId != null, "Se requiere un profesional para este test");

        logger.info("Test: Filtrar citas por profesional");

        List<Cita> citas = citaService.getCitas(0, 100, null, profesionalTestId, "TODOS", null, null);

        assertNotNull(citas, "La lista no debería ser null");

        for (Cita cita : citas) {
            assertEquals(profesionalTestId, cita.getProfesionalId(), "Todas las citas deberían ser del mismo profesional");
        }

        logger.info("Se encontraron {} citas para el profesional ID {}", citas.size(), profesionalTestId);
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Obtener cita por ID")
    void testGetCitaById() {
        logger.info("Test: Obtener cita por ID");

        List<Cita> citas = citaService.getCitas(0, 1, null, null, "TODOS", null, null);
        Assumptions.assumeTrue(!citas.isEmpty(), "Se requiere al menos una cita");

        Integer citaId = citas.get(0).getId();
        Cita cita = citaService.getCitaById(citaId);

        assertNotNull(cita, "La cita no debería ser null");
        assertEquals(citaId, cita.getId(), "Los IDs deberían coincidir");

        logger.info("Cita obtenida: ID {}", cita.getId());
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Actualizar cita")
    void testActualizarCita() {
        Assumptions.assumeTrue(pacienteTestId != null && profesionalTestId != null && clienteTestId != null,
                              "Se requieren pacientes, profesionales y clientes");

        logger.info("Test: Actualizar cita");

        // Crear una cita para actualizar
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0).withSecond(0).withNano(0);
        BigDecimal costo = new BigDecimal("60.00");

        List<Cita> citasParaFactura = new ArrayList<>();
        Cita citaNueva = new Cita(pacienteTestId, profesionalTestId, fechaHora, "Motivo original", costo);
        citasParaFactura.add(citaNueva);

        Integer facturaId = facturaService.crearFactura(clienteTestId, "Guayaquil", 60.00, 7.20, 0.00, 67.20, "EFECTIVO", citasParaFactura);
        Assumptions.assumeTrue(facturaId != null, "La factura debería haberse creado");

        // Obtener la cita recién creada
        List<Cita> citas = citaService.getCitas(0, 100, pacienteTestId, profesionalTestId, "CONFIRMADA", null, null);
        Assumptions.assumeTrue(!citas.isEmpty(), "La cita debería haberse creado");

        Cita cita = citas.get(0);
        Integer citaId = cita.getId();

        // Actualizar la cita
        String nuevoMotivo = "Motivo actualizado";
        String nuevoEstado = "ATENDIDA";
        String observaciones = "Observaciones de prueba";

        boolean resultado = citaService.actualizarCita(citaId, pacienteTestId, profesionalTestId,
                                                       fechaHora, nuevoMotivo, nuevoEstado, observaciones);

        assertTrue(resultado, "La actualización debería ser exitosa");

        // Verificar que se actualizó
        Cita citaActualizada = citaService.getCitaById(citaId);
        assertEquals(nuevoEstado, citaActualizada.getEstado(), "El estado debería estar actualizado");
        assertEquals(observaciones, citaActualizada.getObservaciones(), "Las observaciones deberían estar actualizadas");

        logger.info("Cita actualizada exitosamente");

        // Limpiar
        citaService.eliminarCita(citaId);
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Cambiar estado de cita")
    void testCambiarEstado() {
        Assumptions.assumeTrue(pacienteTestId != null && profesionalTestId != null && clienteTestId != null,
                              "Se requieren pacientes, profesionales y clientes");

        logger.info("Test: Cambiar estado de cita");

        // Crear una cita
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(3).withHour(16).withMinute(0).withSecond(0).withNano(0);
        BigDecimal costo = new BigDecimal("55.00");

        List<Cita> citasParaFactura = new ArrayList<>();
        Cita citaNueva = new Cita(pacienteTestId, profesionalTestId, fechaHora, "Motivo para cambio de estado", costo);
        citasParaFactura.add(citaNueva);

        Integer facturaId = facturaService.crearFactura(clienteTestId, "Guayaquil", 55.00, 6.60, 0.00, 61.60, "EFECTIVO", citasParaFactura);
        Assumptions.assumeTrue(facturaId != null, "La factura debería haberse creado");

        // Obtener la cita
        List<Cita> citas = citaService.getCitas(0, 1, pacienteTestId, profesionalTestId, "CONFIRMADA", null, null);
        Assumptions.assumeTrue(!citas.isEmpty(), "La cita debería existir");

        Integer citaId = citas.get(0).getId();

        // Cambiar estado
        boolean resultado = citaService.cambiarEstado(citaId, "ATENDIDA");
        assertTrue(resultado, "El cambio de estado debería ser exitoso");

        // Verificar
        Cita citaActualizada = citaService.getCitaById(citaId);
        assertEquals("ATENDIDA", citaActualizada.getEstado(), "El estado debería ser ATENDIDA");

        logger.info("Estado cambiado exitosamente");

        // Limpiar
        citaService.eliminarCita(citaId);
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Verificar conflicto de horario")
    void testExisteConflictoHorario() {
        Assumptions.assumeTrue(pacienteTestId != null && profesionalTestId != null && clienteTestId != null,
                              "Se requieren pacientes, profesionales y clientes");

        logger.info("Test: Verificar conflicto de horario");

        LocalDateTime fechaHora = LocalDateTime.now().plusDays(5).withHour(9).withMinute(0).withSecond(0).withNano(0);
        BigDecimal costo = new BigDecimal("45.00");

        // Crear primera cita
        List<Cita> citasParaFactura = new ArrayList<>();
        Cita citaNueva = new Cita(pacienteTestId, profesionalTestId, fechaHora, "Primera cita", costo);
        citasParaFactura.add(citaNueva);

        Integer facturaId = facturaService.crearFactura(clienteTestId, "Guayaquil", 45.00, 5.40, 0.00, 50.40, "EFECTIVO", citasParaFactura);
        Assumptions.assumeTrue(facturaId != null, "La factura debería haberse creado");

        // Verificar que hay conflicto
        boolean existeConflicto = citaService.existeConflictoHorario(profesionalTestId, fechaHora, null);
        assertTrue(existeConflicto, "Debería detectar el conflicto de horario");

        logger.info("Conflicto de horario detectado correctamente");

        // Limpiar
        List<Cita> citas = citaService.getCitas(0, 100, pacienteTestId, profesionalTestId, "TODOS", null, null);
        for (Cita cita : citas) {
            citaService.eliminarCita(cita.getId());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Eliminar cita")
    void testEliminarCita() {
        Assumptions.assumeTrue(pacienteTestId != null && profesionalTestId != null && clienteTestId != null,
                              "Se requieren pacientes, profesionales y clientes");

        logger.info("Test: Eliminar cita");

        // Crear una cita para eliminar
        LocalDateTime fechaHora = LocalDateTime.now().plusDays(6).withHour(11).withMinute(0).withSecond(0).withNano(0);
        BigDecimal costo = new BigDecimal("40.00");

        List<Cita> citasParaFactura = new ArrayList<>();
        Cita citaNueva = new Cita(pacienteTestId, profesionalTestId, fechaHora, "Cita para eliminar", costo);
        citasParaFactura.add(citaNueva);

        Integer facturaId = facturaService.crearFactura(clienteTestId, "Guayaquil", 40.00, 4.80, 0.00, 44.80, "EFECTIVO", citasParaFactura);
        Assumptions.assumeTrue(facturaId != null, "La factura debería haberse creado");

        // Obtener la cita
        List<Cita> citas = citaService.getCitas(0, 1, pacienteTestId, profesionalTestId, "CONFIRMADA", null, null);
        Assumptions.assumeTrue(!citas.isEmpty(), "La cita debería existir");

        Integer citaId = citas.get(0).getId();

        // Eliminar
        boolean resultado = citaService.eliminarCita(citaId);
        assertTrue(resultado, "La eliminación debería ser exitosa");

        // Verificar que ya no existe
        Cita citaEliminada = citaService.getCitaById(citaId);
        assertNull(citaEliminada, "La cita no debería existir después de eliminarla");

        logger.info("Cita eliminada exitosamente");
    }

    @AfterAll
    static void tearDown() {
        logger.info("Tests de CitaService completados");
    }
}
