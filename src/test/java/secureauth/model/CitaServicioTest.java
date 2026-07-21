package secureauth.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

class CitaServicioTest {

    @Test
    void manualAppointmentKeepsSelectedTimeAtThirteenFifteen() {
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(13, 15);

        CitaServicio cita = new CitaServicio(null, "Cliente", "Mascota", "Criollo", "300",
                "Bano", date, time, time.plusMinutes(60), "", "AGENDADA");

        assertEquals(date, cita.fechaServicio());
        assertEquals(time, cita.horaServicio());
    }
}
