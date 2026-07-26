package secureauth.shared.util;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class ServiceScheduleHelperTest {

    @Test
    void testIsMultiDayService() {
        assertTrue(ServiceScheduleHelper.isMultiDayService("Hospedaje Canino", "Servicios"));
        assertTrue(ServiceScheduleHelper.isMultiDayService("Hospitalización de Gato", "Clínica"));
        assertTrue(ServiceScheduleHelper.isMultiDayService("Observación Post-op", "Recuperación"));
        assertTrue(ServiceScheduleHelper.isMultiDayService("Guardería", "General"));

        assertFalse(ServiceScheduleHelper.isMultiDayService("Baño y Peluquería", "Estética"));
        assertFalse(ServiceScheduleHelper.isMultiDayService("Consulta General", "Médica"));
        assertFalse(ServiceScheduleHelper.isMultiDayService("Desparasitación", "Prevención"));
    }

    @Test
    void testCalculateDurationString() {
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalTime startTime = LocalTime.of(9, 0);
        LocalDate endDate = LocalDate.of(2026, 7, 13);
        LocalTime endTime = LocalTime.of(17, 0);

        // Hospedaje (day based): 10, 11, 12, 13 July = 4 days
        String durationHospedaje = ServiceScheduleHelper.calculateDurationString("Hospedaje Canino", startDate, startTime, endDate, endTime);
        assertEquals("4 días", durationHospedaje);

        // Hospitalización (hour based): between 10/07/2026 09:00 and 13/07/2026 17:00 (3 days * 24h + 8h = 80 hours)
        String durationHospitalizacion = ServiceScheduleHelper.calculateDurationString("Hospitalización", startDate, startTime, endDate, endTime);
        assertEquals("80 horas", durationHospitalizacion);

        // Single day: same day, 9:00 to 10:00 (1 hour)
        String durationSingleDay = ServiceScheduleHelper.calculateDurationString("Consulta", startDate, startTime, startDate, LocalTime.of(10, 0));
        assertEquals("1 hora", durationSingleDay);

        // Single day: same day, 9:00 to 10:30 (1 hour 30 mins)
        String durationSingleDayMinutes = ServiceScheduleHelper.calculateDurationString("Consulta", startDate, startTime, startDate, LocalTime.of(10, 30));
        assertEquals("1 hora 30 minutos", durationSingleDayMinutes);
    }

    @Test
    void testValidateInterval() {
        LocalDate startDate = LocalDate.of(2026, 7, 10);
        LocalTime startTime = LocalTime.of(9, 0);

        // Valid
        assertDoesNotThrow(() -> ServiceScheduleHelper.validateInterval(startDate, startTime, startDate.plusDays(1), startTime));

        // Invalid: end date before start date
        assertThrows(IllegalArgumentException.class, () ->
            ServiceScheduleHelper.validateInterval(startDate, startTime, startDate.minusDays(1), startTime)
        );

        // Invalid: end time before start time on same date
        assertThrows(IllegalArgumentException.class, () ->
            ServiceScheduleHelper.validateInterval(startDate, startTime, startDate, startTime.minusHours(1))
        );
    }
}
