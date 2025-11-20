package com.example.ceragen_2.service;

import com.example.ceragen_2.config.DatabaseConfig;
import com.example.ceragen_2.model.Usuario;
import com.example.ceragen_2.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public final class UsuarioService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UsuarioService.class);
    private static UsuarioService instance;

    private UsuarioService() {
    }

    public static synchronized UsuarioService getInstance() {
        if (instance == null) {
            instance = new UsuarioService();
        }
        return instance;
    }

    /**
     * Obtiene usuarios con paginación y filtros
     */
    public List<Usuario> getUsuarios(int offset, int limit, String searchText, String rolFilter, Boolean activoFilter) {
        List<Usuario> usuarios = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, username, password, rol, activo, fecha_creacion FROM usuarios WHERE 1=1");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
        }
        if (rolFilter != null && !rolFilter.equals("TODOS")) {
            sql.append(" AND rol = ?");
        }
        if (activoFilter != null) {
            sql.append(" AND activo = ?");
        }

        sql.append(" ORDER BY fecha_creacion DESC LIMIT ? OFFSET ?");

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (searchText != null && !searchText.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }
            if (rolFilter != null && !rolFilter.equals("TODOS")) {
                stmt.setString(paramIndex++, rolFilter);
            }
            if (activoFilter != null) {
                stmt.setBoolean(paramIndex++, activoFilter);
            }

            stmt.setInt(paramIndex++, limit);
            stmt.setInt(paramIndex, offset);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setUsername(rs.getString("username"));
                usuario.setPassword(rs.getString("password"));
                usuario.setRol(rs.getString("rol"));
                usuario.setActivo(rs.getBoolean("activo"));

                Timestamp timestamp = rs.getTimestamp("fecha_creacion");
                if (timestamp != null) {
                    usuario.setFechaCreacion(timestamp.toLocalDateTime());
                }

                usuarios.add(usuario);
            }

            LOGGER.info("Se obtuvieron {} usuarios", usuarios.size());
        } catch (SQLException e) {
            LOGGER.error("Error al obtener usuarios", e);
        }

        return usuarios;
    }

    /**
     * Cuenta el total de usuarios con filtros aplicados
     */
    public int countUsuarios(String searchText, String rolFilter, Boolean activoFilter) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM usuarios WHERE 1=1");

        if (searchText != null && !searchText.trim().isEmpty()) {
            sql.append(" AND username LIKE ?");
        }
        if (rolFilter != null && !rolFilter.equals("TODOS")) {
            sql.append(" AND rol = ?");
        }
        if (activoFilter != null) {
            sql.append(" AND activo = ?");
        }

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            int paramIndex = 1;

            if (searchText != null && !searchText.trim().isEmpty()) {
                stmt.setString(paramIndex++, "%" + searchText + "%");
            }
            if (rolFilter != null && !rolFilter.equals("TODOS")) {
                stmt.setString(paramIndex++, rolFilter);
            }
            if (activoFilter != null) {
                stmt.setBoolean(paramIndex++, activoFilter);
            }

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error al contar usuarios", e);
        }

        return 0;
    }

    /**
     * Crea un nuevo usuario
     */
    public boolean crearUsuario(String username, String password, String rol) {
        String sql = "INSERT INTO usuarios (username, password, rol, activo) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordUtil.hashPassword(password);

            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, rol);
            stmt.setBoolean(4, true);

            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Usuario creado: {}", username);
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.error("Error al crear usuario", e);
            return false;
        }
    }

    /**
     * Actualiza un usuario existente
     */
    public boolean actualizarUsuario(Integer id, String username, String rol, Boolean activo) {
        String sql = "UPDATE usuarios SET username = ?, rol = ?, activo = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, rol);
            stmt.setBoolean(3, activo);
            stmt.setInt(4, id);

            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Usuario actualizado: {}", username);
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.error("Error al actualizar usuario", e);
            return false;
        }
    }

    /**
     * Desactiva un usuario (eliminación lógica)
     */
    public boolean eliminarUsuario(Integer id) {
        String sql = "UPDATE usuarios SET activo = FALSE WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Usuario desactivado con ID: {}", id);
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.error("Error al desactivar usuario", e);
            return false;
        }
    }

    /**
     * Cuenta cuántos usuarios ADMIN activos hay en el sistema
     */
    public int countAdminsActivos() {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE rol = 'ADMIN' AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.error("Error al contar admins activos", e);
        }

        return 0;
    }

    /**
     * Cambia la contraseña de un usuario
     */
    public boolean cambiarContrasena(Integer id, String nuevaContrasena) {
        String sql = "UPDATE usuarios SET password = ? WHERE id = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String hashedPassword = PasswordUtil.hashPassword(nuevaContrasena);

            stmt.setString(1, hashedPassword);
            stmt.setInt(2, id);

            int rowsAffected = stmt.executeUpdate();
            LOGGER.info("Contraseña cambiada para usuario ID: {}", id);
            return rowsAffected > 0;

        } catch (SQLException e) {
            LOGGER.error("Error al cambiar contraseña", e);
            return false;
        }
    }

    /**
     * Obtiene un usuario por ID (solo usuarios activos)
     */
    public Usuario getUsuarioById(Integer id) {
        String sql = "SELECT id, username, password, rol, activo, fecha_creacion FROM usuarios WHERE id = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Usuario usuario = new Usuario();
                usuario.setId(rs.getInt("id"));
                usuario.setUsername(rs.getString("username"));
                usuario.setPassword(rs.getString("password"));
                usuario.setRol(rs.getString("rol"));
                usuario.setActivo(rs.getBoolean("activo"));

                Timestamp timestamp = rs.getTimestamp("fecha_creacion");
                if (timestamp != null) {
                    usuario.setFechaCreacion(timestamp.toLocalDateTime());
                }

                return usuario;
            }
        } catch (SQLException e) {
            LOGGER.error("Error al obtener usuario por ID", e);
        }

        return null;
    }

    /**
     * Verifica si un username ya existe
     */
    public boolean existeUsername(String username) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Error al verificar username", e);
        }

        return false;
    }

    /**
     * Verifica si un username ya existe excluyendo un ID específico (para edición)
     */
    public boolean existeUsernameExceptoId(String username, Integer idExcluir) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE username = ? AND id != ?";

        try (Connection conn = DatabaseConfig.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setInt(2, idExcluir);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            LOGGER.error("Error al verificar username", e);
        }

        return false;
    }
}
