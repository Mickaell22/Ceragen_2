package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteService {
    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);
    private static ClienteService instance;

    private ClienteService() {
    }

    public static ClienteService getInstance() {
        if (instance == null) {
            instance = new ClienteService();
        }
        return instance;
    }

    /**
     * Obtiene todos los clientes para ComboBox (solo información básica)
     */
    public List<Cliente> getAllClientes() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, cedula, nombres, apellidos FROM clientes WHERE activo = TRUE ORDER BY nombres, apellidos";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setId(rs.getInt("id"));
                cliente.setCedula(rs.getString("cedula"));
                cliente.setNombres(rs.getString("nombres"));
                cliente.setApellidos(rs.getString("apellidos"));
                clientes.add(cliente);
            }

            logger.info("Se obtuvieron {} clientes", clientes.size());
        } catch (SQLException e) {
            logger.error("Error al obtener clientes", e);
        }

        return clientes;
    }

    /**
     * Obtiene todos los clientes con información completa
     */
    public List<Cliente> getAllClientesCompletos() {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, cedula, nombres, apellidos, telefono, email, direccion, activo, fecha_registro " +
                     "FROM clientes WHERE activo = TRUE ORDER BY nombres, apellidos";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Cliente cliente = mapResultSetToCliente(rs);
                clientes.add(cliente);
            }

            logger.info("Se obtuvieron {} clientes completos", clientes.size());
        } catch (SQLException e) {
            logger.error("Error al obtener clientes completos", e);
        }

        return clientes;
    }

    /**
     * Obtiene clientes con paginación y filtros
     */
    public List<Cliente> getClientesPaginados(int offset, int limit, String searchText) {
        List<Cliente> clientes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT id, cedula, nombres, apellidos, telefono, email, direccion, activo, fecha_registro " +
            "FROM clientes WHERE activo = TRUE"
        );

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?)");
        }

        sql.append(" ORDER BY fecha_registro DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
                stmt.setString(paramIndex++, searchPattern);
            }

            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente cliente = mapResultSetToCliente(rs);
                clientes.add(cliente);
            }

            logger.info("Se obtuvieron {} clientes paginados", clientes.size());
        } catch (SQLException e) {
            logger.error("Error al obtener clientes paginados", e);
        }

        return clientes;
    }

    /**
     * Cuenta el total de clientes con filtros aplicados
     */
    public int countClientes(String searchText) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM clientes WHERE activo = TRUE");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?)");
        }

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            if (searchText != null && !searchText.trim().isEmpty()) {
                String searchPattern = "%" + searchText + "%";
                stmt.setString(1, searchPattern);
                stmt.setString(2, searchPattern);
                stmt.setString(3, searchPattern);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error al contar clientes", e);
        }

        return 0;
    }

    /**
     * Obtiene un cliente por ID
     */
    public Cliente getClienteById(Integer id) {
        String sql = "SELECT id, cedula, nombres, apellidos, telefono, email, direccion, activo, fecha_registro " +
                     "FROM clientes WHERE id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                logger.info("Cliente encontrado con ID: {}", id);
                return mapResultSetToCliente(rs);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener cliente por ID", e);
        }

        return null;
    }

    /**
     * Obtiene un cliente por cedula
     */
    public Cliente getClienteByCedula(String cedula) {
        String sql = "SELECT id, cedula, nombres, apellidos, telefono, email, direccion, activo, fecha_registro " +
                     "FROM clientes WHERE cedula = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                logger.info("Cliente encontrado con cedula: {}", cedula);
                return mapResultSetToCliente(rs);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener cliente por cedula", e);
        }

        return null;
    }

    /**
     * Crea un nuevo cliente
     */
    public boolean crearCliente(Cliente cliente) {
        String sql = "INSERT INTO clientes (cedula, nombres, apellidos, telefono, email, direccion) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, cliente.getCedula());
            stmt.setString(2, cliente.getNombres());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getEmail());
            stmt.setString(6, cliente.getDireccion());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        cliente.setId(generatedKeys.getInt(1));
                    }
                }
                logger.info("Cliente creado exitosamente: {}", cliente.getNombreCompleto());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al crear cliente", e);
        }

        return false;
    }

    /**
     * Actualiza un cliente existente
     */
    public boolean actualizarCliente(Cliente cliente) {
        String sql = "UPDATE clientes SET cedula = ?, nombres = ?, apellidos = ?, " +
                     "telefono = ?, email = ?, direccion = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cliente.getCedula());
            stmt.setString(2, cliente.getNombres());
            stmt.setString(3, cliente.getApellidos());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getEmail());
            stmt.setString(6, cliente.getDireccion());
            stmt.setInt(7, cliente.getId());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Cliente actualizado exitosamente: {}", cliente.getNombreCompleto());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar cliente", e);
        }

        return false;
    }

    /**
     * Elimina un cliente por ID (eliminación lógica)
     */
    public boolean eliminarCliente(Integer id) {
        String sql = "UPDATE clientes SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows > 0) {
                logger.info("Cliente desactivado exitosamente con ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al desactivar cliente", e);
        }

        return false;
    }

    /**
     * Busca clientes por nombre o cedula
     */
    public List<Cliente> buscarClientes(String criterio) {
        List<Cliente> clientes = new ArrayList<>();
        String sql = "SELECT id, cedula, nombres, apellidos, telefono, email, direccion, activo, fecha_registro " +
                     "FROM clientes WHERE activo = TRUE AND (cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?) " +
                     "ORDER BY nombres, apellidos";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + criterio + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cliente cliente = mapResultSetToCliente(rs);
                clientes.add(cliente);
            }

            logger.info("Se encontraron {} clientes con el criterio: {}", clientes.size(), criterio);
        } catch (SQLException e) {
            logger.error("Error al buscar clientes", e);
        }

        return clientes;
    }

    /**
     * Mapea un ResultSet a un objeto Cliente
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        cliente.setCedula(rs.getString("cedula"));
        cliente.setNombres(rs.getString("nombres"));
        cliente.setApellidos(rs.getString("apellidos"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        cliente.setDireccion(rs.getString("direccion"));

        // Manejo opcional de activo si existe en la base de datos
        try {
            cliente.setActivo(rs.getBoolean("activo"));
        } catch (SQLException e) {
            // Si la columna no existe, asumimos activo por defecto
            cliente.setActivo(true);
        }

        // Manejo opcional de fecha_registro si existe en la base de datos
        try {
            Timestamp timestamp = rs.getTimestamp("fecha_registro");
            if (timestamp != null) {
                cliente.setFechaRegistro(timestamp.toLocalDateTime());
            }
        } catch (SQLException e) {
            // Si la columna no existe, simplemente continuamos
            logger.debug("Columna fecha_registro no encontrada, continuando...");
        }

        return cliente;
    }
}