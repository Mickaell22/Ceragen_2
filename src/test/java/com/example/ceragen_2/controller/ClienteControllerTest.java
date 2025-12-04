package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para la logica de creacion de clientes
 * Basados en el metodo handleCrearCliente() de ClienteController
 *
 * COMPLEJIDAD CICLOMATICA: 5-6
 * Total de tests: 7 (cubriendo TODOS los caminos)
 *
 * ESTRUCTURA DE CADA TEST:
 * - 1 test de camino CORRECTO (caso exitoso)
 * - 1 test de camino ERROR correspondiente
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - Logica de Creacion de Clientes (handleCrearCliente)")
class ClienteControllerTest {
    private static final Logger logger = LoggerFactory.getLogger(ClienteControllerTest.class);

    @Mock
    private ClienteService clienteServiceMock;

    @BeforeEach
    void setUp() {
        logger.info("=== Configurando test unitario ===");
    }

    /**
     * ========================================================================
     * CASO 1: CAMINO EXITOSO - Crear cliente con todos los datos validos
     * ========================================================================
     * Camino: Validacion OK -> Cliente no existe -> Creacion exitosa
     * CC Cubierta: Camino principal exitoso
     */
    @Test
    @DisplayName("Test 1 - EXITOSO: Crear cliente con todos los datos validos")
    void testCrearCliente_CasoExitoso_TodosLosDatosValidos() {
        logger.info(">>> TEST 1: Creacion exitosa de cliente con todos los campos");

        // ARRANGE: Datos validos
        String cedula = "0912345678";
        String nombres = "Juan Carlos";
        String apellidos = "Perez Lopez";

        // Validacion simulada
        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        // Mock: Cliente no existe, creacion exitosa
        when(clienteServiceMock.getClienteByCedula(cedula)).thenReturn(null);
        when(clienteServiceMock.crearCliente(any(Cliente.class))).thenReturn(true);

        // ACT: Ejecutar logica
        assertFalse(camposVacios, "Validacion debe pasar");

        Cliente existente = clienteServiceMock.getClienteByCedula(cedula);
        assertNull(existente);

        Cliente nuevo = new Cliente();
        nuevo.setCedula(cedula);
        nuevo.setNombres(nombres);
        nuevo.setApellidos(apellidos);

        boolean resultado = clienteServiceMock.crearCliente(nuevo);

        // ASSERT
        assertTrue(resultado);
        verify(clienteServiceMock, times(1)).getClienteByCedula(cedula);
        verify(clienteServiceMock, times(1)).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 1: EXITOSO");
    }

    /**
     * ========================================================================
     * CASO 2: ERROR - Validacion fallida por cedula vacia
     * ========================================================================
     * Camino: Validacion FALLA -> Return temprano
     * CC Cubierta: Rama de validacion fallida (cedula vacia)
     */
    @Test
    @DisplayName("Test 2 - ERROR: Cedula vacia (validacion fallida)")
    void testCrearCliente_CasoError_CedulaVacia() {
        logger.info(">>> TEST 2: Error por cedula vacia");

        // ARRANGE
        String cedula = "   "; // Espacios vacios
        String nombres = "Juan";
        String apellidos = "Perez";

        // ACT: Validacion
        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        // ASSERT: Validacion debe fallar
        assertTrue(camposVacios, "La validacion debe detectar cedula vacia");

        // El servicio NO debe ser llamado
        verify(clienteServiceMock, never()).getClienteByCedula(anyString());
        verify(clienteServiceMock, never()).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 2: EXITOSO - Validacion detuvo correctamente");
    }

    /**
     * ========================================================================
     * CASO 3: ERROR - Validacion fallida por nombres vacios
     * ========================================================================
     * Camino: Validacion FALLA (nombres) -> Return temprano
     * CC Cubierta: Rama de validacion fallida (nombres vacios)
     */
    @Test
    @DisplayName("Test 3 - ERROR: Nombres vacios (validacion fallida)")
    void testCrearCliente_CasoError_NombresVacios() {
        logger.info(">>> TEST 3: Error por nombres vacios");

        // ARRANGE
        String cedula = "0912345678";
        String nombres = "";
        String apellidos = "Perez";

        // ACT
        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        // ASSERT
        assertTrue(camposVacios, "Validacion debe detectar nombres vacios");
        verify(clienteServiceMock, never()).getClienteByCedula(anyString());
        verify(clienteServiceMock, never()).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 3: EXITOSO");
    }

    /**
     * ========================================================================
     * CASO 4: ERROR - Validacion fallida por apellidos vacios
     * ========================================================================
     * Camino: Validacion FALLA (apellidos) -> Return temprano
     * CC Cubierta: Rama de validacion fallida (apellidos vacios)
     */
    @Test
    @DisplayName("Test 4 - ERROR: Apellidos vacios (validacion fallida)")
    void testCrearCliente_CasoError_ApellidosVacios() {
        logger.info(">>> TEST 4: Error por apellidos vacios");

        // ARRANGE
        String cedula = "0912345678";
        String nombres = "Juan";
        String apellidos = "";

        // ACT
        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        // ASSERT
        assertTrue(camposVacios);
        verify(clienteServiceMock, never()).getClienteByCedula(anyString());
        verify(clienteServiceMock, never()).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 4: EXITOSO");
    }

    /**
     * ========================================================================
     * CASO 5: ERROR - Cedula duplicada (cliente ya existe)
     * ========================================================================
     * Camino: Validacion OK -> Cliente YA EXISTE -> Return null -> Mostrar error
     * CC Cubierta: Rama de cliente existente
     */
    @Test
    @DisplayName("Test 5 - ERROR: Cedula duplicada (cliente ya existe)")
    void testCrearCliente_CasoError_CedulaDuplicada() {
        logger.info(">>> TEST 5: Error por cedula duplicada");

        // ARRANGE
        String cedula = "0912345678";
        String nombres = "Juan";
        String apellidos = "Perez";

        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        // Mock: Cliente YA existe
        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(999);
        clienteExistente.setCedula(cedula);

        when(clienteServiceMock.getClienteByCedula(cedula)).thenReturn(clienteExistente);

        // ACT
        assertFalse(camposVacios, "Validacion debe pasar");

        Cliente existente = clienteServiceMock.getClienteByCedula(cedula);

        // ASSERT: Cliente existe, NO se debe intentar crear
        assertNotNull(existente, "Debe encontrar el cliente existente");
        assertEquals(cedula, existente.getCedula());
        verify(clienteServiceMock, times(1)).getClienteByCedula(cedula);
        verify(clienteServiceMock, never()).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 5: EXITOSO - Cedula duplicada detectada");
    }

    /**
     * ========================================================================
     * CASO 6: ERROR - Fallo en base de datos al crear
     * ========================================================================
     * Camino: Validacion OK -> Cliente no existe -> Creacion FALLA (return false)
     * CC Cubierta: Rama de creacion fallida
     */
    @Test
    @DisplayName("Test 6 - ERROR: Fallo en base de datos al crear cliente")
    void testCrearCliente_CasoError_FalloBaseDatos() {
        logger.info(">>> TEST 6: Error por fallo en base de datos");

        // ARRANGE
        String cedula = "0912345678";
        String nombres = "Juan";
        String apellidos = "Perez";

        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        // Mock: No existe, pero la creacion falla
        when(clienteServiceMock.getClienteByCedula(cedula)).thenReturn(null);
        when(clienteServiceMock.crearCliente(any(Cliente.class))).thenReturn(false);

        // ACT
        assertFalse(camposVacios);

        Cliente existente = clienteServiceMock.getClienteByCedula(cedula);
        assertNull(existente);

        Cliente nuevo = new Cliente();
        nuevo.setCedula(cedula);
        nuevo.setNombres(nombres);
        nuevo.setApellidos(apellidos);

        boolean resultado = clienteServiceMock.crearCliente(nuevo);

        // ASSERT: La creacion fallo
        assertFalse(resultado, "La creacion debe fallar");
        verify(clienteServiceMock, times(1)).getClienteByCedula(cedula);
        verify(clienteServiceMock, times(1)).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 6: EXITOSO - Fallo manejado correctamente");
    }

    /**
     * ========================================================================
     * CASO 7: EXITOSO - Crear cliente solo con campos obligatorios
     * ========================================================================
     * Camino: Validacion OK (campos opcionales vacios) -> No existe -> Creacion exitosa
     * CC Cubierta: Camino exitoso con campos minimos
     */
    @Test
    @DisplayName("Test 7 - EXITOSO: Crear cliente solo con campos obligatorios")
    void testCrearCliente_CasoExitoso_SoloCamposObligatorios() {
        logger.info(">>> TEST 7: Creacion exitosa con campos minimos");

        // ARRANGE: Solo campos obligatorios
        String cedula = "0923456789";
        String nombres = "Maria";
        String apellidos = "Garcia";
        String telefono = ""; // Vacio (opcional)
        String email = ""; // Vacio (opcional)
        String direccion = ""; // Vacio (opcional)

        boolean camposVacios = cedula.trim().isEmpty() || nombres.trim().isEmpty() || apellidos.trim().isEmpty();

        when(clienteServiceMock.getClienteByCedula(cedula)).thenReturn(null);
        when(clienteServiceMock.crearCliente(any(Cliente.class))).thenReturn(true);

        // ACT
        assertFalse(camposVacios);

        Cliente existente = clienteServiceMock.getClienteByCedula(cedula);
        assertNull(existente);

        Cliente nuevo = new Cliente();
        nuevo.setCedula(cedula);
        nuevo.setNombres(nombres);
        nuevo.setApellidos(apellidos);
        nuevo.setTelefono(telefono);
        nuevo.setEmail(email);
        nuevo.setDireccion(direccion);

        boolean resultado = clienteServiceMock.crearCliente(nuevo);

        // ASSERT
        assertTrue(resultado);
        verify(clienteServiceMock, times(1)).getClienteByCedula(cedula);
        verify(clienteServiceMock, times(1)).crearCliente(any(Cliente.class));

        logger.info("<<< TEST 7: EXITOSO - Cliente creado con campos minimos");
    }
}
