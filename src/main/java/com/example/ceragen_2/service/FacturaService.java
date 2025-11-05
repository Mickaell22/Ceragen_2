package com.example.ceragen_2.service;


import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

public class FacturaService {
    private static final Logger logger = LoggerFactory.getLogger(FacturaService.class);
    private static FacturaService instance;

    FacturaService(){
    }

    public static FacturaService getInstance(){
        if (instance == null) {
            instance = new FacturaService();
        }
        return instance;
    }

    /**
     * Genera un número de factura con formato FAC-000001, FAC-000002, etc.
     */
    private String generarNumeroFactura(Connection conn) throws SQLException {
        String sql = "SELECT IFNULL(MAX(id), 0) + 1 AS next_id FROM facturas";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                return String.format("FAC-%06d", nextId);
            }
        }
        return "FAC-000001";
    }

    /**
     * Crea una nueva factura y devuelve el id generado en la BD.
     * Retorna null si hubo un error.
     */
    public Integer crearFactura(int clienteId, String ciudad,
                                        double subtotal, double iva,
                                        double descuento, double total,
                                        String metodoPago, List<Cita> citas) {

        Connection conn = null;
        PreparedStatement stmtFactura = null;
        ResultSet generatedKeys = null;

        try {
            conn = DatabaseConfig.getInstance().getConnection();
            conn.setAutoCommit(false); // Iniciar transacción

            // 1. Crear la factura
            String sqlFactura = """
            INSERT INTO facturas
            (numero_factura, cliente_id, fecha_emision, ciudad, subtotal, iva, descuento, total, metodo_pago, estado)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, 'ACTIVA')
            """;

            String numeroFactura = generarNumeroFactura(conn);
            Timestamp fechaEmision = Timestamp.valueOf(LocalDateTime.now());

            stmtFactura = conn.prepareStatement(sqlFactura, Statement.RETURN_GENERATED_KEYS);
            stmtFactura.setString(1, numeroFactura);
            stmtFactura.setInt(2, clienteId);
            stmtFactura.setTimestamp(3, fechaEmision);
            stmtFactura.setString(4, ciudad);
            stmtFactura.setDouble(5, subtotal);
            stmtFactura.setDouble(6, iva);
            stmtFactura.setDouble(7, descuento);
            stmtFactura.setDouble(8, total);
            stmtFactura.setString(9, metodoPago);

            int rows = stmtFactura.executeUpdate();
            if (rows == 0) {
                logger.warn("No se insertó la factura.");
                conn.rollback();
                return null;
            }

            // Obtener el ID de la factura creada
            generatedKeys = stmtFactura.getGeneratedKeys();
            if (!generatedKeys.next()) {
                logger.warn("No se obtuvo el ID generado de la factura.");
                conn.rollback();
                return null;
            }

            int facturaId = generatedKeys.getInt(1);
            logger.info("Factura creada correctamente. ID: {}, Número: {}", facturaId, numeroFactura);

            // 2. Crear cada cita usando CitaService
            CitaService citaService = CitaService.getInstance();
            int citasCreadas = 0;

            for (Cita cita : citas) {
                // Usar el método actualizado de CitaService que incluye facturaId
                boolean citaCreada = citaService.crearCita(
                        cita.getPacienteId(),
                        cita.getProfesionalId(),
                        cita.getFechaHora(),
                        cita.getMotivo(),
                        cita.getCosto(),
                        facturaId  // Pasar el ID de la factura
                );

                if (!citaCreada) {
                    logger.error("Error al crear cita para paciente ID: {}", cita.getPacienteId());
                    conn.rollback();
                    return null;
                }

                citasCreadas++;
                logger.info("Cita {} creada exitosamente para factura ID: {}", citasCreadas, facturaId);
            }

            // Confirmar la transacción
            conn.commit();
            logger.info("Factura creada con {} citas asociadas", citasCreadas);
            return facturaId;

        } catch (SQLException e) {
            logger.error("Error al crear factura con citas", e);
            try {
                if (conn != null) {
                    conn.rollback();
                }
            } catch (SQLException ex) {
                logger.error("Error al hacer rollback", ex);
            }
            return null;
        } finally {
            try { if (generatedKeys != null) generatedKeys.close(); } catch (SQLException ignored) {}
            try { if (stmtFactura != null) stmtFactura.close(); } catch (SQLException ignored) {}
            try {
                if (conn != null) {
                    try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
                    conn.close();
                }
            } catch (SQLException ignored) {}
        }
    }
}
