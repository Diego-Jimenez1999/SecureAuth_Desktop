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

    @Test
    void shouldVerifyHashWithDatabasePadding() {
        String rawPassword = "Secure123";
        String hash = "  " + PasswordHasher.hash(rawPassword) + "  ";

        Assertions.assertTrue(PasswordHasher.verify(rawPassword, hash));
    }

    @Test
    void shouldTemporarilyVerifyPlainTextPasswordForManualDatabaseUsers() {
        Assertions.assertTrue(PasswordHasher.verify("Temporal123", "Temporal123"));
        Assertions.assertTrue(PasswordHasher.needsRehash("Temporal123"));
    }

    @Test
    void shouldVerifyCompatibleBcryptRevisionsAndRehashThemLater() {
        String rawPassword = "Password2026!";
        String hash = PasswordHasher.hash(rawPassword);
        String twoB = "$2b$" + hash.substring(4);
        String twoY = "$2y$" + hash.substring(4);

        Assertions.assertTrue(PasswordHasher.verify(rawPassword, twoB));
        Assertions.assertTrue(PasswordHasher.verify(rawPassword, twoY));
        Assertions.assertTrue(PasswordHasher.needsRehash(twoB));
        Assertions.assertTrue(PasswordHasher.needsRehash(twoY));
    }

    @Test
    void shouldNotThrowForMalformedBcryptLikeHash() {
        Assertions.assertFalse(PasswordHasher.verify("Password2026!", "$2y$bad-salt"));
    }
}
