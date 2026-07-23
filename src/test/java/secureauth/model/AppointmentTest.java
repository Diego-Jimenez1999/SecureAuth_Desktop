package secureauth.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class AppointmentTest {

    @Test
    void defaultConstructorInitializesFieldsToNullOrZero() {
        Appointment appointment = new Appointment();

        assertNull(appointment.getId());
        assertEquals(0, appointment.getServiceId());
        assertNull(appointment.getServiceName());
        assertEquals(0, appointment.getOwnerId());
        assertNull(appointment.getOwnerName());
        assertEquals(0, appointment.getPetId());
        assertNull(appointment.getPetName());
        assertNull(appointment.getAppointmentDate());
        assertNull(appointment.getAppointmentTime());
        assertNull(appointment.getStatus());
        assertNull(appointment.getNotes());
        assertNull(appointment.getCreatedAt());
        assertNull(appointment.getCreatedBy());
    }

    @Test
    void constructorAndSettersExposeAllAppointmentValues() {
        LocalDate appointmentDate = LocalDate.of(2026, 7, 25);
        LocalTime appointmentTime = LocalTime.of(15, 30);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 20, 9, 0);

        Appointment appointment = new Appointment(12, 44, "Baño y corte", 77, "Carlos", 88,
                "Nina", appointmentDate, appointmentTime, "PROGRAMADA", "Traer historial",
                createdAt, "admin");

        appointment.setStatus("COMPLETADA");
        appointment.setNotes("Finalizada sin novedades");
        appointment.setCreatedBy("recepcion");

        assertEquals(12, appointment.getId());
        assertEquals(44, appointment.getServiceId());
        assertEquals("Baño y corte", appointment.getServiceName());
        assertEquals(77, appointment.getOwnerId());
        assertEquals("Carlos", appointment.getOwnerName());
        assertEquals(88, appointment.getPetId());
        assertEquals("Nina", appointment.getPetName());
        assertEquals(appointmentDate, appointment.getAppointmentDate());
        assertEquals(appointmentTime, appointment.getAppointmentTime());
        assertEquals("COMPLETADA", appointment.getStatus());
        assertEquals("Finalizada sin novedades", appointment.getNotes());
        assertEquals(createdAt, appointment.getCreatedAt());
        assertEquals("recepcion", appointment.getCreatedBy());
    }
}
