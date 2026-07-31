package secureauth.service.enterprise;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.Test;

import secureauth.dao.PetDAO;
import secureauth.dao.enterprise.RecentActivityDAO;
import secureauth.dao.enterprise.AppointmentDAO;
import secureauth.model.Appointment;
import secureauth.model.AppointmentStatus;

class AppointmentServiceTest {

    @Test
    void prepareForRegistrationPreservesSelectedAppointmentTime() {
        AppointmentService service = new AppointmentService(new AppointmentDAO(), new PetDAO(),
                new RecentActivityDAO());
        LocalDate date = LocalDate.now().plusDays(1);
        LocalTime time = LocalTime.of(10, 30);
        Appointment appointment = new Appointment(null, 1, "Consulta", 1, "Cliente", 2, "Mascota",
                date, time, AppointmentStatus.PENDING.databaseValue(), "", null, "Vet");

        Appointment prepared = service.prepareForRegistration(appointment);

        assertEquals(date, prepared.getAppointmentDate());
        assertEquals(time, prepared.getAppointmentTime());
    }
}
