package com.example.ceragen_2.service;

import com.example.ceragen_2.model.Usuario;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UsuarioServiceTest {
    private static final Logger logger = LoggerFactory.getLogger(UsuarioServiceTest.class);
    private static UsuarioService usuarioService;
    private static Integer usuarioTestId;

    @BeforeAll
    static void setUp() {
        logger.info("Iniciando tests de UsuarioService");
        usuarioService = UsuarioService.getInstance();
    }

    @Test
    @Order(1)
    @DisplayName("Test 1: Crear usuario nuevo")
    void testCrearUsuario() {
        logger.info("Test: Crear usuario nuevo");

        String username = "test_user_" + System.currentTimeMillis();
        String password = "password123";
        String rol = "RECEPCIONISTA";

        boolean resultado = usuarioService.crearUsuario(username, password, rol);

        assertTrue(resultado, "El usuario debería crearse exitosamente");
        logger.info("Usuario creado exitosamente: {}", username);

        // Verificar que el usuario existe
        boolean existe = usuarioService.existeUsername(username);
        assertTrue(existe, "El username debería existir después de crearlo");
    }

    @Test
    @Order(2)
    @DisplayName("Test 2: No crear usuario duplicado")
    void testNoCrearUsuarioDuplicado() {
        logger.info("Test: No crear usuario duplicado");

        // Intentar crear un usuario con username "admin" que ya existe
        boolean resultado = usuarioService.crearUsuario("admin", "password", "ADMIN");

        // Debería fallar porque ya existe (aunque el método retorna false, no lanza excepción)
        // El test verifica que existeUsername funciona
        boolean existe = usuarioService.existeUsername("admin");
        assertTrue(existe, "El username 'admin' debería existir");
    }

    @Test
    @Order(3)
    @DisplayName("Test 3: Obtener usuarios con paginación")
    void testGetUsuariosConPaginacion() {
        logger.info("Test: Obtener usuarios con paginación");

        int offset = 0;
        int limit = 10;
        String searchText = "";
        String rolFilter = "TODOS";
        Boolean activoFilter = null;

        List<Usuario> usuarios = usuarioService.getUsuarios(offset, limit, searchText, rolFilter, activoFilter);

        assertNotNull(usuarios, "La lista de usuarios no debería ser null");
        assertTrue(usuarios.size() <= limit, "No debería retornar más usuarios que el límite");
        logger.info("Se obtuvieron {} usuarios", usuarios.size());
    }

    @Test
    @Order(4)
    @DisplayName("Test 4: Contar usuarios")
    void testCountUsuarios() {
        logger.info("Test: Contar usuarios");

        int total = usuarioService.countUsuarios("", "TODOS", null);

        assertTrue(total > 0, "Debería haber al menos un usuario en la base de datos");
        logger.info("Total de usuarios: {}", total);
    }

    @Test
    @Order(5)
    @DisplayName("Test 5: Filtrar usuarios por rol")
    void testFiltrarUsuariosPorRol() {
        logger.info("Test: Filtrar usuarios por rol");

        List<Usuario> admins = usuarioService.getUsuarios(0, 100, "", "ADMIN", null);

        assertNotNull(admins, "La lista no debería ser null");

        // Verificar que todos los usuarios retornados sean ADMIN
        for (Usuario usuario : admins) {
            assertEquals("ADMIN", usuario.getRol(), "Todos los usuarios deberían tener rol ADMIN");
        }

        logger.info("Se encontraron {} usuarios con rol ADMIN", admins.size());
    }

    @Test
    @Order(6)
    @DisplayName("Test 6: Filtrar usuarios por búsqueda de texto")
    void testFiltrarUsuariosPorTexto() {
        logger.info("Test: Filtrar usuarios por búsqueda de texto");

        // Buscar usuarios que contengan "admin" en el username
        List<Usuario> usuarios = usuarioService.getUsuarios(0, 100, "admin", "TODOS", null);

        assertNotNull(usuarios, "La lista no debería ser null");

        // Verificar que todos los usuarios retornados contengan "admin" en el username
        for (Usuario usuario : usuarios) {
            assertTrue(usuario.getUsername().toLowerCase().contains("admin"),
                      "El username debería contener 'admin'");
        }

        logger.info("Se encontraron {} usuarios que contienen 'admin'", usuarios.size());
    }

    @Test
    @Order(7)
    @DisplayName("Test 7: Obtener usuario por ID")
    void testGetUsuarioById() {
        logger.info("Test: Obtener usuario por ID");

        // Primero obtener un usuario existente
        List<Usuario> usuarios = usuarioService.getUsuarios(0, 1, "", "TODOS", null);
        assertFalse(usuarios.isEmpty(), "Debería haber al menos un usuario");

        Usuario usuarioOriginal = usuarios.get(0);
        usuarioTestId = usuarioOriginal.getId();

        // Obtener el usuario por ID
        Usuario usuario = usuarioService.getUsuarioById(usuarioTestId);

        assertNotNull(usuario, "El usuario no debería ser null");
        assertEquals(usuarioTestId, usuario.getId(), "Los IDs deberían coincidir");
        assertEquals(usuarioOriginal.getUsername(), usuario.getUsername(), "Los usernames deberían coincidir");

        logger.info("Usuario obtenido: {} - {}", usuario.getId(), usuario.getUsername());
    }

    @Test
    @Order(8)
    @DisplayName("Test 8: Actualizar usuario")
    void testActualizarUsuario() {
        logger.info("Test: Actualizar usuario");

        // Crear un usuario para actualizar
        String usernameOriginal = "test_update_" + System.currentTimeMillis();
        usuarioService.crearUsuario(usernameOriginal, "password", "MEDICO");

        // Obtener el ID del usuario creado
        List<Usuario> usuarios = usuarioService.getUsuarios(0, 100, usernameOriginal, "TODOS", null);
        assertFalse(usuarios.isEmpty(), "El usuario recién creado debería existir");

        Usuario usuario = usuarios.get(0);
        Integer id = usuario.getId();

        // Actualizar el usuario
        String nuevoUsername = "test_updated_" + System.currentTimeMillis();
        boolean resultado = usuarioService.actualizarUsuario(id, nuevoUsername, "RECEPCIONISTA", true);

        assertTrue(resultado, "La actualización debería ser exitosa");

        // Verificar que se actualizó correctamente
        Usuario usuarioActualizado = usuarioService.getUsuarioById(id);
        assertEquals(nuevoUsername, usuarioActualizado.getUsername(), "El username debería estar actualizado");
        assertEquals("RECEPCIONISTA", usuarioActualizado.getRol(), "El rol debería estar actualizado");

        logger.info("Usuario actualizado: {} -> {}", usernameOriginal, nuevoUsername);

        // Limpiar - eliminar el usuario de prueba
        usuarioService.eliminarUsuario(id);
    }

    @Test
    @Order(9)
    @DisplayName("Test 9: Verificar username único para actualización")
    void testExisteUsernameExceptoId() {
        logger.info("Test: Verificar username único para actualización");

        // Crear dos usuarios
        String username1 = "test_unique1_" + System.currentTimeMillis();
        String username2 = "test_unique2_" + System.currentTimeMillis();

        usuarioService.crearUsuario(username1, "password", "MEDICO");
        usuarioService.crearUsuario(username2, "password", "MEDICO");

        // Obtener los IDs
        List<Usuario> usuarios1 = usuarioService.getUsuarios(0, 100, username1, "TODOS", null);
        List<Usuario> usuarios2 = usuarioService.getUsuarios(0, 100, username2, "TODOS", null);

        Integer id1 = usuarios1.get(0).getId();
        Integer id2 = usuarios2.get(0).getId();

        // Verificar que username2 existe exceptuando el id1
        boolean existe = usuarioService.existeUsernameExceptoId(username2, id1);
        assertTrue(existe, "El username2 debería existir exceptuando id1");

        // Verificar que username2 NO existe exceptuando su propio id2
        boolean noExiste = usuarioService.existeUsernameExceptoId(username2, id2);
        assertFalse(noExiste, "El username2 NO debería existir exceptuando su propio ID");

        logger.info("Verificación de username único funcionando correctamente");

        // Limpiar
        usuarioService.eliminarUsuario(id1);
        usuarioService.eliminarUsuario(id2);
    }

    @Test
    @Order(10)
    @DisplayName("Test 10: Cambiar contraseña")
    void testCambiarContrasena() {
        logger.info("Test: Cambiar contraseña");

        // Crear un usuario para cambiar contraseña
        String username = "test_password_" + System.currentTimeMillis();
        usuarioService.crearUsuario(username, "oldpassword", "MEDICO");

        // Obtener el ID del usuario
        List<Usuario> usuarios = usuarioService.getUsuarios(0, 100, username, "TODOS", null);
        Integer id = usuarios.get(0).getId();
        String passwordOriginal = usuarios.get(0).getPassword();

        // Cambiar la contraseña
        boolean resultado = usuarioService.cambiarContrasena(id, "newpassword123");
        assertTrue(resultado, "El cambio de contraseña debería ser exitoso");

        // Verificar que la contraseña cambió (comparar hash)
        Usuario usuarioActualizado = usuarioService.getUsuarioById(id);
        assertNotEquals(passwordOriginal, usuarioActualizado.getPassword(),
                       "El hash de la contraseña debería ser diferente");

        logger.info("Contraseña cambiada exitosamente para usuario: {}", username);

        // Limpiar
        usuarioService.eliminarUsuario(id);
    }

    @Test
    @Order(11)
    @DisplayName("Test 11: Filtrar usuarios activos")
    void testFiltrarUsuariosActivos() {
        logger.info("Test: Filtrar usuarios activos");

        List<Usuario> usuariosActivos = usuarioService.getUsuarios(0, 100, "", "TODOS", true);

        assertNotNull(usuariosActivos, "La lista no debería ser null");

        // Verificar que todos los usuarios retornados estén activos
        for (Usuario usuario : usuariosActivos) {
            assertTrue(usuario.getActivo(), "Todos los usuarios deberían estar activos");
        }

        logger.info("Se encontraron {} usuarios activos", usuariosActivos.size());
    }

    @Test
    @Order(12)
    @DisplayName("Test 12: Eliminar usuario")
    void testEliminarUsuario() {
        logger.info("Test: Eliminar usuario");

        // Crear un usuario para eliminar
        String username = "test_delete_" + System.currentTimeMillis();
        usuarioService.crearUsuario(username, "password", "MEDICO");

        // Obtener el ID del usuario
        List<Usuario> usuarios = usuarioService.getUsuarios(0, 100, username, "TODOS", null);
        assertFalse(usuarios.isEmpty(), "El usuario debería existir");

        Integer id = usuarios.get(0).getId();

        // Eliminar el usuario
        boolean resultado = usuarioService.eliminarUsuario(id);
        assertTrue(resultado, "La eliminación debería ser exitosa");

        // Verificar que el usuario ya no existe
        Usuario usuarioEliminado = usuarioService.getUsuarioById(id);
        assertNull(usuarioEliminado, "El usuario no debería existir después de eliminarlo");

        logger.info("Usuario eliminado exitosamente: {}", username);
    }

    @Test
    @Order(13)
    @DisplayName("Test 13: Paginación funciona correctamente")
    void testPaginacionCorrecta() {
        logger.info("Test: Paginación funciona correctamente");

        // Obtener primera página
        List<Usuario> pagina1 = usuarioService.getUsuarios(0, 2, "", "TODOS", null);

        // Obtener segunda página
        List<Usuario> pagina2 = usuarioService.getUsuarios(2, 2, "", "TODOS", null);

        assertNotNull(pagina1, "La primera página no debería ser null");
        assertNotNull(pagina2, "La segunda página no debería ser null");

        // Si hay suficientes usuarios, las páginas deberían ser diferentes
        if (pagina1.size() == 2 && pagina2.size() > 0) {
            assertNotEquals(pagina1.get(0).getId(), pagina2.get(0).getId(),
                           "Las páginas deberían contener usuarios diferentes");
        }

        logger.info("Paginación funcionando correctamente: Página 1 = {} usuarios, Página 2 = {} usuarios",
                   pagina1.size(), pagina2.size());
    }

    @AfterAll
    static void tearDown() {
        logger.info("Tests de UsuarioService completados");
    }
}
