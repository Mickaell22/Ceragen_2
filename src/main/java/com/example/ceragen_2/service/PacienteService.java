package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Paciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PacienteService {
    private static final Logger logger = LoggerFactory.getLogger(PacienteService.class);
    private static PacienteService instance;

    private PacienteService() {
    }

    public static PacienteService getInstance() {
        if (instance == null) {
            instance = new PacienteService();
        }
        return instance;
    }

    public List<Paciente> getAllPacientes() {
        List<Paciente> pacientes = new ArrayList<>();
        String sql = "SELECT id, cedula, nombres, apellidos FROM pacientes ORDER BY nombres, apellidos";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Paciente paciente = new Paciente();
                paciente.setId(rs.getInt("id"));
                paciente.setCedula(rs.getString("cedula"));
                paciente.setNombres(rs.getString("nombres"));
                paciente.setApellidos(rs.getString("apellidos"));
                pacientes.add(paciente);
            }

            logger.info("Se obtuvieron {} pacientes", pacientes.size());
        } catch (SQLException e) {
            logger.error("Error al obtener pacientes", e);
        }

        return pacientes;
    }

    
    public Paciente getPacienteById(Integer id) {
        String sql = "SELECT id, cedula, nombres, apellidos, telefono, email FROM pacientes WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Paciente paciente = new Paciente();
                paciente.setId(rs.getInt("id"));
                paciente.setCedula(rs.getString("cedula"));
                paciente.setNombres(rs.getString("nombres"));
                paciente.setApellidos(rs.getString("apellidos"));
                paciente.setTelefono(rs.getString("telefono"));
                paciente.setEmail(rs.getString("email"));
                return paciente;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener paciente por ID", e);
        }

        return null;
    }

    public Paciente getPacienteByCedula(String cedula) {
        String sql = "SELECT id, cedula, nombres, apellidos, telefono, email FROM pacientes WHERE cedula = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Paciente paciente = new Paciente();
                paciente.setId(rs.getInt("id"));
                paciente.setCedula(rs.getString("cedula"));
                paciente.setNombres(rs.getString("nombres"));
                paciente.setApellidos(rs.getString("apellidos"));
                paciente.setTelefono(rs.getString("telefono"));
                paciente.setEmail(rs.getString("email"));
                return paciente;
            }
        } catch (SQLException e) {
            logger.error("Error al obtener paciente por cedula", e);
        }

        return null;
    }

    public List<Paciente> getPacientes(int offset, int limit, String searchText, String generoFilter) {
        List<Paciente> pacientes = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT id, cedula, nombres, apellidos, fecha_nacimiento, genero, telefono, email, direccion, grupo_sanguineo, alergias, fecha_registro " +
                "FROM pacientes WHERE 1=1");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?)");
        }
        if (generoFilter != null && !generoFilter.equals("TODOS")) {
            sql.append(" AND genero = ?");
        }

        sql.append(" ORDER BY fecha_registro DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String like = "%" + searchText + "%";
                stmt.setString(paramIndex++, like);
                stmt.setString(paramIndex++, like);
                stmt.setString(paramIndex++, like);
            }
            if (generoFilter != null && !generoFilter.equals("TODOS")) {
                stmt.setString(paramIndex++, generoFilter);
            }

            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Paciente p = new Paciente();
                p.setId(rs.getInt("id"));
                p.setCedula(rs.getString("cedula"));
                p.setNombres(rs.getString("nombres"));
                p.setApellidos(rs.getString("apellidos"));
                Date fn = rs.getDate("fecha_nacimiento");
                if (fn != null) p.setFechaNacimiento(fn.toLocalDate());
                p.setGenero(rs.getString("genero"));
                p.setTelefono(rs.getString("telefono"));
                p.setEmail(rs.getString("email"));
                p.setDireccion(rs.getString("direccion"));
                p.setGrupoSanguineo(rs.getString("grupo_sanguineo"));
                p.setAlergias(rs.getString("alergias"));
                Timestamp fr = rs.getTimestamp("fecha_registro");
                if (fr != null) p.setFechaRegistro(fr.toLocalDateTime());
                pacientes.add(p);
            }
            logger.info("Se obtuvieron {} pacientes (paginado)", pacientes.size());
        } catch (SQLException e) {
            logger.error("Error al obtener pacientes", e);
        }
        return pacientes;
    }

    public int countPacientes(String searchText, String generoFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM pacientes WHERE 1=1");
        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND (cedula LIKE ? OR nombres LIKE ? OR apellidos LIKE ?)");
        }
        if (generoFilter != null && !generoFilter.equals("TODOS")) {
            sql.append(" AND genero = ?");
        }

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            int paramIndex = 1;
            if (searchText != null && !searchText.trim().isEmpty()) {
                String like = "%" + searchText + "%";
                stmt.setString(paramIndex++, like);
                stmt.setString(paramIndex++, like);
                stmt.setString(paramIndex++, like);
            }
            if (generoFilter != null && !generoFilter.equals("TODOS")) {
                stmt.setString(paramIndex++, generoFilter);
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            logger.error("Error al contar pacientes", e);
        }
        return 0;
    }

    public boolean crearPaciente(Paciente p) {
        String sql = "INSERT INTO pacientes (cedula, nombres, apellidos, fecha_nacimiento, genero, telefono, email, direccion, grupo_sanguineo, alergias) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getCedula());
            stmt.setString(2, p.getNombres());
            stmt.setString(3, p.getApellidos());
            if (p.getFechaNacimiento() != null) {
                stmt.setDate(4, Date.valueOf(p.getFechaNacimiento()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setString(5, p.getGenero());
            stmt.setString(6, p.getTelefono());
            stmt.setString(7, p.getEmail());
            stmt.setString(8, p.getDireccion());
            stmt.setString(9, p.getGrupoSanguineo());
            stmt.setString(10, p.getAlergias());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error al crear paciente", e);
            return false;
        }
    }

    public boolean actualizarPaciente(Paciente p) {
        String sql = "UPDATE pacientes SET cedula = ?, nombres = ?, apellidos = ?, fecha_nacimiento = ?, genero = ?, telefono = ?, email = ?, direccion = ?, grupo_sanguineo = ?, alergias = ? WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, p.getCedula());
            stmt.setString(2, p.getNombres());
            stmt.setString(3, p.getApellidos());
            if (p.getFechaNacimiento() != null) {
                stmt.setDate(4, Date.valueOf(p.getFechaNacimiento()));
            } else {
                stmt.setNull(4, Types.DATE);
            }
            stmt.setString(5, p.getGenero());
            stmt.setString(6, p.getTelefono());
            stmt.setString(7, p.getEmail());
            stmt.setString(8, p.getDireccion());
            stmt.setString(9, p.getGrupoSanguineo());
            stmt.setString(10, p.getAlergias());
            stmt.setInt(11, p.getId());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error al actualizar paciente", e);
            return false;
        }
    }

    public boolean eliminarPaciente(Integer id) {
        String sql = "DELETE FROM pacientes WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error al eliminar paciente", e);
            return false;
        }
    }

    public boolean existeCedula(String cedula) {
        String sql = "SELECT COUNT(*) FROM pacientes WHERE cedula = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Error al verificar cedula", e);
        }
        return false;
    }
    
    public boolean existeCedulaExceptoId(String cedula, Integer idExcluir) {
        String sql = "SELECT COUNT(*) FROM pacientes WHERE cedula = ? AND id <> ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, cedula);
            stmt.setInt(2, idExcluir);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            logger.error("Error al verificar cedula (excluir id)", e);
        }
        return false;
    }
}
