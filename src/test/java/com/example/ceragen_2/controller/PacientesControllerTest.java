package com.example.ceragen_2.controller;

import com.example.ceragen_2.model.DocumentoPaciente;
import com.example.ceragen_2.model.Paciente;
import com.example.ceragen_2.service.DocumentoPacienteService;
import com.example.ceragen_2.service.PacienteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests unitarios sencillos para el módulo de Pacientes.
 *
 * NOTA: igual que en ClienteControllerTest, aquí se prueban principalmente
 * las interacciones con los servicios usando mocks, de forma que los tests
 * puedan ejecutarse en consola/IntelliJ sin necesidad de levantar JavaFX.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Tests Unitarios - Modulo Pacientes")
class PacientesControllerTest {

    private static final Logger logger = LoggerFactory.getLogger(PacientesControllerTest.class);

    @Mock
    private PacienteService pacienteServiceMock;

    @Mock
    private DocumentoPacienteService documentoPacienteServiceMock;

    // =====================================================================
    // Tests para la lógica de ELIMINAR PACIENTE (camino exitoso / erróneo)
    // =====================================================================

    @Test
    @DisplayName("Metodo eliminarPaciente - Camino exitoso")
    void testEliminarPaciente_CaminoExitoso() {
        logger.info("TEST eliminarPaciente - camino exitoso");

        // Arrange
        Paciente paciente = new Paciente();
        paciente.setId(10);

        when(pacienteServiceMock.eliminarPaciente(paciente.getId()))
                .thenReturn(true);

        // Act
        boolean exito = pacienteServiceMock.eliminarPaciente(paciente.getId());

        // Assert
        assertTrue(exito, "La eliminación del paciente debe ser exitosa");
        verify(pacienteServiceMock, times(1)).eliminarPaciente(paciente.getId());
    }

    @Test
    @DisplayName("Metodo eliminarPaciente - Camino erróneo")
    void testEliminarPaciente_CaminoErroneo() {
        logger.info("TEST eliminarPaciente - camino erróneo");

        // Arrange
        Paciente paciente = new Paciente();
        paciente.setId(20);

        when(pacienteServiceMock.eliminarPaciente(paciente.getId()))
                .thenReturn(false);

        // Act
        boolean exito = pacienteServiceMock.eliminarPaciente(paciente.getId());

        // Assert
        assertFalse(exito, "La eliminación debe fallar para este caso de prueba");
        verify(pacienteServiceMock, times(1)).eliminarPaciente(paciente.getId());
    }

    // =====================================================================
    // Tests para la lógica de SUBIR DOCUMENTO (camino exitoso / erróneo)
    // =====================================================================

    @Test
    @DisplayName("Metodo handleSubirDocumento - Camino correcto (crearDocumento devuelve true)")
    void testHandleSubirDocumento_CaminoCorrecto() {
        logger.info("TEST handleSubirDocumento - camino correcto");

        // Arrange
        DocumentoPaciente doc = new DocumentoPaciente();
        doc.setPacienteId(1);
        doc.setNombreArchivo("informe.pdf");
        doc.setTipoDocumento("EXAMEN");
        doc.setRutaArchivo("C:/tmp/informe.pdf");

        when(documentoPacienteServiceMock.crearDocumento(any(DocumentoPaciente.class)))
                .thenReturn(true);

        // Act
        boolean ok = documentoPacienteServiceMock.crearDocumento(doc);

        // Assert
        assertTrue(ok, "El registro del documento debe ser exitoso");
        verify(documentoPacienteServiceMock, times(1)).crearDocumento(any(DocumentoPaciente.class));
    }

    @Test
    @DisplayName("Metodo handleSubirDocumento - Camino erróneo (crearDocumento devuelve false)")
    void testHandleSubirDocumento_CaminoErroneo() {
        logger.info("TEST handleSubirDocumento - camino erróneo");

        // Arrange
        DocumentoPaciente doc = new DocumentoPaciente();
        doc.setPacienteId(2);
        doc.setNombreArchivo("archivo_corrupto.pdf");
        doc.setTipoDocumento("HISTORIA_CLINICA");
        doc.setRutaArchivo("C:/tmp/archivo_corrupto.pdf");

        when(documentoPacienteServiceMock.crearDocumento(any(DocumentoPaciente.class)))
                .thenReturn(false);

        // Act
        boolean ok = documentoPacienteServiceMock.crearDocumento(doc);

        // Assert
        assertFalse(ok, "El registro del documento debe fallar en este escenario");
        verify(documentoPacienteServiceMock, times(1)).crearDocumento(any(DocumentoPaciente.class));
    }
}
