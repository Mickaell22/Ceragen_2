package com.example.ceragen_2.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidad para manejar el cifrado y verificación de contraseñas usando BCrypt
 */
public class PasswordUtil {

    // Cost factor para BCrypt (12 es un buen balance entre seguridad y rendimiento)
    private static final int BCRYPT_ROUNDS = 12;

    /**
     * Hashea una contraseña usando BCrypt
     *
     * @param plainPassword Contraseña en texto plano
     * @return Hash BCrypt de la contraseña
     */
    public static String hashPassword(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt
     *
     * @param plainPassword Contraseña en texto plano
     * @param hashedPassword Hash BCrypt almacenado
     * @return true si la contraseña coincide, false en caso contrario
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            // Hash inválido
            return false;
        }
    }

    /**
     * Ejemplo de uso
     */
    public static void main(String[] args) {
        // Ejemplo: hashear la contraseña "admin"
        String password = "admin";
        String hashed = hashPassword(password);

        System.out.println("Password: " + password);
        System.out.println("Hashed: " + hashed);
        System.out.println("Verification: " + verifyPassword(password, hashed));
        System.out.println("Wrong password: " + verifyPassword("wrong", hashed));
    }
}
