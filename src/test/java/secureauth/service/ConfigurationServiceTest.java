package secureauth.service;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class ConfigurationServiceTest {

    @Test
    void testConfigurationServiceLocalFallback() {
        ConfigurationService configService = ConfigurationService.getInstance();
        assertNotNull(configService);

        // Assert local fallback default values
        assertEquals("COP", configService.getSetting("currency", "COP"));
        assertEquals("SecureAuth", configService.getSetting("branding", "SecureAuth"));
    }

    @Test
    void testValidationOfInvalidTaxValueThrowsException() {
        ConfigurationService configService = ConfigurationService.getInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            configService.setSetting("tax", "negative_number_or_string", "Invalid tax");
        });
    }

    @Test
    void testValidationOfInvalidIntervalValueThrowsException() {
        ConfigurationService configService = ConfigurationService.getInstance();
        assertThrows(IllegalArgumentException.class, () -> {
            configService.setSetting("agenda_intervalo", "-5", "Negative interval");
        });
    }
}
