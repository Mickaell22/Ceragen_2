package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Especialidad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadService {

    private static final Logger logger = LoggerFactory.getLogger(EspecialidadService.class);
    private static EspecialidadService instance;

    private EspecialidadService() {}

    public static EspecialidadService getInstance() {
        if (instance == null) {
            instance = new EspecialidadService();
        }
        return instance;
    }

    /**
     * Obtiene todas las especialidades (para tabla / combos).
     */
    public List<Especialidad> getAllEspecialidades() {
        List<Especialidad> especialidades = new ArrayList<>();

        String sql = "SELECT id, nombre, descripcion, costo_consulta " +
                "FROM especialidades " +
                "ORDER BY nombre";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Especialidad e = new Especialidad();
                e.setId(rs.getInt("id"));
                e.setNombre(rs.getString("nombre"));
                e.setDescripcion(rs.getString("descripcion"));
                e.setCostoConsulta(rs.getBigDecimal("costo_consulta"));

                especialidades.add(e);
            }

            logger.info("Se obtuvieron {} especialidades", especialidades.size());
        } catch (SQLException e) {
            logger.error("Error al obtener especialidades", e);
        }

        return especialidades;
    }

    /**
     * Obtiene una especialidad por ID.
     */
    public Especialidad getEspecialidadById(Integer id) {
        String sql = "SELECT id, nombre, descripcion, costo_consulta " +
                "FROM especialidades " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Especialidad e = new Especialidad();
                    e.setId(rs.getInt("id"));
                    e.setNombre(rs.getString("nombre"));
                    e.setDescripcion(rs.getString("descripcion"));
                    e.setCostoConsulta(rs.getBigDecimal("costo_consulta"));
                    return e;
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
    public boolean crearEspecialidad(Especialidad esp) {
        String sql = "INSERT INTO especialidades (nombre, descripcion, costo_consulta) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, esp.getNombre());
            stmt.setString(2, esp.getDescripcion());

            BigDecimal costo = esp.getCostoConsulta();
            if (costo != null) {
                stmt.setBigDecimal(3, costo);
            } else {
                stmt.setNull(3, Types.NUMERIC);
            }

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        esp.setId(rs.getInt(1));
                    }
                }
            }

            logger.info("Especialidad creada correctamente: {}", esp.getNombre());
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al crear especialidad", e);
            return false;
        }
    }

    /**
     * Actualiza una especialidad existente.
     */
    public boolean actualizarEspecialidad(Especialidad esp) {
        String sql = "UPDATE especialidades SET " +
                "nombre = ?, " +
                "descripcion = ?, " +
                "costo_consulta = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, esp.getNombre());
            stmt.setString(2, esp.getDescripcion());

            BigDecimal costo = esp.getCostoConsulta();
            if (costo != null) {
                stmt.setBigDecimal(3, costo);
            } else {
                stmt.setNull(3, Types.NUMERIC);
            }

            stmt.setInt(4, esp.getId());

            int rows = stmt.executeUpdate();
            logger.info("Especialidad actualizada. id={}, filas afectadas={}", esp.getId(), rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al actualizar especialidad", e);
            return false;
        }
    }

    /**
     * Elimina una especialidad por ID (borrado físico; si quieres lógico, cambia la columna).
     */
    public boolean eliminarEspecialidad(Integer id) {
        String sql = "DELETE FROM especialidades WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            logger.info("Especialidad eliminada. id={}, filas afectadas={}", id, rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al eliminar especialidad", e);
            return false;
        }
    }
}
