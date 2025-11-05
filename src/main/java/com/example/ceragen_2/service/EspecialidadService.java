package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Especialidad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EspecialidadService {
    private static final Logger logger = LoggerFactory.getLogger(EspecialidadService.class);
    private static EspecialidadService instance;

    private EspecialidadService() {
    }

    public static EspecialidadService getInstance() {
        if (instance == null) {
            instance = new EspecialidadService();
        }
        return instance;
    }

    /**
     * Obtiene todas las especialidades
     */
    public List<Especialidad> getAllEspecialidades() {
        List<Especialidad> especialidades = new ArrayList<>();
        String sql = "SELECT id, nombre, descripcion, costo_consulta FROM especialidades ORDER BY nombre";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Especialidad especialidad = new Especialidad();
                especialidad.setId(rs.getInt("id"));
                especialidad.setNombre(rs.getString("nombre"));
                especialidad.setDescripcion(rs.getString("descripcion"));
                especialidad.setCostoConsulta(rs.getBigDecimal("costo_consulta"));
                especialidades.add(especialidad);
            }

            logger.info("Se obtuvieron {} especialidades", especialidades.size());
        } catch (SQLException e) {
            logger.error("Error al obtener especialidades", e);
        }

        return especialidades;
    }

    /**
     * Obtiene una especialidad por ID
     */
    public Especialidad getEspecialidadById(Integer id) {
        String sql = "SELECT id, nombre, descripcion, costo_consulta FROM especialidades WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Especialidad especialidad = new Especialidad();
                especialidad.setId(rs.getInt("id"));
                especialidad.setNombre(rs.getString("nombre"));
                especialidad.setDescripcion(rs.getString("descripcion"));
                especialidad.setCostoConsulta(rs.getBigDecimal("costo_consulta"));
                return especialidad;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener especialidad por ID", e);
        }

        return null;
    }

    /**
     * Obtiene una especialidad por nombre
     */
    public Especialidad getEspecialidadByNombre(String nombre) {
        String sql = "SELECT id, nombre, descripcion, costo_consulta FROM especialidades WHERE nombre = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nombre);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Especialidad especialidad = new Especialidad();
                especialidad.setId(rs.getInt("id"));
                especialidad.setNombre(rs.getString("nombre"));
                especialidad.setDescripcion(rs.getString("descripcion"));
                especialidad.setCostoConsulta(rs.getBigDecimal("costo_consulta"));
                return especialidad;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener especialidad por nombre", e);
        }

        return null;
    }

    /**
     * Crea una nueva especialidad
     */
    public boolean crearEspecialidad(Especialidad especialidad) {
        String sql = "INSERT INTO especialidades (nombre, descripcion, costo_consulta) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, especialidad.getNombre());
            stmt.setString(2, especialidad.getDescripcion());
            stmt.setBigDecimal(3, especialidad.getCostoConsulta());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Especialidad creada exitosamente: {}", especialidad.getNombre());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al crear especialidad", e);
        }

        return false;
    }

    /**
     * Actualiza una especialidad existente
     */
    public boolean actualizarEspecialidad(Especialidad especialidad) {
        String sql = "UPDATE especialidades SET nombre = ?, descripcion = ?, costo_consulta = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, especialidad.getNombre());
            stmt.setString(2, especialidad.getDescripcion());
            stmt.setBigDecimal(3, especialidad.getCostoConsulta());
            stmt.setInt(4, especialidad.getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Especialidad actualizada exitosamente: {}", especialidad.getNombre());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al actualizar especialidad", e);
        }

        return false;
    }

    /**
     * Elimina una especialidad por ID
     */
    public boolean eliminarEspecialidad(Integer id) {
        String sql = "DELETE FROM especialidades WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                logger.info("Especialidad eliminada exitosamente. ID: {}", id);
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error al eliminar especialidad", e);
        }

        return false;
    }
}