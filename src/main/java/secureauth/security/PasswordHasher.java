/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package secureauth.security;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase encargada de la seguridad de contraseñas.
 * <p>
 * Implementa BCrypt para generar hashes seguros con sal y factor de costo.
 * También mantiene compatibilidad con hashes SHA-256 legados.
 * 
 * </p>
 *
 * <h2>Responsabilidades</h2>
 * <ul>
 *     <li>Generar hash de contraseñas</li>
 *     <li>Comparar contraseñas con hash almacenado</li>
 * </ul>
 *
 * <h2>Ejemplo de uso</h2>
 * <pre>
 * {@code
 * String hash = PasswordHasher.hash("1234");
 * boolean valid = PasswordHasher.verify("1234", hash);
 * }
 * </pre>
 *
 * @author Diego
 * @version 1.0
 */
public class PasswordHasher {

    private static final int BCRYPT_COST = 12;

    /**
     * Genera el hash BCrypt de una contraseña.
     *
     * @param password contraseña en texto plano
     * @return hash BCrypt
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return BCrypt.hashpw(password, BCrypt.gensalt(BCRYPT_COST));
    }

    /**
     * Verifica si una contraseña coincide con un hash.
     *
     * @param password contraseña ingresada
     * @param storedHash hash almacenado en BD
     * @return true si coinciden, false en caso contrario
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null || storedHash.isEmpty()) {
            return false;
        }

        if (isBcryptHash(storedHash)) {
            return BCrypt.checkpw(password, storedHash);
        }

        // Compatibilidad para usuarios antiguos almacenados con SHA-256.
        return sha256Hex(password).equals(storedHash);
    }

    /**
     * Determina si un hash requiere rehash a un costo más alto.
     *
     * @param storedHash hash guardado
     * @return true si requiere rehash
     */
    public static boolean needsRehash(String storedHash) {
        if (!isBcryptHash(storedHash)) {
            return true;
        }
        return extractCost(storedHash) < BCRYPT_COST;
    }

    /**
     * Verifica si el hash corresponde al formato BCrypt.
     *
     * @param hash hash almacenado
     * @return true si parece hash BCrypt
     */
    public static boolean isBcryptHash(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    private static int extractCost(String hash) {
        try {
            return Integer.parseInt(hash.substring(4, 6));
        } catch (Exception ex) {
            return 0;
        }
    }

    private static String sha256Hex(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al generar hash SHA-256 legado", e);
        }
    }
}
