package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Profesional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.sql.Types;
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
     * Obtiene todos los profesionales para la tabla / filtros.
     * No filtra por activo aquí; el controller se encarga con cmbActivoFiltro.
     */
    public List<Profesional> getAllProfesionales() {
        List<Profesional> profesionales = new ArrayList<>();

        String sql =
                "SELECT p.id, p.cedula, p.nombres, p.apellidos, " +
                        "       p.telefono, p.email, p.numero_licencia, " +
                        "       p.especialidad_id, p.usuario_id, p.activo, " +
                        "       e.nombre AS especialidad_nombre " +
                        "FROM profesionales p " +
                        "LEFT JOIN especialidades e ON p.especialidad_id = e.id " +
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

                profesional.setTelefono(rs.getString("telefono"));
                profesional.setEmail(rs.getString("email"));
                profesional.setNumeroLicencia(rs.getString("numero_licencia"));

                profesional.setEspecialidadId(rs.getInt("especialidad_id"));
                profesional.setEspecialidadNombre(rs.getString("especialidad_nombre"));

                int usuarioId = rs.getInt("usuario_id");
                if (!rs.wasNull()) {
                    profesional.setUsuarioId(usuarioId);
                }

                boolean activo = rs.getBoolean("activo");
                if (rs.wasNull()) {
                    profesional.setActivo(null);
                } else {
                    profesional.setActivo(activo);
                }

                profesionales.add(profesional);
            }

            logger.info("Se obtuvieron {} profesionales", profesionales.size());
        } catch (SQLException e) {
            logger.error("Error al obtener profesionales", e);
        }

        return profesionales;
    }

    /**
     * Obtiene un profesional por ID.
     */
    public Profesional getProfesionalById(Integer id) {
        String sql =
                "SELECT p.id, p.cedula, p.nombres, p.apellidos, " +
                        "       p.telefono, p.email, p.numero_licencia, " +
                        "       p.especialidad_id, p.usuario_id, p.activo, " +
                        "       e.nombre AS especialidad_nombre " +
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

                profesional.setEspecialidadId(rs.getInt("especialidad_id"));
                profesional.setEspecialidadNombre(rs.getString("especialidad_nombre"));

                int usuarioId = rs.getInt("usuario_id");
                if (!rs.wasNull()) {
                    profesional.setUsuarioId(usuarioId);
                }

                boolean activo = rs.getBoolean("activo");
                if (rs.wasNull()) {
                    profesional.setActivo(null);
                } else {
                    profesional.setActivo(activo);
                }

                return profesional;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener profesional por ID", e);
        }

        return null;
    }

    /**
     * Obtiene un profesional por cédula (solo activos).
     */
    public Profesional getProfesionalByCedula(String cedula) {
        String sql =
                "SELECT p.id, p.cedula, p.nombres, p.apellidos, " +
                        "       p.telefono, p.email, p.numero_licencia, " +
                        "       p.especialidad_id, p.usuario_id, p.activo, " +
                        "       e.nombre AS especialidad_nombre " +
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

                profesional.setEspecialidadId(rs.getInt("especialidad_id"));
                profesional.setEspecialidadNombre(rs.getString("especialidad_nombre"));

                int usuarioId = rs.getInt("usuario_id");
                if (!rs.wasNull()) {
                    profesional.setUsuarioId(usuarioId);
                }

                boolean activo = rs.getBoolean("activo");
                if (rs.wasNull()) {
                    profesional.setActivo(null);
                } else {
                    profesional.setActivo(activo);
                }

                return profesional;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener profesional por cédula", e);
        }

        return null;
    }

    /**
     * Crea un nuevo profesional en la BD.
     */
    public boolean crearProfesional(Profesional profesional) {
        String sql = "INSERT INTO profesionales " +
                "(cedula, nombres, apellidos, telefono, email, numero_licencia, " +
                " especialidad_id, usuario_id, activo) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, profesional.getCedula());
            stmt.setString(2, profesional.getNombres());
            stmt.setString(3, profesional.getApellidos());
            stmt.setString(4, profesional.getTelefono());
            stmt.setString(5, profesional.getEmail());
            stmt.setString(6, profesional.getNumeroLicencia());

            // especialidad_id (puede ser null)
            if (profesional.getEspecialidadId() != null) {
                stmt.setInt(7, profesional.getEspecialidadId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            // usuario_id (puede ser null)
            if (profesional.getUsuarioId() != null) {
                stmt.setInt(8, profesional.getUsuarioId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            // activo
            if (profesional.getActivo() == null) {
                stmt.setNull(9, Types.BOOLEAN);
            } else {
                stmt.setBoolean(9, profesional.getActivo());
            }

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        profesional.setId(rs.getInt(1));
                    }
                }
            }

            logger.info("Profesional creado correctamente: {}", profesional.getCedula());
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al crear profesional", e);
            return false;
        }
    }

    /**
     * Actualiza un profesional existente en la BD.
     */
    public boolean actualizarProfesional(Profesional profesional) {
        String sql = "UPDATE profesionales SET " +
                "cedula = ?, " +
                "nombres = ?, " +
                "apellidos = ?, " +
                "telefono = ?, " +
                "email = ?, " +
                "numero_licencia = ?, " +
                "especialidad_id = ?, " +
                "usuario_id = ?, " +
                "activo = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, profesional.getCedula());
            stmt.setString(2, profesional.getNombres());
            stmt.setString(3, profesional.getApellidos());
            stmt.setString(4, profesional.getTelefono());
            stmt.setString(5, profesional.getEmail());
            stmt.setString(6, profesional.getNumeroLicencia());

            // especialidad_id (puede ser null)
            if (profesional.getEspecialidadId() != null) {
                stmt.setInt(7, profesional.getEspecialidadId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            // usuario_id (puede ser null)
            if (profesional.getUsuarioId() != null) {
                stmt.setInt(8, profesional.getUsuarioId());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            // activo
            if (profesional.getActivo() == null) {
                stmt.setNull(9, Types.BOOLEAN);
            } else {
                stmt.setBoolean(9, profesional.getActivo());
            }

            stmt.setInt(10, profesional.getId());

            int rows = stmt.executeUpdate();
            logger.info("Profesional actualizado. id={}, filas afectadas={}", profesional.getId(), rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al actualizar profesional", e);
            return false;
        }
    }
}

