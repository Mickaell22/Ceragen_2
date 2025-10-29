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

    /**
     * Obtiene todos los pacientes activos para ComboBox
     */
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

    /**
     * Obtiene un paciente por ID
     */
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

    /**
     * Obtiene un paciente por cedula
     */
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
}
