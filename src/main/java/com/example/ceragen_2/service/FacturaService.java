package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Cita;
import com.example.ceragen_2.model.DetalleFactura;
import com.example.ceragen_2.model.Factura;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

            // 2. Crear cada cita usando CitaService con la misma conexion (misma transaccion)
            CitaService citaService = CitaService.getInstance();
            int citasCreadas = 0;

            for (Cita cita : citas) {
                boolean citaCreada = citaService.crearCita(
                        conn,  // Pasar la misma conexión para mantener la transacción
                        cita.getPacienteId(),
                        cita.getProfesionalId(),
                        cita.getFechaHora(),
                        cita.getMotivo(),
                        cita.getCosto(),
                        facturaId
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

    /**
     * Obtiene todas las facturas con información resumida para el listado
     */
    public List<Factura> getAllFacturasResumen() {
        List<Factura> facturas = new ArrayList<>();
        // CORREGIDO: Usar CONCAT para unir nombres y apellidos
        String sql = """
            SELECT 
                f.id,
                f.numero_factura,
                f.fecha_emision,
                f.total,
                f.estado,
                CONCAT(c.nombres, ' ', c.apellidos) AS cliente_nombre
            FROM facturas f
            INNER JOIN clientes c ON f.cliente_id = c.id
            ORDER BY f.fecha_emision DESC
            """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Factura factura = new Factura();
                factura.setId(rs.getInt("id"));
                factura.setNumeroFactura(rs.getString("numero_factura"));

                Timestamp fechaEmision = rs.getTimestamp("fecha_emision");
                if (fechaEmision != null) {
                    factura.setFechaEmision(fechaEmision.toLocalDateTime());
                }

                factura.setTotal(rs.getDouble("total"));
                factura.setEstado(rs.getString("estado"));
                factura.setClienteNombre(rs.getString("cliente_nombre"));

                facturas.add(factura);
            }

            logger.info("Se obtuvieron {} facturas", facturas.size());

        } catch (SQLException e) {
            logger.error("Error al obtener todas las facturas", e);
        }

        return facturas;
    }

    /**
     * Obtiene facturas filtradas por estado
     */
    public List<Factura> getFacturasPorEstado(String estado) {
        List<Factura> facturas = new ArrayList<>();
        // CORREGIDO: Usar CONCAT para unir nombres y apellidos
        String sql = """
            SELECT 
                f.id,
                f.numero_factura,
                f.fecha_emision,
                f.total,
                f.estado,
                CONCAT(c.nombres, ' ', c.apellidos) AS cliente_nombre
            FROM facturas f
            INNER JOIN clientes c ON f.cliente_id = c.id
            WHERE f.estado = ?
            ORDER BY f.fecha_emision DESC
            """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, estado);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Factura factura = new Factura();
                    factura.setId(rs.getInt("id"));
                    factura.setNumeroFactura(rs.getString("numero_factura"));

                    Timestamp fechaEmision = rs.getTimestamp("fecha_emision");
                    if (fechaEmision != null) {
                        factura.setFechaEmision(fechaEmision.toLocalDateTime());
                    }

                    factura.setTotal(rs.getDouble("total"));
                    factura.setEstado(rs.getString("estado"));
                    factura.setClienteNombre(rs.getString("cliente_nombre"));

                    facturas.add(factura);
                }
            }

            logger.info("Se obtuvieron {} facturas con estado: {}", facturas.size(), estado);

        } catch (SQLException e) {
            logger.error("Error al obtener facturas por estado: {}", estado, e);
        }

        return facturas;
    }

    /**
     * Anula una factura cambiando su estado a 'ANULADA'
     */
    public boolean anularFactura(Integer facturaId) {
        String sql = "UPDATE facturas SET estado = 'ANULADA' WHERE id = ? AND estado = 'ACTIVA'";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, facturaId);
            int rowsAffected = stmt.executeUpdate();

            boolean exito = rowsAffected > 0;
            if (exito) {
                logger.info("Factura anulada exitosamente. ID: {}", facturaId);
            } else {
                logger.warn("No se pudo anular la factura. ID: {} (posiblemente ya estaba anulada o no existe)", facturaId);
            }

            return exito;

        } catch (SQLException e) {
            logger.error("Error al anular factura ID: {}", facturaId, e);
            return false;
        }
    }

    /**
     * Obtiene una factura completa por su ID (incluyendo detalles)
     */
    public Factura getFacturaById(Integer facturaId) {
        String sql = """
        SELECT 
            f.id,
            f.numero_factura,
            f.cliente_id,
            f.fecha_emision,
            f.ciudad,
            f.subtotal,
            f.iva,
            f.descuento,
            f.total,
            f.metodo_pago,
            f.estado,
            CONCAT(c.nombres, ' ', c.apellidos) AS cliente_nombre,
            c.cedula,
            c.telefono,
            c.direccion
        FROM facturas f
        INNER JOIN clientes c ON f.cliente_id = c.id
        WHERE f.id = ?
        """;

        Factura factura = null;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, facturaId);

            String numeroFactura = null, ciudad = null, metodoPago = null,
                    estado = null, clienteNombre = null, cedula = null,
                    telefono = null, direccion = null;
            int clienteId = 0;
            double subtotal = 0, iva = 0, descuento = 0, total = 0;
            LocalDateTime fechaEmision = null;

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    numeroFactura = rs.getString("numero_factura");
                    clienteId = rs.getInt("cliente_id");
                    Timestamp ts = rs.getTimestamp("fecha_emision");
                    if (ts != null) fechaEmision = ts.toLocalDateTime();
                    ciudad = rs.getString("ciudad");
                    subtotal = rs.getDouble("subtotal");
                    iva = rs.getDouble("iva");
                    descuento = rs.getDouble("descuento");
                    total = rs.getDouble("total");
                    metodoPago = rs.getString("metodo_pago");
                    estado = rs.getString("estado");
                    clienteNombre = rs.getString("cliente_nombre");
                    cedula = rs.getString("cedula");
                    telefono = rs.getString("telefono");
                    direccion = rs.getString("direccion");
                }
            }

            if (numeroFactura != null) {
                factura = new Factura();
                factura.setId(facturaId);
                factura.setNumeroFactura(numeroFactura);
                factura.setClienteId(clienteId);
                factura.setFechaEmision(fechaEmision);
                factura.setCiudad(ciudad);
                factura.setSubtotal(subtotal);
                factura.setIva(iva);
                factura.setDescuento(descuento);
                factura.setTotal(total);
                factura.setMetodoPago(metodoPago);
                factura.setEstado(estado);
                factura.setClienteNombre(clienteNombre);
                //faltan los campos del cliente

                // 👇 Ahora sí, después de cerrar el ResultSet
                List<Cita> citas = getCitasPorFactura(facturaId);
                factura.setDetalles(citas);

                logger.info(factura.toString());
                logger.info("Factura encontrada. ID: {}, Número: {}", facturaId, numeroFactura);
            }

        } catch (SQLException e) {
            logger.error("Error al obtener factura por ID: {}", facturaId, e);
        }

        if (factura == null) {
            logger.warn("Factura no encontrada. ID: {}", facturaId);
        }

        return factura;
    }


    /**
     * Obtiene los detalles de una factura (citas asociadas)
     */
    public List<Cita> getCitasPorFactura(Integer facturaId) {
        List<Cita> citas = new ArrayList<>();
        String sql = """
            SELECT 
                c.id,
                c.paciente_id,
                c.profesional_id,
                c.fecha_hora,
                c.motivo,
                c.costo,
                c.estado,
                CONCAT(p.nombres, ' ', p.apellidos) AS paciente_nombre,
                CONCAT(prof.nombres, ' ', prof.apellidos) AS profesional_nombre
            FROM citas c
            INNER JOIN pacientes p ON c.paciente_id = p.id
            INNER JOIN profesionales prof ON c.profesional_id = prof.id
            WHERE c.factura_id = ?
            ORDER BY c.fecha_hora
            """;

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, facturaId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Cita cita = new Cita();
                    cita.setId(rs.getInt("id"));
                    cita.setPacienteId(rs.getInt("paciente_id"));
                    cita.setProfesionalId(rs.getInt("profesional_id"));

                    Timestamp fechaHora = rs.getTimestamp("fecha_hora");
                    if (fechaHora != null) {
                        cita.setFechaHora(fechaHora.toLocalDateTime());
                    }

                    cita.setMotivo(rs.getString("motivo"));
                    cita.setCosto(rs.getBigDecimal("costo"));
                    cita.setEstado(rs.getString("estado"));
                    cita.setPacienteNombre(rs.getString("paciente_nombre"));
                    cita.setProfesionalNombre(rs.getString("profesional_nombre"));

                    citas.add(cita);
                }
            }

            logger.info("Se obtuvieron {} citas para factura ID: {}", citas.size(), facturaId);

        } catch (SQLException e) {
            logger.error("Error al obtener citas por factura ID: {}", facturaId, e);
        }

        return citas;
    }
}