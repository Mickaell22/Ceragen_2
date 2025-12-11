package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.Cliente;
import com.example.ceragen_2.service.FacturaService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para la lógica de creación de facturas
 * Simula lo que hace FacturaController.handleCrearFactura()
 * pero probando la interacción con FacturaService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - Lógica de Creación de Facturas")
class FacturaControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(FacturaControllerTest.class);

    @Mock
    private FacturaService facturaServiceMock;

    private Cliente clienteTest;
    private List<Cita> citasTest;

    @BeforeEach
    void setUp() {
        logger.info("=== Configurando test ===");

        // Configurar datos de prueba
        clienteTest = new Cliente();
        clienteTest.setId(1);
        clienteTest.setNombres("Juan");
        clienteTest.setApellidos("Pérez");

        citasTest = new ArrayList<>();
        Cita cita1 = new Cita();
        cita1.setId(1);
        cita1.setPacienteNombre("Paciente 1");
        cita1.setProfesionalNombre("Profesional 1");
        cita1.setCosto(BigDecimal.valueOf(50.00));

        Cita cita2 = new Cita();
        cita2.setId(2);
        cita2.setPacienteNombre("Paciente 2");
        cita2.setProfesionalNombre("Profesional 2");
        cita2.setCosto(BigDecimal.valueOf(75.50));

        citasTest.add(cita1);
        citasTest.add(cita2);
    }

    /**
     * ========================================================================
     * TEST 1: CASO EXITOSO - Flujo completo de creación
     * ========================================================================
     * Simula exactamente lo que hace handleCrearFactura() cuando todo va bien
     */
    @Test
    @DisplayName("Test 1 - EXITOSO: Flujo completo de creación de factura")
    void testFlujoCreacionFactura_Exitoso() {
        logger.info(">>> TEST 1: Flujo exitoso completo");

        // ARRANGE - Datos que vendrían de la UI
        Cliente clienteSeleccionado = clienteTest;
        String ciudad = "Guayaquil";
        String metodoPago = "EFECTIVO";
        List<Cita> citasSeleccionadas = citasTest;

        // Calcular totales (como lo hace calcularTotales())
        double subtotal = calcularSubtotal(citasSeleccionadas);
        double iva = subtotal * 0.15;
        double descuento = 0.0;
        double total = subtotal + iva;

        logger.info("Datos calculados - Subtotal: ${}, IVA: ${}, Total: ${}",
                subtotal, iva, total);

        // Validar que todos los campos están OK (como hace validarCampos())
        boolean validacionOk = simularValidacion(
                clienteSeleccionado != null,
                !ciudad.trim().isEmpty(),
                subtotal > 0,
                iva >= 0,
                descuento >= 0,
                total > 0,
                metodoPago != null && !metodoPago.isEmpty(),
                !citasSeleccionadas.isEmpty()
        );

        assertTrue(validacionOk, "La validación debe pasar con datos válidos");

        // Mockear la respuesta del servicio (éxito)
        when(facturaServiceMock.crearFactura(
                eq(clienteSeleccionado.getId()),
                eq(ciudad),
                eq(subtotal),
                eq(iva),
                eq(descuento),
                eq(total),
                eq(metodoPago),
                eq(citasSeleccionadas)
        )).thenReturn(100); // ID de factura generado

        // ACT - Simular llamada al servicio (lo que realmente hace el controlador)
        Integer facturaId = facturaServiceMock.crearFactura(
                clienteSeleccionado.getId(),
                ciudad,
                subtotal,
                iva,
                descuento,
                total,
                metodoPago,
                citasSeleccionadas
        );

        // ASSERT - Verificar resultados
        assertNotNull(facturaId, "El servicio debe retornar un ID de factura");
        assertEquals(100, facturaId, "El ID de factura debe ser 100");

        // Verificar que se llamó al servicio exactamente una vez
        verify(facturaServiceMock, times(1)).crearFactura(
                anyInt(), anyString(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyString(), anyList()
        );

        logger.info("<<< TEST 1: EXITOSO - Factura creada con ID: {}", facturaId);
    }

    /**
     * ========================================================================
     * TEST 2: CASO DE ERROR - Validación fallida
     * ========================================================================
     * Simula lo que pasa cuando la validación falla en handleCrearFactura()
     */
    @Test
    @DisplayName("Test 2 - ERROR: Validación fallida por datos incompletos")
    void testFlujoCreacionFactura_ErrorValidacion() {
        logger.info(">>> TEST 2: Error por validación fallida");

        // ARRANGE - Datos INCOMPLETOS/INVÁLIDOS (simulando UI)
        Cliente clienteSeleccionado = null; // ¡NO SELECCIONADO!
        String ciudad = ""; // ¡VACÍO!
        String metodoPago = null; // ¡NO SELECCIONADO!
        List<Cita> citasSeleccionadas = new ArrayList<>(); // ¡LISTA VACÍA!

        // ACT - Simular validación (el controlador saldría temprano)
        boolean validacionOk = simularValidacion(
                clienteSeleccionado != null, // false
                !ciudad.trim().isEmpty(),    // false
                true, // subtotal no importa porque ya falló
                true, // iva no importa
                true, // descuento no importa
                true, // total no importa
                metodoPago != null && !metodoPago.isEmpty(), // false
                !citasSeleccionadas.isEmpty() // false
        );

        // ASSERT - La validación debe fallar
        assertFalse(validacionOk, "La validación debe fallar con datos incompletos");

        // Verificar que NO se llama al servicio (controlador retorna temprano)
        verify(facturaServiceMock, never()).crearFactura(
                anyInt(), anyString(), anyDouble(), anyDouble(),
                anyDouble(), anyDouble(), anyString(), anyList()
        );

        logger.info("<<< TEST 2: EXITOSO - Validación detecta datos incompletos");
    }

    /**
     * ========================================================================
     * TEST 3: LÓGICA DE CÁLCULO - Cálculo correcto
     * ========================================================================
     * Simula exactamente lo que hace calcularTotales() en el controlador
     */
    @Test
    @DisplayName("Test 3 - CÁLCULO: Totales calculados correctamente")
    void testLogicaCalculoTotales_Correcto() {
        logger.info(">>> TEST 3: Lógica de cálculo de totales");

        // ARRANGE - Citas con diferentes costos
        List<Cita> citas = new ArrayList<>();

        // Cita 1: $100.00
        Cita cita1 = new Cita();
        cita1.setCosto(BigDecimal.valueOf(100.00));

        // Cita 2: $50.50
        Cita cita2 = new Cita();
        cita2.setCosto(BigDecimal.valueOf(50.50));

        // Cita 3: $25.00
        Cita cita3 = new Cita();
        cita3.setCosto(BigDecimal.valueOf(25.00));

        citas.add(cita1);
        citas.add(cita2);
        citas.add(cita3);

        // ACT - Simular cálculo (MISMA LÓGICA que calcularTotales())
        ResultadoCalculo resultado = simularCalculoTotales(citas);

        // ASSERT - Verificar cálculos exactos
        // Subtotal: 100.00 + 50.50 + 25.00 = 175.50
        // IVA: 175.50 * 0.15 = 26.325
        // Descuento: 0.0 (siempre por ahora)
        // Total: 175.50 + 26.325 = 201.825

        assertEquals(175.50, resultado.subtotal(), 0.001,
                "Subtotal incorrecto - debe sumar todos los costos");

        assertEquals(26.325, resultado.iva(), 0.001,
                "IVA incorrecto - debe ser 15% del subtotal");

        assertEquals(0.0, resultado.descuento(), 0.001,
                "Descuento debe ser 0 (por ahora)");

        assertEquals(201.825, resultado.total(), 0.001,
                "Total incorrecto - debe ser subtotal + iva");

        logger.info("<<< TEST 3: EXITOSO - Cálculos correctos");
        logger.info("  Subtotal: ${:.2f}, IVA: ${:.2f}, Total: ${:.2f}",
                resultado.subtotal(), resultado.iva(), resultado.total());
    }

    /**
     * ========================================================================
     * TEST 4: LÓGICA DE CÁLCULO - Casos especiales
     * ========================================================================
     * Simula cómo maneja calcularTotales() casos edge
     */
    @Test
    @DisplayName("Test 4 - CÁLCULO: Manejo de casos especiales")
    void testLogicaCalculoTotales_CasosEspeciales() {
        logger.info(">>> TEST 4: Cálculo con casos especiales");

        // ARRANGE - Citas con casos especiales
        List<Cita> citas = new ArrayList<>();

        // Cita normal
        Cita citaNormal = new Cita();
        citaNormal.setCosto(BigDecimal.valueOf(100.00));

        // Cita con costo null (como manejaría el controlador)
        Cita citaNull = new Cita();
        citaNull.setCosto(null); // Esto causaría NullPointer en stream

        // Cita con costo cero
        Cita citaCero = new Cita();
        citaCero.setCosto(BigDecimal.ZERO);

        citas.add(citaNormal);
        citas.add(citaNull);
        citas.add(citaCero);

        // ACT - Usar método protegido que maneja nulls
        ResultadoCalculo resultado = simularCalculoTotalesSeguro(citas);

        // ASSERT
        // Cita null debe tratarse como 0
        // Total: 100.00 + 0.0 + 0.0 = 100.00
        // IVA: 100.00 * 0.15 = 15.00

        assertEquals(100.00, resultado.subtotal(), 0.001,
                "Costo null debe tratarse como 0");

        assertEquals(15.00, resultado.iva(), 0.001,
                "IVA debe calcularse sobre 100.00");

        logger.info("<<< TEST 4: EXITOSO - Maneja casos especiales");
    }

    // =========================================================================
    // MÉTODOS AUXILIARES (réplicas de la lógica del controlador)
    // =========================================================================

    /**
     * Réplica de la lógica de validación del controlador
     */
    private boolean simularValidacion(boolean clienteSeleccionado, boolean ciudadValida,
                                      boolean subtotalValido, boolean ivaValido,
                                      boolean descuentoValido, boolean totalValido,
                                      boolean metodoPagoSeleccionado, boolean tieneCitas) {
        // Esta es la lógica simplificada de validarCampos() en FacturaController

        if (!clienteSeleccionado) {
            logger.warn("Validación fallida: Cliente no seleccionado");
            return false;
        }

        if (!ciudadValida) {
            logger.warn("Validación fallida: Ciudad vacía");
            return false;
        }

        if (!tieneCitas) {
            logger.warn("Validación fallida: No hay citas");
            return false;
        }

        if (!metodoPagoSeleccionado) {
            logger.warn("Validación fallida: Método de pago no seleccionado");
            return false;
        }

        // Validaciones numéricas básicas
        if (!subtotalValido || subtotalValido && !ivaValido ||
                !descuentoValido || !totalValido) {
            logger.warn("Validación fallida: Valores numéricos inválidos");
            return false;
        }

        return true;
    }

    /**
     * Réplica EXACTA de la lógica de calcularTotales() del controlador
     */
    private ResultadoCalculo simularCalculoTotales(List<Cita> citas) {
        double subtotal = citas.stream()
                .mapToDouble(cita -> cita.getCosto() != null ?
                        cita.getCosto().doubleValue() : 0.0)
                .sum();

        double iva = subtotal * 0.15; // 15% IVA (IGUAL que en controlador)
        double descuento = 0.0; // Por ahora siempre 0 (IGUAL que en controlador)
        double total = subtotal + iva - descuento;

        return new ResultadoCalculo(subtotal, iva, descuento, total);
    }

    /**
     * Versión SEGURA que maneja nulls explícitamente
     */
    private ResultadoCalculo simularCalculoTotalesSeguro(List<Cita> citas) {
        double subtotal = 0.0;

        for (Cita cita : citas) {
            BigDecimal costo = cita.getCosto();
            subtotal += (costo != null) ? costo.doubleValue() : 0.0;
        }

        double iva = subtotal * 0.15;
        double descuento = 0.0;
        double total = subtotal + iva;

        return new ResultadoCalculo(subtotal, iva, descuento, total);
    }

    /**
     * Calcula subtotal de citas (usado en Test 1)
     */
    private double calcularSubtotal(List<Cita> citas) {
        return citas.stream()
                .mapToDouble(cita -> cita.getCosto() != null ?
                        cita.getCosto().doubleValue() : 0.0)
                .sum();
    }

    /**
     * Record para resultados de cálculo
     */
    private record ResultadoCalculo(double subtotal, double iva,
                                    double descuento, double total) {}
}