package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.DocumentoPaciente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DocumentoPacienteService {
    private static final Logger logger = LoggerFactory.getLogger(DocumentoPacienteService.class);
    private static DocumentoPacienteService instance;

    private DocumentoPacienteService() {}

    public static DocumentoPacienteService getInstance() {
        if (instance == null) instance = new DocumentoPacienteService();
        return instance;
    }

    public List<DocumentoPaciente> listarPorPaciente(int pacienteId) {
        List<DocumentoPaciente> docs = new ArrayList<>();
        String sql = "SELECT id, paciente_id, nombre_archivo, tipo_documento, ruta_archivo, fecha_subida FROM documentos_paciente WHERE paciente_id = ? ORDER BY fecha_subida DESC";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pacienteId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                DocumentoPaciente d = new DocumentoPaciente();
                d.setId(rs.getInt("id"));
                d.setPacienteId(rs.getInt("paciente_id"));
                d.setNombreArchivo(rs.getString("nombre_archivo"));
                d.setTipoDocumento(rs.getString("tipo_documento"));
                d.setRutaArchivo(rs.getString("ruta_archivo"));
                Timestamp ts = rs.getTimestamp("fecha_subida");
                if (ts != null) d.setFechaSubida(ts.toLocalDateTime());
                docs.add(d);
            }
        } catch (SQLException e) {
            logger.error("Error al listar documentos del paciente", e);
        }
        return docs;
    }

    public boolean crearDocumento(DocumentoPaciente doc) {
        String sql = "INSERT INTO documentos_paciente (paciente_id, nombre_archivo, tipo_documento, ruta_archivo) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, doc.getPacienteId());
            stmt.setString(2, doc.getNombreArchivo());
            stmt.setString(3, doc.getTipoDocumento());
            stmt.setString(4, doc.getRutaArchivo());
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("Error al crear documento del paciente", e);
            return false;
        }
    }

    public boolean eliminarDocumento(int id) {
        String sql = "DELETE FROM documentos_paciente WHERE id = ?";
        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.error("Error al eliminar documento del paciente", e);
            return false;
        }
    }
}
