package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Especialidad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadService {

    private static final Logger logger =
            LoggerFactory.getLogger(EspecialidadService.class);
    private static EspecialidadService instance;

    private EspecialidadService() {
    }

    public static EspecialidadService getInstance() {
        if (instance == null) {
            instance = new EspecialidadService();
        }
        return instance;
    }

    // =====================================================
    // MAPEO RESULTSET → ESPECIALIDAD
    // =====================================================
    private Especialidad mapRowToEspecialidad(final ResultSet rs)
            throws SQLException {

        Especialidad e = new Especialidad();
        e.setId(rs.getInt("id"));
        e.setNombre(rs.getString("nombre"));
        e.setCodigo(rs.getString("codigo"));
        e.setDescripcion(rs.getString("descripcion"));

        int duracion = rs.getInt("duracion_estandar_min");
        if (rs.wasNull()) {
            e.setDuracionEstandarMin(null);
        } else {
            e.setDuracionEstandarMin(duracion);
        }

        BigDecimal tarifa = rs.getBigDecimal("tarifa_base");
        e.setTarifaBase(tarifa);

        e.setEstado(rs.getString("estado"));

        int usuarioCreadorId = rs.getInt("usuario_creador_id");
        if (rs.wasNull()) {
            e.setUsuarioCreadorId(null);
        } else {
            e.setUsuarioCreadorId(usuarioCreadorId);
        }

        Timestamp ts = rs.getTimestamp("fecha_creacion");
        if (ts != null) {
            e.setFechaCreacion(ts.toLocalDateTime());
        }

        return e;
    }

    // =====================================================
    // CRUD
    // =====================================================

    /**
     * Obtiene todas las especialidades (para tabla / combos).
     */
    public List<Especialidad> getAllEspecialidades() {
        List<Especialidad> especialidades = new ArrayList<>();

        String sql = "SELECT id, nombre, codigo, descripcion, "
                + "       duracion_estandar_min, tarifa_base, "
                + "       estado, usuario_creador_id, fecha_creacion "
                + "FROM especialidades "
                + "ORDER BY nombre";

        try (Connection conn =
                     DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Especialidad e = mapRowToEspecialidad(rs);
                especialidades.add(e);
            }

            logger.info("Se obtuvieron {} especialidades",
                    especialidades.size());
        } catch (SQLException e) {
            logger.error("Error al obtener especialidades", e);
        }

        return especialidades;
    }

    /**
     * Obtiene una especialidad por ID.
     */
    public Especialidad getEspecialidadById(final Integer id) {
        String sql = "SELECT id, nombre, codigo, descripcion, "
                + "       duracion_estandar_min, tarifa_base, "
                + "       estado, usuario_creador_id, fecha_creacion "
                + "FROM especialidades "
                + "WHERE id = ?";

        try (Connection conn =
                     DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToEspecialidad(rs);
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener especialidad por ID", e);
        }

        return null;
    }

    /**
     * Crea una nueva especialidad.
     */
    public boolean crearEspecialidad(final Especialidad esp) {
        String sql = "INSERT INTO especialidades "
                + "  (nombre, codigo, descripcion, "
                + "   duracion_estandar_min, tarifa_base, "
                + "   estado, usuario_creador_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn =
                     DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sql,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            stmt.setString(1, esp.getNombre());
            stmt.setString(2, esp.getCodigo());
            stmt.setString(3, esp.getDescripcion());

            if (esp.getDuracionEstandarMin() != null) {
                stmt.setInt(4, esp.getDuracionEstandarMin());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            BigDecimal tarifa = esp.getTarifaBase();
            if (tarifa != null) {
                stmt.setBigDecimal(5, tarifa);
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }

            stmt.setString(6, esp.getEstado() != null
                    ? esp.getEstado()
                    : "ACTIVO");

            if (esp.getUsuarioCreadorId() != null) {
                stmt.setInt(7, esp.getUsuarioCreadorId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        esp.setId(rs.getInt(1));
                    }
                }
            }

            logger.info("Especialidad creada correctamente: {} ({})",
                    esp.getNombre(), esp.getCodigo());
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al crear especialidad", e);
            return false;
        }
    }

    /**
     * Actualiza una especialidad existente.
     */
    public boolean actualizarEspecialidad(final Especialidad esp) {
        String sql = "UPDATE especialidades SET "
                + "  nombre = ?, "
                + "  codigo = ?, "
                + "  descripcion = ?, "
                + "  duracion_estandar_min = ?, "
                + "  tarifa_base = ?, "
                + "  estado = ? "
                + "WHERE id = ?";

        try (Connection conn =
                     DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setString(1, esp.getNombre());
            stmt.setString(2, esp.getCodigo());
            stmt.setString(3, esp.getDescripcion());

            if (esp.getDuracionEstandarMin() != null) {
                stmt.setInt(4, esp.getDuracionEstandarMin());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }

            BigDecimal tarifa = esp.getTarifaBase();
            if (tarifa != null) {
                stmt.setBigDecimal(5, tarifa);
            } else {
                stmt.setNull(5, Types.DECIMAL);
            }

            stmt.setString(6, esp.getEstado() != null
                    ? esp.getEstado()
                    : "ACTIVO");

            stmt.setInt(7, esp.getId());

            int rows = stmt.executeUpdate();
            logger.info("Especialidad actualizada. id={}, filas={}",
                    esp.getId(), rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al actualizar especialidad", e);
            return false;
        }
    }

    /**
     * Elimina una especialidad por ID.
     */
    public boolean eliminarEspecialidad(final Integer id) {
        String sql = "DELETE FROM especialidades WHERE id = ?";

        try (Connection conn =
                     DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt =
                     conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            logger.info("Especialidad eliminada. id={}, filas={}",
                    id, rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al eliminar especialidad", e);
            return false;
        }
    }
}
