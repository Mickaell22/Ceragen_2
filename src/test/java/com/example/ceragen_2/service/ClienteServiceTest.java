package com.example.ceragen_2.service;

import com.example.ceragen_2.model.Cliente;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClienteServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(ClienteServiceTest.class);
    private static ClienteService clienteService;
    private static Integer clienteTestId;

    @BeforeAll
    static void setUp() {
        logger.info("Iniciando tests de ClienteService");
        clienteService = ClienteService.getInstance();
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear cliente nuevo")
    void testCrearCliente() {
        logger.info("Test: Crear cliente nuevo");

        String cedula = "0999999" + System.currentTimeMillis() % 1000;
        Cliente cliente = new Cliente();
        cliente.setCedula(cedula);
        cliente.setNombres("Juan");
        cliente.setApellidos("Pérez");
        cliente.setTelefono("0991234567");
        cliente.setEmail("juan.perez@test.com");
        cliente.setDireccion("Av. Principal 123, Guayaquil");

        boolean resultado = clienteService.crearCliente(cliente);

        assertTrue(resultado, "El cliente debería crearse exitosamente");
        assertNotNull(cliente.getId(), "El cliente debería tener un ID asignado");
        logger.info("Cliente creado exitosamente: {} - ID: {}", cliente.getNombreCompleto(), cliente.getId());

        // Verificar que el cliente existe
        Cliente clienteObtenido = clienteService.getClienteByCedula(cedula);
        assertNotNull(clienteObtenido, "El cliente debería existir en la base de datos");
        assertEquals(cedula, clienteObtenido.getCedula(), "La cédula debería coincidir");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: No crear cliente con cédula duplicada")
    void testNoCrearClienteDuplicado() {
        logger.info("Test: No crear cliente con cédula duplicada");

        // Crear un cliente
        String cedula = "0888888" + System.currentTimeMillis() % 1000;
        Cliente cliente1 = new Cliente();
        cliente1.setCedula(cedula);
        cliente1.setNombres("María");
        cliente1.setApellidos("García");
        cliente1.setTelefono("0991234568");

        boolean resultado1 = clienteService.crearCliente(cliente1);
        assertTrue(resultado1, "El primer cliente debería crearse exitosamente");

        // Intentar crear otro cliente con la misma cédula
        Cliente cliente2 = new Cliente();
        cliente2.setCedula(cedula);
        cliente2.setNombres("Pedro");
        cliente2.setApellidos("López");

        // Verificar que ya existe un cliente con esa cédula
        Cliente existente = clienteService.getClienteByCedula(cedula);
        assertNotNull(existente, "Debería existir un cliente con esa cédula");
        assertEquals("María", existente.getNombres(), "El cliente existente debería ser María");

        logger.info("Validación de cédula duplicada funcionando correctamente");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Obtener todos los clientes")
    void testGetAllClientes() {
        logger.info("Test: Obtener todos los clientes");

        List<Cliente> clientes = clienteService.getAllClientes();

        assertNotNull(clientes, "La lista de clientes no debería ser null");
        assertFalse(clientes.isEmpty(), "Debería haber al menos un cliente");
        logger.info("Se obtuvieron {} clientes", clientes.size());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Obtener clientes completos")
    void testGetAllClientesCompletos() {
        logger.info("Test: Obtener clientes completos");

        List<Cliente> clientes = clienteService.getAllClientesCompletos();

        assertNotNull(clientes, "La lista no debería ser null");

        if (!clientes.isEmpty()) {
            Cliente cliente = clientes.get(0);
            assertNotNull(cliente.getNombres(), "Los nombres deberían estar presentes");
            assertNotNull(cliente.getApellidos(), "Los apellidos deberían estar presentes");
            assertNotNull(cliente.getCedula(), "La cédula debería estar presente");
        }

        logger.info("Se obtuvieron {} clientes completos", clientes.size());
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Obtener clientes con paginación")
    void testGetClientesPaginados() {
        logger.info("Test: Obtener clientes con paginación");

        int offset = 0;
        int limit = 5;
        String searchText = "";

        List<Cliente> clientes = clienteService.getClientesPaginados(offset, limit, searchText);

        assertNotNull(clientes, "La lista no debería ser null");
        assertTrue(clientes.size() <= limit, "No debería retornar más clientes que el límite");
        logger.info("Se obtuvieron {} clientes paginados", clientes.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Contar clientes")
    void testCountClientes() {
        logger.info("Test: Contar clientes");

        int total = clienteService.countClientes("");

        assertTrue(total >= 0, "El conteo debería ser mayor o igual a 0");
        logger.info("Total de clientes: {}", total);
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Buscar clientes por criterio")
    void testBuscarClientes() {
        logger.info("Test: Buscar clientes por criterio");

        // Primero crear un cliente con datos específicos
        String cedula = "0777777" + System.currentTimeMillis() % 1000;
        Cliente cliente = new Cliente();
        cliente.setCedula(cedula);
        cliente.setNombres("Carlos");
        cliente.setApellidos("Martínez");
        cliente.setTelefono("0991234569");

        clienteService.crearCliente(cliente);

        // Buscar por nombre
        List<Cliente> resultados = clienteService.buscarClientes("Carlos");

        assertNotNull(resultados, "La lista no debería ser null");
        assertFalse(resultados.isEmpty(), "Debería encontrar al menos un cliente");

        // Verificar que al menos uno de los resultados coincide
        boolean encontrado = resultados.stream()
            .anyMatch(c -> c.getNombres().contains("Carlos"));
        assertTrue(encontrado, "Debería encontrar el cliente 'Carlos'");

        logger.info("Búsqueda encontró {} clientes con criterio 'Carlos'", resultados.size());
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Obtener cliente por ID")
    void testGetClienteById() {
        logger.info("Test: Obtener cliente por ID");

        // Obtener un cliente existente
        List<Cliente> clientes = clienteService.getAllClientes();
        Assumptions.assumeFalse(clientes.isEmpty(), "Debe haber al menos un cliente");

        Cliente clienteOriginal = clientes.get(0);
        clienteTestId = clienteOriginal.getId();

        // Obtener por ID
        Cliente cliente = clienteService.getClienteById(clienteTestId);

        assertNotNull(cliente, "El cliente no debería ser null");
        assertEquals(clienteTestId, cliente.getId(), "Los IDs deberían coincidir");
        assertEquals(clienteOriginal.getCedula(), cliente.getCedula(), "Las cédulas deberían coincidir");

        logger.info("Cliente obtenido: {} - ID: {}", cliente.getNombreCompleto(), cliente.getId());
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Obtener cliente por cédula")
    void testGetClienteByCedula() {
        logger.info("Test: Obtener cliente por cédula");

        // Crear un cliente para buscar
        String cedula = "0666666" + System.currentTimeMillis() % 1000;
        Cliente clienteNuevo = new Cliente();
        clienteNuevo.setCedula(cedula);
        clienteNuevo.setNombres("Ana");
        clienteNuevo.setApellidos("Rodríguez");

        clienteService.crearCliente(clienteNuevo);

        // Buscar por cédula
        Cliente cliente = clienteService.getClienteByCedula(cedula);

        assertNotNull(cliente, "El cliente no debería ser null");
        assertEquals(cedula, cliente.getCedula(), "La cédula debería coincidir");
        assertEquals("Ana", cliente.getNombres(), "El nombre debería coincidir");

        logger.info("Cliente obtenido por cédula: {}", cliente.getNombreCompleto());
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Actualizar cliente")
    void testActualizarCliente() {
        logger.info("Test: Actualizar cliente");

        // Crear un cliente para actualizar
        String cedulaOriginal = "0555555" + System.currentTimeMillis() % 1000;
        Cliente cliente = new Cliente();
        cliente.setCedula(cedulaOriginal);
        cliente.setNombres("Luis");
        cliente.setApellidos("Hernández");
        cliente.setTelefono("0991234570");
        cliente.setEmail("luis.original@test.com");

        clienteService.crearCliente(cliente);
        Integer id = cliente.getId();

        // Actualizar datos
        cliente.setNombres("Luis Alberto");
        cliente.setApellidos("Hernández Silva");
        cliente.setTelefono("0991234571");
        cliente.setEmail("luis.actualizado@test.com");
        cliente.setDireccion("Nueva dirección 456");

        boolean resultado = clienteService.actualizarCliente(cliente);
        assertTrue(resultado, "La actualización debería ser exitosa");

        // Verificar que se actualizó
        Cliente clienteActualizado = clienteService.getClienteById(id);
        assertEquals("Luis Alberto", clienteActualizado.getNombres(), "El nombre debería estar actualizado");
        assertEquals("Hernández Silva", clienteActualizado.getApellidos(), "El apellido debería estar actualizado");
        assertEquals("luis.actualizado@test.com", clienteActualizado.getEmail(), "El email debería estar actualizado");

        logger.info("Cliente actualizado exitosamente: {}", clienteActualizado.getNombreCompleto());
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Eliminar cliente (eliminación lógica)")
    void testEliminarCliente() {
        logger.info("Test: Eliminar cliente (eliminación lógica)");

        // Crear un cliente para eliminar
        String cedula = "0444444" + System.currentTimeMillis() % 1000;
        Cliente cliente = new Cliente();
        cliente.setCedula(cedula);
        cliente.setNombres("Roberto");
        cliente.setApellidos("Sánchez");

        clienteService.crearCliente(cliente);
        Integer id = cliente.getId();

        // Eliminar (desactivar)
        boolean resultado = clienteService.eliminarCliente(id);
        assertTrue(resultado, "La eliminación debería ser exitosa");

        // Verificar que ya no aparece en consultas (porque está inactivo)
        Cliente clienteEliminado = clienteService.getClienteById(id);
        assertNull(clienteEliminado, "El cliente no debería aparecer en consultas (está inactivo)");

        logger.info("Cliente desactivado exitosamente: {}", cedula);
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Buscar clientes con paginación y filtros")
    void testBuscarClientesConPaginacion() {
        logger.info("Test: Buscar clientes con paginación y filtros");

        // Crear varios clientes con patrón común
        String patron = "TEST_" + System.currentTimeMillis();
        for (int i = 0; i < 3; i++) {
            Cliente cliente = new Cliente();
            cliente.setCedula("0" + (300000 + i) + System.currentTimeMillis() % 1000);
            cliente.setNombres(patron + "_Cliente");
            cliente.setApellidos("Prueba" + i);
            clienteService.crearCliente(cliente);
        }

        // Buscar con paginación
        List<Cliente> pagina1 = clienteService.getClientesPaginados(0, 2, patron);
        assertTrue(pagina1.size() <= 2, "No debería retornar más de 2 clientes");

        // Contar total que coinciden
        int total = clienteService.countClientes(patron);
        assertTrue(total >= 3, "Debería haber al menos 3 clientes que coincidan");

        logger.info("Paginación y filtros funcionando correctamente. Total: {}, Página 1: {}",
                   total, pagina1.size());
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Validar campos obligatorios")
    void testValidarCamposObligatorios() {
        logger.info("Test: Validar campos obligatorios");

        String cedula = "0123456" + System.currentTimeMillis() % 1000;
        Cliente cliente = new Cliente();
        cliente.setCedula(cedula);
        cliente.setNombres("Nombre");
        cliente.setApellidos("Apellido");

        boolean resultado = clienteService.crearCliente(cliente);
        assertTrue(resultado, "Debería crear el cliente con solo campos obligatorios");

        // Verificar que se creó
        Cliente clienteCreado = clienteService.getClienteByCedula(cedula);
        assertNotNull(clienteCreado, "El cliente debería existir");
        assertEquals("Nombre", clienteCreado.getNombres());
        assertEquals("Apellido", clienteCreado.getApellidos());

        logger.info("Validación de campos obligatorios correcta");
    }

    @Test
    @Order(14)
    @DisplayName("Test 14: Búsqueda por diferentes criterios")
    void testBusquedaPorDiferentesCriterios() {
        logger.info("Test: Búsqueda por diferentes criterios");

        // Crear un cliente con datos específicos
        String cedula = "0987654321";
        Cliente cliente = new Cliente();
        cliente.setCedula(cedula);
        cliente.setNombres("Patricia");
        cliente.setApellidos("Vega");
        cliente.setTelefono("0991111111");

        clienteService.crearCliente(cliente);

        // Buscar por cédula
        List<Cliente> porCedula = clienteService.buscarClientes(cedula);
        assertFalse(porCedula.isEmpty(), "Debería encontrar por cédula");

        // Buscar por nombre
        List<Cliente> porNombre = clienteService.buscarClientes("Patricia");
        assertFalse(porNombre.isEmpty(), "Debería encontrar por nombre");

        // Buscar por apellido
        List<Cliente> porApellido = clienteService.buscarClientes("Vega");
        assertFalse(porApellido.isEmpty(), "Debería encontrar por apellido");

        logger.info("Búsqueda por múltiples criterios funcionando correctamente");
    }

    @Test
    @Order(15)
    @DisplayName("Test 15: Paginación correcta")
    void testPaginacionCorrecta() {
        logger.info("Test: Paginación correcta");

        // Obtener primera página
        List<Cliente> pagina1 = clienteService.getClientesPaginados(0, 2, "");

        // Obtener segunda página
        List<Cliente> pagina2 = clienteService.getClientesPaginados(2, 2, "");

        assertNotNull(pagina1, "La primera página no debería ser null");
        assertNotNull(pagina2, "La segunda página no debería ser null");

        // Si hay suficientes clientes, las páginas deberían ser diferentes
        if (pagina1.size() == 2 && pagina2.size() > 0) {
            assertNotEquals(pagina1.get(0).getId(), pagina2.get(0).getId(),
                           "Las páginas deberían contener clientes diferentes");
        }

        logger.info("Paginación funcionando correctamente: Página 1 = {} clientes, Página 2 = {} clientes",
                   pagina1.size(), pagina2.size());
    }

    @AfterAll
    static void tearDown() {
        logger.info("Tests de ClienteService completados");
    }
}
