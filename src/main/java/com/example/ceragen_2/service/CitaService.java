package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Cita;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CitaService {
    private static final Logger logger = LoggerFactory.getLogger(CitaService.class);
    private static CitaService instance;

    private CitaService() {
    }

    public static CitaService getInstance() {
        if (instance == null) {
            instance = new CitaService();
        }
        return instance;
    }

    /**
     * Obtiene citas con paginación y filtros
     */
    public List<Cita> getCitas(int offset, int limit, Integer pacienteId, Integer profesionalId,
                                String estadoFilter, LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        List<Cita> citas = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT c.id, c.paciente_id, c.profesional_id, c.fecha_hora, c.motivo, c.estado, " +
            "c.observaciones, c.fecha_creacion, " +
            "CONCAT(pac.nombres, ' ', pac.apellidos) as paciente_nombre, " +
            "CONCAT('Dr. ', prof.nombres, ' ', prof.apellidos) as profesional_nombre " +
            "FROM citas c " +
            "INNER JOIN pacientes pac ON c.paciente_id = pac.id " +
            "INNER JOIN profesionales prof ON c.profesional_id = prof.id " +
            "WHERE 1=1"
        );

        if (pacienteId != null) {
            sql.append(" AND c.paciente_id = ?");
        }
        if (profesionalId != null) {
            sql.append(" AND c.profesional_id = ?");
        }
        if (estadoFilter != null && !estadoFilter.equals("TODOS")) {
            sql.append(" AND c.estado = ?");
        }
        if (fechaDesde != null) {
            sql.append(" AND c.fecha_hora >= ?");
        }
        if (fechaHasta != null) {
            sql.append(" AND c.fecha_hora <= ?");
        }

        sql.append(" ORDER BY c.fecha_hora DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (pacienteId != null) {
                stmt.setInt(paramIndex++, pacienteId);
            }
            if (profesionalId != null) {
                stmt.setInt(paramIndex++, profesionalId);
            }
            if (estadoFilter != null && !estadoFilter.equals("TODOS")) {
                stmt.setString(paramIndex++, estadoFilter);
            }
            if (fechaDesde != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaDesde));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaHasta));
            }

            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Cita cita = new Cita();
                cita.setId(rs.getInt("id"));
                cita.setPacienteId(rs.getInt("paciente_id"));
                cita.setProfesionalId(rs.getInt("profesional_id"));

                Timestamp timestamp = rs.getTimestamp("fecha_hora");
                if (timestamp != null) {
                    cita.setFechaHora(timestamp.toLocalDateTime());
                }

                cita.setMotivo(rs.getString("motivo"));
                cita.setEstado(rs.getString("estado"));
                cita.setObservaciones(rs.getString("observaciones"));

                Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
                if (fechaCreacion != null) {
                    cita.setFechaCreacion(fechaCreacion.toLocalDateTime());
                }

                cita.setPacienteNombre(rs.getString("paciente_nombre"));
                cita.setProfesionalNombre(rs.getString("profesional_nombre"));

                citas.add(cita);
            }

            logger.info("Se obtuvieron {} citas", citas.size());
        } catch (SQLException e) {
            logger.error("Error al obtener citas", e);
        }

        return citas;
    }

    /**
     * Cuenta el total de citas con filtros aplicados
     */
    public int countCitas(Integer pacienteId, Integer profesionalId, String estadoFilter,
                          LocalDateTime fechaDesde, LocalDateTime fechaHasta) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM citas WHERE 1=1");

        if (pacienteId != null) {
            sql.append(" AND paciente_id = ?");
        }
        if (profesionalId != null) {
            sql.append(" AND profesional_id = ?");
        }
        if (estadoFilter != null && !estadoFilter.equals("TODOS")) {
            sql.append(" AND estado = ?");
        }
        if (fechaDesde != null) {
            sql.append(" AND fecha_hora >= ?");
        }
        if (fechaHasta != null) {
            sql.append(" AND fecha_hora <= ?");
        }

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (pacienteId != null) {
                stmt.setInt(paramIndex++, pacienteId);
            }
            if (profesionalId != null) {
                stmt.setInt(paramIndex++, profesionalId);
            }
            if (estadoFilter != null && !estadoFilter.equals("TODOS")) {
                stmt.setString(paramIndex++, estadoFilter);
            }
            if (fechaDesde != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaDesde));
            }
            if (fechaHasta != null) {
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(fechaHasta));
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            logger.error("Error al contar citas", e);
        }

        return 0;
    }

    /**
     * Crea una nueva cita
     */
    public boolean crearCita(Integer pacienteId, Integer profesionalId, LocalDateTime fechaHora, String motivo) {
        String sql = "INSERT INTO citas (paciente_id, profesional_id, fecha_hora, motivo, estado) VALUES (?, ?, ?, ?, 'PENDIENTE')";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pacienteId);
            stmt.setInt(2, profesionalId);
            stmt.setTimestamp(3, Timestamp.valueOf(fechaHora));
            stmt.setString(4, motivo);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Cita creada para paciente ID: {}, profesional ID: {}", pacienteId, profesionalId);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error al crear cita", e);
            return false;
        }
    }

    /**
     * Actualiza una cita existente
     */
    public boolean actualizarCita(Integer id, Integer pacienteId, Integer profesionalId,
                                   LocalDateTime fechaHora, String motivo, String estado, String observaciones) {
        String sql = "UPDATE citas SET paciente_id = ?, profesional_id = ?, fecha_hora = ?, " +
                     "motivo = ?, estado = ?, observaciones = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pacienteId);
            stmt.setInt(2, profesionalId);
            stmt.setTimestamp(3, Timestamp.valueOf(fechaHora));
            stmt.setString(4, motivo);
            stmt.setString(5, estado);
            stmt.setString(6, observaciones);
            stmt.setInt(7, id);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Cita actualizada ID: {}", id);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error al actualizar cita", e);
            return false;
        }
    }

    /**
     * Elimina una cita
     */
    public boolean eliminarCita(Integer id) {
        String sql = "DELETE FROM citas WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Cita eliminada con ID: {}", id);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error al eliminar cita", e);
            return false;
        }
    }

    /**
     * Cambia el estado de una cita
     */
    public boolean cambiarEstado(Integer id, String nuevoEstado) {
        String sql = "UPDATE citas SET estado = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nuevoEstado);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            logger.info("Estado de cita ID {} cambiado a: {}", id, nuevoEstado);
            return rowsAffected > 0;

        } catch (SQLException e) {
            logger.error("Error al cambiar estado de cita", e);
            return false;
        }
    }

    /**
     * Obtiene una cita por ID
     */
    public Cita getCitaById(Integer id) {
        String sql = "SELECT c.id, c.paciente_id, c.profesional_id, c.fecha_hora, c.motivo, c.estado, " +
                     "c.observaciones, c.fecha_creacion, " +
                     "CONCAT(pac.nombres, ' ', pac.apellidos) as paciente_nombre, " +
                     "CONCAT('Dr. ', prof.nombres, ' ', prof.apellidos) as profesional_nombre " +
                     "FROM citas c " +
                     "INNER JOIN pacientes pac ON c.paciente_id = pac.id " +
                     "INNER JOIN profesionales prof ON c.profesional_id = prof.id " +
                     "WHERE c.id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Cita cita = new Cita();
                cita.setId(rs.getInt("id"));
                cita.setPacienteId(rs.getInt("paciente_id"));
                cita.setProfesionalId(rs.getInt("profesional_id"));

                Timestamp timestamp = rs.getTimestamp("fecha_hora");
                if (timestamp != null) {
                    cita.setFechaHora(timestamp.toLocalDateTime());
                }

                cita.setMotivo(rs.getString("motivo"));
                cita.setEstado(rs.getString("estado"));
                cita.setObservaciones(rs.getString("observaciones"));

                Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
                if (fechaCreacion != null) {
                    cita.setFechaCreacion(fechaCreacion.toLocalDateTime());
                }

                cita.setPacienteNombre(rs.getString("paciente_nombre"));
                cita.setProfesionalNombre(rs.getString("profesional_nombre"));

                return cita;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener cita por ID", e);
        }

        return null;
    }

    /**
     * Verifica si existe conflicto de horario para un profesional
     */
    public boolean existeConflictoHorario(Integer profesionalId, LocalDateTime fechaHora, Integer citaIdExcluir) {
        String sql = "SELECT COUNT(*) FROM citas WHERE profesional_id = ? AND fecha_hora = ? AND estado != 'CANCELADA'";

        if (citaIdExcluir != null) {
            sql += " AND id != ?";
        }

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, profesionalId);
            stmt.setTimestamp(2, Timestamp.valueOf(fechaHora));

            if (citaIdExcluir != null) {
                stmt.setInt(3, citaIdExcluir);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            logger.error("Error al verificar conflicto de horario", e);
        }

        return false;
    }
}
