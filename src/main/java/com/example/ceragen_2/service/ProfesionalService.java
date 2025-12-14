package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Profesional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ProfesionalService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProfesionalService.class);
    private static ProfesionalService instance;

    private ProfesionalService() {
    }

    public static ProfesionalService getInstance() {
        if (instance == null) {
            instance = new ProfesionalService();
        }
        return instance;
    }

    // ===================== LISTAR / OBTENER =====================

    public List<Profesional> getAllProfesionales() {
        List<Profesional> profesionales = new ArrayList<>();

        String sql =
                "SELECT p.id, p.cedula, p.nombres, p.apellidos, " +
                        "       p.celular, p.email, p.numero_licencia, " +
                        "       p.tipo_usuario_registra, p.modalidad_atencion, " +
                        "       p.usuario_id, p.activo, p.fecha_registro, " +
                        "       pe.especialidad_id, e.nombre AS especialidad_nombre " +
                        "FROM profesionales p " +
                        "LEFT JOIN profesional_especialidades pe " +
                        "       ON p.id = pe.profesional_id " +
                        "      AND pe.es_principal = TRUE " +
                        "LEFT JOIN especialidades e " +
                        "       ON pe.especialidad_id = e.id " +
                        "ORDER BY p.nombres, p.apellidos";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                profesionales.add(mapProfesional(rs));
            }

            logger.info("Se obtuvieron {} profesionales", profesionales.size());
        } catch (SQLException e) {
            logger.error("Error al obtener profesionales", e);
        }

        return profesionales;
    }

    public Profesional getProfesionalById(final Integer id) {
        if (id == null) {
            return null;
        }

        String sql =
                "SELECT p.id, p.cedula, p.nombres, p.apellidos, " +
                        "       p.celular, p.email, p.numero_licencia, " +
                        "       p.tipo_usuario_registra, p.modalidad_atencion, " +
                        "       p.usuario_id, p.activo, p.fecha_registro, " +
                        "       pe.especialidad_id, e.nombre AS especialidad_nombre " +
                        "FROM profesionales p " +
                        "LEFT JOIN profesional_especialidades pe " +
                        "       ON p.id = pe.profesional_id " +
                        "      AND pe.es_principal = TRUE " +
                        "LEFT JOIN especialidades e " +
                        "       ON pe.especialidad_id = e.id " +
                        "WHERE p.id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapProfesional(rs);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener profesional por ID", e);
        }

        return null;
    }

    public Profesional getProfesionalByCedula(final String cedula) {
        String sql =
                "SELECT p.id, p.cedula, p.nombres, p.apellidos, " +
                        "       p.celular, p.email, p.numero_licencia, " +
                        "       p.tipo_usuario_registra, p.modalidad_atencion, " +
                        "       p.usuario_id, p.activo, p.fecha_registro, " +
                        "       pe.especialidad_id, e.nombre AS especialidad_nombre " +
                        "FROM profesionales p " +
                        "LEFT JOIN profesional_especialidades pe " +
                        "       ON p.id = pe.profesional_id " +
                        "      AND pe.es_principal = TRUE " +
                        "LEFT JOIN especialidades e " +
                        "       ON pe.especialidad_id = e.id " +
                        "WHERE p.cedula = ? AND p.activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return mapProfesional(rs);
            }
        } catch (SQLException e) {
            logger.error("Error al obtener profesional por cédula", e);
        }

        return null;
    }

    // ===================== CREAR / ACTUALIZAR / ELIMINAR =====================

    public boolean crearProfesional(final Profesional profesional) {

        String sqlProfesional = "INSERT INTO profesionales " +
                "(cedula, nombres, apellidos, celular, email, " +
                " numero_licencia, usuario_id, activo, " +
                " tipo_usuario_registra, modalidad_atencion) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     sqlProfesional,
                     Statement.RETURN_GENERATED_KEYS
             )) {

            stmt.setString(1, profesional.getCedula());
            stmt.setString(2, profesional.getNombres());
            stmt.setString(3, profesional.getApellidos());
            stmt.setString(4, profesional.getTelefono());
            stmt.setString(5, profesional.getEmail());
            stmt.setString(6, profesional.getNumeroLicencia());

            if (profesional.getUsuarioId() != null) {
                stmt.setInt(7, profesional.getUsuarioId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            if (profesional.getActivo() == null) {
                stmt.setNull(8, Types.BOOLEAN);
            } else {
                stmt.setBoolean(8, profesional.getActivo());
            }

            String tipoUsuarioRegistra = profesional.getTipoUsuarioRegistra();
            if (tipoUsuarioRegistra == null || tipoUsuarioRegistra.isBlank()) {
                tipoUsuarioRegistra = "ADMIN";
            }
            stmt.setString(9, tipoUsuarioRegistra);

            String modalidad = profesional.getModalidadAtencion();
            if (modalidad == null || modalidad.isBlank()) {
                modalidad = "PRESENCIAL";
            }
            stmt.setString(10, modalidad);

            int rows = stmt.executeUpdate();

            if (rows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        profesional.setId(rs.getInt(1));
                    }
                }
            }

            if (profesional.getId() != null
                    && profesional.getEspecialidadId() != null) {
                insertarEspecialidadPrincipal(
                        conn,
                        profesional.getId(),
                        profesional.getEspecialidadId()
                );
            }

            logger.info("Profesional creado correctamente: {}", profesional.getCedula());
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al crear profesional", e);
            return false;
        }
    }

    public boolean actualizarProfesional(final Profesional profesional) {
        if (profesional.getId() == null) {
            return false;
        }

        String sqlProfesional = "UPDATE profesionales SET " +
                "cedula = ?, " +
                "nombres = ?, " +
                "apellidos = ?, " +
                "celular = ?, " +
                "email = ?, " +
                "numero_licencia = ?, " +
                "usuario_id = ?, " +
                "activo = ?, " +
                "modalidad_atencion = ? " +
                "WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlProfesional)) {

            stmt.setString(1, profesional.getCedula());
            stmt.setString(2, profesional.getNombres());
            stmt.setString(3, profesional.getApellidos());
            stmt.setString(4, profesional.getTelefono());
            stmt.setString(5, profesional.getEmail());
            stmt.setString(6, profesional.getNumeroLicencia());

            if (profesional.getUsuarioId() != null) {
                stmt.setInt(7, profesional.getUsuarioId());
            } else {
                stmt.setNull(7, Types.INTEGER);
            }

            if (profesional.getActivo() == null) {
                stmt.setNull(8, Types.BOOLEAN);
            } else {
                stmt.setBoolean(8, profesional.getActivo());
            }

            String modalidad = profesional.getModalidadAtencion();
            if (modalidad == null || modalidad.isBlank()) {
                modalidad = "PRESENCIAL";
            }
            stmt.setString(9, modalidad);

            stmt.setInt(10, profesional.getId());

            int rows = stmt.executeUpdate();
            logger.info("Profesional actualizado. id={}, filas afectadas={}",
                    profesional.getId(), rows);

            if (profesional.getEspecialidadId() != null) {
                actualizarEspecialidadPrincipal(
                        conn,
                        profesional.getId(),
                        profesional.getEspecialidadId()
                );
            }

            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al actualizar profesional", e);
            return false;
        }
    }

    public boolean eliminarProfesional(final Integer id) {
        if (id == null) {
            return false;
        }

        String sql = "DELETE FROM profesionales WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            logger.info("Profesional eliminado. id={}, filas afectadas={}", id, rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("Error al eliminar profesional", e);
            return false;
        }
    }

    // ===================== AUXILIARES ESPECIALIDADES =====================

    private void insertarEspecialidadPrincipal(
            final Connection conn,
            final int profesionalId,
            final int especialidadId
    ) throws SQLException {

        String sql =
                "INSERT INTO profesional_especialidades " +
                        "(profesional_id, especialidad_id, es_principal) " +
                        "VALUES (?, ?, TRUE)";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, profesionalId);
            stmt.setInt(2, especialidadId);
            stmt.executeUpdate();
        }
    }

    private void actualizarEspecialidadPrincipal(
            final Connection conn,
            final int profesionalId,
            final int nuevaEspecialidadId
    ) throws SQLException {

        String deleteSql =
                "DELETE FROM profesional_especialidades " +
                        "WHERE profesional_id = ? AND es_principal = TRUE";

        try (PreparedStatement deleteStmt =
                     conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, profesionalId);
            deleteStmt.executeUpdate();
        }

        insertarEspecialidadPrincipal(conn, profesionalId, nuevaEspecialidadId);
    }

    // ===================== MAPEADOR =====================

    private Profesional mapProfesional(final ResultSet rs)
            throws SQLException {

        Profesional profesional = new Profesional();
        profesional.setId(rs.getInt("id"));
        profesional.setCedula(rs.getString("cedula"));
        profesional.setNombres(rs.getString("nombres"));
        profesional.setApellidos(rs.getString("apellidos"));
        profesional.setTelefono(rs.getString("celular"));
        profesional.setEmail(rs.getString("email"));
        profesional.setNumeroLicencia(
                rs.getString("numero_licencia")
        );

        profesional.setTipoUsuarioRegistra(
                rs.getString("tipo_usuario_registra")
        );
        profesional.setModalidadAtencion(
                rs.getString("modalidad_atencion")
        );

        int especialidadId = rs.getInt("especialidad_id");
        if (!rs.wasNull()) {
            profesional.setEspecialidadId(especialidadId);
        }
        profesional.setEspecialidadNombre(
                rs.getString("especialidad_nombre")
        );

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

        Timestamp ts = rs.getTimestamp("fecha_registro");
        if (ts != null) {
            LocalDateTime fechaRegistro = ts.toLocalDateTime();
            profesional.setFechaRegistro(fechaRegistro);
        }

        return profesional;
    }
}
