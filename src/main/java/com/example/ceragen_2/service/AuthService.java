package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class AuthService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static AuthService instance;
    private String currentUsername;
    private String currentUserRole;
    private Integer currentUserId;
    private Integer currentProfesionalId;

    private AuthService() {}

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public boolean login(String username, String password) {
        String query = "SELECT id, username, password, rol, activo FROM usuarios WHERE username = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String hashedPassword = rs.getString("password");

                    if (PasswordUtil.verifyPassword(password, hashedPassword)) {
                        currentUserId = rs.getInt("id");
                        currentUsername = rs.getString("username");
                        currentUserRole = rs.getString("rol");

                        // Si es MEDICO, obtener su profesional_id
                        if ("MEDICO".equals(currentUserRole)) {
                            currentProfesionalId = obtenerProfesionalIdPorUsuario(currentUserId);
                        } else {
                            currentProfesionalId = null;
                        }

                        LOGGER.info("Usuario autenticado: {} - Rol: {} - ProfesionalId: {}",
                                    currentUsername, currentUserRole, currentProfesionalId);
                        return true;
                    } else {
                        LOGGER.warn("Contraseña incorrecta para usuario: {}", username);
                    }
                } else {
                    LOGGER.warn("Usuario no encontrado o inactivo: {}", username);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error al autenticar usuario: {}", username, e);
        }

        return false;
    }

    public void logout() {
        LOGGER.info("Usuario desconectado: {}", currentUsername);
        currentUserId = null;
        currentUsername = null;
        currentUserRole = null;
        currentProfesionalId = null;
    }

    public boolean isAuthenticated() {
        return currentUsername != null;
    }

    public String getCurrentUsername() {
        return currentUsername;
    }

    public String getCurrentUserRole() {
        return currentUserRole;
    }

    public Integer getCurrentUserId() {
        return currentUserId;
    }

    public Integer getCurrentProfesionalId() {
        return currentProfesionalId;
    }

    private Integer obtenerProfesionalIdPorUsuario(Integer usuarioId) {
        String query = "SELECT id FROM profesionales WHERE usuario_id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, usuarioId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Integer profesionalId = rs.getInt("id");
                    LOGGER.info("Profesional ID {} encontrado para usuario ID {}", profesionalId, usuarioId);
                    return profesionalId;
                } else {
                    LOGGER.warn("No se encontró profesional para usuario ID {}", usuarioId);
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Error al obtener profesional_id para usuario: {}", usuarioId, e);
        }

        return null;
    }
}
