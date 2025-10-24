package com.example.ceragen_2.service;

import com.example.ceragen_2.util.PasswordUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para AuthService
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthServiceTest {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = AuthService.getInstance();
        // Asegurar que no hay sesion activa antes de cada test
        authService.logout();
    }

    @Test
    @Order(1)
    @DisplayName("Generar hash para admin123")
    void generateHashForAdmin123() {
        String password = "admin123";
        String hash = PasswordUtil.hashPassword(password);

        System.out.println("\n===========================================");
        System.out.println("Hash bcrypt para 'admin123':");
        System.out.println(hash);
        System.out.println("===========================================\n");

        // Verificar que el hash generado funciona
        assertTrue(PasswordUtil.verifyPassword(password, hash),
                "El hash generado debe verificar correctamente la contraseña");
    }

    @Test
    @Order(2)
    @DisplayName("Login exitoso con credenciales correctas")
    void testLoginSuccess() {
        // Usando las credenciales que deben estar en la base de datos
        boolean result = authService.login("admin", "admin123");

        assertTrue(result, "El login debe ser exitoso con credenciales correctas");
        assertTrue(authService.isAuthenticated(), "El usuario debe estar autenticado");
        assertEquals("admin", authService.getCurrentUsername(), "El username debe ser 'admin'");
        assertNotNull(authService.getCurrentUserRole(), "El rol no debe ser null");
        assertNotNull(authService.getCurrentUserId(), "El ID de usuario no debe ser null");
    }

    @Test
    @Order(3)
    @DisplayName("Login fallido con contraseña incorrecta")
    void testLoginFailureWrongPassword() {
        boolean result = authService.login("admin", "contraseñaIncorrecta");

        assertFalse(result, "El login debe fallar con contraseña incorrecta");
        assertFalse(authService.isAuthenticated(), "El usuario no debe estar autenticado");
        assertNull(authService.getCurrentUsername(), "El username debe ser null");
        assertNull(authService.getCurrentUserRole(), "El rol debe ser null");
        assertNull(authService.getCurrentUserId(), "El ID debe ser null");
    }

    @Test
    @Order(4)
    @DisplayName("Login fallido con usuario inexistente")
    void testLoginFailureNonexistentUser() {
        boolean result = authService.login("usuarioQueNoExiste", "admin123");

        assertFalse(result, "El login debe fallar con usuario inexistente");
        assertFalse(authService.isAuthenticated(), "El usuario no debe estar autenticado");
    }

    @Test
    @Order(5)
    @DisplayName("Login con campos vacios")
    void testLoginWithEmptyFields() {
        assertFalse(authService.login("", "admin123"), "Login con username vacio debe fallar");
        assertFalse(authService.login("admin", ""), "Login con password vacio debe fallar");
        assertFalse(authService.login("", ""), "Login con ambos campos vacios debe fallar");
    }

    @Test
    @Order(6)
    @DisplayName("Logout debe limpiar la sesion")
    void testLogout() {
        // Primero hacer login
        authService.login("admin", "admin123");
        assertTrue(authService.isAuthenticated(), "Usuario debe estar autenticado");

        // Luego hacer logout
        authService.logout();

        assertFalse(authService.isAuthenticated(), "Usuario no debe estar autenticado despues de logout");
        assertNull(authService.getCurrentUsername(), "Username debe ser null despues de logout");
        assertNull(authService.getCurrentUserRole(), "Rol debe ser null despues de logout");
        assertNull(authService.getCurrentUserId(), "ID debe ser null despues de logout");
    }

    @Test
    @Order(7)
    @DisplayName("Estado inicial sin autenticacion")
    void testInitialUnauthenticatedState() {
        assertFalse(authService.isAuthenticated(), "Usuario no debe estar autenticado inicialmente");
        assertNull(authService.getCurrentUsername(), "Username debe ser null inicialmente");
        assertNull(authService.getCurrentUserRole(), "Rol debe ser null inicialmente");
        assertNull(authService.getCurrentUserId(), "ID debe ser null inicialmente");
    }

    @Test
    @Order(8)
    @DisplayName("Verificar rol de usuario admin")
    void testAdminUserRole() {
        authService.login("admin", "admin123");

        String role = authService.getCurrentUserRole();
        assertNotNull(role, "El rol no debe ser null");
        assertTrue(role.equals("ADMIN") || role.equals("RECEPCIONISTA") || role.equals("MEDICO"),
                "El rol debe ser uno de los valores validos");
    }

    @Test
    @Order(9)
    @DisplayName("Multiples intentos de login")
    void testMultipleLoginAttempts() {
        // Primer login exitoso
        assertTrue(authService.login("admin", "admin123"), "Primer login debe ser exitoso");
        String firstUsername = authService.getCurrentUsername();

        // Logout
        authService.logout();

        // Segundo login exitoso
        assertTrue(authService.login("admin", "admin123"), "Segundo login debe ser exitoso");
        assertEquals(firstUsername, authService.getCurrentUsername(),
                "El username debe ser el mismo en ambos logins");
    }

    @Test
    @Order(10)
    @DisplayName("Verificar que PasswordUtil hashea correctamente")
    void testPasswordUtilHashing() {
        String plainPassword = "testPassword123";
        String hash1 = PasswordUtil.hashPassword(plainPassword);
        String hash2 = PasswordUtil.hashPassword(plainPassword);

        assertNotNull(hash1, "El hash no debe ser null");
        assertNotNull(hash2, "El segundo hash no debe ser null");
        assertNotEquals(hash1, hash2, "Dos hashes de la misma contraseña deben ser diferentes (salt aleatorio)");

        // Ambos hashes deben verificar la contraseña original
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash1), "Hash1 debe verificar la contraseña");
        assertTrue(PasswordUtil.verifyPassword(plainPassword, hash2), "Hash2 debe verificar la contraseña");

        // Contraseña incorrecta no debe verificar
        assertFalse(PasswordUtil.verifyPassword("wrongPassword", hash1),
                "Contraseña incorrecta no debe verificar");
    }
}
