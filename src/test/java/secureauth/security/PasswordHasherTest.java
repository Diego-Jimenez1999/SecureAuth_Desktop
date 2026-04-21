package secureauth.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias basicas para hashing de contrasenas.
 *
 * @author Diego
 * @version 1.0
 */
class PasswordHasherTest {

    /**
     * Verifica que el hash generado no sea el texto plano y que se pueda validar.
     */
    @Test
    void shouldHashAndVerifyPassword() {
        String rawPassword = "Secure123";
        String hash = PasswordHasher.hash(rawPassword);

        Assertions.assertNotNull(hash);
        Assertions.assertNotEquals(rawPassword, hash);
        Assertions.assertTrue(PasswordHasher.verify(rawPassword, hash));
    }
}
