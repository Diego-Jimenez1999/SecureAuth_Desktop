package secureauth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AppointmentStatusTest {

    @Test
    void supportsCurrentAndLegacyStatusValues() {
        assertTrue(AppointmentStatus.isSupported("FINALIZADO"));
        assertTrue(AppointmentStatus.isSupported("FINALIZADA"));
        assertTrue(AppointmentStatus.isSupported("REALIZADO"));
        assertTrue(AppointmentStatus.isSupported("CANCELADA"));
        assertTrue(AppointmentStatus.isSupported("CANCELADO"));
    }

    @Test
    void normalizesLegacyValuesForStorage() {
        assertEquals("FINALIZADO", AppointmentStatus.normalizeForStorage("FINALIZADA"));
        assertEquals("FINALIZADO", AppointmentStatus.normalizeForStorage("REALIZADO"));
        assertEquals("CANCELADA", AppointmentStatus.normalizeForStorage("CANCELADO"));
    }
}
