package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Profesional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProfesionalService {
    private static final Logger logger = LoggerFactory.getLogger(ProfesionalService.class);
    private static ProfesionalService instance;

    private ProfesionalService() {
    }

    public static ProfesionalService getInstance() {
        if (instance == null) {
            instance = new ProfesionalService();
        }
        return instance;
    }

    /**
     * Obtiene todos los profesionales activos para ComboBox
     */
    public List<Profesional> getAllProfesionales() {
        List<Profesional> profesionales = new ArrayList<>();
        String sql = "SELECT p.id, p.cedula, p.nombres, p.apellidos, e.nombre as especialidad_nombre " +
                     "FROM profesionales p " +
                     "LEFT JOIN especialidades e ON p.especialidad_id = e.id " +
                     "WHERE p.activo = TRUE " +
                     "ORDER BY p.nombres, p.apellidos";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Profesional profesional = new Profesional();
                profesional.setId(rs.getInt("id"));
                profesional.setCedula(rs.getString("cedula"));
                profesional.setNombres(rs.getString("nombres"));
                profesional.setApellidos(rs.getString("apellidos"));
                profesional.setEspecialidadNombre(rs.getString("especialidad_nombre"));
                profesionales.add(profesional);
            }

            logger.info("Se obtuvieron {} profesionales activos", profesionales.size());
        } catch (SQLException e) {
            logger.error("Error al obtener profesionales", e);
        }

        return profesionales;
    }

    /**
     * Obtiene un profesional por ID
     */
    public Profesional getProfesionalById(Integer id) {
        String sql = "SELECT p.id, p.cedula, p.nombres, p.apellidos, p.telefono, p.email, p.numero_licencia, " +
                     "e.nombre as especialidad_nombre " +
                     "FROM profesionales p " +
                     "LEFT JOIN especialidades e ON p.especialidad_id = e.id " +
                     "WHERE p.id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Profesional profesional = new Profesional();
                profesional.setId(rs.getInt("id"));
                profesional.setCedula(rs.getString("cedula"));
                profesional.setNombres(rs.getString("nombres"));
                profesional.setApellidos(rs.getString("apellidos"));
                profesional.setTelefono(rs.getString("telefono"));
                profesional.setEmail(rs.getString("email"));
                profesional.setNumeroLicencia(rs.getString("numero_licencia"));
                profesional.setEspecialidadNombre(rs.getString("especialidad_nombre"));
                return profesional;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener profesional por ID", e);
        }

        return null;
    }

    /**
     * Obtiene un profesional por cedula
     */
    public Profesional getProfesionalByCedula(String cedula) {
        String sql = "SELECT p.id, p.cedula, p.nombres, p.apellidos, p.telefono, p.email, p.numero_licencia, " +
                     "e.nombre as especialidad_nombre " +
                     "FROM profesionales p " +
                     "LEFT JOIN especialidades e ON p.especialidad_id = e.id " +
                     "WHERE p.cedula = ? AND p.activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Profesional profesional = new Profesional();
                profesional.setId(rs.getInt("id"));
                profesional.setCedula(rs.getString("cedula"));
                profesional.setNombres(rs.getString("nombres"));
                profesional.setApellidos(rs.getString("apellidos"));
                profesional.setTelefono(rs.getString("telefono"));
                profesional.setEmail(rs.getString("email"));
                profesional.setNumeroLicencia(rs.getString("numero_licencia"));
                profesional.setEspecialidadNombre(rs.getString("especialidad_nombre"));
                return profesional;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener profesional por cedula", e);
        }

        return null;
    }
}
